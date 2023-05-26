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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.nachweis.WiremockITServer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Query;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Arrays;
import javax.inject.Inject;
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
  void testFindGraphQLCorporate() throws InterruptedException {

    assertNotNull(normdatenGraphQLPort);

    GraphQlData graphQlData = normdatenGraphQLPort.findByQuery(
        new GraphQlQuery(
            Query.findCorporateBodyWithID("54abb00b-5458-328c-b1b8-468eb89cf45a",
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
        graphQlData.getData().getFindPersonWithName().get(0).getPreferredName());

    assertEquals("123X",
        graphQlData.getData().getFindPersonWithName().get(0).getGndIdentifier());
  }
}
