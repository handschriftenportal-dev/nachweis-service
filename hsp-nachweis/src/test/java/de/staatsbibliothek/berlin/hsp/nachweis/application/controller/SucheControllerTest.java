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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrAPI.QUERY_WILDCARD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.SucheModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.LazyResearchErgebnisViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Ergebnis;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SortCriteria;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Suche;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 04.03.2020.
 *
 * @see SucheController
 */
class SucheControllerTest {

  @Test
  void testSetup() throws SolrServiceException, IOException {
    SucheController testling = createSucheController(false);

    testling.setup();

    assertNotNull(testling.getSucheModel().getSuche());
    assertNotNull(testling.getSucheModel().getQuery());
    assertNotNull(testling.getSucheModel().getLazyErgebnisModel());
    assertNotNull(testling.getSucheModel().getLazyErgebnisModel().getErgebnis());

    assertNotNull(testling.getDokumentTypFilter());
    assertFalse(testling.getDokumentTypFilter().isEmpty());
    assertNotNull(testling.getVerwaltungsTypFilter());
    assertFalse(testling.getVerwaltungsTypFilter().isEmpty());
    assertNotNull(testling.getInstitutionenFilter());
    assertNotNull(testling.getInstitutionenFilter().getFacetten());
    assertNotNull(testling.getAutorenFilter());
    assertNotNull(testling.getAutorenFilter().getFacetten());
  }

  @Test
  void testSetup_withQuery() throws SolrServiceException, IOException {
    try (MockedStatic<FacesContext> facesContextCurrentInstance = createFacesContextWithQuery("KOD")) {
      assertNotNull(facesContextCurrentInstance);

      SucheController testling = createSucheController(false);

      testling.setup();

      assertNotNull(testling.getSucheModel().getSuche());
      assertNotNull(testling.getSucheModel().getQuery());
      assertNotNull(testling.getSucheModel().getLazyErgebnisModel());
      assertNotNull(testling.getSucheModel().getLazyErgebnisModel().getErgebnis());

      assertNotNull(testling.getDokumentTypFilter());
      assertFalse(testling.getDokumentTypFilter().isEmpty());
      assertNotNull(testling.getVerwaltungsTypFilter());
      assertFalse(testling.getVerwaltungsTypFilter().isEmpty());
      assertNotNull(testling.getInstitutionenFilter());
      assertNotNull(testling.getInstitutionenFilter().getFacetten());
      assertNotNull(testling.getAutorenFilter());
      assertNotNull(testling.getAutorenFilter().getFacetten());
    }
  }

  @Test
  void testSetup_withErgebnis() throws SolrServiceException, IOException {
    SucheController testling = createSucheController(true);

    testling.setup();

    assertNotNull(testling.getSucheModel().getSuche());
    assertNotNull(testling.getSucheModel().getQuery());
    assertNotNull(testling.getSucheModel().getLazyErgebnisModel());
    assertNotNull(testling.getSucheModel().getLazyErgebnisModel().getErgebnis());

    assertNotNull(testling.getDokumentTypFilter());
    assertFalse(testling.getDokumentTypFilter().isEmpty());
    assertNotNull(testling.getVerwaltungsTypFilter());
    assertFalse(testling.getVerwaltungsTypFilter().isEmpty());
    assertNotNull(testling.getInstitutionenFilter());
    assertNotNull(testling.getInstitutionenFilter().getFacetten());
    assertNotNull(testling.getAutorenFilter());
    assertNotNull(testling.getAutorenFilter().getFacetten());
  }

  @Test
  void testSearch() throws SolrServiceException, IOException {
    SucheController testling = createSucheController(true);

    String redirect = testling.search();

    assertNotNull(redirect);
    assertEquals("/suche.xhtml?faces-redirect=true&query=", redirect);

    Mockito.verify(testling.getSucheModel().getLazyErgebnisModel(), Mockito.times(0))
        .search(any());
  }

  @Test
  void testSearchWithoutRedirect() throws SolrServiceException {
    SucheController testling = createSucheController(true);

    testling.searchWithoutRedirect();

    Mockito.verify(testling.getSucheModel().getLazyErgebnisModel(), Mockito.times(1))
        .search(Mockito.any());
  }

