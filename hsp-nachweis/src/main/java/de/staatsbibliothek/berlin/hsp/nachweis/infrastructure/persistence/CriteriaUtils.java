package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class CriteriaUtils {

  private static final ZoneId ZONE_ID = ZoneId.systemDefault();

  private CriteriaUtils() {
  }

  public static Predicate greaterThanOrEqualTo(
      CriteriaBuilder cb, Path<LocalDateTime> path, Instant instant) {
    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZONE_ID);
    return cb.greaterThanOrEqualTo(path, dateTime);
  }

  public static Predicate lessThanOrEqualTo(
      CriteriaBuilder cb, Path<LocalDateTime> path, Instant instant) {
    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZONE_ID);
    return cb.lessThanOrEqualTo(path, dateTime);
  }
}
