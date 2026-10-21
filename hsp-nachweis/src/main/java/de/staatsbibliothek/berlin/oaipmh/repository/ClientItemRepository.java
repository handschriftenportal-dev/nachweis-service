package de.staatsbibliothek.berlin.oaipmh.repository;

import de.staatsbibliothek.berlin.oaipmh.model.OaiItem;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ClientItemRepository {

  Optional<OaiItem> findById(String id);

  List<OaiItem> findAll();

  List<OaiItem> findWithFilters(
      Instant from, Instant until, String setSpec, int maxResponseLength, int offset);

  long countWithFilters(Instant from, Instant until, String setSpec);
}
