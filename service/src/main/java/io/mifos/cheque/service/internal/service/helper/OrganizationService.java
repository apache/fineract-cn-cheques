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
package io.mifos.cheque.service.internal.service.helper;

import io.mifos.cheque.service.ServiceConstants;
import io.mifos.office.api.v1.client.NotFoundException;
import io.mifos.office.api.v1.client.OrganizationManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService {

  private final Logger logger;
  private final OrganizationManager organizationManager;

  @Autowired
  public OrganizationService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                             final OrganizationManager organizationManager) {
    super();
    this.logger = logger;
    this.organizationManager = organizationManager;
  }

  public boolean officeExistsByBranchSortCode(final String branchSortCode) {
    try {
      this.organizationManager.findOfficeByIdentifier(branchSortCode);
      return true;
    } catch (final NotFoundException nfex) {
      return false;
    }
  }
}
