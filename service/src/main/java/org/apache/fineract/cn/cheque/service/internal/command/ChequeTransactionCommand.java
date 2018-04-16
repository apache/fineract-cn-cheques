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
package org.apache.fineract.cn.cheque.service.internal.command;

import org.apache.fineract.cn.cheque.api.v1.domain.Cheque;

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
