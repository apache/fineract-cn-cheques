/*
 * Copyright 2017 Kuelap, Inc.
 *
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Kuelap, Inc and its suppliers, if any.
 * The intellectual and technical concepts contained herein
 * are proprietary to Kuelap, Inc and its suppliers and may
 * be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * Kuelap, Inc.
 */
package io.mifos.cheque.service.internal.command.handler;

import com.google.common.collect.Sets;
import io.mifos.accounting.api.v1.client.AccountNotFoundException;
import io.mifos.accounting.api.v1.domain.Creditor;
import io.mifos.accounting.api.v1.domain.Debtor;
import io.mifos.accounting.api.v1.domain.JournalEntry;
import io.mifos.cheque.api.v1.EventConstants;
import io.mifos.cheque.api.v1.domain.MICR;
import io.mifos.cheque.api.v1.domain.State;
import io.mifos.cheque.service.ServiceConstants;
import io.mifos.cheque.service.internal.command.ApproveChequeTransactionCommand;
import io.mifos.cheque.service.internal.command.CancelChequeTransactionCommand;
import io.mifos.cheque.service.internal.command.ChequeTransactionCommand;
import io.mifos.cheque.service.internal.command.IssueChequesCommand;
import io.mifos.cheque.service.internal.format.MICRParser;
import io.mifos.cheque.service.internal.mapper.ChequeMapper;
import io.mifos.cheque.service.internal.repository.ChequeEntity;
import io.mifos.cheque.service.internal.repository.ChequeRepository;
import io.mifos.cheque.service.internal.repository.IssuedChequeEntity;
import io.mifos.cheque.service.internal.repository.IssuedChequeRepository;
import io.mifos.cheque.service.internal.service.helper.AccountingService;
import io.mifos.cheque.service.internal.service.helper.DepositService;
import io.mifos.cheque.service.internal.service.helper.OrganizationService;
import io.mifos.core.api.util.UserContextHolder;
import io.mifos.core.command.annotation.Aggregate;
import io.mifos.core.command.annotation.CommandHandler;
import io.mifos.core.command.annotation.EventEmitter;
import io.mifos.core.lang.DateConverter;
import io.mifos.core.lang.ServiceException;
import io.mifos.deposit.api.v1.definition.domain.Charge;
import io.mifos.deposit.api.v1.instance.ProductInstanceNotFoundException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Aggregate
public class ChequeAggregate {

  private final Logger logger;
  private final DepositService depositService;
  private final OrganizationService organizationService;
  private final AccountingService accountingService;
  private final IssuedChequeRepository issuedChequeRepository;
  private final ChequeRepository chequeRepository;

