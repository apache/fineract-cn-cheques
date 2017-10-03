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
package io.mifos.cheque.api.v1.domain;

import io.mifos.core.lang.validation.constraints.ValidIdentifier;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ChequeTransaction {

  @NotNull
  @Valid
  private Cheque cheque;
  @ValidIdentifier(maxLength = 34)
  private String chequesReceivableAccount;
  @ValidIdentifier(maxLength = 34)
  private String creditorAccountNumber;

  public ChequeTransaction() {
    super();
  }

  public Cheque getCheque() {
    return this.cheque;
  }

  public void setCheque(final Cheque cheque) {
    this.cheque = cheque;
  }

  public String getChequesReceivableAccount() {
    return this.chequesReceivableAccount;
  }

  public void setChequesReceivableAccount(final String chequesReceivableAccount) {
    this.chequesReceivableAccount = chequesReceivableAccount;
  }

  public String getCreditorAccountNumber() {
    return this.creditorAccountNumber;
  }

  public void setCreditorAccountNumber(final String creditorAccountNumber) {
    this.creditorAccountNumber = creditorAccountNumber;
  }
}
