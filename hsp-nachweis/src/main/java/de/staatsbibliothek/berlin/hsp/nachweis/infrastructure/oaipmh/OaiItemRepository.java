package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oaipmh;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.OaiPmhRepositoryAdapter;
import de.staatsbibliothek.berlin.oaipmh.model.OaiItem;
import de.staatsbibliothek.berlin.oaipmh.repository.ClientItemRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class OaiItemRepository implements ClientItemRepository {

  private static final ZoneId ZONE_ID = ZoneId.systemDefault();
  private final OaiPmhRepositoryAdapter repository;

  @Inject
  public OaiItemRepository(OaiPmhRepositoryAdapter repository) {
    this.repository = repository;
  }

  @Override
  public Optional<OaiItem> findById(String id) {
    var item = repository.findById(id);
    if (Objects.isNull(item)) {
      return Optional.empty();
    }
    return Optional.of(mapKulturObjektDokumentToOaiItem(item));
  }

  @Override
  public List<OaiItem> findAll() {
    return repository.findAll().stream().filter(Objects::nonNull)
        .map(this::mapKulturObjektDokumentToOaiItem).toList();
  }

  @Override
  public List<OaiItem> findWithFilters(
      Instant from, Instant until, String setSpec, int maxResponseLength, int offset) {
    return repository
        .findFilteredKulturObjektDokumente(from, until, setSpec, maxResponseLength, offset)
        .stream()
        .filter(Objects::nonNull)
        .map(this::mapKulturObjektDokumentToOaiItem)
        .toList();
  }

  @Override
  public long countWithFilters(Instant from, Instant until, String setSpec) {
    return repository.countFilteredKulturObjektDokumente(from, until, setSpec);
  }

  private OaiItem mapKulturObjektDokumentToOaiItem(KulturObjektDokument item) {
    return new OaiItem(
        item.getId(),
        item.getTeiXML(),
        item.getRegistrierungsDatum().atZone(ZONE_ID).toInstant(),
        item.getGueltigeIdentifikation().getBesitzer().getId());
  }
}
