package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 03.11.22
 */
class CriteriaQueryBuilder {

  //TODO: extract move rersponsibility to caller
  private static final Set<String> EXACT_MATCH_FIELDS = Set.of("publikationStatus");

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

  static <T> Predicate[] createPredicates(CriteriaBuilder criteriaBuilder, Root<T> root,
      Map<String, String> filterBy) {
    return filterBy.entrySet().stream()
        .filter(entry -> Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue()))
        .map(entry -> toPredicate(criteriaBuilder, root, entry.getKey(), entry.getValue()))
        .toArray(Predicate[]::new);
  }

  private static <T> Predicate toPredicate(CriteriaBuilder criteriaBuilder, Root<T> root,
      String field, String value) {
    Path<Object> path = root.get(field);
    if (EXACT_MATCH_FIELDS.contains(field)) {
      Class<?> javaType = path.getJavaType();
      List<?> values = Arrays.stream(value.split(","))
          .map(String::trim)
          .filter(v -> !v.isEmpty())
          .map(v -> coerce(javaType, v))
          .toList();
      return values.size() == 1 ? criteriaBuilder.equal(path, values.getFirst()) : path.in(values);
    }
    Path<String> stringPath = root.get(field);
    return criteriaBuilder.like(
        criteriaBuilder.lower(stringPath),
        "%" + value.toLowerCase() + "%");
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static Object coerce(Class<?> targetType, String value) {
    if (targetType.isEnum()) {
      return Enum.valueOf((Class<Enum>) targetType, value);
    }
    return value;
  }

  static <T> Order[] createOrders(CriteriaBuilder criteriaBuilder, Root<T> root,
      Map<String, Boolean> sortByAsc) {
    return sortByAsc.entrySet().stream()
        .map(entry -> entry.getValue() ?
            criteriaBuilder.asc(root.get(entry.getKey()))
            : criteriaBuilder.desc(root.get(entry.getKey())))
        .toArray(Order[]::new);
  }

}
