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
package io.mifos.cheque.service.internal.service.helper;

import io.mifos.cheque.service.ServiceConstants;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.fineract.cn.deposit.api.v1.client.DepositAccountManager;
import org.apache.fineract.cn.deposit.api.v1.definition.domain.Action;
import org.apache.fineract.cn.deposit.api.v1.definition.domain.Charge;
import org.apache.fineract.cn.deposit.api.v1.definition.domain.ProductDefinition;
import org.apache.fineract.cn.deposit.api.v1.instance.ProductInstanceNotFoundException;
import org.apache.fineract.cn.deposit.api.v1.instance.domain.ProductInstance;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class DepositService {

  private final Logger logger;
  private final DepositAccountManager depositAccountManager;

  @Autowired
  public DepositService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                        final DepositAccountManager depositAccountManager) {
    super();
    this.logger = logger;
    this.depositAccountManager = depositAccountManager;
  }

  public List<Charge> getIssueChequeCharges(final String accountIdentifier) {

    final List<Action> actions = this.depositAccountManager.fetchActions();

    final List<String> issueChequeActions =
        actions
            .stream()
            .filter(action -> action.getTransactionType().equals(ServiceConstants.TX_ISSUE_CHEQUES))
            .map(Action::getIdentifier)
            .collect(Collectors.toList());

    final ProductInstance productInstance =
        this.depositAccountManager.findProductInstance(accountIdentifier);

    final ProductDefinition productDefinition =
        this.depositAccountManager.findProductDefinition(productInstance.getProductIdentifier());

    return productDefinition.getCharges()
        .stream()
        .filter(charge -> issueChequeActions.contains(charge.getActionIdentifier()))
        .collect(Collectors.toList());
  }

  public String getCashAccountForProduct(final String accountIdentifier) {
    final ProductInstance productInstance =
        this.depositAccountManager.findProductInstance(accountIdentifier);

    final ProductDefinition productDefinition =
        this.depositAccountManager.findProductDefinition(productInstance.getProductIdentifier());

    return productDefinition.getCashAccountIdentifier();
  }

  public Optional<ProductInstance> findProductInstance(final String accountNumber) {
    try {
      return Optional.of(this.depositAccountManager.findProductInstance(accountNumber));
    } catch (final ProductInstanceNotFoundException pinfex) {
      return Optional.empty();
    }
  }
}
