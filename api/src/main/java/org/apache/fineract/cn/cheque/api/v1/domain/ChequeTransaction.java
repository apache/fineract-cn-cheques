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
package org.apache.fineract.cn.cheque.api.v1.domain;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.fineract.cn.lang.validation.constraints.ValidIdentifier;

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
