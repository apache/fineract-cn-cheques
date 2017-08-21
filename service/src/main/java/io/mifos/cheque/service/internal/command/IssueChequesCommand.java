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
package io.mifos.cheque.service.internal.command;

public class IssueChequesCommand {

  private final String accountIdentifer;
  private final Integer startSequence;
  private final Integer issueCount;

  public IssueChequesCommand(final String accountIdentifer, final Integer startSequence, final Integer issueCount) {
    super();
    this.accountIdentifer = accountIdentifer;
    this.startSequence = startSequence;
    this.issueCount = issueCount;
  }

  public String accountIdentifer() {
    return this.accountIdentifer;
  }

  public Integer startSequence() {
    return this.startSequence;
  }

  public Integer issueCount() {
    return this.issueCount;
  }
}
