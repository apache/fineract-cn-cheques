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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class IssuingCount {

  private String accountIdentifier;
  private Integer start;
  @NotNull
  @Min(1)
  private Integer amount;

  public IssuingCount() {
    super();
  }

  public String getAccountIdentifier() {
    return this.accountIdentifier;
  }

  public void setAccountIdentifier(final String accountIdentifier) {
    this.accountIdentifier = accountIdentifier;
  }

  public Integer getStart() {
    return this.start;
  }

  public void setStart(final Integer start) {
    this.start = start;
  }

  public Integer getAmount() {
    return this.amount;
  }

  public void setAmount(final Integer amount) {
    this.amount = amount;
  }
}
