package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.nachweis.WiremockITServer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Query;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Arrays;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.04.2021.
 * @version 1.0
 */

@QuarkusTest
@QuarkusTestResource(WiremockITServer.class)
public class NormdatenGraphQLPortIT {

  @Inject
  @RestClient
  NormdatenGraphQLPort normdatenGraphQLPort;

  @Test
  void testCreateCoporateBodyWithAuthorization() throws InterruptedException {

    assertNotNull(normdatenGraphQLPort);

    GraphQlData graphQlData = normdatenGraphQLPort.findByQuery(
        new GraphQlQuery(
            Query.createGNDEntityFactAsNode(
                new GNDEntityFact.GNDEntityFactBuilder().withPreferredName(
                        "Domstiftsbibliothek St. Petri")
                    .withTypeName("CorporateBody")
                    .withGndIdentifier("1027608000")
                    .build())));

    assertNotNull(graphQlData);

    assertEquals("Domstiftsbibliothek St. Petri",
        graphQlData.getData().getCreate().getPreferredName());

    assertEquals("1027608000",
        graphQlData.getData().getCreate().getGndIdentifier());
  }

  @Test
  void testFindGraphQLCorporate() throws InterruptedException {

    assertNotNull(normdatenGraphQLPort);

    GraphQlData graphQlData = normdatenGraphQLPort.findByQuery(
        new GraphQlQuery(
            Query.findCorporateBodyWithID(
                "54abb00b-5458-328c-b1b8-468eb89cf45a",
                Arrays.asList("preferredName", "gndIdentifier"))));

    assertNotNull(graphQlData);

    assertEquals("Domstiftsbibliothek St. Petri",
        graphQlData.getData().getFindCorporateBodyWithID().getPreferredName());

    assertEquals("1027608000",
        graphQlData.getData().getFindCorporateBodyWithID().getGndIdentifier());
  }

  @Test
  void testFindGraphQLPersons() throws InterruptedException {

    assertNotNull(normdatenGraphQLPort);

    GraphQlData graphQlData = normdatenGraphQLPort.findByQuery(
        new GraphQlQuery(
            Query.findPersonWithName("Robert",
                Arrays.asList("preferredName", "gndIdentifier", "id"))));

    assertNotNull(graphQlData);

    assertEquals("Robert",
        graphQlData.getData().getFindPersonWithName().get(0)
            .getPreferredName());

    assertEquals("123X",
        graphQlData.getData().getFindPersonWithName().get(0)
            .getGndIdentifier());
  }

  @Test
  void testFindConceptsWithConceptSchemeType() throws InterruptedException {
    assertNotNull(normdatenGraphQLPort);
    GraphQlData graphQlData = normdatenGraphQLPort.findByQuery(
        new GraphQlQuery(
            Query.findConcepteWithSchemeType("sourceType", Concept.FIELDS)));

    assertNotNull(graphQlData);

    assertEquals("NORM-cc73d86f-2707-3284-bd08-ba61cdacca42",
        graphQlData.getData().getFindConcepteWithSchemeType().get(0).getId());

    assertEquals(
        "https://normdaten.staatsbibliothek-berlin.de/vocabulary/sourceType/reproduction",
        graphQlData.getData().getFindConcepteWithSchemeType().get(0).getUri());

    assertEquals("reproduction",
        graphQlData.getData().getFindConcepteWithSchemeType().get(0)
            .getNotation());

    assertEquals(2,
        graphQlData.getData().getFindConcepteWithSchemeType().get(0).getLabels()
            .size());

    graphQlData.getData().getFindConcepteWithSchemeType().get(0).getLabels()
        .stream()
        .filter(l -> l.getIsoCode().equals("de")).findFirst()
        .ifPresent(l -> assertEquals("Reproduktion", l.getText()));

  }

  @Test
  void testFindSkosConceptSchemesWithinSchemeConceptSchem()
      throws InterruptedException {
    assertNotNull(normdatenGraphQLPort);
    GraphQlData graphQlData = normdatenGraphQLPort.findByQuery(
        new GraphQlQuery(
            Query.findSkosConceptSchemesWithinSchemeConceptScheme("DIGI",
                ConceptScheme.FIELDS)));

    assertNotNull(graphQlData);

    assertEquals("NORM-c3de1202-9e0e-33ce-beec-4d64dcb09e97",
        graphQlData.getData()
            .getFindSkosConceptSchemesWithinSchemeConceptScheme().get(0)
            .getId());

    assertEquals("digitizationType",
        graphQlData.getData()
            .getFindSkosConceptSchemesWithinSchemeConceptScheme().get(0)
            .getNotation());

    assertEquals(
        "https://normdaten.staatsbibliothek-berlin.de/vocabulary/digitizationType",
        graphQlData.getData()
            .getFindSkosConceptSchemesWithinSchemeConceptScheme().get(0)
            .getUri());

    assertEquals("NORM-5ab77627-345a-3347-b34d-a97a041920c9",
        graphQlData.getData()
            .getFindSkosConceptSchemesWithinSchemeConceptScheme().get(1)
            .getId());

    assertEquals("sourceType",
        graphQlData.getData()
            .getFindSkosConceptSchemesWithinSchemeConceptScheme().get(1)
            .getNotation());

    assertEquals(
        "https://normdaten.staatsbibliothek-berlin.de/vocabulary/sourceType",
        graphQlData.getData()
            .getFindSkosConceptSchemesWithinSchemeConceptScheme().get(1)
            .getUri());

    assertEquals(2,
        graphQlData.getData()
            .getFindSkosConceptSchemesWithinSchemeConceptScheme().get(1)
            .getLabels().size());

    graphQlData.getData().getFindSkosConceptSchemesWithinSchemeConceptScheme()
        .get(1).getLabels()
        .stream()
        .filter(l -> l.getIsoCode().equals("de")).findFirst()
        .ifPresent(l -> assertEquals("Art der Quelle", l.getText()));

  }
}
