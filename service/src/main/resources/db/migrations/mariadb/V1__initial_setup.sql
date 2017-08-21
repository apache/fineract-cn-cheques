--
-- Copyright 2017 Kuelap, Inc.
--
-- All Rights Reserved.
--
-- NOTICE:  All information contained herein is, and remains
-- the property of Kuelap, Inc and its suppliers, if any.
-- The intellectual and technical concepts contained herein
-- are proprietary to Kuelap, Inc and its suppliers and may
-- be covered by U.S. and Foreign Patents, patents in process,
-- and are protected by trade secret or copyright law.
-- Dissemination of this information or reproduction of this material
-- is strictly forbidden unless prior written permission is obtained
-- Kuelap, Inc.
--

CREATE TABLE sopdet_issued_cheques (
  id                         BIGINT         NOT NULL AUTO_INCREMENT,
  account_identifier         VARCHAR(34)    NOT NULL,
  last_issued_number         NUMERIC(8)     NOT NULL,
  created_on                 TIMESTAMP(3)   NOT NULL,
  created_by                 VARCHAR(32)    NOT NULL,
  last_modified_on           TIMESTAMP(3)   NULL,
  last_modified_by           VARCHAR(32)    NULL,
  CONSTRAINT sopdet_issued_cheques_pk PRIMARY KEY (id),
  CONSTRAINT sopdet_issued_cheques_uq UNIQUE (account_identifier)
);

CREATE TABLE sopdet_cheques (
  id                         BIGINT         NOT NULL AUTO_INCREMENT,
  cheque_number              VARCHAR(8)     NOT NULL,
  branch_sort_code           VARCHAR(11)    NOT NULL,
  account_number             VARCHAR(34)    NOT NULL,
  drawee                     VARCHAR(2048)  NOT NULL,
  drawer                     VARCHAR(256)   NOT NULL,
  payee                      VARCHAR(256)   NOT NULL,
  amount                     NUMERIC(15,5)  NOT NULL,
  date_issued                DATE           NOT NULL,
  open_cheque                BOOLEAN        NULL,
  state                      VARCHAR(32)    NOT NULL,
  journal_entry_identifier   VARCHAR(2200)  NOT NULL,
  created_on                 TIMESTAMP(3)   NOT NULL,
  created_by                 VARCHAR(32)    NOT NULL,
  last_modified_on           TIMESTAMP(3)   NULL,
  last_modified_by           VARCHAR(32)    NULL,
  CONSTRAINT sopdet_cheques_pk PRIMARY KEY (id),
  CONSTRAINT sopdet_cheques_uq UNIQUE (cheque_number, branch_sort_code, account_number)
);
