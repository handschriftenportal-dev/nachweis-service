package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.DigitalisatErfassenController.DIGITIZATION_SCHEME_TYPE;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getLocale;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.DigitalisatsRechte.PERMISSION_DIGITALISAT_BEARBEITEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.DigitalisatsRechte.PERMISSION_DIGITALISAT_LOESCHEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.DigitalisatsRechte.PERMISSION_DIGITALISAT_MANUELL_ANLEGEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte.PERMISSION_KOD_BEARBEITEN;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessource;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessourceTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Forschungsdokumentation;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.DigitalisatErfassenModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.KODKerndatenModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.IdentifikationViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.util.Dialog;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

/**
 * Created by Christoph Marten on 24.09.2020 at 11:31
 */
@ViewScoped
@Named
@Slf4j
@LoginCheck
@Getter
@Setter
public class KulturObjektDokumentViewController implements Serializable {

  @Serial
  private static final long serialVersionUID = -7951285555430545723L;
  private static final String KOD_DIGITALISAT_ANLEGEN_BASE_URL =
      "/kulturObjektDokument/digitalisat-anlegen.xhtml?faces-redirect=true&kodid=";

  private DigitalisatErfassenModel digitalisatErfassenModel;
  private KulturObjektDokumentViewModel kulturObjektDokumentViewModel;
  private KODKerndatenModel kodKerndatenModel;

  private KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  private BearbeiterBoundary bearbeiterBoundary;

  private ImportBoundary importBoundary;

  private boolean showTEI = false;

  private Sperre sperre;
  private String id;
  private boolean editKerndaten;

  private boolean editSignature;

  private boolean addAlternativeSignature;

  private String neueSignature;

  private String neueAlternativeSignature;

  private boolean addExterneRessource;

  private ExterneRessourceTyp neueExterneRessourceTyp;

  private String neueExterneRessourceUrl;

  private boolean addForschungsdokumentation;

  private String neueForschungsdokumentationRessource;

  private String neueForschungsdokumentationLink;

  private NormdatenReferenzBoundary normdatenReferenzBoundary;

  KulturObjektDokumentViewController() {
  }

  public void setup() {
    log.info("setup: id={}", id);
    if (Objects.nonNull(this.id) && !this.id.isEmpty()) {
      mapKulturObjektDokumentViewModel();
      try {
        sperre = kulturObjektDokumentBoundary.findSperreForKulturObjektDokument(id).orElse(null);
      } catch (DokumentSperreException dse) {
        throw new RuntimeException("Error finding Sperre for KOD {}" + this.id, dse);
      }
    }
  }

  @PreDestroy
  public void onPreDestroy() {

    log.info(
        "onPreDestroy: id={}, editKerndaten={}, editSignature={}, "
            + "addAlternativeSignature={}, addExterneRessource={}, "
            + "addForschungsdokumentation={}",
        id,
        editKerndaten,
        editSignature,
        addAlternativeSignature,
        addExterneRessource,
        addForschungsdokumentation);

    if (isBearbeitungAktiv()
        && Objects.nonNull(this.id)
        && Objects.nonNull(sperre)
        && bearbeiterBoundary
        .getLoggedBearbeiter()
        .equals(sperre.getBearbeiter())) {

      kodEntsperren(false);
    }
  }

  /**
   * Whether an edit form of this view owns the Sperre.
   *
   * <p>Deliberately not {@code sperre != null}: {@link #setup()} picks up an existing Sperre on
   * page load which no form owns, and which must not stop other actions from releasing what they
   * acquired.</p>
   */
  public boolean isBearbeitungAktiv() {

    return editKerndaten
        || editSignature
        || addAlternativeSignature
        || addExterneRessource
        || addForschungsdokumentation;
  }

  /**
   * Reloads the KulturObjektDokumentViewModel from the database. Call this method to refresh the
   * view after external changes (e.g., publication requests).
   */
  public void reloadViewModel() {
    mapKulturObjektDokumentViewModel();
  }

  private void mapKulturObjektDokumentViewModel() {
    log.debug("fillKulturObjektDokumentValues for ID {}", this.id);

    this.kulturObjektDokumentViewModel =
        this.kulturObjektDokumentBoundary.buildKulturObjektDokumentViewModel(this.id).orElse(null);

    this.kodKerndatenModel.init(this.kulturObjektDokumentViewModel);
  }

