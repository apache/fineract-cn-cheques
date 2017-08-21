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
package io.mifos.cheque.listener;

import io.mifos.cheque.AbstractChequeTest;
import io.mifos.cheque.api.v1.EventConstants;
import io.mifos.core.lang.config.TenantHeaderFilter;
import io.mifos.core.test.listener.EventRecorder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class ChequeEventListener {

  private final Logger logger;
  private final EventRecorder eventRecorder;

  public ChequeEventListener(@Qualifier(AbstractChequeTest.TEST_LOGGER) final Logger logger,
                             final EventRecorder eventRecorder) {
    super();
    this.logger = logger;
    this.eventRecorder = eventRecorder;
  }

  @JmsListener(
      destination = EventConstants.DESTINATION,
      selector = EventConstants.SELECTOR_ISSUE_CHEQUES,
      subscription = EventConstants.DESTINATION
  )
  public void onIssueCheques(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                             final String payload) {
    this.logger.debug("Cheques issued.");
    this.eventRecorder.event(tenant, EventConstants.ISSUE_CHEQUES, payload, String.class);
  }

  @JmsListener(
      destination = EventConstants.DESTINATION,
      selector = EventConstants.SELECTOR_CHEQUE_TRANSACTION,
      subscription = EventConstants.DESTINATION
  )
  public void onChequeTransaction(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                                  final String payload) {
    this.logger.debug("Cheque transaction processed.");
    this.eventRecorder.event(tenant, EventConstants.CHEQUE_TRANSACTION, payload, String.class);
  }

  @JmsListener(
      destination = EventConstants.DESTINATION,
      selector = EventConstants.SELECTOR_CHEQUE_TRANSACTION_APPROVED,
      subscription = EventConstants.DESTINATION
  )
  public void onChequeApproved(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                               final String payload) {
    this.logger.debug("Cheque transaction approved.");
    this.eventRecorder.event(tenant, EventConstants.CHEQUE_TRANSACTION_APPROVED, payload, String.class);
  }

  @JmsListener(
      destination = EventConstants.DESTINATION,
      selector = EventConstants.SELECTOR_CHEQUE_TRANSACTION_CANCELED,
      subscription = EventConstants.DESTINATION
  )
  public void onChequeCanceled(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                               final String payload) {
    this.logger.debug("Cheque transaction canceled.");
    this.eventRecorder.event(tenant, EventConstants.CHEQUE_TRANSACTION_CANCELED, payload, String.class);
  }
}
