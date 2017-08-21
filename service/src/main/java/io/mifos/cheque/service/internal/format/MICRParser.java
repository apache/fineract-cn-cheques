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
package io.mifos.cheque.service.internal.format;

import io.mifos.cheque.api.v1.domain.MICR;

public class MICRParser {

  private static final String DELIMITER = "~";

  private MICRParser() {
    super();
  }

  public static String toIdentifier(final MICR micr) {
    if (micr == null
        || micr.getChequeNumber() == null || micr.getChequeNumber().isEmpty()
        || micr.getBranchSortCode() == null || micr.getBranchSortCode().isEmpty()
        || micr.getAccountNumber() == null || micr.getAccountNumber().isEmpty()) {
      throw new IllegalArgumentException("MICR must be given and all values need to be set.");
    }

    return micr.getChequeNumber()
        + MICRParser.DELIMITER
        + micr.getBranchSortCode()
        + MICRParser.DELIMITER
        + micr.getAccountNumber();
  }

  public static MICR fromIdentifier(final String identifier) {
    if (identifier == null || identifier.isEmpty()) {
      throw new IllegalArgumentException("Identifier must be given.");
    }

    final String[] micrParts = identifier.split(MICRParser.DELIMITER);

    if (micrParts.length != 3) {
      throw new IllegalArgumentException("Identifier must contain 3 parts delimited by " + MICRParser.DELIMITER + ".");
    }

    final MICR micr = new MICR();
    micr.setChequeNumber(micrParts[0]);
    micr.setBranchSortCode(micrParts[1]);
    micr.setAccountNumber(micrParts[2]);
    return micr;
  }
}
