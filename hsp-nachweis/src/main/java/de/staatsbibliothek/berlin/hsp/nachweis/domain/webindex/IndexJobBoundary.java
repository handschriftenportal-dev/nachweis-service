package de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.TEIDokument;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;

public interface IndexJobBoundary {

  void startIndexingDocuments(IndexJob indexJob);

  void indexDocument(TEIDokument document, ActivityStreamAction action);
}

