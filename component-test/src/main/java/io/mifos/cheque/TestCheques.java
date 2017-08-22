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
package io.mifos.cheque;

import com.google.common.collect.Sets;
import io.mifos.accounting.api.v1.domain.Creditor;
import io.mifos.accounting.api.v1.domain.Debtor;
import io.mifos.accounting.api.v1.domain.JournalEntry;
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
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.UUID;

public class TestCheques extends AbstractChequeTest {

  @MockBean
  private OrganizationService organizationServiceSpy;

  @MockBean
  private DepositService depositServiceSpy;

  @MockBean
  private AccountingService accountingServiceSpy;

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

    final ChequeTransaction chequeTransaction = new ChequeTransaction();
    chequeTransaction.setCheque(randomCheque);
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
}
