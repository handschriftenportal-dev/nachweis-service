package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte.PERMISSION_KOD_BEARBEITEN;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessourceTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.ExterneRessourcenImportModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportStateService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResult;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResultCSVWriter;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.Getter;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

/**
 * Provides the JSF actions and view state for starting an external-resources import, polling its
 * status and downloading the localized result CSV.
 *
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 24.08.2026
 */
@Named
@ViewScoped
public class ExterneRessourcenImportController implements Serializable {

  @Serial
  private static final long serialVersionUID = 6103680632568757819L;

  private static final String CONTENT_TYPE_CSV = "text/csv;charset=UTF-8";

  private static final List<String> SUPPORTED_RESOURCE_TYPES =
      Arrays.stream(ExterneRessourceTyp.values())
          .sorted(Comparator.comparing(ExterneRessourceTyp.BESCHREIBUNG::equals))
          .map(type -> type.getTeiType().toUpperCase(Locale.ROOT))
          .toList();

  private final ExterneRessourcenImportModel externeRessourcenImportModel;
  private final ExterneRessourcenImportStateService stateService;
  private final BearbeiterBoundary bearbeiterBoundary;
  private final ExterneRessourcenImportResultCSVWriter resultCSVWriter;

  @Getter
  private boolean polling;

  private Instant handledFailureFinishTime;

  @Inject
  public ExterneRessourcenImportController(
      ExterneRessourcenImportModel externeRessourcenImportModel,
      ExterneRessourcenImportStateService stateService,
      BearbeiterBoundary bearbeiterBoundary,
      ExterneRessourcenImportResultCSVWriter resultCSVWriter) {

    this.externeRessourcenImportModel = externeRessourcenImportModel;
    this.stateService = stateService;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.resultCSVWriter = resultCSVWriter;
  }

  @PostConstruct
  public void init() {
    ExterneRessourcenImportStatus status = getStatus();
    polling = status == ExterneRessourcenImportStatus.RUNNING
        || status == ExterneRessourcenImportStatus.FAILED;
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public void handleFileUpload(FileUploadEvent event) {
    UploadedFile file = event == null ? null : event.getFile();
    byte[] content = file == null ? null : file.getContent();

    if (content == null || content.length == 0) {
      addGlobalMessage(
          FacesMessage.SEVERITY_ERROR,
          I18NController.getMessage("kod_externe_ressourcen_import_file_empty"));
      return;
    }

    if (!externeRessourcenImportModel.startImport(Arrays.copyOf(content, content.length))) {
      addGlobalMessage(
          FacesMessage.SEVERITY_WARN,
          I18NController.getMessage("kod_externe_ressourcen_import_already_running"));
      return;
    }

    handledFailureFinishTime = null;
    polling = true;
  }

  public void checkImportStatus() {
    ExterneRessourcenImportStatus status = getStatus();
    polling = status == ExterneRessourcenImportStatus.RUNNING;

    if (status == ExterneRessourcenImportStatus.FAILED) {
      showFailureOnce();
    }
  }

  public ExterneRessourcenImportStatus getStatus() {
    return stateService.getStatus(getBearbeiterId());
  }

  public String getStatusText() {
    ExterneRessourcenImportStatus status = getStatus();
    if (status == null) {
      return "";
    }

    return I18NController.getMessage(switch (status) {
      case RUNNING -> "kod_externe_ressourcen_import_status_running";
      case SUCCESS -> "kod_externe_ressourcen_import_status_success";
      case FAILED -> "kod_externe_ressourcen_import_status_failed";
    });
  }

  public boolean isRunning() {
    return stateService.isRunning();
  }

  public boolean isSuccessful() {
    return getStatus() == ExterneRessourcenImportStatus.SUCCESS;
  }

  public boolean isFailed() {
    return getStatus() == ExterneRessourcenImportStatus.FAILED;
  }

  public List<String> getSupportedResourceTypes() {
    return SUPPORTED_RESOURCE_TYPES;
  }

  @CheckPermission(permission = PERMISSION_KOD_BEARBEITEN)
  public StreamedContent getResultCsv() {
    String bearbeiterId = getBearbeiterId();
    List<ExterneRessourcenImportResult> results = stateService.getResults(bearbeiterId);
    String resultFileName = stateService.getResultFileName(bearbeiterId);
    if (results == null || resultFileName == null) {
      return null;
    }

    byte[] resultCsv;
    try {
      resultCsv = resultCSVWriter.write(results, I18NController.getLocale());
    } catch (ExterneRessourcenImportException exception) {
      addGlobalMessage(
          FacesMessage.SEVERITY_ERROR,
          I18NController.getMessage("kod_externe_ressourcen_import_result_write_error"));
      return null;
    }

    return DefaultStreamedContent.builder()
        .name(resultFileName)
        .contentType(CONTENT_TYPE_CSV)
        .stream(() -> new ByteArrayInputStream(resultCsv))
        .build();
  }

  private void showFailureOnce() {
    String bearbeiterId = getBearbeiterId();
    Instant finishTime = stateService.getFinishTime(bearbeiterId);
    if (finishTime == null || finishTime.equals(handledFailureFinishTime)) {
      return;
    }

    ExterneRessourcenImportErrorMessageKey messageKey =
        stateService.getErrorMessageKey(bearbeiterId);
    Object[] messageArgs = stateService.getErrorMessageArgs(bearbeiterId);
    addGlobalMessage(
        FacesMessage.SEVERITY_ERROR,
        I18NController.getMessage(
            messageKey.getKey(), messageArgs == null ? new Object[0] : messageArgs));
    handledFailureFinishTime = finishTime;
  }

  private String getBearbeiterId() {
    return bearbeiterBoundary.getLoggedBearbeiter().getId();
  }

  private void addGlobalMessage(FacesMessage.Severity severity, String summary) {
    FacesContext.getCurrentInstance().addMessage(
        "globalmessages", new FacesMessage(severity, summary, null));
  }
}
