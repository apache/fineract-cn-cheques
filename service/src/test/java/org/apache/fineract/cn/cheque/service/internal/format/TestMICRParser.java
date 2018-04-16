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
package org.apache.fineract.cn.cheque.service.internal.format;

import org.apache.fineract.cn.cheque.api.v1.domain.MICR;
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
