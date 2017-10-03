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
package io.mifos.cheque.service.internal.service.helper;

import com.google.common.collect.Sets;
import io.mifos.accounting.api.v1.client.AccountNotFoundException;
import io.mifos.accounting.api.v1.client.LedgerManager;
import io.mifos.accounting.api.v1.domain.Account;
import io.mifos.accounting.api.v1.domain.AccountPage;
import io.mifos.accounting.api.v1.domain.Creditor;
import io.mifos.accounting.api.v1.domain.Debtor;
import io.mifos.accounting.api.v1.domain.JournalEntry;
import io.mifos.cheque.service.ServiceConstants;
import io.mifos.core.api.util.UserContextHolder;
import io.mifos.core.lang.DateConverter;
import io.mifos.core.lang.ServiceException;
import io.mifos.deposit.api.v1.definition.domain.Charge;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountingService {

  private final Logger logger;
  private final LedgerManager ledgerManager;

  @Autowired
  public AccountingService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                           final LedgerManager ledgerManager) {
    super();
    this.logger = logger;
    this.ledgerManager = ledgerManager;
  }

  public void bookCharges(final String sourceAccount, final List<Charge> charges) {

    final Double totalCharges = charges.stream().mapToDouble(charge -> {
      if (charge.getProportional()) {
        this.logger.info("Charges for issuing cheques must be fixed.");
        return 0.00D;
      } else {
        return charge.getAmount();
      }
    }).sum();
    if (totalCharges == 0.00D) {
      return;
    }

    final Account account = this.findAccount(sourceAccount).orElseThrow(AccountNotFoundException::new);
    if (account.getBalance() < totalCharges) {
      throw ServiceException.conflict("Insufficient account balance.");
    }

    final JournalEntry journalEntry = new JournalEntry();
    journalEntry.setTransactionIdentifier("chq-iss-" + UUID.randomUUID().toString());
    journalEntry.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    journalEntry.setTransactionType(ServiceConstants.TX_ISSUE_CHEQUES);
    journalEntry.setMessage(ServiceConstants.TX_ISSUE_CHEQUES);
    journalEntry.setClerk(UserContextHolder.checkedGetUser());

    final Debtor debtor = new Debtor();
    debtor.setAccountNumber(account.getIdentifier());
    debtor.setAmount(totalCharges.toString());
    journalEntry.setDebtors(Sets.newHashSet(debtor));

    journalEntry.setCreditors(
        charges
            .stream()
            .map(charge -> {
              final Creditor creditor = new Creditor();
              creditor.setAccountNumber(charge.getIncomeAccountIdentifier());
              creditor.setAmount(charge.getAmount().toString());
              return creditor;
            })
            .collect(Collectors.toSet())
    );

    this.ledgerManager.createJournalEntry(journalEntry);
  }

  public boolean accountExists(final String accountIdentifier) {
    return this.findAccount(accountIdentifier).isPresent();
  }

  public JournalEntry findJournalEntry(final String identifier) {
    return this.ledgerManager.findJournalEntry(identifier);
  }

  public void processJournalEntry(final JournalEntry journalEntry) {
    this.ledgerManager.createJournalEntry(journalEntry);
  }

  public Optional<Account> findAccount(final String accountNumber) {
    try {
      return Optional.of(this.ledgerManager.findAccount(accountNumber));
    } catch (final AccountNotFoundException anfex) {
      final AccountPage accountPage = this.ledgerManager.fetchAccounts(true, accountNumber, null, true,
          0, 10, null, null);

      return accountPage.getAccounts()
          .stream()
          .filter(account -> account.getAlternativeAccountNumber().equals(accountNumber))
          .findFirst();
    }
  }
}
