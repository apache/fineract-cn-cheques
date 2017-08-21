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
package io.mifos.cheque.service.internal.command.handler;

import io.mifos.cheque.api.v1.EventConstants;
import io.mifos.cheque.service.ServiceConstants;
import io.mifos.cheque.service.internal.command.MigrationCommand;
import io.mifos.core.command.annotation.Aggregate;
import io.mifos.core.command.annotation.CommandHandler;
import io.mifos.core.command.annotation.EventEmitter;
import io.mifos.core.lang.ApplicationName;
import io.mifos.core.mariadb.domain.FlywayFactoryBean;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Aggregate
public class ManagementAggregate {

  private final Logger logger;
  private final DataSource dataSource;
  private final FlywayFactoryBean flywayFactoryBean;
  private final ApplicationName applicationName;

  @Autowired
  public ManagementAggregate(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                             final DataSource dataSource,
                             final FlywayFactoryBean flywayFactoryBean,
                             final ApplicationName applicationName) {
    super();
    this.logger = logger;
    this.dataSource = dataSource;
    this.flywayFactoryBean = flywayFactoryBean;
    this.applicationName = applicationName;
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.INITIALIZE)
  public String process(final MigrationCommand migrationCommand) {
    this.logger.info("Starting migration for cheques version: {}.", applicationName.getVersionString());
    this.flywayFactoryBean.create(this.dataSource).migrate();

    this.logger.info("Migration finished.");
    return this.applicationName.getVersionString();
  }
}
