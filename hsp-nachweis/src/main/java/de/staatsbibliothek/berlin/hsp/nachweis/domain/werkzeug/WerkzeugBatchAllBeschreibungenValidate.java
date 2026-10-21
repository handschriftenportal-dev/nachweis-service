package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungValidationReportItem;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 08.04.24.
 * @version 1.0
 */

@ApplicationScoped
@Slf4j
public class WerkzeugBatchAllBeschreibungenValidate {

  public static final String MSG_ODD_JOB_TITLE   = "werkzeug_beschreibungen_odd_job_title";
  public static final String MSG_ALL_JOB_TITLE   = "werkzeug_beschreibungen_all_job_title";

  public static final String MSG_ODD_SOME_INVALID = "werkzeug_beschreibungen_odd_some_invalid";
  public static final String MSG_ALL_SOME_INVALID = "werkzeug_beschreibungen_all_some_invalid";

  public static final String MSG_ODD_ALL_VALID    = "werkzeug_beschreibungen_odd_all_valid";
  public static final String MSG_ALL_ALL_VALID    = "werkzeug_beschreibungen_all_all_valid";

  public static final String MSG_ODD_FAILED       = "werkzeug_beschreibungen_odd_validation_failed";
  public static final String MSG_ALL_FAILED       = "werkzeug_beschreibungen_all_validation_failed";

  public static final String MSG_ODD_ABORTED      = "werkzeug_beschreibungen_odd_validation_aborted";
  public static final String MSG_ALL_ABORTED      = "werkzeug_beschreibungen_all_validation_aborted";

  private final BeschreibungsBoundary beschreibungsBoundary;
  private final BearbeiterBoundary bearbeiterBoundary;

  @Getter
  private final WerkzeugBatchJob batchOdd;

  @Getter
  private final WerkzeugBatchJob batchAll;

  enum Mode { ODD, ALL }

  @Inject
  public WerkzeugBatchAllBeschreibungenValidate(BeschreibungsBoundary beschreibungsBoundary,
      BearbeiterBoundary bearbeiterBoundary) {
    this.beschreibungsBoundary = beschreibungsBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.batchOdd = new WerkzeugBatchJob(MSG_ODD_JOB_TITLE);
    this.batchAll = new WerkzeugBatchJob(MSG_ALL_JOB_TITLE);
  }

  public void startAndDispatchODD() {
    runAsync(Mode.ODD);
  }

  public void startAndDispatchALL() {
    runAsync(Mode.ALL);
  }

  private void runAsync(Mode mode) {
    var batch = getBatch(mode);
    synchronized (batch) {
      if (batch.getStatus() != WerkzeugBatchJobStatus.RUNNING) {
        batch.start(bearbeiterBoundary.getLoggedBearbeiter());
        CompletableFuture.runAsync(() -> execute(mode));
      } else {
        log.debug("Batch {} already RUNNING -> ignored start.", batch);
      }
    }
  }

  void execute(Mode mode) {
    var batch = getBatch(mode);

    if (batch.getStatus() != WerkzeugBatchJobStatus.RUNNING) {
      batch.start(bearbeiterBoundary.getLoggedBearbeiter());
    }

    try {
      Set<BeschreibungValidationReportItem> report =
          (mode == Mode.ODD)
              ? beschreibungsBoundary.findAllInvalidODD()
              : beschreibungsBoundary.findAllInvalidALL();

      if (report.isEmpty()) {
        batch.end(
            (mode == Mode.ODD)
                ? MSG_ODD_ALL_VALID
                : MSG_ALL_ALL_VALID
        );
      } else {
        String csvWithBom = "\uFEFF" + buildCsv(report);
        log.info("Invalid Beschreibungen count={}, mode={}", report.size(), mode);
        batch.endWithContent(
            (mode == Mode.ODD)
                ? MSG_ODD_SOME_INVALID
                : MSG_ALL_SOME_INVALID,
            csvWithBom.getBytes(StandardCharsets.UTF_8)
        );
      }

    } catch (Exception e) {
      String msgKey = (mode == Mode.ODD) ? MSG_ODD_FAILED : MSG_ALL_FAILED;
      log.error("{}: {}", msgKey, e.getMessage(), e);
      batch.fail(msgKey);

    } finally {
      if (batch.getStatus() == WerkzeugBatchJobStatus.RUNNING) {
        batch.fail(
            (mode == Mode.ODD) ? MSG_ODD_ABORTED : MSG_ALL_ABORTED
        );
      }
    }
  }

  void executeInternalForTests(Mode mode) {
    execute(mode);
  }

  private WerkzeugBatchJob getBatch(Mode mode) {
    return (mode == Mode.ODD) ? batchOdd : batchAll;
  }

  private String buildCsv(Set<BeschreibungValidationReportItem> report) {
    final String NL = "\r\n";
    StringBuilder csv = new StringBuilder();
    csv.append("HSP-ID-Beschreibung;Signatur;Bestandshaltende Einrichtung;Ort;Fehlermeldung")
        .append(NL);
    report.forEach(r -> csv.append(r.toCSV(Locale.GERMAN)).append(NL));
    return csv.toString();
  }
}
