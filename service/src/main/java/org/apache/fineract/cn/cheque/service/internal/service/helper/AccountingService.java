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
package org.apache.fineract.cn.cheque.service.internal.service.helper;

import com.google.common.collect.Sets;
import org.apache.fineract.cn.cheque.service.ServiceConstants;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.fineract.cn.accounting.api.v1.client.AccountNotFoundException;
import org.apache.fineract.cn.accounting.api.v1.client.LedgerManager;
import org.apache.fineract.cn.accounting.api.v1.domain.Account;
import org.apache.fineract.cn.accounting.api.v1.domain.AccountPage;
import org.apache.fineract.cn.accounting.api.v1.domain.Creditor;
import org.apache.fineract.cn.accounting.api.v1.domain.Debtor;
import org.apache.fineract.cn.accounting.api.v1.domain.JournalEntry;
import org.apache.fineract.cn.api.util.UserContextHolder;
import org.apache.fineract.cn.deposit.api.v1.definition.domain.Charge;
import org.apache.fineract.cn.lang.DateConverter;
import org.apache.fineract.cn.lang.ServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
