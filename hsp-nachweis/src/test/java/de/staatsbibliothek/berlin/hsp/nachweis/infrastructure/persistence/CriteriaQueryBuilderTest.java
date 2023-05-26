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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import io.quarkus.test.junit.QuarkusTest;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
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
    Map<String, Boolean> sortByAsc = new HashMap<>();
    sortByAsc.put("id", Boolean.TRUE);

    Map<String, String> filterBy = new HashMap<>();
    filterBy.put("bearbeitername", "Unbekannt");

    CriteriaQuery<Bearbeiter> criteriaQuery =
        CriteriaQueryBuilder.buildListQuery(Bearbeiter.class, entityManager, filterBy, sortByAsc);

    assertNotNull(criteriaQuery);

    TypedQuery<Bearbeiter> typedQuery = entityManager.createQuery(criteriaQuery);
    assertNotNull(typedQuery);

    assertEquals("select generatedAlias0 from Bearbeiter as generatedAlias0 where "
            + "lower(generatedAlias0.bearbeitername) like :param0 "
            + "order by generatedAlias0.id asc",
        typedQuery.unwrap(org.hibernate.query.Query.class).getQueryString());
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

    assertEquals("select count(generatedAlias0) from Bearbeiter as generatedAlias0 "
            + "where lower(generatedAlias0.bearbeitername) like :param0",
        typedQuery.unwrap(org.hibernate.query.Query.class).getQueryString());
  }

}
