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
package io.mifos.cheque.service.rest;

import io.mifos.anubis.annotation.AcceptedTokenType;
import io.mifos.anubis.annotation.Permittable;
import io.mifos.cheque.api.v1.PermittableGroupIds;
import io.mifos.cheque.api.v1.domain.Cheque;
import io.mifos.cheque.api.v1.domain.ChequeTransaction;
import io.mifos.cheque.service.ServiceConstants;
import io.mifos.cheque.service.internal.command.ChequeTransactionCommand;
import io.mifos.cheque.service.internal.format.MICRParser;
import io.mifos.cheque.service.internal.service.ChequeService;
import io.mifos.core.command.gateway.CommandGateway;
import io.mifos.core.lang.ServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/transactions")
public class ChequeTransactionRestController {

  private final Logger logger;
  private final CommandGateway commandGateway;
  private final ChequeService chequeService;

  @Autowired
  public ChequeTransactionRestController(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                         final CommandGateway commandGateway,
                                         final ChequeService chequeService) {
    super();
    this.logger = logger;
    this.commandGateway = commandGateway;
    this.chequeService = chequeService;
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CHEQUE_TRANSACTION)
  @RequestMapping(
      value = "/",
      method = RequestMethod.POST,
      produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE}
  )
  @ResponseBody
  ResponseEntity<Void> process(@RequestBody @Valid final ChequeTransaction chequeTransaction) {

    final Cheque cheque = chequeTransaction.getCheque();

    if (this.chequeService.chequeExists(chequeTransaction.getCheque())) {
      throw ServiceException.conflict("Cheque {0} already used.",
          MICRParser.toIdentifier(chequeTransaction.getCheque().getMicr()));
    }

    this.commandGateway.process(new ChequeTransactionCommand(cheque, chequeTransaction.getChequesReceivableAccount(),  chequeTransaction.getCreditorAccountNumber()));

    return ResponseEntity.accepted().build();
  }
}
