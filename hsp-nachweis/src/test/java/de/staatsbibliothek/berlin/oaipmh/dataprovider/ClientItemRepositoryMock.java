package de.staatsbibliothek.berlin.oaipmh.dataprovider;

import de.staatsbibliothek.berlin.oaipmh.model.OaiItem;
import de.staatsbibliothek.berlin.oaipmh.repository.ClientItemRepository;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ClientItemRepositoryMock implements ClientItemRepository {

  private final String xml;

  public ClientItemRepositoryMock() throws Exception {
    URL url =
        this.getClass()
            .getClassLoader()
            .getResource("oai-pmh/tei-HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469.xml");
    xml = Files.readString(Paths.get(Objects.requireNonNull(url).toURI()));
  }

  @Override

  public Optional<OaiItem> findById(String id) {
    return Optional.of(new OaiItem(id, xml, Instant.now(), null));
  }

  @Override
  public List<OaiItem> findAll() {
    return List.of();
  }

  @Override
  public List<OaiItem> findWithFilters(
      Instant from, Instant until, String setSpec, int maxResponseLength, int offset) {
    return Stream.of(
            new OaiItem(
                "HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469",
                xml,
                Instant.parse("2010-01-01T00:00:00Z"),
                "HSP-1"),
            new OaiItem(
                "HSP-814ec961-7bf0-382f-86ff-ebb16b3f4460",
                xml,
                Instant.parse("2012-01-01T00:00:00Z"),
                "HSP-1"))
        .filter(
            kod ->
                kod.registrierungsDatum().isAfter(from)
                    && kod.registrierungsDatum().isBefore(until)
                    && (setSpec == null || kod.setSpec().equals(setSpec)))
        .toList();
  }

  @Override
  public long countWithFilters(Instant from, Instant until, String setSpec) {
    return 0L;
  }
}
