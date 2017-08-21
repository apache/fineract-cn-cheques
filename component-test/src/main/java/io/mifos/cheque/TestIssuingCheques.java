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

import io.mifos.cheque.api.v1.client.IssuingCount;
import io.mifos.cheque.service.internal.repository.IssuedChequeEntity;
import io.mifos.cheque.service.internal.repository.IssuedChequeRepository;
import io.mifos.cheque.service.internal.service.helper.AccountingService;
import io.mifos.cheque.service.internal.service.helper.DepositService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.Optional;

public class TestIssuingCheques extends AbstractChequeTest {

  @MockBean
  private DepositService depositServiceSpy;

  @MockBean
  private AccountingService accountingServiceSpy;

  @Autowired
  private IssuedChequeRepository issuedChequeRepository;

  public TestIssuingCheques() {
    super();
  }

  @Test
  public void shouldIssueChequesWithGivenStartSequence() throws Exception {
    final IssuingCount issuingCount = new IssuingCount();
    issuingCount.setAccountIdentifier(RandomStringUtils.randomAlphanumeric(34));
    issuingCount.setStart(201);
    issuingCount.setAmount(10);

    Mockito
        .doAnswer(invocation -> Collections.emptyList())
        .when(this.depositServiceSpy).getIssueChequeCharges(issuingCount.getAccountIdentifier());

    final String accountIdentifier = super.chequeManager.issue(issuingCount);
    Assert.assertEquals(issuingCount.getAccountIdentifier(), accountIdentifier);

    final Optional<IssuedChequeEntity> optionalIssuedCheque =
        this.issuedChequeRepository.findByAccountIdentifier(accountIdentifier);

    Assert.assertTrue(optionalIssuedCheque.isPresent());
    Assert.assertEquals(Integer.valueOf(210), optionalIssuedCheque.get().getLastIssuedNumber());
  }

  @Test
  public void shouldIssueChequesForNextIssuing() throws Exception {
    final IssuingCount issuingCount = new IssuingCount();
    issuingCount.setAccountIdentifier(RandomStringUtils.randomAlphanumeric(34));
    issuingCount.setAmount(50);

    Mockito
        .doAnswer(invocation -> Collections.emptyList())
        .when(this.depositServiceSpy).getIssueChequeCharges(issuingCount.getAccountIdentifier());

    final String accountIdentifier = super.chequeManager.issue(issuingCount);
    Assert.assertEquals(issuingCount.getAccountIdentifier(), accountIdentifier);

    final Optional<IssuedChequeEntity> optionalIssuedCheque =
        this.issuedChequeRepository.findByAccountIdentifier(accountIdentifier);

    Assert.assertTrue(optionalIssuedCheque.isPresent());
    Assert.assertEquals(Integer.valueOf(50), optionalIssuedCheque.get().getLastIssuedNumber());

    issuingCount.setAmount(50);

    final String nextFetchedAccountIdentifier = super.chequeManager.issue(issuingCount);
    Assert.assertEquals(issuingCount.getAccountIdentifier(), nextFetchedAccountIdentifier);

    final Optional<IssuedChequeEntity> nextOptionalIssuedCheque =
        this.issuedChequeRepository.findByAccountIdentifier(nextFetchedAccountIdentifier);

    Assert.assertTrue(nextOptionalIssuedCheque.isPresent());
    Assert.assertEquals(Integer.valueOf(100), nextOptionalIssuedCheque.get().getLastIssuedNumber());
  }
}
