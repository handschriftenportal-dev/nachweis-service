/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import java.util.Map;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 03.11.22
 */
class CriteriaQueryBuilder {

  private CriteriaQueryBuilder() {
  }

  static <T> CriteriaQuery<T> buildListQuery(Class<T> entityClass, EntityManager entityManager,
      Map<String, String> filterBy, Map<String, Boolean> sortByAsc) {

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);

    Root<T> root = criteriaQuery.from(entityClass);
    criteriaQuery = criteriaQuery.select(root);

    Predicate[] predicates = createPredicates(criteriaBuilder, root, filterBy);
    if (predicates.length > 0) {
      criteriaQuery.where(criteriaBuilder.and(predicates));
    }

    Order[] orders = createOrders(criteriaBuilder, root, sortByAsc);
    if (orders.length > 0) {
      criteriaQuery.orderBy(orders);
    }

    return criteriaQuery;
  }

  static <T> CriteriaQuery<Long> buildCountQuery(Class<T> entityClass, EntityManager entityManager,
      Map<String, String> filterBy) {

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);

    Root<T> root = criteriaQuery.from(entityClass);
    criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

    Predicate[] predicates = createPredicates(criteriaBuilder, root, filterBy);
    if (predicates.length > 0) {
      criteriaQuery.where(criteriaBuilder.and(predicates));
    }

    return criteriaQuery;
  }

  static <T> Predicate[] createPredicates(CriteriaBuilder criteriaBuilder, Root<T> root, Map<String, String> filterBy) {
    return filterBy.entrySet().stream()
        .filter(entry -> Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue()))
        .map(entry ->
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get(entry.getKey())),
                "%" + entry.getValue().toLowerCase() + "%"))
        .toArray(Predicate[]::new);
  }

  static <T> Order[] createOrders(CriteriaBuilder criteriaBuilder, Root<T> root, Map<String, Boolean> sortByAsc) {
    return sortByAsc.entrySet().stream()
        .map(entry -> entry.getValue() ?
            criteriaBuilder.asc(root.get(entry.getKey())) : criteriaBuilder.desc(root.get(entry.getKey())))
        .toArray(Order[]::new);
  }

}
