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
package io.mifos.cheque.api.v1;

@SuppressWarnings("unused")
public interface EventConstants {

  String DESTINATION = "cheques-v1";
  String SELECTOR_NAME = "action";

  String INITIALIZE = "initialize";
  String SELECTOR_INITIALIZE = SELECTOR_NAME + " = '" + INITIALIZE + "'";

  String ISSUE_CHEQUES = "issue-cheques";
  String SELECTOR_ISSUE_CHEQUES = SELECTOR_NAME + " = '" + ISSUE_CHEQUES + "'";
  String CHEQUE_TRANSACTION = "cheque-transaction";
  String SELECTOR_CHEQUE_TRANSACTION = SELECTOR_NAME + " = '" + CHEQUE_TRANSACTION + "'";
  String CHEQUE_TRANSACTION_APPROVED = "cheque-transaction-approved";
  String SELECTOR_CHEQUE_TRANSACTION_APPROVED = SELECTOR_NAME + " = '" + CHEQUE_TRANSACTION_APPROVED + "'";
  String CHEQUE_TRANSACTION_CANCELED = "cheque-transaction-canceled";
  String SELECTOR_CHEQUE_TRANSACTION_CANCELED = SELECTOR_NAME + " = '" + CHEQUE_TRANSACTION_CANCELED + "'";
}
