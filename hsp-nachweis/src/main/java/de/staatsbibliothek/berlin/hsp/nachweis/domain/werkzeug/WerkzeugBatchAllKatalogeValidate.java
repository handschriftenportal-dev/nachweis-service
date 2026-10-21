package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogValidationReportItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class WerkzeugBatchAllKatalogeValidate {

  public static final String MSG_JOB_TITLE          = "werkzeug_kataloge_validierung_job_title";
  public static final String MSG_SOME_INVALID       = "werkzeug_kataloge_validation_some_invalid";
  public static final String MSG_ALL_VALID          = "werkzeug_kataloge_validation_all_valid";
  public static final String MSG_FAILED             = "werkzeug_kataloge_validation_failed";
  public static final String MSG_ABORTED            = "werkzeug_kataloge_validation_aborted";

  private final KatalogBoundary katalogBoundary;
  private final BearbeiterBoundary bearbeiterBoundary;

  @Getter
  private final WerkzeugBatchJob batch;

  @Inject
  public WerkzeugBatchAllKatalogeValidate(KatalogBoundary katalogBoundary,
      BearbeiterBoundary bearbeiterBoundary) {
    this.katalogBoundary = katalogBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.batch = new WerkzeugBatchJob(MSG_JOB_TITLE);
  }

  public void startAndDispatch() {
    if (batch.getStatus() != WerkzeugBatchJobStatus.RUNNING) {
      batch.start(bearbeiterBoundary.getLoggedBearbeiter());
    }
    CompletableFuture.runAsync(this::execute);
  }

  public void execute() {
    if (batch.getStatus() != WerkzeugBatchJobStatus.RUNNING) {
      batch.start(bearbeiterBoundary.getLoggedBearbeiter());
    }

    try {
      Set<KatalogValidationReportItem> report = katalogBoundary.findAllInvalid();

      if (report.isEmpty()) {
        batch.end(MSG_ALL_VALID);
      } else {
        Locale locale = Locale.GERMAN;
        String csvWithBom = "\uFEFF" + buildCsv(report, locale);
        log.info("Invalid Katalog count={}", report.size());
        batch.endWithContent(
            MSG_SOME_INVALID,
            csvWithBom.getBytes(StandardCharsets.UTF_8)
        );
      }

    } catch (Exception exception) {
      log.error("Validierung fehlgeschlagen: {}", exception.getMessage(), exception);
      batch.fail(MSG_FAILED);
    } finally {
      if (batch.getStatus() == WerkzeugBatchJobStatus.RUNNING) {
        batch.fail(MSG_ABORTED);
      }
    }
  }

  private String buildCsv(Set<KatalogValidationReportItem> report, Locale locale) {
    final String NL = "\r\n";

    StringBuilder csv = new StringBuilder();
    csv.append("HSP-ID;Titel;Publikationsjahr;Fehlermeldung")
        .append(NL);

    report.forEach(r -> csv.append(r.toCSV(locale)).append(NL));
    return csv.toString();
  }
}