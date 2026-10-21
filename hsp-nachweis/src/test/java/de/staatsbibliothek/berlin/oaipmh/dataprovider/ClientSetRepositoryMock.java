package de.staatsbibliothek.berlin.oaipmh.dataprovider;

import de.staatsbibliothek.berlin.oaipmh.model.OaiSet;
import de.staatsbibliothek.berlin.oaipmh.repository.ClientSetRepository;
import java.util.List;

public class ClientSetRepositoryMock implements ClientSetRepository {

  @Override
  public List<OaiSet> getSets() {
    return List.of(new OaiSet("HSP-1", "A"), new OaiSet("HSP-2", "B"), new OaiSet("HSP-3", "C"));
  }
}
