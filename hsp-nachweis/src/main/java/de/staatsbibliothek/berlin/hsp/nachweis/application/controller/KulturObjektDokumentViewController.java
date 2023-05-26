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

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte.PERMISSION_KOD_BEARBEITEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte.PERMISSION_KOD_HART_LOESCHEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte.PERMISSION_KOD_REGISTRIEREN;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.DigitalisatsRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.DigitalisatErfassenModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.KODKerndatenModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

/**
 * Created by Christoph Marten on 24.09.2020 at 11:31
 */

@ViewScoped
@Named
@Slf4j
@LoginCheck
public class KulturObjektDokumentViewController implements Serializable {

  private static final long serialVersionUID = -7951285555430545723L;

  private DigitalisatErfassenModel digitalisatErfassenModel;
  private KulturObjektDokumentViewModel kulturObjektDokumentViewModel;
  private KODKerndatenModel kodKerndatenModel;

  private KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  private BearbeiterBoundary bearbeiterBoundary;
  private BeschreibungsRechte beschreibungsRechte;

  private BeschreibungsBoundary beschreibungBoundary;
  private ImportBoundary importBoundary;
  private boolean showTEI = false;
  private Sperre sperre;
  private boolean deleteEnabled;
  private String id;
  private boolean editKerndaten;

  KulturObjektDokumentViewController() {

  }

  public void setup() {
    log.info("setup: id={}", id);
    if (Objects.nonNull(this.id) && !this.id.isEmpty()) {
      fillKulturObjektDokumentViewModelValues();

      try {
        sperre = kulturObjektDokumentBoundary.findSperreForKulturObjektDokument(id).orElse(null);
      } catch (DokumentSperreException dse) {
        throw new RuntimeException("Error finding Sperre for KOD " + this.id, dse);
      }
    }
  }

  @PreDestroy
  public void onPreDestroy() {
    log.info("onPreDestroy: id={}, editKerndaten={}", id, editKerndaten);
    if (editKerndaten
        && Objects.nonNull(this.id)
        && Objects.nonNull(sperre)
        && bearbeiterBoundary.getLoggedBearbeiter().equals(sperre.getBearbeiter())) {
      kodEntsperren(false);
    }
  }

  private void fillKulturObjektDokumentViewModelValues() {
    log.debug("fillKulturObjektDokumentValues for ID {}", this.id);

    this.kulturObjektDokumentViewModel = this.kulturObjektDokumentBoundary
        .buildKulturObjektDokumentViewModel(this.id)
        .orElse(null);

    this.kodKerndatenModel.init(this.kulturObjektDokumentViewModel);
  }

  public void neueBeschreibungErstellen() {

    if (isAutomatischeUebernahmeAktiv()) {
      if (Objects.nonNull(PrimeFaces.current())) {
        FacesMessage msg = new FacesMessage(getMessage("kod_detail_autouebernahme_aktiv"),
            getMessage("kod_detail_autouebernahme_aktiv_detail"));
        PrimeFaces.current().dialog().showMessageDynamic(msg);
      }
      return;
    }

    Map<String, List<String>> parameter = new HashMap<>();
    parameter.put("kodid", List.of(this.id));

    Map<String, Object> options = new HashMap<>();
    options.put("modal", true);
    options.put("closable", false);
    options.put("width", "900");
    options.put("height", "700");
    options.put("minWidth", "900");
    options.put("minHeight", "700");
    options.put("contentWidth", "100%");
    options.put("contentHeight", "100%");
    options.put("headerElement", "headerTitle");

    boolean gesperrt = kodSperren("BeschreibungErstellen",
        getMessage("kod_detail_beschreibungerstellen_sperre_fehler"));
    if (!gesperrt) {
      return;
    }

    PrimeFaces.current().dialog().openDynamic("beschreibungErstellen", options, parameter);
  }


