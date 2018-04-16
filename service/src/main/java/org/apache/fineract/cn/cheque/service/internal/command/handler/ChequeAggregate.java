/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cn.cheque.service.internal.command.handler;

import com.google.common.collect.Sets;
import org.apache.fineract.cn.cheque.api.v1.EventConstants;
import org.apache.fineract.cn.cheque.api.v1.domain.MICR;
import org.apache.fineract.cn.cheque.api.v1.domain.State;
import org.apache.fineract.cn.cheque.service.ServiceConstants;
import org.apache.fineract.cn.cheque.service.internal.command.ApproveChequeTransactionCommand;
import org.apache.fineract.cn.cheque.service.internal.command.CancelChequeTransactionCommand;
import org.apache.fineract.cn.cheque.service.internal.command.ChequeTransactionCommand;
import org.apache.fineract.cn.cheque.service.internal.command.IssueChequesCommand;
import org.apache.fineract.cn.cheque.service.internal.format.MICRParser;
import org.apache.fineract.cn.cheque.service.internal.mapper.ChequeMapper;
import org.apache.fineract.cn.cheque.service.internal.repository.ChequeEntity;
import org.apache.fineract.cn.cheque.service.internal.repository.ChequeRepository;
import org.apache.fineract.cn.cheque.service.internal.repository.IssuedChequeEntity;
import org.apache.fineract.cn.cheque.service.internal.repository.IssuedChequeRepository;
import org.apache.fineract.cn.cheque.service.internal.service.helper.AccountingService;
import org.apache.fineract.cn.cheque.service.internal.service.helper.DepositService;
import org.apache.fineract.cn.cheque.service.internal.service.helper.OrganizationService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.fineract.cn.accounting.api.v1.client.AccountNotFoundException;
import org.apache.fineract.cn.accounting.api.v1.domain.Creditor;
import org.apache.fineract.cn.accounting.api.v1.domain.Debtor;
import org.apache.fineract.cn.accounting.api.v1.domain.JournalEntry;
import org.apache.fineract.cn.api.util.UserContextHolder;
import org.apache.fineract.cn.command.annotation.Aggregate;
import org.apache.fineract.cn.command.annotation.CommandHandler;
import org.apache.fineract.cn.command.annotation.EventEmitter;
import org.apache.fineract.cn.deposit.api.v1.definition.domain.Charge;
import org.apache.fineract.cn.deposit.api.v1.instance.ProductInstanceNotFoundException;
import org.apache.fineract.cn.lang.DateConverter;
import org.apache.fineract.cn.lang.ServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

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
