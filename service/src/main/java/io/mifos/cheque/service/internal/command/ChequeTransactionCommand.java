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
package io.mifos.cheque.service.internal.command;

import io.mifos.cheque.api.v1.domain.Cheque;

public class ChequeTransactionCommand {
  private final Cheque cheque;
  private final String chequesReceivableAccount;
  private final String creditorAccount;

  public ChequeTransactionCommand(final Cheque cheque,
                                  final String chequesReceivableAccount,
                                  final String creditorAccount) {
    super();
    this.cheque = cheque;
    this.chequesReceivableAccount = chequesReceivableAccount;
    this.creditorAccount = creditorAccount;
  }

  public Cheque cheque() {
    return this.cheque;
  }

  public String chequesReceivableAccount() {
    return this.chequesReceivableAccount;
  }

  public String creditorAccount() {
    return this.creditorAccount;
  }
}
