package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.WerkzeugeRechte.PERMISSION_KANN_BESCHREIBUNGEN_INDEX_AKTUALISIEREN;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.sendIndexAllBeschreibungenForNachweis;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.06.23.
 * @version 1.0
 */
@Slf4j
@ApplicationScoped
public class WerkzeugBatchIndexAllBeschreibungen {

  private Event<WerkzeugBatchJob> cdiEvent;
  private BearbeiterBoundary bearbeiterBoundary;
  @Getter
  private WerkzeugBatchJob batch = new WerkzeugBatchJob("Indexing all manual description");

  @Inject
  public WerkzeugBatchIndexAllBeschreibungen(
      @sendIndexAllBeschreibungenForNachweis Event<WerkzeugBatchJob> cdiEvent,
      BearbeiterBoundary bearbeiterBoundary) {
    this.cdiEvent = cdiEvent;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  @CheckPermission(permission = PERMISSION_KANN_BESCHREIBUNGEN_INDEX_AKTUALISIEREN)
  public void execute() {
    log.info("Reindex beschreibungen..");
    batch.start(bearbeiterBoundary.getLoggedBearbeiter());
    cdiEvent.fireAsync(batch);
  }
}
