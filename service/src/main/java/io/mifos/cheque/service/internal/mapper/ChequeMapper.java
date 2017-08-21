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
package io.mifos.cheque.service.internal.mapper;

import io.mifos.cheque.api.v1.domain.Cheque;
import io.mifos.cheque.api.v1.domain.MICR;
import io.mifos.cheque.service.internal.repository.ChequeEntity;
import io.mifos.core.api.util.UserContextHolder;
import io.mifos.core.lang.DateConverter;

import java.sql.Date;
import java.time.Clock;
import java.time.LocalDateTime;

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
