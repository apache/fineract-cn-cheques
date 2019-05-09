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
package org.apache.fineract.cn.cheque.service.internal.repository;

import java.sql.Date;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.apache.fineract.cn.postgresql.util.LocalDateTimeConverter;

@Entity
@Table(name = "sopdet_cheques")
public class ChequeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;
  @Column(name = "cheque_number", nullable = false, length = 8)
  private String chequeNumber;
  @Column(name = "branch_sort_code", nullable = false, length = 11)
  private String branchSortCode;
  @Column(name = "account_number", nullable = false, length = 34)
  private String accountNumber;
  @Column(name = "drawee", nullable = false, length = 2048)
  private String drawee;
  @Column(name = "drawer", nullable = false, length = 256)
  private String drawer;
  @Column(name = "payee", nullable = false, length = 256)
  private String payee;
  @Column(name = "amount", nullable = false)
  private Double amount;
  @Column(name = "date_issued", nullable = false)
  private Date dateIssued;
  @Column(name = "open_cheque", nullable = true)
  private Boolean openCheque;
  @Column(name = "state", nullable = false)
  private String state;
  @Column(name = "journal_entry_identifier", nullable = false, length = 2200)
  private String journalEntryIdentifier;
  @Column(name = "created_by", nullable = false)
  private String createdBy;
  @Column(name = "created_on", nullable = false)
  @Convert(converter = LocalDateTimeConverter.class)
  private LocalDateTime createdOn;
  @Column(name = "last_modified_by", nullable = true)
  private String lastModifiedBy;
  @Column(name = "last_modified_on")
  @Convert(converter = LocalDateTimeConverter.class)
  private LocalDateTime lastModifiedOn;

  public ChequeEntity() {
    super();
  }

  public Long getId() {
    return this.id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getChequeNumber() {
    return this.chequeNumber;
  }

  public void setChequeNumber(final String chequeNumber) {
    this.chequeNumber = chequeNumber;
  }

  public String getBranchSortCode() {
    return this.branchSortCode;
  }

  public void setBranchSortCode(final String branchSortCode) {
    this.branchSortCode = branchSortCode;
  }

  public String getAccountNumber() {
    return this.accountNumber;
  }

  public void setAccountNumber(final String accountNumber) {
    this.accountNumber = accountNumber;
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

  public Double getAmount() {
    return this.amount;
  }

  public void setAmount(final Double amount) {
    this.amount = amount;
  }

  public Date getDateIssued() {
    return this.dateIssued;
  }

  public void setDateIssued(final Date dateIssued) {
    this.dateIssued = dateIssued;
  }

  public Boolean getOpenCheque() {
    return this.openCheque;
  }

  public void setOpenCheque(final Boolean openCheque) {
    this.openCheque = openCheque;
  }

  public String getState() {
    return this.state;
  }

  public void setState(final String state) {
    this.state = state;
  }

  public String getJournalEntryIdentifier() {
    return this.journalEntryIdentifier;
  }

  public void setJournalEntryIdentifier(final String journalEntryIdentifier) {
    this.journalEntryIdentifier = journalEntryIdentifier;
  }

  public String getCreatedBy() {
    return this.createdBy;
  }

  public void setCreatedBy(final String createdBy) {
    this.createdBy = createdBy;
  }

  public LocalDateTime getCreatedOn() {
    return this.createdOn;
  }

  public void setCreatedOn(final LocalDateTime createdOn) {
    this.createdOn = createdOn;
  }

  public String getLastModifiedBy() {
    return this.lastModifiedBy;
  }

  public void setLastModifiedBy(final String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public LocalDateTime getLastModifiedOn() {
    return this.lastModifiedOn;
  }

  public void setLastModifiedOn(final LocalDateTime lastModifiedOn) {
    this.lastModifiedOn = lastModifiedOn;
  }
}
