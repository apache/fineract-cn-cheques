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
package io.mifos.cheque.service;

import io.mifos.accounting.api.v1.client.LedgerManager;
import io.mifos.anubis.config.EnableAnubis;
import io.mifos.core.async.config.EnableAsync;
import io.mifos.core.cassandra.config.EnableCassandra;
import io.mifos.core.command.config.EnableCommandProcessing;
import io.mifos.core.lang.config.EnableServiceException;
import io.mifos.core.lang.config.EnableTenantContext;
import io.mifos.core.mariadb.config.EnableMariaDB;
import io.mifos.customer.api.v1.client.CustomerManager;
import io.mifos.deposit.api.v1.client.DepositAccountManager;
import io.mifos.office.api.v1.client.OrganizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SuppressWarnings("WeakerAccess")
@Configuration
@EnableAutoConfiguration
@EnableDiscoveryClient
@EnableAsync
@EnableTenantContext
@EnableCassandra
@EnableMariaDB
@EnableCommandProcessing
@EnableAnubis
@EnableServiceException
@EnableFeignClients(
    clients = {
        LedgerManager.class,
        DepositAccountManager.class,
        OrganizationManager.class,
        CustomerManager.class
    }
)
@ComponentScan({
    "io.mifos.cheque.service.rest",
    "io.mifos.cheque.service.internal.service",
    "io.mifos.cheque.service.internal.repository",
    "io.mifos.cheque.service.internal.command.handler"
})
@EnableJpaRepositories({
    "io.mifos.cheque.service.internal.repository"
})
public class ChequeConfiguration extends WebMvcConfigurerAdapter {

  public ChequeConfiguration() {
    super();
  }

  @Bean(name = ServiceConstants.LOGGER_NAME)
  public Logger logger() {
    return LoggerFactory.getLogger(ServiceConstants.LOGGER_NAME);
  }

  @Override
  public void configurePathMatch(final PathMatchConfigurer configurer) {
    configurer.setUseSuffixPatternMatch(Boolean.FALSE);
  }
}
