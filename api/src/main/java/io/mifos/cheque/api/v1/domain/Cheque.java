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

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;

public class Cheque {

  @Valid
  private MICR micr;
  @NotEmpty
  private String drawee;
  @NotEmpty
  private String drawer;
  @NotEmpty
  private String payee;
  @NotEmpty
  private String amount;
  @NotEmpty
  private String dateIssued;
  private Boolean openCheque;
  private State state;
  private String journalEntryIdentifier;

  public Cheque() {
    super();
  }

  public MICR getMicr() {
    return this.micr;
  }

  public void setMicr(final MICR micr) {
    this.micr = micr;
  }

  public String getDrawee() {
    return this.drawee;
  }

  public void setDrawee(final String drawee) {
    this.drawee = drawee;
  }

  public String getDrawer() {
    return this.drawer;
  }

  public void setDrawer(final String drawer) {
    this.drawer = drawer;
  }

  public String getPayee() {
    return this.payee;
  }

  public void setPayee(final String payee) {
    this.payee = payee;
  }

  public String getAmount() {
    return this.amount;
  }

  public void setAmount(final String amount) {
    this.amount = amount;
  }

  public String getDateIssued() {
    return this.dateIssued;
  }

  public void setDateIssued(final String dateIssued) {
    this.dateIssued = dateIssued;
  }

  public Boolean isOpenCheque() {
    return this.openCheque;
  }

  public void setOpenCheque(final Boolean openCheque) {
    this.openCheque = openCheque;
  }

  public String getState() {
    return this.state != null ? this.state.name() : null;
  }

  public void setState(final String state) {
    this.state = State.valueOf(state);
  }

  public String getJournalEntryIdentifier() {
    return this.journalEntryIdentifier;
  }

  public void setJournalEntryIdentifier(final String journalEntryIdentifier) {
    this.journalEntryIdentifier = journalEntryIdentifier;
  }
}
