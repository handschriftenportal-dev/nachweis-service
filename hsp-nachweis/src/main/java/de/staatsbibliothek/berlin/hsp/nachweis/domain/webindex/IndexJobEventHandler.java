package de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.sendIndexAllForPraesentation;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.sendIndexJobEvent;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchJob;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class IndexJobEventHandler implements Serializable {

  @Serial
  private static final long serialVersionUID = -3370632423451641027L;

  private final IndexJobBoundary indexJobService;

  @Inject
  public IndexJobEventHandler(IndexJobBoundary indexJobService) {
    this.indexJobService = indexJobService;
  }

  public void onEvent(@ObservesAsync @sendIndexJobEvent IndexJob indexJob) {

    indexJobService.startIndexingDocuments(indexJob);
  }

  public void onWerkzeugIndexPraesentation(
      @ObservesAsync @sendIndexAllForPraesentation WerkzeugBatchJob batch) {
    try {
      String actorName = Optional.ofNullable(batch.getBearbeiter())
          .map(Bearbeiter::getName)
          .orElse(BearbeiterBoundary.SYSTEM_USERNAME);
      indexJobService.startIndexingDocuments(new IndexJob(actorName, IndexJobDocumentType.ALL));
      batch.end("All indexing messages sent to the presentation!");
    } catch (Exception error) {
      log.error("Error during indexing the presentation", error);
      batch.fail(error.getMessage());
    }
  }
}
