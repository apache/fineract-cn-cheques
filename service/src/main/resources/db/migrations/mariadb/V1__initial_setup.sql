--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--   http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.
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
