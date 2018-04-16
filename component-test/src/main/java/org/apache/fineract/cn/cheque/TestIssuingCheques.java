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
package org.apache.fineract.cn.cheque;

import org.apache.fineract.cn.cheque.api.v1.domain.IssuingCount;
import org.apache.fineract.cn.cheque.service.internal.repository.IssuedChequeEntity;
import org.apache.fineract.cn.cheque.service.internal.repository.IssuedChequeRepository;
import org.apache.fineract.cn.cheque.service.internal.service.helper.AccountingService;
import org.apache.fineract.cn.cheque.service.internal.service.helper.DepositService;
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
