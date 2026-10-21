package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.WerkzeugeRechte.PERMISSION_KANN_NORMDATEN_AKTUALISIEREN;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import jakarta.ejb.Asynchronous;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 11.04.24.
 * @version 1.0
 */
@ApplicationScoped
@Slf4j
public class WerkzeugBatchUpdateNormdatenReferenz {

  private final BearbeiterBoundary bearbeiterBoundary;

  private final NormdatenReferenzBoundary normdatenReferenzBoundary;

  @Getter
  private final WerkzeugBatchJob batch =
      new WerkzeugBatchJob("Aktualisierung aller Normdatenreferenzen");

  @Inject
  public WerkzeugBatchUpdateNormdatenReferenz(
      BearbeiterBoundary bearbeiterBoundary, NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @Asynchronous
  @CheckPermission(permission = PERMISSION_KANN_NORMDATEN_AKTUALISIEREN)
  public void execute() {

    batch.start(bearbeiterBoundary.getLoggedBearbeiter());

    normdatenReferenzBoundary.updateAllNormdatenReferenzen();

    batch.end("Successfully updated all Normdatenreferenzen");
  }
}
