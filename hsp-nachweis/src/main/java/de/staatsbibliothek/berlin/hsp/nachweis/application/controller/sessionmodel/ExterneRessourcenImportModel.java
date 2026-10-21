package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResult;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportStateService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.context.ManagedExecutor;

/**
 * Starts and orchestrates the asynchronous external resources import.
 *
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 24.08.2026
 */
@Named
@ViewScoped
@Slf4j
public class ExterneRessourcenImportModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 5144790906320244434L;

  static final String RESULT_FILE_NAME = "externe-ressourcen-import-ergebnis.csv";

  private static final ExterneRessourcenImportErrorMessageKey UNEXPECTED_ERROR_KEY =
      ExterneRessourcenImportErrorMessageKey.UNEXPECTED_ERROR;

  private final ExterneRessourcenImportService externeRessourcenImportService;

  private final ExterneRessourcenImportStateService stateService;

  private final BearbeiterBoundary bearbeiterBoundary;

  private final transient ManagedExecutor managedExecutor;

  @Inject
  public ExterneRessourcenImportModel(
      ExterneRessourcenImportService externeRessourcenImportService,
      ExterneRessourcenImportStateService stateService,
      BearbeiterBoundary bearbeiterBoundary,
      ManagedExecutor managedExecutor) {

    this.externeRessourcenImportService = externeRessourcenImportService;
    this.stateService = stateService;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.managedExecutor = managedExecutor;
  }

  public boolean startImport(byte[] content) {
    Objects.requireNonNull(content, "content is required");

    String bearbeiterId = bearbeiterBoundary.getLoggedBearbeiter().getId();
    byte[] importContent = Arrays.copyOf(content, content.length);
    if (!stateService.tryStartImport(bearbeiterId)) {
      return false;
    }

    try {
      runImport(() -> executeImport(importContent));
    } catch (RuntimeException exception) {
      log.error("Unable to schedule external resources import", exception);
      if (stateService.isRunning()) {
        stateService.finishWithError(UNEXPECTED_ERROR_KEY);
      }
    }
    return true;
  }

  void executeImport(byte[] content) {
    try {
      List<ExterneRessourcenImportResult> results =
          externeRessourcenImportService.importieren(content);
      stateService.finishSuccessfully(results, RESULT_FILE_NAME);

    } catch (ExterneRessourcenImportException exception) {
      log.error("External resources import failed", exception);
      stateService.finishWithError(exception.getMessageKey(), exception.getArgs());

    } catch (Exception exception) {
      log.error("Unexpected external resources import error", exception);
      stateService.finishWithError(UNEXPECTED_ERROR_KEY);

    } catch (Error error) {
      log.error("Fatal external resources import error", error);
      if (stateService.isRunning()) {
        stateService.finishWithError(UNEXPECTED_ERROR_KEY);
      }
      throw error;
    }
  }

  private void runImport(Runnable importTask) {
    if (managedExecutor == null) {
      log.info(
          "ManagedExecutor not available falling to sync execution instead"
              + " - for unittest this is normal behaviour.");
      importTask.run();
    } else {
      managedExecutor.runAsync(importTask);
    }
  }
}
