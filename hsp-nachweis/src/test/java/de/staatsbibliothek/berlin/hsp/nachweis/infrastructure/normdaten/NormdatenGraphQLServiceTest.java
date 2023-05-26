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

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.MockedTransactionManager;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact.GNDEntityFactBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Query;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 18.05.21
 */
class NormdatenGraphQLServiceTest extends MockedTransactionManager {

  private NormdatenGraphQLPort port = Mockito.mock(NormdatenGraphQLPort.class);
  private NormdatenGraphQLService service = new NormdatenGraphQLService(port,
      transactionManagerMOCK);

  @Test
  void testFindByQuery() throws InterruptedException {
    GraphQlQuery graphQlQuery = new GraphQlQuery(
        Query.findCorporateBodyWithID("190571-7777-7575-5757-190571aabb",
            Arrays.asList("preferredName", "gndIdentifier")));
    service.findByQuery(graphQlQuery);
    Mockito.verify(port, Mockito.times(1)).findByQuery(graphQlQuery);
  }

  @Test
  void testFindByIdOrNameAndNodeLabel() throws InterruptedException {
    service.findOneByIdOrNameAndNodeLabel("1", GNDEntityFact.PLACE_TYPE_NAME);
    Mockito.verify(port, Mockito.times(1)).findByQuery(new GraphQlQuery(
        Query.findGNDEntityFacts("1", GNDEntityFact.PLACE_TYPE_NAME, GNDEntityFact.FIELDS)));
  }

  @Test
  void testFindAllByIdOrNameAndNodeLabel() throws InterruptedException {
    service.findAllByIdOrNameAndNodeLabel("1", GNDEntityFact.PLACE_TYPE_NAME, true);
    Mockito.verify(port, Mockito.times(1)).findByQuery(new GraphQlQuery(
        Query.findGNDEntityFacts("1", GNDEntityFact.PLACE_TYPE_NAME, GNDEntityFact.FIELDS)));
  }

  @Test
  void testFindLanguageById() throws InterruptedException {
    service.findLanguageById("1");
    Mockito.verify(port, Mockito.times(1))
        .findByQuery(new GraphQlQuery(Query.findLanguageWithID("1", GNDEntityFact.FIELDS)));
  }

  @Test
  void testFindCorporateBodiesByPlaceId() throws InterruptedException {
    service.findCorporateBodiesByPlaceId("1", false);
    Mockito.verify(port, Mockito.times(1))
        .findByQuery(new GraphQlQuery(Query.findCorporateBodiesByPlaceId("1", GNDEntityFact.FIELDS_MIN)));
  }

  @Test
  void testCreateGNDEntityFactAsNode() throws Exception {
    GNDEntityFact gndEntityFact = new GNDEntityFactBuilder()
        .withTypeName(GNDEntityFact.PLACE_TYPE_NAME)
        .withGndIdentifier("GND-123")
        .withPreferredName("Berlin")
        .build();

    service.createGNDEntityFactAsNode(gndEntityFact);
    Mockito.verify(port, Mockito.times(1))
        .findByQuery(new GraphQlQuery(Query.createGNDEntityFactAsNode(gndEntityFact)));

  }

}
