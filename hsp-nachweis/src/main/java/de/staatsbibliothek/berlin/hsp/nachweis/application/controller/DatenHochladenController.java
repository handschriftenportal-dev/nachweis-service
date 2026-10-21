package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_LOKALE_REDAKTEUR_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_NORMDATEN_REDAKTEUR_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_REDAKTEUR_APP;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObjectTag;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamObjectTagId;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.HSPActivityStreamObjectTag;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.DatenHochladenModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DatenDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.XMLFormate;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportUploadDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportVorgangBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportVorgangException;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.05.2020.
 * @version 1.0
 * <p>
 * Controller class to handle the view model and action for JSF Dateiupload HTML presentation.
 */

@Named
@ViewScoped
@LoginCheck
public class DatenHochladenController implements Serializable {

  private static final Logger logger = LoggerFactory.getLogger(DatenHochladenController.class);

  @Serial
  private static final long serialVersionUID = 3130815945279275715L;

  private final Bearbeiter bearbeiter;
  @Getter
  private final Set<DatenDokumentTyp> dokumentTypen = new LinkedHashSet<>();
  private transient ImportVorgangBoundary importVorgangBoundary;
  private transient ImportBoundary importBoundary;
  private DatenHochladenModel datenHochladenModel;
  @Getter
  private boolean uploadSuccessful;
  @Setter
  @Getter
  private String dokumentTyp;
  @Getter
  @Setter
  private boolean hspFormatCheck = false;
  private ImportRechte importRechte;

  DatenHochladenController(BearbeiterBoundary bearbeiterBoundary) {
    this.bearbeiter = bearbeiterBoundary.getLoggedBearbeiter();
  }

  @Inject
  public DatenHochladenController(ImportVorgangBoundary importVorgangBoundary,
      DatenHochladenModel datenHochladenModel, ImportRechte importRechte,
      BearbeiterBoundary bearbeiterBoundary, ImportBoundary importBoundary) {
    this.importVorgangBoundary = importVorgangBoundary;
    this.datenHochladenModel = datenHochladenModel;
    this.importRechte = importRechte;
    this.bearbeiter = bearbeiterBoundary.getLoggedBearbeiter();
    this.importBoundary = importBoundary;
  }

  private static ActivityStreamsDokumentTyp mapToActivityStreamTyp(
      DatenDokumentTyp selectedDokumentTyp) {
    return switch (selectedDokumentTyp) {
      case KOD -> ActivityStreamsDokumentTyp.KOD;
      case BESCHREIBUNG -> ActivityStreamsDokumentTyp.BESCHREIBUNG;
      case KATALOG -> ActivityStreamsDokumentTyp.KATALOG;
      case ORT -> ActivityStreamsDokumentTyp.ORT;
      case KOERPERSCHAFT -> ActivityStreamsDokumentTyp.KOERPERSCHAFT;
      case BEZIEHUNG -> ActivityStreamsDokumentTyp.BEZIEHUNG;
      case DIGITALISAT -> ActivityStreamsDokumentTyp.DIGITALISAT;
      case PERSON -> ActivityStreamsDokumentTyp.PERSON;
      case SPRACHE -> ActivityStreamsDokumentTyp.SPRACHE;
      case KONZEPT -> ActivityStreamsDokumentTyp.KONZEPT;
      case ONTOLOGIE -> ActivityStreamsDokumentTyp.ONTOLOGIE;
      case INITIEN -> ActivityStreamsDokumentTyp.INITIEN;
    };
  }

  public void setup() {
    logger.debug("setup called for DatenHochladenController.");

    initDokumentTypen();

    DatenDokumentTyp selectedTyp = findAndValidateDokumentTyp(this.dokumentTyp);
    datenHochladenModel.setSelectedDokumentTyp(selectedTyp);

    boolean requiresDefaultFormat = (selectedTyp != null
        && selectedTyp != DatenDokumentTyp.BESCHREIBUNG);
    datenHochladenModel.setSelectedXMLFormat(requiresDefaultFormat ? XMLFormate.TEI_ALL : null);
  }

