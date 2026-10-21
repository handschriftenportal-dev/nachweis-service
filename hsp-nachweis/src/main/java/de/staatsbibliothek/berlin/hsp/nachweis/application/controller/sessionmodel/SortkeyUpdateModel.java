package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.KulturObjektSortkeyUpdateReport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyImportStateService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyPartialImportException;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.FacesMessage.Severity;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 01.06.2026
 */

@ViewScoped
@Named
@Slf4j
public class SortkeyUpdateModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 3609367491144278679L;
  private static final String DEFAULT_CSV_NAME = "upload.csv";
  private final KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  private final SortkeyImportStateService sortkeyImportStateService;
  private final BearbeiterBoundary bearbeiterBoundary;
  private final transient ManagedExecutor managedExecutor;
  @Setter
  @Getter
  private transient KulturObjektSortkeyUpdateReport report;
  @Getter
  @Setter
  private boolean renderProgressbar;

  @Inject
  public SortkeyUpdateModel(
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      SortkeyImportStateService sortkeyImportStateService,
      BearbeiterBoundary bearbeiterBoundary,
      ManagedExecutor managedExecutor) {

    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
    this.sortkeyImportStateService = sortkeyImportStateService;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.managedExecutor = managedExecutor;
  }

  public void handleSortkeysUpload(FileUploadEvent event) {

    report = null;

    if (event == null || event.getFile() == null) {
      addGlobalMessage(
          FacesMessage.SEVERITY_ERROR,
          I18NController.getMessage("kod_sortkeys_file_empty")
      );
      return;
    }

    String bearbeiterId = bearbeiterBoundary.getLoggedBearbeiter().getId();

    if (!sortkeyImportStateService.tryStartImport(bearbeiterId)) {
      addGlobalMessage(
          FacesMessage.SEVERITY_WARN,
          I18NController.getMessage("kod_sortkeys_import_running")
      );
      return;
    }

    UploadedFile file = event.getFile();
    var fileName =
        StringUtils.isNotBlank(file.getFileName())
            ? file.getFileName()
            : DEFAULT_CSV_NAME;
    byte[] content = file.getContent();

    renderProgressbar = true;

    runImport(() -> executeImport(content, fileName));

    addGlobalMessage(
        FacesMessage.SEVERITY_INFO,
        I18NController.getMessage("kod_sortkeys_import_started")
    );
  }

  /**
   * Runs the import and releases the import slot in EVERY case — including Errors — so the
   * application-wide status can never get stuck on RUNNING. Runs on the executor thread: there is
   * no FacesContext here, user feedback is delivered by the poll via the StateService.
   */
  void executeImport(byte[] content, String fileName) {

    boolean finished = false;
    try {
      KulturObjektSortkeyUpdateReport result =
          kulturObjektDokumentBoundary.updateSortkeys(content, fileName);

      sortkeyImportStateService.finishSuccessfully(result);
      finished = true;

    } catch (SortkeyPartialImportException exception) {

      log.error("Sortkey update aborted after partial import", exception);
      sortkeyImportStateService.finishWithError(exception.getUpdatedCountBeforeFailure());
      finished = true;

    } catch (Exception exception) {

      log.error("Sortkey update failed", exception);
      sortkeyImportStateService.finishWithError(0);
      finished = true;

    } finally {
      if (!finished) {
        sortkeyImportStateService.finishWithError(0);
      }
    }
  }

  private void runImport(Runnable importTask) {

    if (managedExecutor == null) {
      log.info(
          "ManagedExecutor not available falling to sync execution instead"
              + " - for unittest ist this normal behaviour.");
      importTask.run();
    } else {
      managedExecutor.runAsync(importTask);
    }
  }

  private void addGlobalMessage(Severity severity, String summary) {
    FacesContext context = FacesContext.getCurrentInstance();
    context.addMessage(
        "globalmessages",
        new FacesMessage(severity, summary, null)
    );
  }
}
