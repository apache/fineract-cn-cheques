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
package io.mifos.cheque.service.internal.service.helper;

import io.mifos.cheque.service.ServiceConstants;
import io.mifos.deposit.api.v1.client.DepositAccountManager;
import io.mifos.deposit.api.v1.definition.domain.Action;
import io.mifos.deposit.api.v1.definition.domain.Charge;
import io.mifos.deposit.api.v1.definition.domain.ProductDefinition;
import io.mifos.deposit.api.v1.instance.ProductInstanceNotFoundException;
import io.mifos.deposit.api.v1.instance.domain.ProductInstance;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