  /**
   * Parses, validates, and checks permissions for the given document type string.
   *
   * @param dokTypString The raw string value from the request.
   * @return The valid DatenDokumentTyp if allowed, otherwise null.
   */
  private DatenDokumentTyp findAndValidateDokumentTyp(String dokTypString) {
    if (dokTypString == null || dokTypString.isBlank() || dokTypString.equalsIgnoreCase("KOD")) {
      return null;
    }

    try {
      DatenDokumentTyp potentialTyp = DatenDokumentTyp.valueOf(
          dokTypString.trim().toUpperCase(Locale.ROOT));
      // Check if the user is allowed to use this type.
      if (isUserAllowedToUseType(potentialTyp)) {
        return potentialTyp;
      } else {
        logger.info("DokumentTyp {} is not allowed.", potentialTyp);
        addWarningMessage();
        return null;
      }
    } catch (IllegalArgumentException e) {
      logger.info("Invalid value for dokumentTyp: {}", dokTypString, e);
      return null;
    }
  }

  private boolean isUserAllowedToUseType(DatenDokumentTyp potentialTyp) {
    return dokumentTypen.contains(potentialTyp);
  }

  private void addWarningMessage() {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    if (facesContext != null) {
      String message = getMessage("dateiupload_selectdatatype_keineberechtigung");
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, message, ""));
    }
  }

  public UploadedFile getDatei() {
    return datenHochladenModel.getDatei();
  }

  @CheckPermission(permission = {
      B_HSP_ERSCHLIESSER_APP,
      B_HSP_NORMDATEN_REDAKTEUR_APP,
      B_HSP_LOKALE_REDAKTEUR_APP,
      B_HSP_REDAKTEUR_APP})
  public void setDatei(UploadedFile datei) {
    datenHochladenModel.setDatei(datei);
  }

  @CheckPermission(permission = {
      B_HSP_ERSCHLIESSER_APP,
      B_HSP_NORMDATEN_REDAKTEUR_APP,
      B_HSP_LOKALE_REDAKTEUR_APP,
      B_HSP_REDAKTEUR_APP})
  public void handleFileUpload(FileUploadEvent event) {
    if (event == null) {
      return;
    }

    UploadedFile datei = event.getFile();
    DatenDokumentTyp selectedDokumentTyp = datenHochladenModel.getSelectedDokumentTyp();
    if (datei == null || selectedDokumentTyp == null) {
      return;
    }
    logger.info("Getting File Upload Event {} ", datei.getFileName());

    datenHochladenModel.setDatei(datei);

    ImportUploadDatei importUploadDatei = new ImportUploadDatei(
        datei.getFileName(),
        datei.getContent(),
        datei.getContentType()
    );

    try {
      final ActivityStreamsDokumentTyp typ = mapToActivityStreamTyp(
          selectedDokumentTyp);

      if (typ == ActivityStreamsDokumentTyp.BESCHREIBUNG) {
        ActivityStreamObjectTag tagHspFormatCheckBeschreibung = new HSPActivityStreamObjectTag(
            "boolean",
            ActivityStreamObjectTagId.DEACTIVATE_HSP_FORMAT_VALIDATION,
            String.valueOf(hspFormatCheck));
        importVorgangBoundary.sendDateiUploadMessageToImportService(importUploadDatei, typ,
            bearbeiter,
            List.of(tagHspFormatCheckBeschreibung));
      } else {
        importVorgangBoundary.sendDateiUploadMessageToImportService(importUploadDatei, typ,
            bearbeiter);
      }

      uploadSuccessful = true;
      setSelectedDokumentTyp(null);
      setSelectedXMLFormat(null);

      FacesMessage msg = new FacesMessage(
          "Datei " + datei.getFileName() + " wurde erfolgreich hochgeladen.");
      sendViewMessage(msg);

    } catch (ImportVorgangException e) {
      logger.error("Fehler beim Versenden der Datei!", e);
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          "Fehler beim Versenden der Datei! ",
          e.getMessage());
      sendViewMessage(msg);
    }


  }

  void sendViewMessage(FacesMessage message) {
    FacesContext context = FacesContext.getCurrentInstance();
    if (context != null) {
      context.addMessage(null, message);
    }
  }

  public void handleViewRequest() {
    this.uploadSuccessful = false;

    if (FacesContext.getCurrentInstance() != null) {
      PrimeFaces.current().ajax().update("successmessage");
    }
  }

  public void handleValueChanged(ValueChangeEvent event) {
    if (event != null) {
      logger.info("Getting File Upload Value Changed Auswahl {}",
          ((UploadedFile) event.getNewValue()).getFileName());
    }
    this.uploadSuccessful = false;
  }

  public DatenDokumentTyp getSelectedDokumentTyp() {
    return datenHochladenModel.getSelectedDokumentTyp();
  }

  public void setSelectedDokumentTyp(DatenDokumentTyp selectedDokumentTyp) {
    if (selectedDokumentTyp == DatenDokumentTyp.BESCHREIBUNG
        && importBoundary.isAutomatischenUebernahmeAktiv()) {
      FacesMessage msg = new FacesMessage(getMessage("dateiupload_autouebernahme_aktiv"), "");
      sendViewMessage(msg);
      return;
    }

    datenHochladenModel.setSelectedDokumentTyp(selectedDokumentTyp);
  }

  public XMLFormate[] getXMLFormate() {
    return XMLFormate.values();
  }

  public XMLFormate getSelectedXMLFormat() {
    return datenHochladenModel.getSelectedXMLFormat();
  }

  public void setSelectedXMLFormat(XMLFormate selectedXMLFormat) {
    datenHochladenModel.setSelectedXMLFormat(selectedXMLFormat);
  }

  public void onChangeDokumententype() {
    logger.debug("Neuer Dokumententyp ausgewählt {} ",
        datenHochladenModel.getSelectedDokumentTyp());
    if (datenHochladenModel.getSelectedDokumentTyp() != DatenDokumentTyp.BESCHREIBUNG) {
      datenHochladenModel.setSelectedXMLFormat(XMLFormate.TEI_ALL);
    }
    this.uploadSuccessful = false;
  }

  public void onChangeXMLFormat() {
    logger.debug("Neues XML Format ausgewählt {} ", datenHochladenModel.getSelectedXMLFormat());
  }

  public void initDokumentTypen() {
    dokumentTypen.clear();
    addDokumentTyp(DatenDokumentTyp.BESCHREIBUNG,
        importRechte.kannBeschreibungManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.KATALOG, importRechte.kannKatalogManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.ORT, importRechte.kannOrtManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.KOERPERSCHAFT,
        importRechte.kannKoerperschaftManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.BEZIEHUNG, importRechte.kannBeziehungManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.DIGITALISAT, importRechte.kannDigitalisatManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.PERSON, importRechte.kannPersonManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.SPRACHE, importRechte.kannSpracheManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.KONZEPT, importRechte.kannKonzeptManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.ONTOLOGIE, importRechte.kannOntologieManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.INITIEN, importRechte.kannInitiumManuellImportieren());
  }

  void addDokumentTyp(DatenDokumentTyp datenDokumentTyp, boolean isAllowed) {
    if (isAllowed) {
      dokumentTypen.add(datenDokumentTyp);
    }
  }

  ImportVorgangBoundary getImportVorgangBoundary() {
    return importVorgangBoundary;
  }

  void setUploadSuccessful(boolean uploadSuccessful) {
    this.uploadSuccessful = uploadSuccessful;
  }

  public boolean isDateiauswahlDisabled() {
    if (!importRechte.kannDateiupload() || Objects.isNull(getSelectedDokumentTyp())) {
      return true;
    }

    if (getSelectedDokumentTyp() == DatenDokumentTyp.BESCHREIBUNG) {
      boolean hasNoFormat = Objects.isNull(getSelectedXMLFormat());
      boolean isAutomaticImport = importBoundary.isAutomatischenUebernahmeAktiv();

      return hasNoFormat || isAutomaticImport; // Disabled if either is true
    }

    return false;
  }
}
