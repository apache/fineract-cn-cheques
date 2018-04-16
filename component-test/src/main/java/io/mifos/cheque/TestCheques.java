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
package io.mifos.cheque;

import com.google.common.collect.Sets;
import io.mifos.cheque.api.v1.EventConstants;
import io.mifos.cheque.api.v1.domain.Action;
import io.mifos.cheque.api.v1.domain.Cheque;
import io.mifos.cheque.api.v1.domain.ChequeProcessingCommand;
import io.mifos.cheque.api.v1.domain.ChequeTransaction;
import io.mifos.cheque.api.v1.domain.IssuingCount;
import io.mifos.cheque.api.v1.domain.State;
import io.mifos.cheque.service.internal.format.MICRParser;
import io.mifos.cheque.service.internal.service.helper.AccountingService;
import io.mifos.cheque.service.internal.service.helper.DepositService;
import io.mifos.cheque.service.internal.service.helper.OrganizationService;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.cn.accounting.api.v1.domain.Account;
import org.apache.fineract.cn.accounting.api.v1.domain.Creditor;
import org.apache.fineract.cn.accounting.api.v1.domain.Debtor;
import org.apache.fineract.cn.accounting.api.v1.domain.JournalEntry;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;

public class TestCheques extends AbstractChequeTest {

  @MockBean
  private OrganizationService organizationServiceSpy;

  @MockBean
  private DepositService depositServiceSpy;

  @MockBean
  private AccountingService accountingServiceSpy;

  @Autowired
  private Validator validator;

  public TestCheques() {
    super();
  }

  @Test
  public void shouldProcessChequeOnUs() throws Exception {
    final Cheque randomCheque = Fixture.createRandomCheque();

    final IssuingCount issuingCount = new IssuingCount();
    issuingCount.setAccountIdentifier(randomCheque.getMicr().getAccountNumber());
    issuingCount.setAmount(100);

    Mockito
        .doAnswer(invocation -> Collections.emptyList())
        .when(this.depositServiceSpy).getIssueChequeCharges(randomCheque.getMicr().getAccountNumber());

    super.chequeManager.issue(issuingCount);

    Assert.assertTrue(
        super.eventRecorder.wait(EventConstants.ISSUE_CHEQUES, randomCheque.getMicr().getAccountNumber())
    );

    Mockito
        .doAnswer(invocation -> true)
        .when(this.organizationServiceSpy).officeExistsByBranchSortCode(randomCheque.getMicr().getBranchSortCode());

    Mockito
        .doAnswer(invocation -> true)
        .when(this.accountingServiceSpy).accountExists(randomCheque.getMicr().getAccountNumber());

    final Account mockedAccount = new Account();
    mockedAccount.setIdentifier(randomCheque.getMicr().getAccountNumber());
    Mockito
        .doAnswer(invocation -> Optional.of(mockedAccount))
        .when(this.accountingServiceSpy).findAccount(randomCheque.getMicr().getAccountNumber());

    final ChequeTransaction chequeTransaction = new ChequeTransaction();
    chequeTransaction.setCheque(randomCheque);
    chequeTransaction.setChequesReceivableAccount(RandomStringUtils.randomAlphabetic(34));
    chequeTransaction.setCreditorAccountNumber(RandomStringUtils.randomAlphanumeric(34));
    super.chequeManager.process(chequeTransaction);

    Assert.assertTrue(
      super.eventRecorder.wait(EventConstants.CHEQUE_TRANSACTION, MICRParser.toIdentifier(randomCheque.getMicr()))
    );

    final Cheque fetchedCheque = super.chequeManager.get(MICRParser.toIdentifier(randomCheque.getMicr()));
    Assert.assertNotNull(fetchedCheque);
    Assert.assertEquals(State.PROCESSED.name(), fetchedCheque.getState());

    Mockito
        .verify(this.accountingServiceSpy, Mockito.times(1))
        .processJournalEntry(Matchers.any(JournalEntry.class));
  }

  @Test
  public void shouldApproveChequeNotOnUs() throws Exception {
    final Cheque randomCheque = Fixture.createRandomCheque();

    Mockito
        .doAnswer(invocation -> false)
        .when(this.organizationServiceSpy).officeExistsByBranchSortCode(randomCheque.getMicr().getBranchSortCode());

    Mockito
        .doAnswer(invocation -> false)
        .when(this.accountingServiceSpy).accountExists(randomCheque.getMicr().getAccountNumber());


    final ChequeTransaction chequeTransaction = new ChequeTransaction();
    chequeTransaction.setCheque(randomCheque);
    chequeTransaction.setChequesReceivableAccount(RandomStringUtils.randomAlphabetic(34));
    chequeTransaction.setCreditorAccountNumber(RandomStringUtils.randomAlphanumeric(34));
    super.chequeManager.process(chequeTransaction);

    Assert.assertTrue(
        super.eventRecorder.wait(EventConstants.CHEQUE_TRANSACTION, MICRParser.toIdentifier(randomCheque.getMicr()))
    );

    final Cheque fetchedCheque = super.chequeManager.get(MICRParser.toIdentifier(randomCheque.getMicr()));
    Assert.assertNotNull(fetchedCheque);
    Assert.assertEquals(State.PENDING.name(), fetchedCheque.getState());

    final ChequeProcessingCommand chequeProcessingCommand = new ChequeProcessingCommand();
    chequeProcessingCommand.setAction(Action.APPROVE.name());

    super.chequeManager.process(MICRParser.toIdentifier(fetchedCheque.getMicr()), chequeProcessingCommand);

    Assert.assertTrue(
        super.eventRecorder.wait(EventConstants.CHEQUE_TRANSACTION_APPROVED,
            MICRParser.toIdentifier(fetchedCheque.getMicr()))
    );

    final Cheque approvedCheque = super.chequeManager.get(MICRParser.toIdentifier(randomCheque.getMicr()));
    Assert.assertNotNull(approvedCheque);
    Assert.assertEquals(State.PROCESSED.name(), approvedCheque.getState());

    Mockito
        .verify(this.accountingServiceSpy, Mockito.times(1))
        .processJournalEntry(Matchers.any(JournalEntry.class));
  }

