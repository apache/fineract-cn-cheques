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
import io.mifos.cheque.api.v1.domain.Action;
import io.mifos.cheque.api.v1.domain.Cheque;
import io.mifos.cheque.api.v1.domain.ChequeProcessingCommand;
import io.mifos.cheque.api.v1.domain.IssuingCount;
import io.mifos.cheque.service.ServiceConstants;
import io.mifos.cheque.service.internal.command.ApproveChequeTransactionCommand;
import io.mifos.cheque.service.internal.command.CancelChequeTransactionCommand;
import io.mifos.cheque.service.internal.command.IssueChequesCommand;
import io.mifos.cheque.service.internal.format.MICRParser;
import io.mifos.cheque.service.internal.service.ChequeService;
import io.mifos.core.command.domain.CommandCallback;
import io.mifos.core.command.gateway.CommandGateway;
import io.mifos.core.lang.ServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/cheques")
public class ChequeRestController {

  private final Logger logger;
  private final CommandGateway commandGateway;
  private final ChequeService chequeService;

  @Autowired
  public ChequeRestController(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                              final CommandGateway commandGateway,
                              final ChequeService chequeService) {
    super();
    this.logger = logger;
    this.commandGateway = commandGateway;
    this.chequeService = chequeService;
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CHEQUE_MANAGEMENT)
  @RequestMapping(
      value = "/",
      method = RequestMethod.POST,
      produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE}
  )
  @ResponseBody
  ResponseEntity<String> issue(@RequestBody @Valid final IssuingCount issuingCount) {

    try {
      final CommandCallback<String> callback = this.commandGateway.process(
          new IssueChequesCommand(
              issuingCount.getAccountIdentifier(), issuingCount.getStart(), issuingCount.getAmount()),
          String.class
      );

      return ResponseEntity.ok(callback.get());
    } catch (final ServiceException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw ServiceException.internalError(ex.getMessage());
    }
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CHEQUE_MANAGEMENT)
  @RequestMapping(
      value = "/",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.ALL_VALUE}
  )
  @ResponseBody
  ResponseEntity<List<Cheque>> fetch(
      @RequestParam(value = "state", required = false, defaultValue = "ALL") final String state,
      @RequestParam(value = "account", required = false) final String accountIdentifier) {
    return ResponseEntity.ok(this.chequeService.fetchCheques(state, accountIdentifier));
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CHEQUE_MANAGEMENT)
  @RequestMapping(
      value = "/{identifier}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.ALL_VALUE}
  )
  @ResponseBody
  ResponseEntity<Cheque> get(@PathVariable("identifier") final String identifier) {
    return ResponseEntity.ok(
        this.chequeService.findBy(MICRParser.fromIdentifier(identifier))
            .orElseThrow(() -> ServiceException.notFound("Cheque {0} not found.", identifier))
    );
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CHEQUE_MANAGEMENT)
  @RequestMapping(
      value = "/{identifier}/commands",
      method = RequestMethod.POST,
      produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE}
  )
  @ResponseBody
  ResponseEntity<Void> process(@PathVariable("identifier") final String identifier,
                               @RequestBody @Valid final ChequeProcessingCommand chequeProcessingCommand) {

    if (!this.chequeService.findBy(MICRParser.fromIdentifier(identifier)).isPresent()) {
      throw ServiceException.notFound("Cheque {0} not found.", identifier);
    }

    switch (Action.valueOf(chequeProcessingCommand.getAction())) {
      case APPROVE:
        this.commandGateway.process(new ApproveChequeTransactionCommand(identifier));
        break;
      case CANCEL:
        this.commandGateway.process(new CancelChequeTransactionCommand(identifier));
        break;
      default:
        throw ServiceException.badRequest("Unknown cheque command {0}.", chequeProcessingCommand.getAction());
    }

    return ResponseEntity.accepted().build();
  }
}
