/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import com.google.inject.Inject;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlData.Data;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Field;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Query;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.Dependent;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 12.05.21
 */
@Dependent
public class NormdatenGraphQLService extends NormdatenServiceAccessXAResource implements
    NormdatenGraphQLBoundary, Serializable {

  public static final String CACHE_NAME_GRAPH_QL_DATA_BY_QUERY = "GraphQlDataByQuery";
  private static final long serialVersionUID = 8749302986118278223L;
  private static final Logger logger = LoggerFactory.getLogger(NormdatenGraphQLService.class);

  private final transient NormdatenGraphQLPort normdatenGraphQLPort;

  @Inject
  public NormdatenGraphQLService(@RestClient NormdatenGraphQLPort normdatenGraphQLPort,
      TransactionManager transactionManager) {
    super(logger, transactionManager);
    this.normdatenGraphQLPort = normdatenGraphQLPort;
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
      logger.error("Error calling normdatenGraphQLBoundary.findByQuery with query " + query.getQuery(), e);
      return null;
    }
  }

  @Override
  @Transactional
  public Optional<GNDEntityFact> findOneByIdOrNameAndNodeLabel(String idOrName,
      String nodeLabel) {

    GraphQlQuery query = new GraphQlQuery(Query.findGNDEntityFacts(idOrName, nodeLabel, GNDEntityFact.FIELDS));
    GraphQlData result = findByQuery(query);

    return Optional.ofNullable(result)
        .map(GraphQlData::getData)
        .map(Data::getFindGNDEntityFacts)
        .filter(gndEntityFacts -> gndEntityFacts.size() == 1)
        .map(gndEntityFacts -> gndEntityFacts.get(0));
  }

  @Override
  @Transactional
  public Set<GNDEntityFact> findAllByIdOrNameAndNodeLabel(String idOrName, String nodeLabel, boolean allFields) {
    if ((Objects.isNull(idOrName) || idOrName.isEmpty()) && (Objects.isNull(nodeLabel) || nodeLabel.isEmpty())) {
      return Collections.emptySet();
    }

    Set<Field> fields = allFields ? GNDEntityFact.FIELDS : GNDEntityFact.FIELDS_MIN;
    GraphQlQuery query = new GraphQlQuery(Query.findGNDEntityFacts(idOrName, nodeLabel, fields));
    GraphQlData result = findByQuery(query);

    return Optional.ofNullable(result)
        .map(GraphQlData::getData)
        .stream()
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

    return Optional.ofNullable(graphQlData)
        .map(GraphQlData::getData)
        .stream()
        .map(Data::getFindCorporateBodiesByPlaceId)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  @Override
  @Transactional
  @CacheInvalidateAll(cacheName = CACHE_NAME_GRAPH_QL_DATA_BY_QUERY)
  public Optional<GNDEntityFact> createGNDEntityFactAsNode(GNDEntityFact gndEntityFact) {

    GraphQlQuery query;
    try {
      query = new GraphQlQuery(Query.createGNDEntityFactAsNode(gndEntityFact));
    } catch (Exception e) {
      logger.error("Error creating GraphQlQuery for gndEntityFact " + gndEntityFact, e);
      return Optional.empty();
    }
    GraphQlData graphQlData = findByQuery(query);

    return Optional.ofNullable(graphQlData)
        .flatMap(graph -> Optional.ofNullable(graph.getData()))
        .flatMap(data -> Optional.ofNullable(data.getCreate()));
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
