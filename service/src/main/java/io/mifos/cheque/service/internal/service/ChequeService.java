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
