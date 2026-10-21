package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlData.Data;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Field;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Query;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 12.05.21
 */
@Dependent
public class NormdatenGraphQLService extends NormdatenServiceAccessXAResource
    implements NormdatenGraphQLBoundary {

  public static final String CACHE_NAME_GRAPH_QL_DATA_BY_QUERY = "GraphQlDataByQuery";
  private static final Logger logger = LoggerFactory.getLogger(NormdatenGraphQLService.class);
  private final NormdatenGraphQLPort normdatenGraphQLPort;

  @Inject
  public NormdatenGraphQLService(
      @RestClient NormdatenGraphQLPort normdatenGraphQLPort,
      TransactionManager transactionManager) {
    super(logger, transactionManager);
    this.normdatenGraphQLPort = normdatenGraphQLPort;
  }

  private static boolean checkNumberOfResult(List<GNDEntityFact> gndEntityFacts) {

    if (gndEntityFacts.size() > 1) {
      logger.debug("Too many result found as GNDEntityFacts {}", gndEntityFacts.size());
    } else if (gndEntityFacts.isEmpty()) {
      logger.debug("No GNDEntityFact found");
    }

    return gndEntityFacts.size() == 1;
  }

  @Override
  @Transactional
  @CacheResult(cacheName = CACHE_NAME_GRAPH_QL_DATA_BY_QUERY)
  public GraphQlData findByQuery(GraphQlQuery query) {
    if (Objects.isNull(query)) {
      return null;
    }

    try {
      return getNormdatenGraphQLPort().findByQuery(query);
    } catch (Exception e) {
      throw new ServiceNotAvailableException(
          "Normdatenservice is not available. Query: {} " + query.getQuery(),
          e);
    }
  }

  @Override
  @Transactional
  public Optional<GNDEntityFact> findOneByIdOrNameAndNodeLabel(String idOrName, String nodeLabel) {

    GraphQlQuery query =
        new GraphQlQuery(Query.findGNDEntityFacts(idOrName, nodeLabel, GNDEntityFact.FIELDS));
    GraphQlData result = findByQuery(query);

    return Optional.ofNullable(result)
        .map(GraphQlData::getData)
        .map(Data::getFindGNDEntityFacts)
        .filter(NormdatenGraphQLService::checkNumberOfResult)
        .map(List::getFirst);
  }

  @Override
  @Transactional
  public Set<GNDEntityFact> findAllByIdOrNameAndNodeLabel(
      String idOrName, String nodeLabel, boolean allFields) {
    if ((Objects.isNull(idOrName) || idOrName.isEmpty())
        && (Objects.isNull(nodeLabel) || nodeLabel.isEmpty())) {
      return Collections.emptySet();
    }

    Set<Field> fields = allFields ? GNDEntityFact.FIELDS : GNDEntityFact.FIELDS_MIN;
    GraphQlQuery query = new GraphQlQuery(Query.findGNDEntityFacts(idOrName, nodeLabel, fields));
    GraphQlData result = findByQuery(query);

    return Optional.ofNullable(result).map(GraphQlData::getData).stream()
        .map(Data::getFindGNDEntityFacts)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  @Override
  @Transactional
  public Optional<GNDEntityFact> findLanguageById(String id) {
    if (Objects.isNull(id) || id.isEmpty()) {
      return Optional.empty();
    }

    GraphQlQuery query = new GraphQlQuery(Query.findLanguageWithID(id, GNDEntityFact.FIELDS));
    GraphQlData graphQlData = findByQuery(query);

    return Optional.ofNullable(graphQlData)
        .flatMap(graph -> Optional.ofNullable(graph.getData()))
        .flatMap(data -> Optional.ofNullable(data.getFindLanguageWithID()));
  }

  @Override
  @Transactional
  public Set<GNDEntityFact> findCorporateBodiesByPlaceId(String id, boolean allFields) {
    if (Objects.isNull(id) || id.isEmpty()) {
      return Collections.emptySet();
    }

    Set<Field> fields = allFields ? GNDEntityFact.FIELDS : GNDEntityFact.FIELDS_MIN;
    GraphQlQuery query = new GraphQlQuery(Query.findCorporateBodiesByPlaceId(id, fields));
    GraphQlData graphQlData = findByQuery(query);

    return Optional.ofNullable(graphQlData).map(GraphQlData::getData).stream()
        .map(Data::getFindCorporateBodiesByPlaceId)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  @Override
  @Transactional
  @CacheInvalidateAll(cacheName = CACHE_NAME_GRAPH_QL_DATA_BY_QUERY)
  public Optional<GNDEntityFact> createGNDEntityFactAsNode(GNDEntityFact gndEntityFact) {
    try {
      var query = new GraphQlQuery(Query.createGNDEntityFactAsNode(gndEntityFact));
      logger.info("Call GraphQL Mutation for {}", query);
      var graphQlData = normdatenGraphQLPort.findByQuery(query);

      return Optional.ofNullable(graphQlData)
          .flatMap(graph -> Optional.ofNullable(graph.getData()))
          .flatMap(data -> Optional.ofNullable(data.getCreate()));
    } catch (Exception e) {
      logger.error("Error creating GraphQlQuery for gndEntityFact " + gndEntityFact, e);
      return Optional.empty();
    }
  }

  @Override
  @Transactional
  public List<Concept> findConceptsWithConceptSchemeType(String conceptSchemeType) {

    if (conceptSchemeType == null || conceptSchemeType.isEmpty()) {
      return List.of();
    }

    GraphQlQuery query =
        new GraphQlQuery(Query.findConcepteWithSchemeType(conceptSchemeType, Concept.FIELDS));
    GraphQlData graphQlData = findByQuery(query);

    return Optional.ofNullable(graphQlData).map(GraphQlData::getData).stream()
        .map(Data::getFindConcepteWithSchemeType)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public List<ConceptScheme> findSkosConceptSchemesWithinSchemeConceptScheme(String conceptScheme) {

    if (conceptScheme == null || conceptScheme.isEmpty()) {
      return List.of();
    }

    GraphQlQuery query =
        new GraphQlQuery(
            Query.findSkosConceptSchemesWithinSchemeConceptScheme(
                conceptScheme, ConceptScheme.FIELDS));
    GraphQlData graphQlData = findByQuery(query);

    return Optional.ofNullable(graphQlData).map(GraphQlData::getData).stream()
        .map(Data::getFindSkosConceptSchemesWithinSchemeConceptScheme)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  @Transactional
  public NormdatenGraphQLPort getNormdatenGraphQLPort() {
    registerForTransaction();
    return normdatenGraphQLPort;
  }

  @Override
  @CacheInvalidateAll(cacheName = CACHE_NAME_GRAPH_QL_DATA_BY_QUERY)
  void cleanUpCache() {
    super.cleanUpCache();
    logger.info("Clean up cache with the name {}", CACHE_NAME_GRAPH_QL_DATA_BY_QUERY);
  }
}
