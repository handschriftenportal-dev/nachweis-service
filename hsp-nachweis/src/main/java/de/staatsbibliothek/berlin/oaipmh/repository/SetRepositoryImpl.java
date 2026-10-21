package de.staatsbibliothek.berlin.oaipmh.repository;

import io.gdcc.xoai.dataprovider.model.Set;
import io.gdcc.xoai.dataprovider.repository.SetRepository;
import java.util.List;

public class SetRepositoryImpl implements SetRepository {

  private final ClientSetRepository clientSetRepository;

  public SetRepositoryImpl(ClientSetRepository clientSetRepository) {
    this.clientSetRepository = clientSetRepository;
  }

  @Override
  public List<Set> getSets() {
    return clientSetRepository.getSets().stream()
        .map(s -> new Set(s.specName()).withName(s.name()))
        .toList();
  }

  @Override
  public boolean supportSets() {
    return true;
  }

  @Override
  public boolean exists(String setSpec) {
    return clientSetRepository.getSets().stream().anyMatch(s -> s.specName().equals(setSpec));
  }
}
