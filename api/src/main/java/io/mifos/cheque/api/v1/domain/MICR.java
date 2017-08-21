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

public class MICR {

  @ValidIdentifier(maxLength = 8)
  private String chequeNumber;
  @ValidIdentifier(maxLength = 11)
  private String branchSortCode;
  @ValidIdentifier(maxLength = 34)
  private String accountNumber;

  public MICR() {
    super();
  }

  public String getChequeNumber() {
    return this.chequeNumber;
  }

  public void setChequeNumber(final String chequeNumber) {
    this.chequeNumber = chequeNumber;
  }

  public String getBranchSortCode() {
    return this.branchSortCode;
  }

  public void setBranchSortCode(final String branchSortCode) {
    this.branchSortCode = branchSortCode;
  }

  public String getAccountNumber() {
    return this.accountNumber;
  }

  public void setAccountNumber(final String accountNumber) {
    this.accountNumber = accountNumber;
  }
}
