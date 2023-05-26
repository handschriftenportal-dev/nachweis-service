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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import java.time.LocalDate;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 19.01.23
 */
class SucheTest {

  @Test
  void testBuilder() {
    String query = "HSP";
    Sort sort = new Sort(SortCriteria.valueOf(SortCriteria.SIGNATUR.name()), SortType.ASC);
    Page page = new Page(25, 0);

    Suche suche = Suche.builder()
        .withQuery(query)
        .withPage(new Page(25, 0))
        .withSort(sort)
        .withHighlighting(true)
        .withFilterContainsBuchschmuck(Boolean.TRUE)
        .withFilterContainsDigitalisat(Boolean.TRUE)
        .withFilterAutoren("Autor")
        .withFilterContainsBeschreibung(Boolean.TRUE)
        .withFilterVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withFilterBestandshaltendeInstitutionen("SBB")
        .withFilterPubliziert(Boolean.TRUE)
        .withFilterLastUpdateFrom(LocalDate.of(2022, 1, 1))
        .withFilterLastUpdateTo(LocalDate.of(2022, 12, 31))
        .build();

    assertEquals(query, suche.getQuery());
    assertEquals(page, suche.getPage());
    assertEquals(sort, suche.getSort());

    assertFilter(suche, FilterCriteria.CONTAINS_DIGITALISAT, Boolean.TRUE.toString());
    assertFilter(suche, FilterCriteria.CONTAINS_BUCHSCHMUCK, Boolean.TRUE.toString());
    assertFilter(suche, FilterCriteria.AUTOR, "(\"Autor\")");
    assertFilter(suche, FilterCriteria.CONTAINS_BESCHREIBUNG, Boolean.TRUE.toString());
    assertFilter(suche, FilterCriteria.TYP, null);
    assertFilter(suche, FilterCriteria.PUBLIZIERT, Boolean.TRUE.toString());
    assertFilter(suche, FilterCriteria.BESTANDSHALTENDE_INSTITUTION, "(\"SBB\")");
    assertFilter(suche, FilterCriteria.VERWALTUNGS_TYP, VerwaltungsTyp.EXTERN.toString());
    assertFilter(suche, FilterCriteria.LAST_UPDATE_FROM, "2022-01-01T00:00:00Z");
    assertFilter(suche, FilterCriteria.LAST_UPDATE_TO, "2022-12-31T23:59:59Z");
  }

  @Test
  void testBuilderTypKOD() {
    Suche suche = Suche.builder()
        .withFilterSuchDokumentTyp(SuchDokumentTyp.KOD)
        .withFilterContainsBuchschmuck(Boolean.TRUE)
        .withFilterContainsDigitalisat(Boolean.TRUE)
        .withFilterAutoren("Autor")
        .withFilterContainsBeschreibung(Boolean.TRUE)
        .withFilterVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withFilterBestandshaltendeInstitutionen("SBB", "BSB")
        .withFilterPubliziert(Boolean.TRUE)
        .build();

    assertFilter(suche, FilterCriteria.TYP, SuchDokumentTyp.KOD.toString());
    assertFilter(suche, FilterCriteria.CONTAINS_DIGITALISAT, Boolean.TRUE.toString());
    assertFilter(suche, FilterCriteria.CONTAINS_BUCHSCHMUCK, Boolean.TRUE.toString());
    assertFilter(suche, FilterCriteria.CONTAINS_BESCHREIBUNG, Boolean.TRUE.toString());
    assertFilter(suche, FilterCriteria.AUTOR, null);
    assertFilter(suche, FilterCriteria.PUBLIZIERT, null);
    assertFilter(suche, FilterCriteria.VERWALTUNGS_TYP, null);
    assertFilter(suche, FilterCriteria.BESTANDSHALTENDE_INSTITUTION, "(\"SBB\" AND \"BSB\")");
  }

  @Test
  void testBuilderTypBS() {
    Suche suche = Suche.builder()
        .withFilterSuchDokumentTyp(SuchDokumentTyp.BS)
        .withFilterContainsBuchschmuck(Boolean.TRUE)
        .withFilterContainsDigitalisat(Boolean.TRUE)
        .withFilterAutoren("Autor_1", "Autor_2")
        .withFilterContainsBeschreibung(Boolean.TRUE)
        .withFilterVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withFilterBestandshaltendeInstitutionen("SBB", "BSB")
        .withFilterPubliziert(Boolean.TRUE)
        .build();

    assertFilter(suche, FilterCriteria.TYP, SuchDokumentTyp.BS.toString());
    assertFilter(suche, FilterCriteria.CONTAINS_DIGITALISAT, null);
    assertFilter(suche, FilterCriteria.AUTOR, "(\"Autor_1\" AND \"Autor_2\")");
    assertFilter(suche, FilterCriteria.CONTAINS_BUCHSCHMUCK, null);
    assertFilter(suche, FilterCriteria.CONTAINS_BESCHREIBUNG, null);
    assertFilter(suche, FilterCriteria.PUBLIZIERT, Boolean.TRUE.toString());
    assertFilter(suche, FilterCriteria.VERWALTUNGS_TYP, VerwaltungsTyp.EXTERN.toString());
    assertFilter(suche, FilterCriteria.BESTANDSHALTENDE_INSTITUTION, null);
  }

  @Test
  void testBuilderNoFilters() {
    Suche suche = Suche.builder()
        .withFilterContainsBuchschmuck(false)
        .withFilterContainsDigitalisat(false)
        .withFilterAutoren((String[]) null)
        .withFilterContainsBeschreibung(false)
        .withFilterVerwaltungsTyp(null)
        .withFilterBestandshaltendeInstitutionen((String[]) null)
        .withFilterPubliziert(false)
        .withFilterLastUpdateFrom(null)
        .withFilterLastUpdateTo(null)
        .build();

    assertFilter(suche, FilterCriteria.CONTAINS_DIGITALISAT, null);
    assertFilter(suche, FilterCriteria.CONTAINS_BUCHSCHMUCK, null);
    assertFilter(suche, FilterCriteria.AUTOR, null);
    assertFilter(suche, FilterCriteria.CONTAINS_BESCHREIBUNG, null);
    assertFilter(suche, FilterCriteria.TYP, null);
    assertFilter(suche, FilterCriteria.PUBLIZIERT, null);
    assertFilter(suche, FilterCriteria.BESTANDSHALTENDE_INSTITUTION, null);
    assertFilter(suche, FilterCriteria.VERWALTUNGS_TYP, null);
    assertFilter(suche, FilterCriteria.LAST_UPDATE_FROM, null);
    assertFilter(suche, FilterCriteria.LAST_UPDATE_TO, null);
  }

  private void assertFilter(Suche suche, FilterCriteria criteria, String value) {
    if (Objects.isNull(value)) {
      assertFalse(suche.getFilter().containsKey(criteria), "Unexpected criteria in filter: " + criteria);
    } else {
      assertEquals(value, suche.getFilter().get(criteria), "Unexpected value for criteria: " + criteria);
    }
  }

}
