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

import io.mifos.cheque.api.v1.domain.Cheque;
import io.mifos.cheque.api.v1.domain.MICR;
import io.mifos.cheque.service.ServiceConstants;
import io.mifos.cheque.service.internal.mapper.ChequeMapper;
import io.mifos.cheque.service.internal.repository.ChequeRepository;
import io.mifos.cheque.service.internal.repository.specification.ChequeSpecification;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChequeService {

  private final Logger logger;
  private final ChequeRepository chequeRepository;

  @Autowired
  public ChequeService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                       final ChequeRepository chequeRepository) {
    super();
    this.logger = logger;
    this.chequeRepository = chequeRepository;
  }

  public boolean chequeExists(final Cheque cheque) {
    final MICR micr = cheque.getMicr();
    return this.chequeRepository.findByChequeNumberAndBranchSortCodeAndAccountNumber(
        micr.getChequeNumber(), micr.getBranchSortCode(), micr.getAccountNumber()
    ).isPresent();
  }

  public List<Cheque> fetchCheques(final String state, final String accountIdentifier) {
    return this.chequeRepository.findAll(ChequeSpecification.createListSpecification(state, accountIdentifier))
        .stream()
        .map(ChequeMapper::map)
        .collect(Collectors.toList());
  }

  public Optional<Cheque> findBy(final MICR micr) {
    return
        this.chequeRepository.findByChequeNumberAndBranchSortCodeAndAccountNumber(
            micr.getChequeNumber(), micr.getBranchSortCode(), micr.getAccountNumber())
            .map(ChequeMapper::map);
  }
}
