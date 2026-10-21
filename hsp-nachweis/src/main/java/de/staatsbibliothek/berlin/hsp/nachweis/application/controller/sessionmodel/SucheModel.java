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
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
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

  @Serial
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
  private String[] selectedBearbeiter;

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
        .withFilterBearbeiter(selectedBearbeiter)
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
    sortCriteria = SortCriteria.SIGNATUR.name();
  }

}