  @Autowired
  public ChequeAggregate(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                         final DepositService depositService,
                         final OrganizationService organizationService,
                         final AccountingService accountingService,
                         final IssuedChequeRepository issuedChequeRepository,
                         final ChequeRepository chequeRepository) {
    super();
    this.logger = logger;
    this.depositService = depositService;
    this.organizationService = organizationService;
    this.accountingService = accountingService;
    this.issuedChequeRepository = issuedChequeRepository;
    this.chequeRepository = chequeRepository;
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.ISSUE_CHEQUES)
  public String process(final IssueChequesCommand issueChequesCommand) {

    final Optional<IssuedChequeEntity> optionalIssuedCheque =
        this.issuedChequeRepository.findByAccountIdentifier(issueChequesCommand.accountIdentifer());

    final IssuedChequeEntity issuedChequeEntity = optionalIssuedCheque.orElseGet(() -> {
      final IssuedChequeEntity toCreate = new IssuedChequeEntity();
      toCreate.setAccountIdentifier(issueChequesCommand.accountIdentifer());
      toCreate.setCreatedBy(UserContextHolder.checkedGetUser());
      toCreate.setCreatedOn(LocalDateTime.now(Clock.systemUTC()));
      return toCreate;
    });

    final Integer lastIssuedNumber;
    if (issuedChequeEntity.getLastIssuedNumber() == null) {
      lastIssuedNumber = issueChequesCommand.startSequence() != null
          ? (issueChequesCommand.startSequence() - 1) + issueChequesCommand.issueCount()
          : issueChequesCommand.issueCount();
    } else {
      lastIssuedNumber = issuedChequeEntity.getLastIssuedNumber() + issueChequesCommand.issueCount();
    }

    issuedChequeEntity.setLastIssuedNumber(lastIssuedNumber);
    issuedChequeEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
    issuedChequeEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

    this.issuedChequeRepository.save(issuedChequeEntity);

    try {
      final List<Charge> issueChequeCharges =
          this.depositService.getIssueChequeCharges(issueChequesCommand.accountIdentifer());
      if (!issueChequeCharges.isEmpty()) {
        this.accountingService.bookCharges(issueChequesCommand.accountIdentifer(), issueChequeCharges);
      }
    } catch (final ProductInstanceNotFoundException | AccountNotFoundException ex) {
      throw ServiceException.notFound("Account {0} not found", issueChequesCommand.accountIdentifer());
    }

    return issueChequesCommand.accountIdentifer();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.CHEQUE_TRANSACTION)
  public String process(final ChequeTransactionCommand chequeTransactionCommand) {
    final ChequeEntity chequeEntity = ChequeMapper.map(chequeTransactionCommand.cheque());

    final MICR micr = chequeTransactionCommand.cheque().getMicr();

    final JournalEntry journalEntry = new JournalEntry();
    journalEntry.setTransactionIdentifier("chq-tx-" + UUID.randomUUID().toString());
    final String transactionType =
        chequeEntity.getOpenCheque() != null && chequeEntity.getOpenCheque() ? "OPCQ" : "ORCQ";
    journalEntry.setTransactionType(transactionType);
    journalEntry.setTransactionDate(DateConverter.toIsoString(chequeEntity.getCreatedOn()));
    journalEntry.setMessage(transactionType);
    journalEntry.setClerk(UserContextHolder.checkedGetUser());

    final Debtor debtor = new Debtor();
    debtor.setAmount(chequeEntity.getAmount().toString());
    if (this.onUs(micr)) {
      final Optional<IssuedChequeEntity> optionalIssuedCheque =
          this.issuedChequeRepository.findByAccountIdentifier(micr.getAccountNumber());

      if (optionalIssuedCheque.isPresent()) {
        final Integer lastIssuedNumber = optionalIssuedCheque.get().getLastIssuedNumber();
        if (Integer.valueOf(chequeEntity.getChequeNumber()) > lastIssuedNumber) {
          throw ServiceException.conflict("Unknown cheque {0}.", MICRParser.toIdentifier(micr));
        }
      } else {
        throw ServiceException.conflict("Account {0} never issued cheques.", micr.getAccountNumber());
      }
      this.accountingService.findAccount(micr.getAccountNumber())
          .ifPresent(account -> debtor.setAccountNumber(account.getIdentifier()));
      chequeEntity.setState(State.PROCESSED.name());
    } else {
      debtor.setAccountNumber(chequeTransactionCommand.chequesReceivableAccount());
      chequeEntity.setState(State.PENDING.name());
    }
    journalEntry.setDebtors(Sets.newHashSet(debtor));

    final Creditor creditor = new Creditor();
    creditor.setAmount(chequeEntity.getAmount().toString());
    creditor.setAccountNumber(chequeTransactionCommand.creditorAccount());
    journalEntry.setCreditors(Sets.newHashSet(creditor));

    this.accountingService.processJournalEntry(journalEntry);

    chequeEntity.setJournalEntryIdentifier(journalEntry.getTransactionIdentifier());

    this.chequeRepository.save(chequeEntity);

    return MICRParser.toIdentifier(micr);
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.CHEQUE_TRANSACTION_APPROVED)
  public String process(final ApproveChequeTransactionCommand approveChequeTransactionCommand) {
    final MICR micr = MICRParser.fromIdentifier(approveChequeTransactionCommand.identifier());
    final Optional<ChequeEntity> optionalCheque = this.chequeRepository.findByChequeNumberAndBranchSortCodeAndAccountNumber(
        micr.getChequeNumber(), micr.getBranchSortCode(), micr.getAccountNumber()
    );

    if (optionalCheque.isPresent()) {
      final ChequeEntity chequeEntity = optionalCheque.get();
      chequeEntity.setState(State.PROCESSED.name());
      chequeEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
      chequeEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));
      this.chequeRepository.save(chequeEntity);
      return approveChequeTransactionCommand.identifier();
    } else {
      throw ServiceException.notFound("Cheque {0} not found.", approveChequeTransactionCommand.identifier());
    }
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.CHEQUE_TRANSACTION_CANCELED)
  public String process(final CancelChequeTransactionCommand cancelChequeTransactionCommand) {
    final MICR micr = MICRParser.fromIdentifier(cancelChequeTransactionCommand.identifier());
    final Optional<ChequeEntity> optionalCheque = this.chequeRepository.findByChequeNumberAndBranchSortCodeAndAccountNumber(
        micr.getChequeNumber(), micr.getBranchSortCode(), micr.getAccountNumber()
    );

    if (optionalCheque.isPresent()) {

      final ChequeEntity chequeEntity = optionalCheque.get();

      final JournalEntry journalEntryToReverse =
          this.accountingService.findJournalEntry(chequeEntity.getJournalEntryIdentifier());

      final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());

      final JournalEntry journalEntry = new JournalEntry();
      journalEntry.setTransactionIdentifier("chq-cnl-" + UUID.randomUUID().toString());
      journalEntry.setTransactionDate(DateConverter.toIsoString(now));
      journalEntry.setTransactionType("CQRV");
      journalEntry.setMessage("CQRV");
      journalEntry.setNote("Cheque canceled, reversed transaction: " + journalEntryToReverse.getTransactionIdentifier() + ".");
      journalEntry.setClerk(UserContextHolder.checkedGetUser());
      journalEntry.setDebtors(
          journalEntryToReverse.getCreditors().stream().map(creditor -> {
            final Debtor debtor = new Debtor();
            debtor.setAccountNumber(creditor.getAccountNumber());
            debtor.setAmount(creditor.getAmount());
            return debtor;
          }).collect(Collectors.toSet())
      );
      journalEntry.setCreditors(
          journalEntryToReverse.getDebtors().stream().map(debtor -> {
            final Creditor creditor = new Creditor();
            creditor.setAccountNumber(debtor.getAccountNumber());
            creditor.setAmount(debtor.getAmount());
            return creditor;
          }).collect(Collectors.toSet())
      );

      this.accountingService.processJournalEntry(journalEntry);

      chequeEntity.setState(State.CANCELED.name());
      chequeEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
      chequeEntity.setLastModifiedOn(now);
      this.chequeRepository.save(chequeEntity);
      return cancelChequeTransactionCommand.identifier();
    } else {
      throw ServiceException.notFound("Cheque {0} not found.", cancelChequeTransactionCommand.identifier());
    }
  }

  private boolean onUs(final MICR micr) {
    return this.organizationService.officeExistsByBranchSortCode(micr.getBranchSortCode())
        && this.accountingService.accountExists(micr.getAccountNumber());
  }
}
