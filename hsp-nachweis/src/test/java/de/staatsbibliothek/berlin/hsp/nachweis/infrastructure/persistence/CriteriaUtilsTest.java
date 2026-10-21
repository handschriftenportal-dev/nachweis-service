package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CriteriaUtilsTest {

  private CriteriaBuilder cb;
  private Path<LocalDateTime> path;

  @BeforeEach
  void setUp() {
    cb = mock(CriteriaBuilder.class);
    path = mock(Path.class);
  }

  @Test
  void greaterThanOrEqualTo() {
    Instant instant = Instant.now();
    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    Predicate expectedPredicate = mock(Predicate.class);

    when(cb.greaterThanOrEqualTo(path, dateTime)).thenReturn(expectedPredicate);

    Predicate result = CriteriaUtils.greaterThanOrEqualTo(cb, path, instant);

    assertEquals(expectedPredicate, result);
    verify(cb).greaterThanOrEqualTo(path, dateTime);
  }

  @Test
  void lessThanOrEqualTo() {
    Instant instant = Instant.now();
    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    Predicate expectedPredicate = mock(Predicate.class);

    when(cb.lessThanOrEqualTo(path, dateTime)).thenReturn(expectedPredicate);

    Predicate result = CriteriaUtils.lessThanOrEqualTo(cb, path, instant);

    assertEquals(expectedPredicate, result);
    verify(cb).lessThanOrEqualTo(path, dateTime);
  }
}
