package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.MockedTransactionManager;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact.GNDEntityFactBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Query;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 18.05.21
 */
class NormdatenGraphQLServiceTest extends MockedTransactionManager {

  private final NormdatenGraphQLPort port = mock(NormdatenGraphQLPort.class);

  private NormdatenGraphQLService service = new NormdatenGraphQLService(port,
      transactionManagerMOCK);

  @Test
  void testFindByQuery() throws InterruptedException {
    GraphQlQuery graphQlQuery = new GraphQlQuery(
        Query.findCorporateBodyWithID("190571-7777-7575-5757-190571aabb",
            Arrays.asList("preferredName", "gndIdentifier")));
    service.findByQuery(graphQlQuery);
    verify(port, Mockito.times(1)).findByQuery(graphQlQuery);
  }

  @Test
  void testFindByIdOrNameAndNodeLabel() throws InterruptedException {
    service.findOneByIdOrNameAndNodeLabel("1", GNDEntityFact.PLACE_TYPE_NAME);
    verify(port, Mockito.times(1)).findByQuery(new GraphQlQuery(
        Query.findGNDEntityFacts("1", GNDEntityFact.PLACE_TYPE_NAME, GNDEntityFact.FIELDS)));
  }

  @Test
  void testFindAllByIdOrNameAndNodeLabel() throws InterruptedException {
    service.findAllByIdOrNameAndNodeLabel("1", GNDEntityFact.PLACE_TYPE_NAME, true);
    verify(port, Mockito.times(1)).findByQuery(new GraphQlQuery(
        Query.findGNDEntityFacts("1", GNDEntityFact.PLACE_TYPE_NAME, GNDEntityFact.FIELDS)));
  }

  @Test
  void testFindLanguageById() throws InterruptedException {
    service.findLanguageById("1");
    verify(port, Mockito.times(1))
        .findByQuery(new GraphQlQuery(Query.findLanguageWithID("1", GNDEntityFact.FIELDS)));
  }

  @Test
  void testFindCorporateBodiesByPlaceId() throws InterruptedException {
    service.findCorporateBodiesByPlaceId("1", false);
    verify(port, Mockito.times(1))
        .findByQuery(
            new GraphQlQuery(Query.findCorporateBodiesByPlaceId("1", GNDEntityFact.FIELDS_MIN)));
  }

  @Test
  void testCreateGNDEntityFactAsNode() throws Exception {
    GNDEntityFact gndEntityFact = new GNDEntityFactBuilder()
        .withTypeName(GNDEntityFact.PLACE_TYPE_NAME)
        .withGndIdentifier("GND-123")
        .withPreferredName("Berlin")
        .build();

    service.createGNDEntityFactAsNode(gndEntityFact);
    verify(port, Mockito.times(1))
        .findByQuery(
            new GraphQlQuery(Query.createGNDEntityFactAsNode(gndEntityFact)));

  }

  @Test
  void testFindConceptsWithConceptSchemeType() throws InterruptedException {
    Assertions.assertEquals(0, service.findConceptsWithConceptSchemeType("").size());
    service.findConceptsWithConceptSchemeType("sourceType");

    verify(port, Mockito.times(1))
        .findByQuery(
            new GraphQlQuery(Query.findConcepteWithSchemeType("sourceType", Concept.FIELDS)));

  }

  @Test
  void testFindSkosConceptSchemesWithinSchemeConceptScheme() throws InterruptedException {
    Assertions.assertEquals(0, service.findSkosConceptSchemesWithinSchemeConceptScheme("").size());
    service.findSkosConceptSchemesWithinSchemeConceptScheme("DIGI");

    verify(port, Mockito.times(1))
        .findByQuery(new GraphQlQuery(
            Query.findSkosConceptSchemesWithinSchemeConceptScheme("DIGI",
                ConceptScheme.FIELDS)));

  }

}
