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
package io.mifos.cheque.service.internal.service;

import io.mifos.cheque.api.v1.domain.MICR;
import io.mifos.cheque.api.v1.domain.MICRResolution;
import io.mifos.cheque.service.ServiceConstants;
import io.mifos.cheque.service.internal.repository.IssuedChequeEntity;
import io.mifos.cheque.service.internal.repository.IssuedChequeRepository;
import io.mifos.cheque.service.internal.service.helper.CustomerService;
import io.mifos.cheque.service.internal.service.helper.DepositService;
import io.mifos.cheque.service.internal.service.helper.OrganizationService;
import io.mifos.core.lang.ServiceException;
import io.mifos.customer.api.v1.domain.Customer;
import io.mifos.deposit.api.v1.instance.domain.ProductInstance;
import io.mifos.office.api.v1.domain.Office;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class MICRService {

  private final Logger logger;
  private final ChequeService chequeService;
  private final OrganizationService organizationService;
  private final DepositService depositService;
  private final CustomerService customerService;
  private final IssuedChequeRepository issuedChequeRepository;

  @Autowired
  public MICRService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                     final ChequeService chequeService,
                     final OrganizationService organizationService,
                     final DepositService depositService,
                     final CustomerService customerService,
                     final IssuedChequeRepository issuedChequeRepository) {
    super();
    this.logger = logger;
    this.chequeService = chequeService;
    this.organizationService = organizationService;
    this.depositService = depositService;
    this.customerService = customerService;
    this.issuedChequeRepository = issuedChequeRepository;
  }

  public MICRResolution expand(final MICR micr) {

    if (this.chequeService.findBy(micr).isPresent()) {
      throw ServiceException.conflict("Cheque already used.");
    }

    final Office office =
        this.organizationService.findOffice(micr.getBranchSortCode())
            .orElseThrow(() -> ServiceException.notFound("Given MICR is unknown."));

    final IssuedChequeEntity issuedChequeEntity =
        this.issuedChequeRepository.findByAccountIdentifier(micr.getAccountNumber())
            .orElseThrow(() -> ServiceException.conflict("Cheque was never issued."));
    if (Integer.valueOf(micr.getChequeNumber()) > issuedChequeEntity.getLastIssuedNumber()) {
      throw ServiceException.conflict("Cheque number invalid.");
    }

    final ProductInstance productInstance =
        this.depositService.findProductInstance(micr.getAccountNumber())
            .orElseThrow(() -> ServiceException.badRequest("Given account not valid."));

    final Customer customer =
        this.customerService.findCustomer(productInstance.getCustomerIdentifier())
            .orElseThrow(() -> ServiceException.badRequest("Given customer not valid."));

    final MICRResolution micrResolution = new MICRResolution();
    micrResolution.setOffice(office.getName());
    micrResolution.setCustomer(customer.getGivenName() + " " + customer.getSurname());
    return micrResolution;
  }
}
