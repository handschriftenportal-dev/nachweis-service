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
import static de.staatsbibliothek.berlin.hsp.nachweis.application.model.DatenDokumentTyp.BESCHREIBUNG;
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
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportUploadDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgangBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgangException;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
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

  private static final long serialVersionUID = 3130815945279275715L;

  private final Bearbeiter bearbeiter;
  private transient ImportVorgangBoundary importVorgangBoundary;
  private transient ImportBoundary importBoundary;

  private DatenHochladenModel datenHochladenModel;
  private boolean uploadSuccessful;
  private Set<DatenDokumentTyp> dokumentTypen = new LinkedHashSet<>();
  private String dokumentTyp;
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

  public void setup() {
    logger.debug("setup called for DatenHochladenController.");

    initDokumentTypen();

    if (Objects.nonNull(dokumentTyp) && !dokumentTyp.isBlank()
        && !dokumentTyp.equalsIgnoreCase(DatenDokumentTyp.KOD.toString())) {
      try {
        DatenDokumentTyp selectedDokumentTyp = DatenDokumentTyp.valueOf(dokumentTyp.trim().toUpperCase(Locale.ROOT));
        if (dokumentTypen.contains(selectedDokumentTyp)) {
          datenHochladenModel.setSelectedDokumentTyp(selectedDokumentTyp);
        } else {
          logger.info("SelectedDokumentTyp {} is not allowed.", selectedDokumentTyp);

          FacesContext facesContext = FacesContext.getCurrentInstance();
          if (FacesContext.getCurrentInstance() != null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                getMessage("dateiupload_selectdatatype_keineberechtigung"), ""));
          }

          datenHochladenModel.setSelectedDokumentTyp(null);
        }
      } catch (IllegalArgumentException iae) {
        logger.info("Invalid value for dokumentTyp: " + dokumentTyp, iae);
        datenHochladenModel.setSelectedDokumentTyp(null);
      }
    } else {
      datenHochladenModel.setSelectedDokumentTyp(null);
    }

    if (datenHochladenModel.getSelectedDokumentTyp() != null
        && BESCHREIBUNG != datenHochladenModel.getSelectedDokumentTyp()) {
      datenHochladenModel.setSelectedXMLFormat(XMLFormate.TEI_ALL);
    } else {
      datenHochladenModel.setSelectedXMLFormat(null);
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
    if (event != null) {
      UploadedFile datei = event.getFile();
      DatenDokumentTyp selectedDokumentTyp = datenHochladenModel.getSelectedDokumentTyp();
      if (datei != null && selectedDokumentTyp != null) {
        logger.info("Getting File Upload Event {} ", datei.getFileName());

        datenHochladenModel.setDatei(datei);

        ImportUploadDatei importUploadDatei = new ImportUploadDatei(datei.getFileName(),
            datei.getContent(),
            datei.getContentType());

        try {

          ActivityStreamsDokumentTyp typ = null;

          switch (selectedDokumentTyp) {
            case KOD:
              typ = ActivityStreamsDokumentTyp.KOD;
              break;
            case BESCHREIBUNG:
              typ = ActivityStreamsDokumentTyp.BESCHREIBUNG;
              break;
            case KATALOG:
              typ = ActivityStreamsDokumentTyp.KATALOG;
              break;
            case ORT:
              typ = ActivityStreamsDokumentTyp.ORT;
              break;
            case KOERPERSCHAFT:
              typ = ActivityStreamsDokumentTyp.KOERPERSCHAFT;
              break;
            case BEZIEHUNG:
              typ = ActivityStreamsDokumentTyp.BEZIEHUNG;
              break;
            case DIGITALISAT:
              typ = ActivityStreamsDokumentTyp.DIGITALISAT;
              break;
            case PERSON:
              typ = ActivityStreamsDokumentTyp.PERSON;
              break;
            case SPRACHE:
              typ = ActivityStreamsDokumentTyp.SPRACHE;
              break;
            default:
              logger.warn("Unhandled ActivityStreamsDokumentTyp= '{}'", selectedDokumentTyp);
              break;
          }

          if (ActivityStreamsDokumentTyp.BESCHREIBUNG.equals(typ)) {
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
          sendViewMessage(null, msg);

        } catch (ImportVorgangException e) {
          logger.error("Fehler beim Versenden der Datei!", e);
          FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
              "Fehler beim Versenden der Datei! ",
              e.getMessage());
          sendViewMessage(null, msg);
        }

      }
    }

  }

  void sendViewMessage(String clientid, FacesMessage message) {

    FacesContext context = FacesContext.getCurrentInstance();

    if (context != null) {
      context.addMessage(clientid, message);
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
    if (BESCHREIBUNG == selectedDokumentTyp && importBoundary.isAutomatischenUebernahmeAktiv()) {
      FacesMessage msg = new FacesMessage(getMessage("dateiupload_autouebernahme_aktiv"), "");
      sendViewMessage(null, msg);
      return;
    }

    datenHochladenModel.setSelectedDokumentTyp(selectedDokumentTyp);
  }

  public Set<DatenDokumentTyp> getDokumentTypen() {
    return dokumentTypen;
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
    if (BESCHREIBUNG != datenHochladenModel.getSelectedDokumentTyp()) {
      datenHochladenModel.setSelectedXMLFormat(XMLFormate.TEI_ALL);
    }
    this.uploadSuccessful = false;
  }

  public void onChangeXMLFormat() {
    logger.debug("Neues XML Format ausgewählt {} ", datenHochladenModel.getSelectedXMLFormat());
  }

  public void initDokumentTypen() {
    dokumentTypen.clear();
    addDokumentTyp(DatenDokumentTyp.BESCHREIBUNG, importRechte.kannBeschreibungManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.KATALOG, importRechte.kannKatalogManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.ORT, importRechte.kannOrtManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.KOERPERSCHAFT, importRechte.kannKoerperschaftManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.BEZIEHUNG, importRechte.kannBeziehungManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.DIGITALISAT, importRechte.kannDigitalisatManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.PERSON, importRechte.kannPersonManuellImportieren());
    addDokumentTyp(DatenDokumentTyp.SPRACHE, importRechte.kannSpracheManuellImportieren());
  }

  void addDokumentTyp(DatenDokumentTyp datenDokumentTyp, boolean isAllowed) {
    if (isAllowed) {
      dokumentTypen.add(datenDokumentTyp);
    }
  }

  ImportVorgangBoundary getImportVorgangBoundary() {
    return importVorgangBoundary;
  }

  public boolean isUploadSuccessful() {
    return uploadSuccessful;
  }

  void setUploadSuccessful(boolean uploadSuccessful) {
    this.uploadSuccessful = uploadSuccessful;
  }

  public boolean isHspFormatCheck() {
    return hspFormatCheck;
  }

  public void setHspFormatCheck(boolean hspFormatCheck) {
    this.hspFormatCheck = hspFormatCheck;
  }

  public boolean isDateiauswahlDisabled() {
    if (importRechte.kannDateiupload()
        && Objects.nonNull(getSelectedDokumentTyp())
        && (BESCHREIBUNG != getSelectedDokumentTyp() || Objects.nonNull(getSelectedXMLFormat()))) {
      return isBeschreibungUploadDisabled();
    }
    return true;
  }

  private boolean isBeschreibungUploadDisabled() {
    return importBoundary.isAutomatischenUebernahmeAktiv()
        && BESCHREIBUNG == getSelectedDokumentTyp();
  }

  public String getDokumentTyp() {
    return dokumentTyp;
  }

  public void setDokumentTyp(String dokumentTyp) {
    this.dokumentTyp = dokumentTyp;
  }
}
