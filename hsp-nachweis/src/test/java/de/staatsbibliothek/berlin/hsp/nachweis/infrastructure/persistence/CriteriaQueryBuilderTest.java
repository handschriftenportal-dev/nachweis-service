package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.query.sqm.tree.select.SqmSelectQuery;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 03.11.22
 */
@QuarkusTest
class CriteriaQueryBuilderTest {

  @Inject
  EntityManager entityManager;

  @Test
  void testBuildListQuery() {
    Map<String, String> filterBy = new HashMap<>();
    Map<String, Boolean> sortBy = new HashMap<>();
    filterBy.put("bearbeitername", "Unbekannt");

    CriteriaQuery<Bearbeiter> criteriaQuery =
        CriteriaQueryBuilder.buildListQuery(Bearbeiter.class, entityManager, filterBy, sortBy);

    assertNotNull(criteriaQuery);
    TypedQuery<Bearbeiter> typedQuery = entityManager.createQuery(criteriaQuery);
    assertNotNull(typedQuery);

    var sqmSelectQuery = typedQuery.unwrap(SqmSelectQuery.class);

    String sql = sqmSelectQuery.getQueryPart().toHqlString()
        .replaceAll("var_[0-9]", "generatedAlias0");

    assertEquals(
        "select generatedAlias0 from de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter generatedAlias0 where lower(generatedAlias0.bearbeitername) like '%unbekannt%'",
        sql);
  }

  @Test
  void testBuildCountQuery() {
    Map<String, String> filterBy = new HashMap<>();
    filterBy.put("bearbeitername", "Unbekannt");

    CriteriaQuery<Long> criteriaQuery =
        CriteriaQueryBuilder.buildCountQuery(Bearbeiter.class, entityManager, filterBy);

    assertNotNull(criteriaQuery);

    TypedQuery<Long> typedQuery = entityManager.createQuery(criteriaQuery);
    assertNotNull(typedQuery);

    var sqmSelectQuery = typedQuery.unwrap(SqmSelectQuery.class);

    String sql = sqmSelectQuery.getQueryPart().toHqlString()
        .replaceAll("var_[0-9]", "generatedAlias0");

    assertEquals(
        "select count() from de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter generatedAlias0 where lower(generatedAlias0.bearbeitername) like '%unbekannt%'",
        sql);
  }

}