  @Test
  void testResetFromUpdate() throws Exception {
    SucheController testling = createSucheController(true);

    testling.getSucheModel().setFromUpdate(LocalDate.now());

    testling.resetFromUpdate();

    assertNull(testling.getSucheModel().getFromUpdate());

    Mockito.verify(testling.getSucheModel().getLazyErgebnisModel(), Mockito.times(1))
        .search(any());
  }

  @Test
  void testResetToUpdate() throws Exception {
    SucheController testling = createSucheController(true);

    testling.getSucheModel().setToUpdate(LocalDate.now());

    testling.resetToUpdate();

    assertNull(testling.getSucheModel().getToUpdate());

    Mockito.verify(testling.getSucheModel().getLazyErgebnisModel(), Mockito.times(1))
        .search(any());
  }

  @Test
  void testResetFilters() throws SolrServiceException {
    SucheController testling = createSucheController(true);
    SucheModel sucheModel = testling.getSucheModel();

    sucheModel.setContainsBuchschmuck(Boolean.TRUE);
    sucheModel.setContainsDigitalisat(Boolean.TRUE);
    sucheModel.setSelectedVerwaltungsTyp(VerwaltungsTyp.INTERN);
    sucheModel.setSelectedDokumentTyp(SuchDokumentTyp.KOD);
    sucheModel.setSelectedInstitutionen(new String[]{"StaBi Berlin"});
    sucheModel.setSelectedAutoren(new String[]{"Bernhard Tönnies"});
    sucheModel.setFromUpdate(LocalDate.of(2020, 1, 2));
    sucheModel.setToUpdate(LocalDate.of(2021, 3, 4));

    testling.resetFilters();

    assertFalse(sucheModel.isContainsBuchschmuck());
    assertFalse(sucheModel.isContainsDigitalisat());
    assertNull(sucheModel.getSelectedVerwaltungsTyp());
    assertNull(sucheModel.getSelectedDokumentTyp());
    assertNull(sucheModel.getSelectedInstitutionen());
    assertNull(sucheModel.getSelectedAutoren());
    assertNull(sucheModel.getFromUpdate());
    assertNull(sucheModel.getToUpdate());

    Mockito.verify(testling.getSucheModel().getLazyErgebnisModel(), Mockito.times(1))
        .search(any());
  }

  @Test
  void testResetSearch() throws SolrServiceException {
    SucheController testling = createSucheController(true);
    SucheModel sucheModel = testling.getSucheModel();

    sucheModel.setQuery("Augustinus");
    sucheModel.setToUpdate(LocalDate.now());
    sucheModel.setSortCriteria(SortCriteria.RELEVANZ.name());

    sucheModel.search();

    assertNotNull(sucheModel.getSuche());
    assertNotNull(sucheModel.getSuche().getQuery());

    testling.resetSearch();

    assertNotNull(sucheModel.getSuche());
    assertEquals(QUERY_WILDCARD, sucheModel.getSuche().getQuery());

    assertNotNull(sucheModel.getSuche().getFilter());
    assertTrue(sucheModel.getSuche().getFilter().isEmpty());

    assertNotNull(sucheModel.getSuche().getSort());
    assertSame(SortCriteria.SIGNATUR, sucheModel.getSuche().getSort().getCriteria());

    assertNotNull(sucheModel.getSuche().getPage());
    assertSame(0, sucheModel.getSuche().getPage().getOffset());
    assertSame(25, sucheModel.getSuche().getPage().getNumberOfRows());
  }

  @Test
  void testCreateSelectItem() throws SolrServiceException {
    SucheController testling = createSucheController(true);

    SelectItem selectItem = testling.createSelectItem(SuchDokumentTyp.KOD.toString(), "suche_dokumenttyp_kod",
        (Integer) null);
    assertNotNull(selectItem);
    assertEquals("KOD (0)", selectItem.getLabel());
    assertEquals("KOD", selectItem.getValue());
    assertTrue(selectItem.isDisabled());

    selectItem = testling.createSelectItem(SuchDokumentTyp.BS.toString(), "suche_dokumenttyp_beschreibung", 10);
    assertNotNull(selectItem);
    assertEquals("Beschreibung (10)", selectItem.getLabel());
    assertEquals("BS", selectItem.getValue());
    assertFalse(selectItem.isDisabled());
  }

