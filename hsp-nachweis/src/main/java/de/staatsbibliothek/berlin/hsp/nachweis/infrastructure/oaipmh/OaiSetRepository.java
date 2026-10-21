package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oaipmh;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzRepository;
import de.staatsbibliothek.berlin.oaipmh.model.OaiSet;
import de.staatsbibliothek.berlin.oaipmh.repository.ClientSetRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class OaiSetRepository implements ClientSetRepository {

  private final NormdatenReferenzRepository repository;

  public OaiSetRepository(NormdatenReferenzRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<OaiSet> getSets() {
    var corporatesBodies = repository.findReferencedCorporateBodies();

    return corporatesBodies.stream()
        .map(
            corporateBody -> {
              var specName = corporateBody.getId();
              return new OaiSet(specName, corporateBody.getName());
            })
        .toList();
  }
}
