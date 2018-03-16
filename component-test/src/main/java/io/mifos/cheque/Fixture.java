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
