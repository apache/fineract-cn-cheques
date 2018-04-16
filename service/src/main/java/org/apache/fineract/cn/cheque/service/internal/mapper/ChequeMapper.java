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
package org.apache.fineract.cn.cheque.service.internal.mapper;

import org.apache.fineract.cn.cheque.api.v1.domain.Cheque;
import org.apache.fineract.cn.cheque.api.v1.domain.MICR;
import org.apache.fineract.cn.cheque.service.internal.repository.ChequeEntity;
import java.sql.Date;
import java.time.Clock;
import java.time.LocalDateTime;
import org.apache.fineract.cn.api.util.UserContextHolder;
import org.apache.fineract.cn.lang.DateConverter;

public class ChequeMapper {

  private ChequeMapper() {
    super();
  }

  public static ChequeEntity map(final Cheque cheque) {
    final ChequeEntity chequeEntity = new ChequeEntity();
    chequeEntity.setChequeNumber(cheque.getMicr().getChequeNumber());
    chequeEntity.setBranchSortCode(cheque.getMicr().getBranchSortCode());
    chequeEntity.setAccountNumber(cheque.getMicr().getAccountNumber());
    chequeEntity.setDrawee(cheque.getDrawee());
    chequeEntity.setDrawer(cheque.getDrawer());
    chequeEntity.setPayee(cheque.getPayee());
    chequeEntity.setAmount(Double.valueOf(cheque.getAmount()));
    final Date dateIssued = Date.valueOf(DateConverter.dateFromIsoString(cheque.getDateIssued()));
    chequeEntity.setDateIssued(dateIssued);
    chequeEntity.setOpenCheque(cheque.isOpenCheque());
    chequeEntity.setJournalEntryIdentifier(cheque.getJournalEntryIdentifier());
    chequeEntity.setCreatedBy(UserContextHolder.checkedGetUser());
    chequeEntity.setCreatedOn(LocalDateTime.now(Clock.systemUTC()));
    return chequeEntity;
  }

  public static Cheque map(final ChequeEntity chequeEntity) {
    final MICR micr = new MICR();
    micr.setChequeNumber(chequeEntity.getChequeNumber());
    micr.setBranchSortCode(chequeEntity.getBranchSortCode());
    micr.setAccountNumber(chequeEntity.getAccountNumber());

    final Cheque cheque = new Cheque();
    cheque.setMicr(micr);
    cheque.setDrawee(chequeEntity.getDrawee());
    cheque.setDrawer(chequeEntity.getDrawer());
    cheque.setPayee(chequeEntity.getPayee());
    cheque.setAmount(chequeEntity.getAmount().toString());
    cheque.setDateIssued(DateConverter.toIsoString(chequeEntity.getDateIssued().toLocalDate()));
    cheque.setOpenCheque(chequeEntity.getOpenCheque());
    cheque.setState(chequeEntity.getState());
    cheque.setJournalEntryIdentifier(chequeEntity.getJournalEntryIdentifier());
    return cheque;
  }
}