  public void neueBeschreibungErstellen() {

    if (isAutomatischeUebernahmeAktiv()) {
      if (Objects.nonNull(PrimeFaces.current())) {
        FacesMessage msg =
            new FacesMessage(
                getMessage("kod_detail_autouebernahme_aktiv"),
                getMessage("kod_detail_autouebernahme_aktiv_detail"));
        PrimeFaces.current().dialog().showMessageDynamic(msg);
      }
      return;
    }

    boolean gesperrt =
        kodSperren(
            "BeschreibungErstellen", getMessage("kod_detail_beschreibungerstellen_sperre_fehler"));
    if (!gesperrt) {
      return;
    }

    Dialog.forOutcome("beschreibungErstellen")
        .withSize("900", "700")
        .withParam("kodid", this.id)
        .open();
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
      FacesContext.getCurrentInstance()
          .getExternalContext()
          .redirect("/beschreibung/beschreibung.xhtml?edit=true&id=" + beschreibungId);
    }
  }

  public void copyToClipboard(DigitalisatViewModel model) {
    showFacesInfoMessage(
        "kod_detail_digitalisat_zwischenablage",
        !"null".equals(model.getManifestURL())
            ? model.getManifestURL()
            : model.getAlternativeUrl());
  }

  @CheckPermission(permission = PERMISSION_DIGITALISAT_BEARBEITEN)
  public String digitalisatBearbeiten(DigitalisatViewModel model) {

    boolean gesperrt =
        kodSperren(
            "DigitalisatBearbeiten", getMessage("kod_detail_digitalisatbearbeiten_sperre_fehler"));
    if (!gesperrt) {
      return KOD_DIGITALISAT_ANLEGEN_BASE_URL + this.kulturObjektDokumentViewModel.getId();
    }

    digitalisatErfassenModel.setDigitalisatViewModel(model);
    return KOD_DIGITALISAT_ANLEGEN_BASE_URL
        + this.kulturObjektDokumentViewModel.getId()
        + "&digitalisatid="
        + model.getId();
  }

  @CheckPermission(permission = PERMISSION_DIGITALISAT_LOESCHEN)
  public void digitalisatLoeschen(DigitalisatViewModel model) {
    boolean gesperrt =
        kodSperren(
            "DigitalisatLöschen", getMessage("kod_detail_digitalisatloeschen_sperre_fehler"));

    if (gesperrt) {
      try {
        kulturObjektDokumentBoundary.digitalisatLoeschen(this.kulturObjektDokumentViewModel.getId(),
            model);
        kulturObjektDokumentBoundary.kulturObjektDokumentEntsperren(
            kulturObjektDokumentViewModel.getId());
        this.mapKulturObjektDokumentViewModel();
        showFacesInfoMessage("kod_detail_digitalisatloeschen_erfolg", "");
      } catch (Exception e) {
        log.error("Error in digitalisatLoeschen!", e);
        FacesMessage msg =
            new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                getMessage("kod_detail_digitalisatloeschen_fehler"),
                e.getMessage());
        FacesContext.getCurrentInstance().addMessage("hspGrowl", msg);
      }
    }
  }

  @CheckPermission(permission = PERMISSION_DIGITALISAT_MANUELL_ANLEGEN)
  public String digitalisatAnlegen() {
    boolean gesperrt =
        kodSperren("DigitalisatAnlegen", getMessage("kod_detail_digitalisatanlegen_sperre_fehler"));
    if (!gesperrt) {
      return null;
    }

    digitalisatErfassenModel.setDigitalisatViewModel(new DigitalisatViewModel());
    return KOD_DIGITALISAT_ANLEGEN_BASE_URL + this.kulturObjektDokumentViewModel.getId();
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

  boolean kodSperren(String sperreGrund, String summary) {
    try {
      sperre =
          kulturObjektDokumentBoundary.kulturObjektDokumentSperren(
              bearbeiterBoundary.getLoggedBearbeiter(),
              sperreGrund,
              kulturObjektDokumentViewModel.getId());
      return true;
    } catch (SperreAlreadyExistException error) {
      log.error("Sperre already exists for KOD {}", kulturObjektDokumentViewModel.getId(), error);
      handleSperreAlreadyExistException(error, summary);
    } catch (Exception exception) {
      log.error(
          "Error acquiring sperre for KOD {}", kulturObjektDokumentViewModel.getId(), exception);
      FacesMessage msg =
          new FacesMessage(
              FacesMessage.SEVERITY_ERROR,
              getMessage("kod_detail_sperren_fehler"),
              exception.getMessage());
      PrimeFaces.current().dialog().showMessageDynamic(msg);
    }
    return false;
  }

  private void handleSperreAlreadyExistException(SperreAlreadyExistException saee, String summary) {
    FacesMessage msg;
    try {
      sperre = Stream.ofNullable(saee.getSperren()).flatMap(List::stream).findFirst().orElse(null);

      String sperreBearbeiterName =
          Optional.ofNullable(sperre)
              .map(Sperre::getBearbeiter)
              .map(Bearbeiter::getName)
              .orElse(getMessage("kod_detail_unbekannt"));

      String sperreDatum =
          Optional.ofNullable(sperre)
              .map(Sperre::getStartDatum)
              .map(I18NController::formatDate)
              .orElse(getMessage("kod_detail_unbekannt"));

      msg =
          new FacesMessage(
              FacesMessage.SEVERITY_ERROR,
              summary,
              getMessage("kod_detail_sperre_fehler_detail", sperreBearbeiterName, sperreDatum));
    } catch (Exception exception) {
      msg =
          new FacesMessage(
              FacesMessage.SEVERITY_ERROR,
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
      log.error("Error in kulturObjektDokumentEntsperren for kodId={}", this.id, error);
      if (showFacesMessageOnError) {
        showFacesErrorMessage("kod_detail_entsperren_fehler", error.getMessage());
      }
    }
    return false;
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void kerndatenBearbeiten() {
    log.info("kerndatenBearbeiten: kodId={} ", id);
    boolean gesperrt =
        kodSperren(
            "KerndatenBearbeiten", getMessage("kod_detail_kerndatenbearbeiten_sperre_fehler"));
    if (gesperrt) {
      kodKerndatenModel.bearbeiten();
      this.editKerndaten = true;
    }
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void enableAddAlternativeSignature() {
    kodSperren("KODAddAlternativeSignature", getMessage("kod_signature_changed_lock_error"));
    this.addAlternativeSignature = true;
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void disableAddAlternativeSignature() {
    this.neueAlternativeSignature = "";
    kodEntsperren(true);
    this.addAlternativeSignature = false;
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void alternativeSignatureHinzufuegen() {
    log.info("Alternative Signatur hinzufuegen for kod {} ", this.id);

    try {
      this.kulturObjektDokumentBoundary.alternativeSignaturHinzufuegen(
          neueAlternativeSignature, this.kulturObjektDokumentViewModel.getId(), sperre);
      showLocalFacesMessage(
          FacesMessage.SEVERITY_INFO,
          "kod_altsignatur_add_successfull",
          "",
          "alternativeKODMessage");
    } catch (Exception error) {
      log.error("Error signatur aendern: kodId={}", this.id, error);
      showLocalFacesMessage(
          FacesMessage.SEVERITY_ERROR,
          "kod_alternativesignatur_add_error",
          error.getMessage(),
          "alternativeKODMessage");
    } finally {
      this.addAlternativeSignature = false;
      this.neueAlternativeSignature = "";
      mapKulturObjektDokumentViewModel();
    }
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void enableEditSignature() {
    kodSperren("KODSignatureAendern", getMessage("kod_signature_changed_lock_error"));
    this.neueSignature = kulturObjektDokumentViewModel.getSignatur();
    this.editSignature = true;
  }

  public void disableEditSignature() {
    this.neueSignature = kulturObjektDokumentViewModel.getSignatur();
    kodEntsperren(true);
    this.editSignature = false;
  }

  public void kerndatenBearbeitenAbbrechen() {
    log.info("kerndatenBearbeitenAbbrechen: kodId={} ", id);

    boolean kodEntsperrt = kodEntsperren(true);
    if (kodEntsperrt) {
      this.editKerndaten = false;
      this.mapKulturObjektDokumentViewModel();
    }
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void kerndatenSpeichern() {
    log.info("kerndatenSpeichern: kodId={} ", this.id);

    try {
      this.kodKerndatenModel.kerndatenUebernehmen(this.kulturObjektDokumentViewModel);
      this.kulturObjektDokumentBoundary.kulturObjektDokumentAttributsreferenzenAktualisieren(
          this.kulturObjektDokumentViewModel);
      this.kulturObjektDokumentBoundary.kulturObjektDokumentEntsperren(
          this.kulturObjektDokumentViewModel.getId());
      this.sperre = null;
      this.editKerndaten = false;
      this.mapKulturObjektDokumentViewModel();
      showFacesInfoMessage("kod_detail_kerndatenspeichern_success", "");
    } catch (Exception error) {
      log.error("Error in kerndatenSpeichern: kodId={}", this.id, error);
      showFacesErrorMessage("kod_detail_kerndatenspeichern_fehler", error.getMessage());
    }
  }

  public String getSignatureChangeConfirmMessage() {
    return getMessage(
        "kod_signaturechange_confirm",
        this.kulturObjektDokumentViewModel.getSignatur(),
        neueSignature);
  }

  public String addAlternativeSignatureConfirmMessage() {
    return getMessage("kod_addalternative_confirm", neueAlternativeSignature);
  }

  public String deleteAlternativeSignatureConfirmMessage(IdentifikationViewModel signatur) {
    return getMessage("kod_delete_alternativesignatur_header", signatur.signatur());
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void gueltigeSignaturBearbeiten() {
    log.info("Gueltige Signatur bearbeiten for kod {} ", this.id);

    try {
      this.kulturObjektDokumentBoundary.gueltigeSignaturVeraendern(
          neueSignature, this.kulturObjektDokumentViewModel.getId(), sperre);
      showFacesInfoMessage("kod_signatur_change_successfull", "");
    } catch (Exception error) {
      log.error("Error signatur aendern: kodId={}", this.id, error);
      showFacesErrorMessage("kod_signatur_change_error", error.getMessage());
    } finally {
      this.editSignature = false;
      mapKulturObjektDokumentViewModel();
    }
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void enableAddExterneRessource() {

    boolean gesperrt = kodSperren("KODAddExterneRessource",
        getMessage("kod_signature_changed_lock_error"));

    if (gesperrt) {
      this.addExterneRessource = true;
    }
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void disableAddExterneRessource() {
    this.neueExterneRessourceTyp = null;
    this.neueExterneRessourceUrl = "";
    kodEntsperren(true);
    this.addExterneRessource = false;
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void externeRessourceHinzufuegen() {

    log.info(
        "Add external resource for KOD {}",
        this.id);

    try {
      kulturObjektDokumentBoundary
          .externeRessourceHinzufuegen(
              kulturObjektDokumentViewModel.getId(),
              neueExterneRessourceTyp,
              neueExterneRessourceUrl,
              this.sperre);

      kodEntsperren(true);

      showFacesMessage(
          FacesMessage.SEVERITY_INFO,
          "kod_externe_ressource_add_successful",
          "");

      this.addExterneRessource = false;
      this.neueExterneRessourceTyp = null;
      this.neueExterneRessourceUrl = "";

      mapKulturObjektDokumentViewModel();

    } catch (Exception error) {
      log.error(
          "Error adding external resource: kodId={}",
          this.id,
          error);

      showFacesMessage(
          FacesMessage.SEVERITY_ERROR,
          "kod_externe_ressource_add_error",
          error.getMessage());

      // Keep the form and lock so that the user can correct the input.
      this.addExterneRessource = true;
    }
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void enableAddForschungsdokumentation() {

    boolean gesperrt = kodSperren(
        "KODAddForschungsdokumentation",
        getMessage("kod_forschungsdokumentation_lock_error"));

    if (gesperrt) {
      this.addForschungsdokumentation = true;
    }
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void disableAddForschungsdokumentation() {
    this.neueForschungsdokumentationRessource = "";
    this.neueForschungsdokumentationLink = "";
    kodEntsperren(true);
    this.addForschungsdokumentation = false;
  }

  /** Opens the confirmation dialog after the JSF validation phase has completed successfully. */
  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void forschungsdokumentationBestaetigen() {
    PrimeFaces.current().executeScript("PF('addForschungsdokumentationConfirm').show()");
  }

  public String addForschungsdokumentationConfirmMessage() {
    return getMessage("kod_forschungsdokumentation_add_confirm");
  }

  public boolean isForschungsdokumentationSpeichernDisabled() {
    return StringUtils.isBlank(neueForschungsdokumentationRessource);
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void forschungsdokumentationHinzufuegen() {

    log.info("Add research documentation for KOD {}", this.id);

    try {
      kulturObjektDokumentBoundary.forschungsdokumentationHinzufuegen(
          kulturObjektDokumentViewModel.getId(),
          neueForschungsdokumentationRessource,
          neueForschungsdokumentationLink,
          this.sperre);

      kodEntsperren(true);
      showFacesMessage(
          FacesMessage.SEVERITY_INFO,
          "kod_forschungsdokumentation_add_successful",
          "");

      this.addForschungsdokumentation = false;
      this.neueForschungsdokumentationRessource = "";
      this.neueForschungsdokumentationLink = "";
      mapKulturObjektDokumentViewModel();

    } catch (Exception error) {
      log.error("Error adding research documentation: kodId={}", this.id, error);
      showFacesMessage(
          FacesMessage.SEVERITY_ERROR,
          "kod_forschungsdokumentation_add_error",
          error.getMessage());

      // Keep the form, its values and the lock so that the user can correct the input.
      this.addForschungsdokumentation = true;
    }
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void externeRessourceLoeschen(ExterneRessource ressource) {

    // An open edit form owns the Sperre, so reuse it and leave it alone. Otherwise acquire one for
    // this action and release it again below — including the case where setup() picked up an
    // existing Sperre that no form of this view owns.
    boolean sperreGehoertEinemFormular = isBearbeitungAktiv();

    if (!sperreGehoertEinemFormular
        && !kodSperren("KODDeleteExterneRessource", getMessage("kod_detail_sperren_exists"))) {
      return;
    }

    try {
      log.info(
          "Delete external resource: kodId={}, type={}, url={}",
          this.id,
          ressource.typ(),
          ressource.url());

      kulturObjektDokumentBoundary.externeRessourceLoeschen(
          this.id,
          ressource.typ(),
          ressource.url(),
          this.sperre);

      mapKulturObjektDokumentViewModel();

      showFacesInfoMessage(
          "kod_externe_ressource_delete_successful",
          "");

      log.info(
          "Deleted external resource: kodId={}, type={}, url={}",
          this.id,
          ressource.typ(),
          ressource.url());

    } catch (Exception error) {
      log.error(
          "Error deleting external resource: kodId={}, type={}, url={}",
          this.id,
          ressource.typ(),
          ressource.url(),
          error);

      showFacesErrorMessage(
          "kod_externe_ressource_delete_error",
          error.getMessage());

    } finally {
      if (!sperreGehoertEinemFormular) {
        kodEntsperren(false);
      }
    }
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void forschungsdokumentationLoeschen(Forschungsdokumentation forschungsdokumentation) {
    boolean sperreGehoertEinemFormular = isBearbeitungAktiv();

    if (!sperreGehoertEinemFormular
        && !kodSperren("KODDeleteForschungsdokumentation",
            getMessage("kod_detail_sperren_exists"))) {
      return;
    }

    try {
      log.info("Delete research documentation: kodId={}, resource={}, link={}",
          this.id, forschungsdokumentation.ressource(), forschungsdokumentation.link());

      kulturObjektDokumentBoundary.forschungsdokumentationLoeschen(
          this.id,
          forschungsdokumentation.ressource(),
          forschungsdokumentation.link(),
          this.sperre);

      mapKulturObjektDokumentViewModel();
      showFacesInfoMessage("kod_forschungsdokumentation_delete_successful", "");
    } catch (Exception error) {
      log.error("Error deleting research documentation: kodId={}, resource={}, link={}",
          this.id,
          forschungsdokumentation.ressource(),
          forschungsdokumentation.link(),
          error);
      showFacesErrorMessage("kod_forschungsdokumentation_delete_error", error.getMessage());
    } finally {
      if (!sperreGehoertEinemFormular) {
        kodEntsperren(false);
      }
    }
  }

  public ExterneRessourceTyp[] getExterneRessourceTypen() {

    Collator collator = Collator.getInstance(getLocale());

    return Arrays.stream(ExterneRessourceTyp.values())
        .sorted((first, second) -> {

          if (first == ExterneRessourceTyp.SONSTIGES) {
            return second == ExterneRessourceTyp.SONSTIGES ? 0 : 1;
          }

          if (second == ExterneRessourceTyp.SONSTIGES) {
            return -1;
          }

          return collator.compare(getMessage(first.getMessageKey()),
              getMessage(second.getMessageKey()));
        })
        .toArray(ExterneRessourceTyp[]::new);
  }

  /**
   * Groups by localized type order. Sorting is stable, so within one type the TEI document order is
   * preserved.
   */
  public List<ExterneRessource> getExterneRessourcen() {

    if (Objects.isNull(kulturObjektDokumentViewModel)) {
      return List.of();
    }

    List<ExterneRessourceTyp> anzeigeReihenfolge =
        Arrays.asList(getExterneRessourceTypen());

    return kulturObjektDokumentViewModel.getExterneRessourcen().stream()
        .sorted(Comparator.comparingInt(
            externeRessource -> anzeigeReihenfolge.indexOf(externeRessource.typ())))
        .toList();
  }

  /**
   * Returns a locale-aware display order without changing the TEI order held by the ViewModel.
   *
   * @return a sorted copy of the Forschungsdokumentationen, or an empty list when no ViewModel is
   *     available
   */
  public List<Forschungsdokumentation> getForschungsdokumentationen() {
    if (Objects.isNull(kulturObjektDokumentViewModel)) {
      return List.of();
    }

    Collator collator = Collator.getInstance(getLocale());
    return kulturObjektDokumentViewModel.getForschungsdokumentationen().stream()
        .sorted((first, second) -> collator.compare(first.ressource(), second.ressource()))
        .toList();
  }

  /**
   * JSF only reaches this method once Process Validations succeeded, so an invalid typ or url never
   * opens the dialog — the field gets {@code ui-state-error} and the message goes to the growl.
   */
  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void externeRessourceBestaetigen() {

    PrimeFaces.current().executeScript("PF('addExterneRessourceConfirm').show()");
  }


  public String addExterneRessourceConfirmMessage() {

    if (Objects.isNull(neueExterneRessourceTyp)) {
      return "";
    }

    return getMessage(
        "kod_externe_ressource_add_confirm",
        getMessage(
            neueExterneRessourceTyp.getMessageKey()),
        neueExterneRessourceUrl);
  }

  public String deleteExterneRessourceConfirmMessage(
      ExterneRessource ressource) {

    if (Objects.isNull(ressource)) {
      return "";
    }

    return getMessage(
        "kod_externe_ressource_delete_confirm",
        getMessage(ressource.typ().getMessageKey()),
        ressource.url());
  }

  public String deleteForschungsdokumentationConfirmMessage() {
    return getMessage("kod_forschungsdokumentation_delete_confirm");
  }

  public boolean isAltIdentifikationDeletable(IdentifikationViewModel alternativeSignatureId) {
    return this.kulturObjektDokumentBoundary.isAltIdentifikationDeletable(
        alternativeSignatureId.id());
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
      FacesContext.getCurrentInstance().addMessage("hspGrowl", msg);
    }
  }

  private void showLocalFacesMessage(
      FacesMessage.Severity severity, String messageKey, String details, String messageID) {
    if (Objects.nonNull(FacesContext.getCurrentInstance())) {
      FacesMessage msg = new FacesMessage(severity, getMessage(messageKey), details);
      FacesContext.getCurrentInstance().addMessage(messageID, msg);
    }
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void deleteAltIdentifikation(String identifikationId) {
    try {
      log.info("Delete AltIdentifikation {} for KOD {}", identifikationId, id);
      this.kulturObjektDokumentBoundary.deleteAltIdentifikation(
          bearbeiterBoundary.getLoggedBearbeiter(), id, identifikationId);
      this.mapKulturObjektDokumentViewModel();
      showFacesInfoMessage("kod_detail_altidentifikation_loeschen_erfolg", "");
    } catch (SperreAlreadyExistException error) {
      log.error("Sperre already exists for KOD {}", id, error);
      handleSperreAlreadyExistException(error, getMessage("kod_detail_sperren_exists"));
    } catch (Exception error) {
      log.error("Error deleting AltIdentifikation {} for KOD {}", identifikationId, id, error);
      showFacesErrorMessage("kod_detail_altidentifikationloeschen_fehler", error.getMessage());
    }
    log.info("Deleted AltIdentifikation {}", identifikationId);
  }


  public String findLabelForConceptScheme(String typeName) {

    log.info("Find Labels for Scheme {} ", typeName);

    if (typeName == null || this.normdatenReferenzBoundary == null) {
      return "";
    }

    return normdatenReferenzBoundary.findConceptSchemesWithType(DIGITIZATION_SCHEME_TYPE).stream()
        .peek(n -> log.info("Found scheme {}", n))
        .filter(c -> c.getName().equals(typeName))
        .findFirst()
        .map(n -> n.getVariantName(getLocale().getLanguage()))
        .orElse("");
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
  public void setImportBoundary(ImportBoundary importBoundary) {
    this.importBoundary = importBoundary;
  }

  @Inject
  public void setKODKerndatenModel(KODKerndatenModel kodKerndatenModel) {
    this.kodKerndatenModel = kodKerndatenModel;
  }

  @Inject
  public void setNormdatenReferenzBoundary(NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }
}
