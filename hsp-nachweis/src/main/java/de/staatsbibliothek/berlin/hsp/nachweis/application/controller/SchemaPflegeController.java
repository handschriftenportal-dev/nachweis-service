package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SchemaPflegeRechte.PERMISSION_SCHEMAPFLEGEDATEN_ERSETZEN;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.SchemaPflegeDateienModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDateiBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Objects;
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

  @Getter
  @Setter
  private String dateiId;

  @Getter
  @Setter
  private String version;

  @Inject
  SchemaPflegeController(SchemaPflegeDateienModel schemaPflegeDateienModel,
      BearbeiterBoundary bearbeiterBoundary,
      SchemaPflegeDateiBoundary schemaResourceDateiBoundary) {
    this.schemaPflegeDateienModel = schemaPflegeDateienModel;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.schemaResourceDateiBoundary = schemaResourceDateiBoundary;
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

        boolean success = schemaResourceDateiBoundary.upload(dateiId, version, bearbeitername,
            datei.getInputStream());
        if (success) {
          sendViewMessage(new FacesMessage(getMessage("schemapflege_upload_erfolg"), ""));
        } else {
          sendViewMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR,
              getMessage("schemapflege_upload_fehler"), "Upload fehlgeschlagen!"));
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