  @Test
  void testUpdateFilters() throws SolrServiceException {
    SucheController testling = createSucheController(true);

    Ergebnis ergebnis = new Ergebnis();
    ergebnis.setFacetCountTypeKOD(5);
    ergebnis.setFacetCountTypeBeschreibung(10);
    ergebnis.setFacetCountVerwaltungExtern(7);
    ergebnis.setFacetCountVerwaltungIntern(3);

    Map<String, Integer> institutionen = new HashMap<>();
    institutionen.put("SBB", 10);
    ergebnis.setInstitutionen(institutionen);

    Map<String, Integer> autoren = new HashMap<>();
    autoren.put("Muster, Max", 2);
    autoren.put("Müller, Marianne", 1);
    ergebnis.setAutoren(autoren);

    testling.updateFilters(ergebnis);

    assertNotNull(testling.getDokumentTypFilter());
    assertEquals(3, testling.getDokumentTypFilter().size());
    assertTrue(testling.getDokumentTypFilter().stream()
        .anyMatch(selectItem -> "KOD (5)".equals(selectItem.getLabel())));
    assertTrue(testling.getDokumentTypFilter().stream()
        .anyMatch(selectItem -> "Beschreibung (10)".equals(selectItem.getLabel())));

    assertNotNull(testling.getVerwaltungsTypFilter());
    assertEquals(3, testling.getVerwaltungsTypFilter().size());
    assertTrue(testling.getVerwaltungsTypFilter().stream()
        .anyMatch(selectItem -> "Extern (7)".equals(selectItem.getLabel())));
    assertTrue(testling.getVerwaltungsTypFilter().stream()
        .anyMatch(selectItem -> "Intern (3)".equals(selectItem.getLabel())));

    assertNotNull(testling.getInstitutionenFilter());
    assertNotNull(testling.getInstitutionenFilter().getFilteredFacetten());
    assertEquals(1, testling.getInstitutionenFilter().getFilteredFacetten().size());

    assertNotNull(testling.getAutorenFilter());
    assertNotNull(testling.getAutorenFilter().getFilteredFacetten());
    assertEquals(2, testling.getAutorenFilter().getFilteredFacetten().size());
  }

  @Test
  void testGetParamQuery() throws SolrServiceException {
    try (MockedStatic<FacesContext> facesContextCurrentInstance = createFacesContextWithQuery("HSP")) {
      assertNotNull(facesContextCurrentInstance);

      SucheController testling = createSucheController(false);
      Optional<String> query = testling.getParamQuery();

      assertNotNull(query);
      assertTrue(query.isPresent());
      assertEquals("HSP", query.get());

    }
  }

  MockedStatic<FacesContext> createFacesContextWithQuery(String query) {
    final MockedStatic<FacesContext> facesContextCurrentInstance = Mockito.mockStatic(FacesContext.class);
    FacesContext facesContextMock = mock(FacesContext.class);
    facesContextCurrentInstance.when(FacesContext::getCurrentInstance).thenReturn(facesContextMock);

    ExternalContext externalContextMock = mock(ExternalContext.class);
    when(facesContextMock.getExternalContext()).thenReturn(externalContextMock);

    HttpServletRequest requestMock = mock(HttpServletRequest.class);
    when(externalContextMock.getRequest()).thenReturn(requestMock);

    when(requestMock.getParameter(SucheController.PARAM_QUERY)).thenReturn(query);

    return facesContextCurrentInstance;
  }

  SucheController createSucheController(boolean withExistingErgebnis) throws SolrServiceException {
    SuchDokumentBoundary suchDokumentBoundaryMock = mock(SuchDokumentBoundary.class);
    when(suchDokumentBoundaryMock.search(any(Suche.class)))
        .thenReturn(new Ergebnis());

    SucheModel sucheModel = new SucheModel(suchDokumentBoundaryMock);
    sucheModel.init();

    if (withExistingErgebnis) {
      LazyResearchErgebnisViewModel lazyViewModelMock = mock(LazyResearchErgebnisViewModel.class);
      when(lazyViewModelMock.getErgebnis()).thenReturn(new Ergebnis());
      sucheModel.setLazyErgebnisModel(lazyViewModelMock);
    }

    LanguageController languageControllerMock = mock(LanguageController.class);
    when(languageControllerMock.getLocale()).thenReturn(Locale.GERMAN);

    return new SucheController(sucheModel, languageControllerMock);
  }


}
