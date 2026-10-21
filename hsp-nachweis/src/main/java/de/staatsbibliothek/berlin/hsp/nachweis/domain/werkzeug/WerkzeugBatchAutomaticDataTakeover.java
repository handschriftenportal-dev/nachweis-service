package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SchemaPflegeRechte.PERMISSION_AUTOMATISCHE_UEBERNAHME_AKTIVIEREN;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.06.23.
 * @version 1.0
 */
@ApplicationScoped
@Slf4j
public class WerkzeugBatchAutomaticDataTakeover {

  private final ImportBoundary importBoundary;
  @Getter
  private final WerkzeugBatchJob batch = new WerkzeugBatchJob(
      "Adjust automatic manual description data import!");

  @Setter
  private boolean automatischenUebernahmeAktiv;

  private final BearbeiterBoundary bearbeiterBoundary;

  @Inject
  public WerkzeugBatchAutomaticDataTakeover(ImportBoundary importBoundary,
      BearbeiterBoundary bearbeiterBoundary) {
    this.importBoundary = importBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  @CheckPermission(permission = {PERMISSION_AUTOMATISCHE_UEBERNAHME_AKTIVIEREN})
  public void execute(ValueChangeEvent event) {
    log.info("changeAutomatischenUebernahme: value{}", event.getNewValue());
    this.batch.start(bearbeiterBoundary.getLoggedBearbeiter());
    importBoundary.setAutomatischenUebernahmeAktiv(
        Boolean.TRUE.equals(event.getNewValue()));
    this.batch.end("");
  }

  public boolean isAutomatischenUebernahmeAktiv() {
    return importBoundary.isAutomatischenUebernahmeAktiv();
  }
}
