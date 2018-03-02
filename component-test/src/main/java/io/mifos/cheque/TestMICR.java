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

import io.mifos.accounting.api.v1.domain.Account;
import io.mifos.cheque.api.v1.EventConstants;
import io.mifos.cheque.api.v1.client.DependingResourceNotValidException;
import io.mifos.cheque.api.v1.client.InvalidChequeNumberException;
import io.mifos.cheque.api.v1.domain.Cheque;
import io.mifos.cheque.api.v1.domain.ChequeTransaction;
import io.mifos.cheque.api.v1.domain.IssuingCount;
import io.mifos.cheque.api.v1.domain.MICR;
import io.mifos.cheque.api.v1.domain.MICRResolution;
import io.mifos.cheque.service.internal.format.MICRParser;
import io.mifos.cheque.service.internal.service.helper.AccountingService;
import io.mifos.cheque.service.internal.service.helper.CustomerService;
import io.mifos.cheque.service.internal.service.helper.DepositService;
import io.mifos.cheque.service.internal.service.helper.OrganizationService;
import io.mifos.core.api.util.NotFoundException;
import io.mifos.customer.api.v1.domain.Customer;
import io.mifos.deposit.api.v1.instance.domain.ProductInstance;
import io.mifos.office.api.v1.domain.Office;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.Optional;

public class TestMICR extends AbstractChequeTest {

  private static final String OFFICE_NAME = "Money Bin";
  private static final String CUSTOMER_IDENTIFIER = "scrooge.mcduck";
  private static final String GIVEN_NAME = "Scrooge";
  private static final String SURNAME = "Mc Duck";

  @MockBean
  private OrganizationService organizationServiceSpy;

  @MockBean
  private DepositService depositServiceSpy;

  @MockBean
  private AccountingService accountingServiceSpy;

  @MockBean
  private CustomerService customerServiceSpy;

  @Autowired
  public TestMICR() {
    super();
  }

  @Test
  public void shouldReturnResolution() throws Exception {

    final MICR micr = Fixture.createRandomCheque().getMicr();

    Mockito
        .doAnswer(invocation -> {
          final Office mockedOffice = new Office();
          mockedOffice.setName(TestMICR.OFFICE_NAME);
          return Optional.of(mockedOffice);
        })
        .when(this.organizationServiceSpy).findOffice(micr.getBranchSortCode());

    Mockito
        .doAnswer(invocation -> {
          final ProductInstance mockedProductInstance = new ProductInstance();
          mockedProductInstance.setCustomerIdentifier(TestMICR.CUSTOMER_IDENTIFIER);
          return Optional.of(mockedProductInstance);
        })
        .when(this.depositServiceSpy).findProductInstance(micr.getAccountNumber());

    Mockito
        .doAnswer(invocation -> Collections.emptyList())
        .when(this.depositServiceSpy).getIssueChequeCharges(micr.getAccountNumber());

    Mockito
        .doAnswer(invocation -> {
          final Customer mockedCustomer = new Customer();
          mockedCustomer.setGivenName(TestMICR.GIVEN_NAME);
          mockedCustomer.setSurname(TestMICR.SURNAME);
          return Optional.of(mockedCustomer);
        })
        .when(this.customerServiceSpy).findCustomer(TestMICR.CUSTOMER_IDENTIFIER);

    final IssuingCount issuingCount = new IssuingCount();
    issuingCount.setAccountIdentifier(micr.getAccountNumber());
    issuingCount.setAmount(100);

    super.chequeManager.issue(issuingCount);

    Assert.assertTrue(
        super.eventRecorder.wait(EventConstants.ISSUE_CHEQUES, micr.getAccountNumber())
    );

    final MICRResolution micrResolution = super.chequeManager.expandMicr(MICRParser.toIdentifier(micr));
    Assert.assertNotNull(micrResolution);
    Assert.assertEquals(OFFICE_NAME, micrResolution.getOffice());
    Assert.assertEquals(GIVEN_NAME + " " + SURNAME, micrResolution.getCustomer());
  }

  @Test(expected = NotFoundException.class)
  public void shouldNotReturnResolutionNotOnUs() throws Exception {
    final MICR micr = Fixture.createRandomCheque().getMicr();

    Mockito
        .doAnswer(invocation -> Optional.empty())
        .when(this.organizationServiceSpy).findOffice(micr.getBranchSortCode());

    super.chequeManager.expandMicr(MICRParser.toIdentifier(micr));
  }

