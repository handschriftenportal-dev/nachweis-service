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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.SucheModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.FilteredFacetteModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Ergebnis;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SortCriteria;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 19.02.2020.
 */
@ViewScoped
@Named("sucheController")
@Slf4j
public class SucheController implements Serializable {

  static final String PARAM_QUERY = "query";
  private static final long serialVersionUID = 5107546744941881494L;
  private static final String TABLE_ID = "sucheForm:ergebnisTable";
  private static final String SEARCH_URL = "/suche.xhtml?faces-redirect=true&" + PARAM_QUERY + "=";

  @Getter
  private final SucheModel sucheModel;

  private final LanguageController languageController;

  @Getter
  private final FilteredFacetteModel institutionenFilter;
  @Getter
  private final FilteredFacetteModel autorenFilter;
  @Getter
  private Set<SelectItem> verwaltungsTypFilter;
  @Getter
  private Set<SelectItem> dokumentTypFilter;

  @Inject
  public SucheController(SucheModel sucheModel, LanguageController languageController) {
    this.sucheModel = sucheModel;
    this.languageController = languageController;

    this.institutionenFilter = new FilteredFacetteModel();
    this.autorenFilter = new FilteredFacetteModel();
  }

  public void setup() throws SolrServiceException, IOException {
    Optional<String> query = getParamQuery();

    if (query.isPresent()) {
      log.info("init search with param query={}", query.get());
      sucheModel.init();
      sucheModel.setQuery(query.get());
      searchWithoutRedirect();

    } else if (Objects.nonNull(sucheModel.getLazyErgebnisModel())) {
      if (Objects.isNull(sucheModel.getLazyErgebnisModel().getErgebnis())) {
        log.info("init search with empty query");
        sucheModel.setQuery("");
        searchWithoutRedirect();
      } else {
        updateFilters(sucheModel.getLazyErgebnisModel().getErgebnis());
      }
    }
  }

  public String search() throws SolrServiceException, IOException {
    log.info("starting search");
    resetDataTable();

    return SEARCH_URL + sucheModel.getQuery();
  }

  public void searchWithoutRedirect() throws SolrServiceException {
    log.info("starting searchWithoutRedirect");
    resetDataTable();

    Ergebnis ergebnis = sucheModel.search();

    if (Objects.nonNull(ergebnis)) {
      updateFilters(ergebnis);
    }
  }

  public void resetFromUpdate() throws SolrServiceException {
    sucheModel.setFromUpdate(null);
    searchWithoutRedirect();
  }

  public void resetToUpdate() throws SolrServiceException {
    sucheModel.setToUpdate(null);
    searchWithoutRedirect();
  }

  public void resetFilters() throws SolrServiceException {
    sucheModel.resetFilterValues();
    resetDataTable();
    searchWithoutRedirect();
  }

  public String resetSearch() {
    sucheModel.setQuery("");
    sucheModel.setSortCriteria(SortCriteria.SIGNATUR.name());
    sucheModel.resetFilterValues();
    resetDataTable();
    sucheModel.init();
    return "/suche.xhtml?faces-redirect=true";
  }

  void updateFilters(Ergebnis ergebnis) {
    dokumentTypFilter =
        createDokumentTypFilter(ergebnis.getFacetCountTypeKOD(), ergebnis.getFacetCountTypeBeschreibung());
    verwaltungsTypFilter =
        createVerwaltungsTypFilter(ergebnis.getFacetCountVerwaltungExtern(), ergebnis.getFacetCountVerwaltungIntern());

    institutionenFilter.updateFacetten(ergebnis.getInstitutionen());
    autorenFilter.updateFacetten(ergebnis.getAutoren());
  }

  Set<SelectItem> createDokumentTypFilter(Integer kods, Integer beschreibungen) {
    Set<SelectItem> filters = new LinkedHashSet<>();
    filters.add(createSelectItem(null, "suche_filter_alle", kods, beschreibungen));
    filters.add(createSelectItem(SuchDokumentTyp.KOD, "suche_dokumenttyp_kod", kods));
    filters.add(createSelectItem(SuchDokumentTyp.BS, "suche_dokumenttyp_beschreibung", beschreibungen));
    return filters;
  }

  Set<SelectItem> createVerwaltungsTypFilter(Integer extern, Integer intern) {
    Set<SelectItem> filters = new LinkedHashSet<>();
    filters.add(createSelectItem(null, "suche_filter_alle", extern, intern));
    filters.add(createSelectItem(VerwaltungsTyp.EXTERN, "suche_verwaltungs_typ_extern", extern));
    filters.add(createSelectItem(VerwaltungsTyp.INTERN, "suche_verwaltungs_typ_intern", intern));
    return filters;
  }

  SelectItem createSelectItem(Object value, String key, Integer... amount) {
    int sum = Optional.ofNullable(amount)
        .stream()
        .flatMap(Arrays::stream)
        .map(i -> Optional.ofNullable(i).orElse(0))
        .reduce(0, Integer::sum);

    String label = I18NController.getMessage(languageController.getLocale(), key, sum);
    return new SelectItem(value, label, null, sum == 0);
  }

  private void resetDataTable() {
    PrimeFaces primeFaces = PrimeFaces.current();
    FacesContext facesContext = FacesContext.getCurrentInstance();

    if (Objects.nonNull(facesContext) && Objects.nonNull(primeFaces)) {
      UIViewRoot viewRoot = facesContext.getViewRoot();

      if (Objects.nonNull(viewRoot)) {
        DataTable dataTable = (DataTable) viewRoot.findComponent(TABLE_ID);
        if (dataTable != null) {
          dataTable.clearLazyCache();
          dataTable.reset();
          primeFaces.multiViewState().clear(viewRoot.getViewId(), TABLE_ID);
        }
      }
    }
  }

  Optional<String> getParamQuery() {
    return Optional.ofNullable(FacesContext.getCurrentInstance())
        .map(FacesContext::getExternalContext)
        .map(ExternalContext::getRequest)
        .filter(HttpServletRequest.class::isInstance)
        .map(HttpServletRequest.class::cast)
        .map(request -> request.getParameter(PARAM_QUERY));
  }

}


