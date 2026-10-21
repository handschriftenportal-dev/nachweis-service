package de.staatsbibliothek.berlin.oaipmh.repository;

import de.staatsbibliothek.berlin.oaipmh.model.OaiSet;
import java.util.List;

public interface ClientSetRepository {
  List<OaiSet> getSets();
}
