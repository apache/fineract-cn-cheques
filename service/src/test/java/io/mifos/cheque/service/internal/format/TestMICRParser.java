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
import org.junit.Assert;
import org.junit.Test;

public class TestMICRParser {

  @Test
  public void shouldParseIdentifier() throws Exception {
    final String identifier = "1357~2468~1470";

    final MICR micr = MICRParser.fromIdentifier(identifier);
    Assert.assertEquals("1357", micr.getChequeNumber());
    Assert.assertEquals("2468", micr.getBranchSortCode());
    Assert.assertEquals("1470", micr.getAccountNumber());
  }

  @Test
  public void shouldParseMIRC() throws Exception {
    final MICR micr = new MICR();
    micr.setChequeNumber("1357");
    micr.setBranchSortCode("2468");
    micr.setAccountNumber("1470");

    final String identifier = MICRParser.toIdentifier(micr);
    Assert.assertEquals("1357~2468~1470", identifier);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailIdentifierNull() throws Exception {
    MICRParser.fromIdentifier(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailIdentifierEmpty() throws Exception {
    MICRParser.fromIdentifier("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailIdentifierLessParts() throws Exception {
    MICRParser.fromIdentifier("1357~2468");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailIdentifierManyParts() throws Exception {
    MICRParser.fromIdentifier("1357~2468~1470~1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailMICRNull() throws Exception {
    MICRParser.toIdentifier(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailChequeNumberNull() throws Exception {
    final MICR micr = new MICR();
    micr.setBranchSortCode("2468");
    micr.setAccountNumber("1470");

    MICRParser.toIdentifier(micr);
  }
  @Test(expected = IllegalArgumentException.class)
  public void shouldFailChequeNumberMissing() throws Exception {
    final MICR micr = new MICR();
    micr.setChequeNumber("");
    micr.setBranchSortCode("2468");
    micr.setAccountNumber("1470");

    MICRParser.toIdentifier(micr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailBranchSortCodeNull() throws Exception {
    final MICR micr = new MICR();
    micr.setChequeNumber("1357");
    micr.setAccountNumber("1470");

    MICRParser.toIdentifier(micr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailBranchSortCodeMissing() throws Exception {
    final MICR micr = new MICR();
    micr.setChequeNumber("1357");
    micr.setBranchSortCode("");
    micr.setAccountNumber("1470");

    MICRParser.toIdentifier(micr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailAccountNumberNull() throws Exception {
    final MICR micr = new MICR();
    micr.setChequeNumber("1357");
    micr.setBranchSortCode("2468");

    MICRParser.toIdentifier(micr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailAccountNumberMissing() throws Exception {
    final MICR micr = new MICR();
    micr.setChequeNumber("1357");
    micr.setBranchSortCode("2468");
    micr.setAccountNumber("");

    MICRParser.toIdentifier(micr);
  }
}
