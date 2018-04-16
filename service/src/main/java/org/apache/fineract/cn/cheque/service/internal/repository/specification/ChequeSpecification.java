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
package org.apache.fineract.cn.cheque.service.internal.repository.specification;

import org.apache.fineract.cn.cheque.api.v1.domain.State;
import org.apache.fineract.cn.cheque.service.internal.repository.ChequeEntity;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;

public class ChequeSpecification {

  public static Specification<ChequeEntity> createListSpecification(
      final String state, final String account) {

    return (root, query, cb) -> {

      final ArrayList<Predicate> predicates = new ArrayList<>();

      if (!state.equalsIgnoreCase("ALL")) {
        predicates.add(cb.equal(root.get("state"), State.valueOf(state.toUpperCase()).name()));
      }

      if (account != null && ! account.isEmpty()) {
        predicates.add(cb.equal(root.get("accountNumber"), account));
      }

      return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    };
  }
}
