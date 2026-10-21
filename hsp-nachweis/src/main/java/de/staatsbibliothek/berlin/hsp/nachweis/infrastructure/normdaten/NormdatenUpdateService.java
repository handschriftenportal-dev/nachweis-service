package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import java.util.Optional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
@Slf4j
public class NormdatenUpdateService {

  private static final String JSON_SUFFIX = ".json";

  private final NormdatenGraphQLBoundary normdatenGraphQLBoundary;
  private final NormdatenGraphQLMutationClient mutationClient;
  private final LobidOrgPort lobidOrgPort;

  @Inject
  public NormdatenUpdateService(
      NormdatenGraphQLBoundary normdatenGraphQLBoundary,
      NormdatenGraphQLMutationClient mutationClient,
      @RestClient LobidOrgPort lobidOrgPort) {
    this.normdatenGraphQLBoundary = normdatenGraphQLBoundary;
    this.mutationClient = mutationClient;
    this.lobidOrgPort = lobidOrgPort;
  }

  public Optional<GNDEntityFact> ensureInGraph(String gndid, NormdatenTyp normdatenTyp) {
    log.debug("ensureInGraph: gndid={}, type={}", gndid, normdatenTyp);
    return normdatenGraphQLBoundary
        .findOneByIdOrNameAndNodeLabel(gndid, toNodeLabel(normdatenTyp))
        .or(() -> createEntityFact(gndid, toNodeLabel(normdatenTyp)));
  }

  public Optional<GNDEntityFact> createPlaceIfNotExist(String gndid) {
    return ensureInGraph(gndid, NormdatenTyp.Place);
  }

  private Optional<GNDEntityFact> createEntityFact(String gndid, String typeName) {
    try {
      log.debug("createEntityFact: fetching from lobid gndid={}, typeName={}", gndid, typeName);
      var lobidFact = lobidOrgPort.findByGNDID(gndid + JSON_SUFFIX);
      if (lobidFact == null) {
        log.warn("Lobid returned null for gndid {}", gndid);
        return Optional.empty();
      }
      var entityFact = lobidFact.toGNDEntityFact(typeName);
      var result = mutationClient.create(GNDEntityFactGraphQLInput.from(entityFact));
      if (result.hasErrors()) {
        log.error("create mutation returned errors for {}: {}", gndid, result.getErrors());
        return Optional.empty();
      }
      var created = Optional.ofNullable(result.get());
      log.info("create mutation succeeded for gndid {}: {}", gndid, created);
      return created;
    } catch (Exception error) {
      log.error("Error during createEntityFact for gndid {}", gndid, error);
      return Optional.empty();
    }
  }

  private String toNodeLabel(NormdatenTyp normdatenTyp) {
    return switch (normdatenTyp) {
      case Person -> GNDEntityFact.PERSON_TYPE_NAME;
      case Place -> GNDEntityFact.PLACE_TYPE_NAME;
      case CorporateBody -> GNDEntityFact.CORPORATE_BODY_TYPE_NAME;
      case Language -> GNDEntityFact.LANGUAGE_TYPE_NAME;
    };
  }
}
