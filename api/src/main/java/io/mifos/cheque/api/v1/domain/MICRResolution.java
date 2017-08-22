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

public class MICRResolution {

  private String office;
  private String customer;

  public MICRResolution() {
    super();
  }

  public String getOffice() {
    return this.office;
  }

  public void setOffice(final String office) {
    this.office = office;
  }

  public String getCustomer() {
    return this.customer;
  }

  public void setCustomer(final String customer) {
    this.customer = customer;
  }
}