  public void neueBeschreibungAnzeigen(SelectEvent<String> event) throws IOException {
    String beschreibungId = event.getObject();
    log.info("neueBeschreibungAnzeigen: beschreibungId={}", beschreibungId);

    boolean kodEntsperrt = kodEntsperren(true);

    if (kodEntsperrt
        && Objects.nonNull(FacesContext.getCurrentInstance())
        && Objects.nonNull(FacesContext.getCurrentInstance().getExternalContext())
        && Objects.nonNull(beschreibungId)
        && !beschreibungId.isBlank()) {
      FacesContext.getCurrentInstance().getExternalContext()
          .redirect("/beschreibung/beschreibung.xhtml?edit=true&id=" + beschreibungId);
    }
  }

  public KulturObjektDokumentViewModel getKulturObjektDokumentViewModel() {
    return kulturObjektDokumentViewModel;
  }

  public void setKulturObjektDokumentViewModel(
      KulturObjektDokumentViewModel kulturObjektDokumentViewModel) {
    this.kulturObjektDokumentViewModel = kulturObjektDokumentViewModel;
  }

  public void copyToClipboard(DigitalisatViewModel model) {
    showFacesInfoMessage("kod_detail_digitalisat_zwischenablage",
        !"null".equals(model.getManifestURL()) ? model.getManifestURL()
            : model.getAlternativeUrl());
  }

  @CheckPermission(permission = PERMISSION_KOD_REGISTRIEREN)
  public String digitalisatBearbeiten(DigitalisatViewModel model) {

    boolean gesperrt = kodSperren("DigitalisatBearbeiten",
        getMessage("kod_detail_digitalisatbearbeiten_sperre_fehler"));
    if (!gesperrt) {
      return null;
    }

    digitalisatErfassenModel.setDigitalisatViewModel(model);
    return "/kulturObjektDokument/digitalisat-anlegen.xhtml?faces-redirect=true&kodid="
        + this.kulturObjektDokumentViewModel.getId()
        + "&digitalisatid="
        + model.getId();
  }

