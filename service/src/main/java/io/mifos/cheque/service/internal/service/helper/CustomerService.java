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
import io.mifos.customer.api.v1.client.CustomerManager;
import io.mifos.customer.api.v1.client.CustomerNotFoundException;
import io.mifos.customer.api.v1.domain.Customer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {

  private final Logger logger;
  private final CustomerManager customerManager;

  @Autowired
  public CustomerService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                         final CustomerManager customerManager) {
    super();
    this.logger = logger;
    this.customerManager = customerManager;
  }

  public Optional<Customer> findCustomer(final String identifier) {
    try {
      return Optional.of(this.customerManager.findCustomer(identifier));
    } catch (final CustomerNotFoundException cnfex) {
      return Optional.empty();
    }
  }
}
