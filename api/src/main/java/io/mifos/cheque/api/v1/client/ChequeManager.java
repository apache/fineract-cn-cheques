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
package io.mifos.cheque.api.v1.client;

import io.mifos.cheque.api.v1.domain.Cheque;
import io.mifos.cheque.api.v1.domain.ChequeProcessingCommand;
import io.mifos.cheque.api.v1.domain.ChequeTransaction;
import io.mifos.cheque.api.v1.domain.IssuingCount;
import io.mifos.cheque.api.v1.domain.MICRResolution;
import io.mifos.core.api.annotation.ThrowsException;
import io.mifos.core.api.annotation.ThrowsExceptions;
import io.mifos.core.api.util.CustomFeignClientsConfiguration;
import io.mifos.core.api.util.NotFoundException;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

@SuppressWarnings("unused")
@FeignClient(value = "cheques-v1", path = "/cheques/v1", configuration = CustomFeignClientsConfiguration.class)
public interface ChequeManager {

  @RequestMapping(
      value = "/cheques/",
      method = RequestMethod.POST,
      produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE}
  )
  String issue(@RequestBody @Valid final IssuingCount issuingCount);

  @RequestMapping(
      value = "/cheques/",
      method = RequestMethod.GET,
      produces = {MediaType.ALL_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE}
  )
  List<Cheque> fetch(@RequestParam(value = "state", required = false, defaultValue = "ALL") final String state,
                     @RequestParam(value = "account", required = false) final String accountIdentifier);

  /**
   * The identifier is created by concatenating the MICR parts with the character <code>~</code>.
   * Sample:<br/>
   * &nbsp;&nbsp;{cheque-no}~{branch-sort-code}~{account-no}<br/>
   * &nbsp;&nbsp;246~13579~20030011<br/>
   *
   * @param identifier concatenated MICR
   * @return
   */
  @RequestMapping(
      value = "/cheques/{identifier}",
      method = RequestMethod.GET,
      produces = {MediaType.ALL_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE}
  )
  Cheque get(@PathVariable("identifier") final String identifier);

  @RequestMapping(
      value = "/cheques/{identifier}/commands",
      method = RequestMethod.POST,
      produces = {MediaType.ALL_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE}
  )
  void process(@PathVariable("identifier") final String identifier,
               @RequestBody @Valid final ChequeProcessingCommand chequeProcessingCommand);

  @RequestMapping(
      value = "/transactions/",
      method = RequestMethod.POST,
      produces = {MediaType.ALL_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE}
  )
  void process(@RequestBody @Valid final ChequeTransaction chequeTransaction);

  @RequestMapping(
      value = "/micr/{identifier}",
      method = RequestMethod.GET,
      produces = {MediaType.ALL_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE}
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.NOT_FOUND, exception = NotFoundException.class),
      @ThrowsException(status = HttpStatus.CONFLICT, exception = InvalidChequeNumberException.class),
      @ThrowsException(status = HttpStatus.BAD_REQUEST, exception = DependingResourceNotValidException.class)
  })
  MICRResolution expandMicr(@PathVariable("identifier") final String identifier);
}
