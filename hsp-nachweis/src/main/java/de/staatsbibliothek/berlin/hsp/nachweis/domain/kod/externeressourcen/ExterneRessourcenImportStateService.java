package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportStatus.FAILED;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportStatus.RUNNING;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportStatus.SUCCESS;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Holds the application-wide execution state and completed results of external-resources imports.
 *
 * <p>Only one import can be {@link ExterneRessourcenImportStatus#RUNNING} at a time for the entire
 * application. A completed success or failure is retained separately for the Bearbeiter who
 * started it, so that a page reload or a new view can still access that Bearbeiter's latest result.
 * The previous completed result of a Bearbeiter is replaced when that Bearbeiter starts another
 * import or explicitly resets it.
 *
 * <p>Successful imports are stored as language-neutral {@link ExterneRessourcenImportResult}
 * objects. The result CSV is generated and localized only when it is downloaded. All state access
 * is synchronized to coordinate the JSF request thread and the asynchronous import thread.
 *
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 24.08.2026
 */
@ApplicationScoped
public class ExterneRessourcenImportStateService {

  private final Map<String, CompletedImport> completedImports = new HashMap<>();

  private String runningBearbeiterId;

  private Instant runningStartTime;

  public synchronized boolean isRunning() {
    return runningBearbeiterId != null;
  }

  public synchronized boolean tryStartImport(String bearbeiterId) {
    Objects.requireNonNull(bearbeiterId, "bearbeiterId is required");

    if (isRunning()) {
      return false;
    }

    completedImports.remove(bearbeiterId);
    runningBearbeiterId = bearbeiterId;
    runningStartTime = Instant.now();
    return true;
  }

  public synchronized void finishSuccessfully(
      List<ExterneRessourcenImportResult> results, String resultFileName) {
    if (!isRunning()) {
      throw new IllegalStateException("No external resources import is running");
    }

    completedImports.put(
        runningBearbeiterId,
        new CompletedImport(
            SUCCESS,
            runningStartTime,
            Instant.now(),
            List.copyOf(results),
            resultFileName,
            null,
            null));
    clearRunningImport();
  }

  public synchronized void finishWithError(
      ExterneRessourcenImportErrorMessageKey messageKey, Object... args) {
    if (!isRunning()) {
      throw new IllegalStateException("No external resources import is running");
    }

    completedImports.put(
        runningBearbeiterId,
        new CompletedImport(
            FAILED,
            runningStartTime,
            Instant.now(),
            null,
            null,
            messageKey,
            args == null ? null : Arrays.copyOf(args, args.length)));
    clearRunningImport();
  }

  public synchronized ExterneRessourcenImportStatus getStatus(String bearbeiterId) {
    CompletedImport completedImport = completedImports.get(bearbeiterId);
    if (completedImport != null) {
      return completedImport.status();
    }
    return isRunning() ? RUNNING : null;
  }

  public synchronized Instant getStartTime(String bearbeiterId) {
    CompletedImport completedImport = completedImports.get(bearbeiterId);
    if (completedImport != null) {
      return completedImport.startTime();
    }
    return Objects.equals(runningBearbeiterId, bearbeiterId) ? runningStartTime : null;
  }

  public synchronized Instant getFinishTime(String bearbeiterId) {
    CompletedImport completedImport = completedImports.get(bearbeiterId);
    return completedImport == null ? null : completedImport.finishTime();
  }

  public synchronized List<ExterneRessourcenImportResult> getResults(String bearbeiterId) {
    CompletedImport completedImport = completedImports.get(bearbeiterId);
    if (completedImport == null || completedImport.status() != SUCCESS) {
      return null;
    }
    return completedImport.results();
  }

  public synchronized String getResultFileName(String bearbeiterId) {
    CompletedImport completedImport = completedImports.get(bearbeiterId);
    if (completedImport == null || completedImport.status() != SUCCESS) {
      return null;
    }
    return completedImport.resultFileName();
  }

  public synchronized ExterneRessourcenImportErrorMessageKey getErrorMessageKey(
      String bearbeiterId) {
    CompletedImport completedImport = completedImports.get(bearbeiterId);
    if (completedImport == null || completedImport.status() != FAILED) {
      return null;
    }
    return completedImport.errorMessageKey();
  }

  public synchronized Object[] getErrorMessageArgs(String bearbeiterId) {
    CompletedImport completedImport = completedImports.get(bearbeiterId);
    if (completedImport == null
        || completedImport.status() != FAILED
        || completedImport.errorMessageArgs() == null) {
      return null;
    }
    return Arrays.copyOf(
        completedImport.errorMessageArgs(), completedImport.errorMessageArgs().length);
  }

  public synchronized void reset(String bearbeiterId) {
    completedImports.remove(bearbeiterId);
  }

  private void clearRunningImport() {
    runningBearbeiterId = null;
    runningStartTime = null;
  }

  private record CompletedImport(
      ExterneRessourcenImportStatus status,
      Instant startTime,
      Instant finishTime,
      List<ExterneRessourcenImportResult> results,
      String resultFileName,
      ExterneRessourcenImportErrorMessageKey errorMessageKey,
      Object[] errorMessageArgs) {
  }
}
