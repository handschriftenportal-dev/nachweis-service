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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.LazyResearchErgebnisViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Ergebnis;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Page;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Sort;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SortCriteria;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SortType;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Suche;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.time.LocalDate;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 09.07.20
 */
@SessionScoped
@Named
@Slf4j
@Data
@LoginCheck
public class SucheModel implements Serializable {

  private static final long serialVersionUID = -2509070643697269968L;

  private SuchDokumentBoundary suchDokumentBoundary;
  private LazyResearchErgebnisViewModel lazyErgebnisModel = null;
  private String query = "";
  private String sortCriteria = SortCriteria.SIGNATUR.name();

  private boolean containsDigitalisat;
  private boolean containsBuchschmuck;
  private boolean containsBeschreibung;

  private SuchDokumentTyp selectedDokumentTyp;
  private VerwaltungsTyp selectedVerwaltungsTyp;

  private String[] selectedInstitutionen;
  private String[] selectedAutoren;

  private LocalDate fromUpdate;
  private LocalDate toUpdate;

  private Suche suche;

  SucheModel() {
  }

  @Inject
  public SucheModel(SuchDokumentBoundary suchDokumentBoundary) {
    this();
    this.suchDokumentBoundary = suchDokumentBoundary;
  }

  @PostConstruct
  public void init() {
    lazyErgebnisModel = new LazyResearchErgebnisViewModel(suchDokumentBoundary);

    buildNewSuche();
  }

  public Ergebnis search() throws SolrServiceException {
    buildNewSuche();
    lazyErgebnisModel.search(getSuche());
    return lazyErgebnisModel.getErgebnis();
  }

  public void buildNewSuche() {
    this.suche = Suche.builder()
        .withQuery(query)
        .withPage(new Page(25, 0))
        .withSort(new Sort(SortCriteria.valueOf(sortCriteria), SortType.ASC))
        .withHighlighting(true)
        .withFilterContainsBuchschmuck(containsBuchschmuck)
        .withFilterContainsDigitalisat(containsDigitalisat)
        .withFilterSuchDokumentTyp(selectedDokumentTyp)
        .withFilterAutoren(selectedAutoren)
        .withFilterContainsBeschreibung(containsBeschreibung)
        .withFilterVerwaltungsTyp(selectedVerwaltungsTyp)
        .withFilterBestandshaltendeInstitutionen(selectedInstitutionen)
        .withFilterLastUpdateFrom(fromUpdate)
        .withFilterLastUpdateTo(toUpdate)
        .build();
  }

  public void resetFilterValues() {
    containsDigitalisat = false;
    containsBuchschmuck = false;
    containsBeschreibung = false;
    selectedVerwaltungsTyp = null;
    selectedDokumentTyp = null;
    selectedInstitutionen = null;
    selectedAutoren = null;
    fromUpdate = null;
    toUpdate = null;
  }

}