  @Test
  public void shouldCancelChequeNotOnUs() throws Exception {
    final Cheque randomCheque = Fixture.createRandomCheque();

    Mockito
        .doAnswer(invocation -> false)
        .when(this.organizationServiceSpy).officeExistsByBranchSortCode(randomCheque.getMicr().getBranchSortCode());

    Mockito
        .doAnswer(invocation -> false)
        .when(this.accountingServiceSpy).accountExists(randomCheque.getMicr().getAccountNumber());

    final ChequeTransaction chequeTransaction = new ChequeTransaction();
    chequeTransaction.setChequesReceivableAccount(RandomStringUtils.randomAlphabetic(34));
    chequeTransaction.setCheque(randomCheque);
    chequeTransaction.setCreditorAccountNumber(RandomStringUtils.randomAlphanumeric(34));
    super.chequeManager.process(chequeTransaction);

    Assert.assertTrue(
        super.eventRecorder.wait(EventConstants.CHEQUE_TRANSACTION, MICRParser.toIdentifier(randomCheque.getMicr()))
    );

    final Cheque fetchedCheque = super.chequeManager.get(MICRParser.toIdentifier(randomCheque.getMicr()));
    Assert.assertNotNull(fetchedCheque);
    Assert.assertEquals(State.PENDING.name(), fetchedCheque.getState());

    final ChequeProcessingCommand chequeProcessingCommand = new ChequeProcessingCommand();
    chequeProcessingCommand.setAction(Action.CANCEL.name());

    Mockito
        .doAnswer(invocation -> {
          final JournalEntry journalEntry = new JournalEntry();
          journalEntry.setTransactionIdentifier(UUID.randomUUID().toString());

          final Debtor debtor = new Debtor();
          debtor.setAccountNumber(RandomStringUtils.randomAlphanumeric(34));
          debtor.setAmount("100.00");
          journalEntry.setDebtors(Sets.newHashSet(debtor));

          final Creditor creditor = new Creditor();
          creditor.setAccountNumber(RandomStringUtils.randomAlphanumeric(34));
          creditor.setAmount("100.00");
          journalEntry.setCreditors(Sets.newHashSet(creditor));
          return journalEntry;
        })
        .when(this.accountingServiceSpy).findJournalEntry(Matchers.anyString());

    super.chequeManager.process(MICRParser.toIdentifier(fetchedCheque.getMicr()), chequeProcessingCommand);

    Assert.assertTrue(
        super.eventRecorder.wait(EventConstants.CHEQUE_TRANSACTION_CANCELED,
            MICRParser.toIdentifier(fetchedCheque.getMicr()))
    );

    final Cheque canceledCheque = super.chequeManager.get(MICRParser.toIdentifier(randomCheque.getMicr()));
    Assert.assertNotNull(canceledCheque);
    Assert.assertEquals(State.CANCELED.name(), canceledCheque.getState());

    Mockito
        .verify(this.accountingServiceSpy, Mockito.times(2))
        .processJournalEntry(Matchers.any(JournalEntry.class));
  }

  @Test
  public void shouldProcessOpenCheque() throws Exception {
      final Cheque randomCheque = Fixture.createRandomCheque();
      randomCheque.setOpenCheque(Boolean.TRUE);

      Mockito
          .doAnswer(invocation -> false)
          .when(this.organizationServiceSpy).officeExistsByBranchSortCode(randomCheque.getMicr().getBranchSortCode());

      Mockito
          .doAnswer(invocation -> false)
          .when(this.accountingServiceSpy).accountExists(randomCheque.getMicr().getAccountNumber());

      Mockito.doAnswer(invocation -> {
        final JournalEntry journalEntry = invocation.getArgumentAt(0, JournalEntry.class);
        final BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(journalEntry, "journalEntry");
        this.validator.validate(journalEntry, bindingResult);
        if (bindingResult.getErrorCount() != 0) {
          bindingResult.getAllErrors().forEach(objectError -> {
            System.out.println(objectError.toString());
          });
        }
        return invocation.getMethod().getReturnType();
      }).when(this.accountingServiceSpy).processJournalEntry(Matchers.any(JournalEntry.class));

      final ChequeTransaction chequeTransaction = new ChequeTransaction();
      chequeTransaction.setChequesReceivableAccount(RandomStringUtils.randomAlphabetic(34));
      chequeTransaction.setCheque(randomCheque);
      chequeTransaction.setCreditorAccountNumber(RandomStringUtils.randomAlphanumeric(34));
      super.chequeManager.process(chequeTransaction);

      Assert.assertTrue(
          super.eventRecorder.wait(EventConstants.CHEQUE_TRANSACTION, MICRParser.toIdentifier(randomCheque.getMicr()))
      );

    }
}
