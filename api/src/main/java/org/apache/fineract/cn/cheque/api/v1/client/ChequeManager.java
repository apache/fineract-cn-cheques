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
package org.apache.fineract.cn.cheque.api.v1.client;

import org.apache.fineract.cn.cheque.api.v1.domain.Cheque;
import org.apache.fineract.cn.cheque.api.v1.domain.ChequeProcessingCommand;
import org.apache.fineract.cn.cheque.api.v1.domain.ChequeTransaction;
import org.apache.fineract.cn.cheque.api.v1.domain.IssuingCount;
import org.apache.fineract.cn.cheque.api.v1.domain.MICRResolution;
import java.util.List;
import javax.validation.Valid;
import org.apache.fineract.cn.api.annotation.ThrowsException;
import org.apache.fineract.cn.api.annotation.ThrowsExceptions;
import org.apache.fineract.cn.api.util.CustomFeignClientsConfiguration;
import org.apache.fineract.cn.api.util.NotFoundException;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
