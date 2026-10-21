package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Holds the state of the single application-wide SortKey import. The import runs asynchronously;
 * the outcome (report or failure) is bound to the Bearbeiter who started the import and is only
 * delivered to their sessions — other users merely see that an import is running.
 */
@Slf4j
@ApplicationScoped
public class SortkeyImportStateService {

  /**
   * Safety net against a permanently blocked import slot in case the import thread ends without
   * calling finishWithError (e.g. a JVM anomaly). Must be generously above the longest plausible
   * import duration.
   */
  static final Duration STALE_TIMEOUT = Duration.ofHours(3);

  @Getter
  private volatile SortkeyImportStatus status = SortkeyImportStatus.IDLE;

  private KulturObjektSortkeyUpdateReport report;

  private SortkeyImportFailure failure;

  private String startedByBearbeiterId;

  @Getter
  private volatile Instant startTime;

  public boolean isRunning() {
    return status == SortkeyImportStatus.RUNNING;
  }

  /**
   * Tries to start a new import for the given Bearbeiter.
   *
   * @return true if no import was running and the new import was started, false if another import
   * is already running.
   */
  public synchronized boolean tryStartImport(String bearbeiterId) {

    if (status == SortkeyImportStatus.RUNNING) {

      if (startTime == null
          || Duration.between(startTime, Instant.now()).compareTo(STALE_TIMEOUT) < 0) {
        return false;
      }

      log.warn(
          "SortKey import started at {} by {} exceeded stale timeout of {} - allowing new import.",
          startTime,
          startedByBearbeiterId,
          STALE_TIMEOUT);
    }

    report = null;
    failure = null;
    startedByBearbeiterId = bearbeiterId;
    startTime = Instant.now();
    status = SortkeyImportStatus.RUNNING;

    return true;
  }

  public synchronized void finishSuccessfully(KulturObjektSortkeyUpdateReport report) {

    this.report = report;
    failure = null;
    status = SortkeyImportStatus.SUCCESS;
  }

  public synchronized void finishWithError(int updatedCountBeforeFailure) {

    report = null;
    failure = new SortkeyImportFailure(updatedCountBeforeFailure);
    status = SortkeyImportStatus.FAILED;
  }

  /**
   * Delivers the report of a successful import exactly once to the Bearbeiter who started it;
   * afterwards the state is IDLE. For other Bearbeiter and in every other state the state is left
   * untouched and null is returned.
   */
  public synchronized KulturObjektSortkeyUpdateReport consumeReport(String bearbeiterId) {

    if (status != SortkeyImportStatus.SUCCESS
        || !Objects.equals(startedByBearbeiterId, bearbeiterId)) {
      return null;
    }

    KulturObjektSortkeyUpdateReport result = report;
    reset();

    return result;
  }

  /**
   * Delivers the failure result of a failed import exactly once to the Bearbeiter who started it;
   * afterwards the state is IDLE. For other Bearbeiter and in every other state the state is left
   * untouched and null is returned.
   */
  public synchronized SortkeyImportFailure consumeFailure(String bearbeiterId) {

    if (status != SortkeyImportStatus.FAILED
        || !Objects.equals(startedByBearbeiterId, bearbeiterId)) {
      return null;
    }

    SortkeyImportFailure result = failure;
    reset();

    return result;
  }

  private void reset() {
    report = null;
    failure = null;
    startedByBearbeiterId = null;
    startTime = null;
    status = SortkeyImportStatus.IDLE;
  }
}
