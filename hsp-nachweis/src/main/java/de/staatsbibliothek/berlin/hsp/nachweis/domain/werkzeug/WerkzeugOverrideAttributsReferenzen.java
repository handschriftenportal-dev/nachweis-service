package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.WerkzeugeRechte.PERMISSION_KANN_KOD_KERNDATEN_BEI_IMPORT_UEBERSCHREIBEN;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.konfiguration.KonfigurationBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.03.25.
 * @version 1.0
 */

@ApplicationScoped
@Slf4j
public class WerkzeugOverrideAttributsReferenzen {

  public static final String ATTRIBUTSREFERENZEN_OVERRIDE = "attributsreferenzen.override";
  private final KonfigurationBoundary konfigurationBoundary;
  private final BearbeiterBoundary bearbeiterBoundary;
  @Getter
  private final WerkzeugBatchJob batch = new WerkzeugBatchJob(
      "Set override attributes for Kulturobjektdokument Attributsferenzen");

  @Inject
  public WerkzeugOverrideAttributsReferenzen(
      KonfigurationBoundary konfigurationBoundary,
      BearbeiterBoundary bearbeiterBoundary) {
    this.konfigurationBoundary = konfigurationBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  public boolean isAttributsreferenzenOverride() {
    return konfigurationBoundary.getWert(ATTRIBUTSREFERENZEN_OVERRIDE)
        .map(Boolean::valueOf).orElse(false);
  }

  @CheckPermission(permission = {PERMISSION_KANN_KOD_KERNDATEN_BEI_IMPORT_UEBERSCHREIBEN})
  public void setAttributsreferenzenOverride(boolean value) {
    log.info("Setting attributsreferenzen override to: {}", value);
    this.batch.start(bearbeiterBoundary.getLoggedBearbeiter());
    konfigurationBoundary.setWert(ATTRIBUTSREFERENZEN_OVERRIDE, String.valueOf(value));
    this.batch.end("Override Attributesreferenzen: " + value);
  }
}
