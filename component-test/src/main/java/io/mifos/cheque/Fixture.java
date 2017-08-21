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
package io.mifos.cheque;

import io.mifos.cheque.api.v1.domain.Cheque;
import io.mifos.cheque.api.v1.domain.MICR;
import io.mifos.core.lang.DateConverter;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.Clock;
import java.time.LocalDate;

public class Fixture {

  private Fixture() {
    super();
  }

  public static Cheque createRandomCheque() {
    final MICR micr = new MICR();
    micr.setChequeNumber(RandomStringUtils.randomNumeric(2));
    micr.setBranchSortCode(RandomStringUtils.randomAlphanumeric(11));
    micr.setAccountNumber(RandomStringUtils.randomAlphanumeric(34));

    final Cheque cheque = new Cheque();
    cheque.setMicr(micr);
    cheque.setDrawee(RandomStringUtils.randomAlphanumeric(2048));
    cheque.setDrawer(RandomStringUtils.randomAlphanumeric(256));
    cheque.setPayee(RandomStringUtils.randomAlphanumeric(256));
    cheque.setAmount(Double.valueOf(5000.00).toString());
    cheque.setDateIssued(DateConverter.toIsoString(LocalDate.now(Clock.systemUTC())));
    cheque.setJournalEntryIdentifier(RandomStringUtils.randomAlphanumeric(2200));

    return cheque;
  }
}