  @CheckPermission(permission = PERMISSION_KOD_HART_LOESCHEN)
  public void digitalisatLoeschen(DigitalisatViewModel model) {
    boolean gesperrt = kodSperren("DigitalisatLöschen",
        getMessage("kod_detail_digitalisatloeschen_sperre_fehler"));

    if (gesperrt) {
      try {
        kulturObjektDokumentBoundary.digitalisatLoeschen(this.kulturObjektDokumentViewModel, model);
        kulturObjektDokumentBoundary.kulturObjektDokumentEntsperren(kulturObjektDokumentViewModel.getId());
        this.fillKulturObjektDokumentViewModelValues();
        showFacesInfoMessage("kod_detail_digitalisatloeschen_erfolg", "");
      } catch (Exception e) {
        log.error("Error in digitalisatLoeschen!", e);
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
            getMessage("kod_detail_digitalisatloeschen_fehler"),
            e.getMessage());
        FacesContext.getCurrentInstance().addMessage("globalmessages", msg);
      }
    }
  }

  @CheckPermission(permission = DigitalisatsRechte.PERMISSION_DIGITALISAT_MANUELL_ANLEGEN)
  public String digitalisatAnlegen() {
    boolean gesperrt = kodSperren("DigitalisatAnlegen",
        getMessage("kod_detail_digitalisatanlegen_sperre_fehler"));
    if (!gesperrt) {
      return null;
    }

    digitalisatErfassenModel.setDigitalisatViewModel(new DigitalisatViewModel());
    return "/kulturObjektDokument/digitalisat-anlegen.xhtml?faces-redirect=true&kodid="
        + this.kulturObjektDokumentViewModel.getId();
  }

  public boolean isShowTEI() {
    return showTEI;
  }

  public void setShowTEI(boolean showTEI) {
    this.showTEI = showTEI;
  }

  public boolean isFremdeSperreVorhanden() {
    if (kulturObjektDokumentViewModel == null || bearbeiterBoundary == null || sperre == null) {
      return false;
    }
    return !bearbeiterBoundary.getLoggedBearbeiter().equals(sperre.getBearbeiter());
  }

  public boolean isAutomatischeUebernahmeAktiv() {
    return importBoundary.isAutomatischenUebernahmeAktiv();
  }

  public Sperre getSperre() {
    return sperre;
  }

  boolean kodSperren(String sperreGrund, String summary) {
    try {
      sperre = kulturObjektDokumentBoundary.kulturObjektDokumentSperren(bearbeiterBoundary.getLoggedBearbeiter(),
          sperreGrund,
          kulturObjektDokumentViewModel.getId());
      return true;
    } catch (SperreAlreadyExistException saee) {
      log.info("Sperre already exists for KOD " + kulturObjektDokumentViewModel.getId());
      handleSperreAlreadyExistException(saee, summary);
    } catch (Exception exception) {
      log.error("Error acquiring sperre for KOD " + kulturObjektDokumentViewModel.getId(), exception);
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("kod_detail_sperren_fehler"),
          exception.getMessage());
      PrimeFaces.current().dialog().showMessageDynamic(msg);
    }
    return false;
  }

  private void handleSperreAlreadyExistException(SperreAlreadyExistException saee, String summary) {
    FacesMessage msg;
    try {
      sperre = Stream.ofNullable(saee.getSperren())
          .flatMap(List::stream)
          .findFirst()
          .orElse(null);

      String sperreBearbeiterName = Optional.ofNullable(sperre)
          .map(Sperre::getBearbeiter)
          .map(Bearbeiter::getName)
          .orElse(getMessage("kod_detail_unbekannt"));

      String sperreDatum = Optional.ofNullable(sperre)
          .map(Sperre::getStartDatum)
          .map(I18NController::formatDate)
          .orElse(getMessage("kod_detail_unbekannt"));

      msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, summary,
          getMessage("kod_detail_sperre_fehler_detail", sperreBearbeiterName, sperreDatum));
    } catch (Exception exception) {
      msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("kod_detail_sperre_laden_fehler"),
          exception.getMessage());
    }
    PrimeFaces.current().dialog().showMessageDynamic(msg);
  }

  boolean kodEntsperren(boolean showFacesMessageOnError) {
    log.info("kodEntsperren: kodId={}", this.id);
    try {
      kulturObjektDokumentBoundary.kulturObjektDokumentEntsperren(this.id);
      sperre = null;
      return true;
    } catch (Exception error) {
      log.error("Error in kulturObjektDokumentEntsperren for kodId=" + this.id, error);
      if (showFacesMessageOnError) {
        showFacesErrorMessage("kod_detail_entsperren_fehler", error.getMessage());
      }
    }
    return false;
  }

  @CheckPermission(permission = PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN)
  @TransactionConfiguration(timeout = 7200)
  @Transactional
  public void beschreibungLoeschen(String beschreibungsID) {

    try {
      log.info("Beschreibung loeschen {} ", beschreibungsID);

      Optional<Beschreibung> beschreibung = beschreibungBoundary.findById(beschreibungsID);

      if (beschreibung.isPresent()) {
        boolean gesperrt = kodSperren("BeschreibungLöschen",
            getMessage("kod_detail_beschreibungloeschen_fehler"));
        if (!gesperrt) {
          return;
        }
        beschreibungBoundary.delete(bearbeiterBoundary.getLoggedBearbeiter(), beschreibung.get());
        kulturObjektDokumentBoundary.kulturObjektDokumentEntsperren(kulturObjektDokumentViewModel.getId());
        this.fillKulturObjektDokumentViewModelValues();
      }

    } catch (Exception error) {
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("kod_detail_beschreibungloeschen_fehler"),
          error.getMessage());
      PrimeFaces.current().dialog().showMessageDynamic(msg);
    }
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void kerndatenBearbeiten() {
    log.info("kerndatenBearbeiten: kodId={} ", id);
    boolean gesperrt = kodSperren("KerndatenBearbeiten",
        getMessage("kod_detail_kerndatenbearbeiten_sperre_fehler"));
    if (gesperrt) {
      kodKerndatenModel.bearbeiten();
      this.editKerndaten = true;
    }
  }

  public void kerndatenBearbeitenAbbrechen() {
    log.info("kerndatenBearbeitenAbbrechen: kodId={} ", id);

    boolean kodEntsperrt = kodEntsperren(true);
    if (kodEntsperrt) {
      this.editKerndaten = false;
      this.fillKulturObjektDokumentViewModelValues();
    }
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void kerndatenSpeichern() {
    log.info("kerndatenSpeichern: kodId={} ", this.id);

    try {
      this.kodKerndatenModel.kerndatenUebernehmen(this.kulturObjektDokumentViewModel);
      this.kulturObjektDokumentBoundary.kulturObjektDokumentAktualisieren(this.kulturObjektDokumentViewModel);
      this.kulturObjektDokumentBoundary.kulturObjektDokumentEntsperren(this.kulturObjektDokumentViewModel.getId());
      this.sperre = null;
      this.editKerndaten = false;
      this.fillKulturObjektDokumentViewModelValues();
      showFacesInfoMessage("kod_detail_kerndatenspeichern_success", "");
    } catch (Exception error) {
      log.error("Error in kerndatenSpeichern: kodId=" + this.id, error);
      showFacesErrorMessage("kod_detail_kerndatenspeichern_fehler", error.getMessage());
    }
  }

  void showFacesErrorMessage(String messageKey, String details) {
    showFacesMessage(FacesMessage.SEVERITY_ERROR, messageKey, details);
  }

  void showFacesInfoMessage(String messageKey, String details) {
    showFacesMessage(FacesMessage.SEVERITY_INFO, messageKey, details);
  }

  private void showFacesMessage(FacesMessage.Severity severity, String messageKey, String details) {
    if (Objects.nonNull(FacesContext.getCurrentInstance())) {
      FacesMessage msg = new FacesMessage(severity, getMessage(messageKey), details);
      FacesContext.getCurrentInstance().addMessage("globalmessages", msg);
    }
  }

  public boolean isDeleteEnabled() {
    return deleteEnabled && beschreibungsRechte.kannAllerDritterhartLoeschen();
  }

  @Inject
  public void setDeleteEnabled(
      @ConfigProperty(name = "workaround.BESCHREIBUNG_LIST_DELETE_ENABLED") boolean deleteEnabled) {
    this.deleteEnabled = deleteEnabled;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isEditKerndaten() {
    return editKerndaten;
  }

  @Inject
  public void setDigitalisatErfassenModel(DigitalisatErfassenModel digitalisatErfassenModel) {
    this.digitalisatErfassenModel = digitalisatErfassenModel;
  }

  @Inject
  public void setKulturObjektDokumentBoundary(
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary) {
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
  }

  @Inject
  public void setBearbeiterBoundary(BearbeiterBoundary bearbeiterBoundary) {
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  @Inject
  public void setBeschreibungsBoundary(
      BeschreibungsBoundary beschreibungBoundary) {
    this.beschreibungBoundary = beschreibungBoundary;
  }

  @Inject
  public void setImportBoundary(ImportBoundary importBoundary) {
    this.importBoundary = importBoundary;
  }

  @Inject
  public void setBeschreibungsRechte(BeschreibungsRechte beschreibungsRechte) {
    this.beschreibungsRechte = beschreibungsRechte;
  }

  @Inject
  public void setKODKerndatenModel(KODKerndatenModel kodKerndatenModel) {
    this.kodKerndatenModel = kodKerndatenModel;
  }

}