  @Test(expected = InvalidChequeNumberException.class)
  public void shouldNotReturnResolutionChequeAlreadyUsed() throws Exception {
    final Cheque randomCheque = Fixture.createRandomCheque();
    final MICR micr = randomCheque.getMicr();

    final IssuingCount issuingCount = new IssuingCount();
    issuingCount.setAccountIdentifier(micr.getAccountNumber());
    issuingCount.setAmount(100);

    super.chequeManager.issue(issuingCount);

    Assert.assertTrue(
        super.eventRecorder.wait(EventConstants.ISSUE_CHEQUES, micr.getAccountNumber())
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
    chequeTransaction.setCreditorAccountNumber(RandomStringUtils.randomAlphanumeric(34));
    chequeTransaction.setChequesReceivableAccount(RandomStringUtils.randomAlphanumeric(34));
    super.chequeManager.process(chequeTransaction);

    Assert.assertTrue(
        super.eventRecorder.wait(EventConstants.CHEQUE_TRANSACTION, MICRParser.toIdentifier(randomCheque.getMicr()))
    );

    super.chequeManager.expandMicr(MICRParser.toIdentifier(micr));
  }

  @Test(expected = InvalidChequeNumberException.class)
  public void shouldNotReturnResolutionChequeNotIssued() throws Exception {
    final MICR micr = Fixture.createRandomCheque().getMicr();

    Mockito
        .doAnswer(invocation -> {
          final Office mockedOffice = new Office();
          mockedOffice.setName(TestMICR.OFFICE_NAME);
          return Optional.of(mockedOffice);
        })
        .when(this.organizationServiceSpy).findOffice(micr.getBranchSortCode());

    super.chequeManager.expandMicr(MICRParser.toIdentifier(micr));
  }

  @Test(expected = InvalidChequeNumberException.class)
  public void shouldNotReturnResolutionUnknownChequeNumber() throws Exception {
    final MICR micr = Fixture.createRandomCheque().getMicr();
    micr.setChequeNumber("0815");

    final IssuingCount issuingCount = new IssuingCount();
    issuingCount.setAccountIdentifier(micr.getAccountNumber());
    issuingCount.setAmount(100);

    super.chequeManager.issue(issuingCount);

    Assert.assertTrue(
        super.eventRecorder.wait(EventConstants.ISSUE_CHEQUES, micr.getAccountNumber())
    );

    Mockito
        .doAnswer(invocation -> {
          final Office mockedOffice = new Office();
          mockedOffice.setName(TestMICR.OFFICE_NAME);
          return Optional.of(mockedOffice);
        })
        .when(this.organizationServiceSpy).findOffice(micr.getBranchSortCode());

    super.chequeManager.expandMicr(MICRParser.toIdentifier(micr));
  }

  @Test(expected = DependingResourceNotValidException.class)
  public void shouldNotReturnResolutionUnknownAccount() throws Exception {
    final MICR micr = Fixture.createRandomCheque().getMicr();

    Mockito
        .doAnswer(invocation -> {
          final Office mockedOffice = new Office();
          mockedOffice.setName(TestMICR.OFFICE_NAME);
          return Optional.of(mockedOffice);
        })
        .when(this.organizationServiceSpy).findOffice(micr.getBranchSortCode());

    Mockito
        .doAnswer(invocation -> Optional.empty())
        .when(this.depositServiceSpy).findProductInstance(micr.getAccountNumber());

    final IssuingCount issuingCount = new IssuingCount();
    issuingCount.setAccountIdentifier(micr.getAccountNumber());
    issuingCount.setAmount(100);

    super.chequeManager.issue(issuingCount);

    Assert.assertTrue(
        super.eventRecorder.wait(EventConstants.ISSUE_CHEQUES, micr.getAccountNumber())
    );

    super.chequeManager.expandMicr(MICRParser.toIdentifier(micr));
  }

  @Test(expected = DependingResourceNotValidException.class)
  public void shouldNotReturnResolutionUnknownCustomer() throws Exception {
    final MICR micr = Fixture.createRandomCheque().getMicr();

    Mockito
        .doAnswer(invocation -> {
          final Office mockedOffice = new Office();
          mockedOffice.setName(TestMICR.OFFICE_NAME);
          return Optional.of(mockedOffice);
        })
        .when(this.organizationServiceSpy).findOffice(micr.getBranchSortCode());

    Mockito
        .doAnswer(invocation -> {
          final ProductInstance mockedProductInstance = new ProductInstance();
          mockedProductInstance.setCustomerIdentifier(TestMICR.CUSTOMER_IDENTIFIER);
          return Optional.of(mockedProductInstance);
        })
        .when(this.depositServiceSpy).findProductInstance(micr.getAccountNumber());

    Mockito
        .doAnswer(invocation -> Optional.empty())
        .when(this.customerServiceSpy).findCustomer(TestMICR.CUSTOMER_IDENTIFIER);

    final IssuingCount issuingCount = new IssuingCount();
    issuingCount.setAccountIdentifier(micr.getAccountNumber());
    issuingCount.setAmount(100);

    super.chequeManager.issue(issuingCount);

    Assert.assertTrue(
        super.eventRecorder.wait(EventConstants.ISSUE_CHEQUES, micr.getAccountNumber())
    );

    super.chequeManager.expandMicr(MICRParser.toIdentifier(micr));
  }
}
