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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrAPI.QUERY_WILDCARD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Ergebnis;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.FilterCriteria;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Page;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Sort;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SortCriteria;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SortType;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Suche;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 18.08.2020.
 * @version 1.0
 */
class SucheModelTest {

  @Test
  void testBuildSuche() {

    SuchDokumentBoundary suchDokumentBoundary = Mockito.mock(SuchDokumentBoundary.class);

    SucheModel model = new SucheModel(suchDokumentBoundary);
    model.init();

    assertNotNull(model.getLazyErgebnisModel());

    model.buildNewSuche();

    assertEquals(QUERY_WILDCARD, model.getSuche().getQuery());

    assertEquals(new Page(25, 0), model.getSuche().getPage());

    assertEquals(new Sort(SortCriteria.valueOf(model.getSortCriteria()), SortType.ASC), model.getSuche().getSort());

    assertTrue(model.getSuche().getFilter().isEmpty());
  }

  @Test
  void testBuildWithDigitalisierteHandschriftTypeKOD() {

    SuchDokumentBoundary suchDokumentBoundary = Mockito.mock(SuchDokumentBoundary.class);

    SucheModel model = new SucheModel(suchDokumentBoundary);
    model.init();
    model.setSelectedDokumentTyp(SuchDokumentTyp.KOD);
    model.setContainsDigitalisat(true);
    model.buildNewSuche();

    assertEquals(2, model.getSuche().getFilter().size());
  }

  @Test
  void testBuildWithDigitalisierteHandschriftTypeBS() {

    SuchDokumentBoundary suchDokumentBoundary = Mockito.mock(SuchDokumentBoundary.class);

    SucheModel model = new SucheModel(suchDokumentBoundary);
    model.init();
    model.setSelectedDokumentTyp(SuchDokumentTyp.BS);
    model.setContainsDigitalisat(true);
    model.buildNewSuche();

    assertEquals(1, model.getSuche().getFilter().size());
  }

  @Test
  void testBuildWithVerwaltungsTypeExtern() {

    SuchDokumentBoundary suchDokumentBoundary = Mockito.mock(SuchDokumentBoundary.class);

    SucheModel model = new SucheModel(suchDokumentBoundary);
    model.init();
    model.setSelectedVerwaltungsTyp(VerwaltungsTyp.EXTERN);
    model.buildNewSuche();

    assertEquals("extern", model.getSuche().getFilter().get(FilterCriteria.VERWALTUNGS_TYP));
  }

  @Test
  void testBuildWithFromUpdate() {
    SuchDokumentBoundary suchDokumentBoundary = Mockito.mock(SuchDokumentBoundary.class);

    SucheModel model = new SucheModel(suchDokumentBoundary);
    model.init();
    model.setFromUpdate(LocalDate.of(2022, 1, 2));
    model.buildNewSuche();

    assertEquals("2022-01-02T00:00:00Z", model.getSuche().getFilter().get(FilterCriteria.LAST_UPDATE_FROM));
  }

  @Test
  void testBuildWithToUpdate() {
    SuchDokumentBoundary suchDokumentBoundary = Mockito.mock(SuchDokumentBoundary.class);

    SucheModel model = new SucheModel(suchDokumentBoundary);
    model.init();
    model.setToUpdate(LocalDate.of(2022, 1, 2));
    model.buildNewSuche();

    assertEquals("2022-01-02T23:59:59Z", model.getSuche().getFilter().get(FilterCriteria.LAST_UPDATE_TO));
  }

  @Test
  void testSearch() throws SolrServiceException {
    SuchDokumentBoundary suchDokumentBoundary = Mockito.mock(SuchDokumentBoundary.class);
    when(suchDokumentBoundary.search(any(Suche.class))).thenReturn(new Ergebnis());

    SucheModel model = new SucheModel(suchDokumentBoundary);
    model.init();

    model.setQuery("test");

    Ergebnis ergebnis = model.search();

    assertNotNull(ergebnis);
    assertNotNull(model.getLazyErgebnisModel());
    assertNotNull(model.getLazyErgebnisModel().getErgebnis());
    assertEquals(ergebnis, model.getLazyErgebnisModel().getErgebnis());
    assertNotNull(model.getSuche());
    assertEquals("test", model.getSuche().getQuery());
  }

  @Test
  void testResetFilterValues() {

    SuchDokumentBoundary suchDokumentBoundary = Mockito.mock(SuchDokumentBoundary.class);

    SucheModel model = new SucheModel(suchDokumentBoundary);
    model.init();

    model.setContainsDigitalisat(true);
    model.setContainsBuchschmuck(true);
    model.setSelectedVerwaltungsTyp(VerwaltungsTyp.EXTERN);
    model.setSelectedDokumentTyp(SuchDokumentTyp.KOD);
    model.setSelectedInstitutionen(new String[]{"BSB"});
    model.setSelectedAutoren(new String[]{"Gans, Gustav"});
    model.setFromUpdate(LocalDate.of(2021, 5, 7));
    model.setToUpdate(LocalDate.of(2021, 6, 8));

    model.resetFilterValues();
    assertFalse(model.isContainsDigitalisat());
    assertFalse(model.isContainsBuchschmuck());
    assertFalse(model.isContainsBeschreibung());
    assertNull(model.getSelectedVerwaltungsTyp());
    assertNull(model.getSelectedDokumentTyp());
    assertNull(model.getSelectedInstitutionen());
    assertNull(model.getSelectedAutoren());
    assertNull(model.getFromUpdate());
    assertNull(model.getToUpdate());
  }

  @Test
  void buildNewSuche() {
    SuchDokumentBoundary suchDokumentBoundary = Mockito.mock(SuchDokumentBoundary.class);
    SucheModel testling = new SucheModel(suchDokumentBoundary);

    testling.init();
    testling.buildNewSuche();
    assertNotNull(testling.getSuche());
    assertEquals(QUERY_WILDCARD, testling.getSuche().getQuery());
    assertNotNull(testling.getSuche().getPage());
    assertNotNull(testling.getSuche().getSort());
    assertNotNull(testling.getSuche().getFilter());
    assertTrue(testling.getSuche().getFilter().isEmpty());

    testling.setContainsBuchschmuck(true);
    testling.buildNewSuche();
    assertTrue(testling.getSuche().getFilter().containsKey(FilterCriteria.CONTAINS_BUCHSCHMUCK));

    testling.setContainsBuchschmuck(false);
    testling.buildNewSuche();
    assertFalse(testling.getSuche().getFilter().containsKey(FilterCriteria.CONTAINS_BUCHSCHMUCK));
  }

}
