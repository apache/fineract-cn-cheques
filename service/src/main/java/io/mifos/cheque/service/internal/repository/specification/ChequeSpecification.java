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
package io.mifos.cheque.service.internal.repository.specification;

import io.mifos.cheque.api.v1.domain.State;
import io.mifos.cheque.service.internal.repository.ChequeEntity;
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
