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
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SchemaPflegeRechte.PERMISSION_AUTOMATISCHE_UEBERNAHME_AKTIVIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SchemaPflegeRechte.PERMISSION_SCHEMAPFLEGEDATEN_ERSETZEN;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.SchemaPflegeDateienModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDateiBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.03.2022
 */

@Slf4j
@ViewScoped
@Named
@LoginCheck
public class SchemaPflegeController implements Serializable {

  private static final long serialVersionUID = 767994729311215102L;

  private static final String VERSION_PATTERN = "([1-9]\\d*)\\.(\\d+)\\.(\\d+)(?:-([a-zA-Z0-9]+))?";

  @Delegate
  private final SchemaPflegeDateienModel schemaPflegeDateienModel;

  private transient BearbeiterBoundary bearbeiterBoundary;
  private transient SchemaPflegeDateiBoundary schemaResourceDateiBoundary;
  private transient ImportBoundary importBoundary;

  @Getter
  @Setter
  private String dateiId;

  @Getter
  @Setter
  private String version;

  @Getter
  @Setter
  private boolean automatischenUebernahmeAktiv;

  @Inject
  SchemaPflegeController(SchemaPflegeDateienModel schemaPflegeDateienModel,
      BearbeiterBoundary bearbeiterBoundary,
      SchemaPflegeDateiBoundary schemaResourceDateiBoundary,
      ImportBoundary importBoundary) {
    this.schemaPflegeDateienModel = schemaPflegeDateienModel;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.schemaResourceDateiBoundary = schemaResourceDateiBoundary;
    this.importBoundary = importBoundary;
  }

  @PostConstruct
  public void init() {
    this.automatischenUebernahmeAktiv = importBoundary.isAutomatischenUebernahmeAktiv();
  }

  public boolean isAuswahlButtonDisabled() {
    return Objects.isNull(dateiId) || dateiId.isEmpty()
        || Objects.isNull(version) || version.isEmpty();
  }

  @CheckPermission(permission = {PERMISSION_SCHEMAPFLEGEDATEN_ERSETZEN})
  public void handleFileUpload(FileUploadEvent event) {

    if (event != null && bearbeiterBoundary.getLoggedBearbeiter() != null) {
      UploadedFile datei = event.getFile();

      try {
        String bearbeitername = bearbeiterBoundary.getLoggedBearbeiter().getBearbeitername();

        boolean success = schemaResourceDateiBoundary.upload(dateiId, version, bearbeitername, datei.getInputStream());
        if (success) {
          sendViewMessage(new FacesMessage(getMessage("schemapflege_upload_erfolg"), ""));
        } else {
          sendViewMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR,
              getMessage("schemapflege_upload_fehler"), ""));
        }
      } catch (Exception e) {
        log.error("Error uploading file for dateiId " + dateiId, e);
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
            getMessage("schemapflege_upload_fehler"),
            e.getMessage());
        sendViewMessage(msg);
      }

      setDateiId(null);
      setVersion(null);
      schemaPflegeDateienModel.setup();

    }
  }

  @CheckPermission(permission = {PERMISSION_AUTOMATISCHE_UEBERNAHME_AKTIVIEREN})
  public void changeAutomatischenUebernahme(ValueChangeEvent event) {
    log.debug("changeAutomatischenUebernahme: value=" + event.getNewValue());
    importBoundary.setAutomatischenUebernahmeAktiv(Boolean.TRUE.equals(event.getNewValue()));
  }

  void sendViewMessage(FacesMessage message) {
    FacesContext context = FacesContext.getCurrentInstance();
    if (context != null) {
      context.addMessage(null, message);
    }
  }

  public String getVersionPattern() {
    return VERSION_PATTERN;
  }

}
