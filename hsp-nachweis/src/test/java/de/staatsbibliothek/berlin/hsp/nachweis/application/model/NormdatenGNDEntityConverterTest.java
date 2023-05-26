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

package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.05.2021.
 * @version 1.0
 */
public class NormdatenGNDEntityConverterTest {

  @Test
  void testgetAsObject() {

    String gndEntityString = "123" + NormdatenGNDEntityConverter.SEPARATOR + "GNDID"
        + NormdatenGNDEntityConverter.SEPARATOR + "Konrad"
        + NormdatenGNDEntityConverter.SEPARATOR + GNDEntityFact.PERSON_TYPE_NAME;

    NormdatenGNDEntityConverter converter = new NormdatenGNDEntityConverter();

    GNDEntityFact gndEntityFact = converter.getAsObject(null, null, gndEntityString);

    assertNotNull(gndEntityFact);

    assertEquals("123", gndEntityFact.getId());

    assertEquals("Konrad", gndEntityFact.getPreferredName());

    assertEquals("GNDID", gndEntityFact.getGndIdentifier());

    String gndEntityStringBad = "123" + NormdatenGNDEntityConverter.SEPARATOR + ""
        + NormdatenGNDEntityConverter.SEPARATOR + "Konrad";

    gndEntityFact = converter.getAsObject(null, null, gndEntityStringBad);

    assertNotNull(gndEntityFact);

    assertEquals("123", gndEntityFact.getId());

    assertEquals("Konrad", gndEntityFact.getPreferredName());

    assertEquals("", gndEntityFact.getGndIdentifier());

    assertNull(gndEntityFact.getTypeName());
  }

  @Test
  void testgetAsString() {

    NormdatenGNDEntityConverter converter = new NormdatenGNDEntityConverter();

    GNDEntityFact autor = new GNDEntityFact("123", "GNDID", "Konrad", GNDEntityFact.PERSON_TYPE_NAME);

    String gndEntityString = "123" + NormdatenGNDEntityConverter.SEPARATOR + "GNDID"
        + NormdatenGNDEntityConverter.SEPARATOR + "Konrad"
        + NormdatenGNDEntityConverter.SEPARATOR + GNDEntityFact.PERSON_TYPE_NAME;

    assertEquals(gndEntityString, converter.getAsString(null, null, autor));

    GNDEntityFact autorNoType = new GNDEntityFact("123", "GNDID", "Konrad");

    String gndEntityStringNoType = "123" + NormdatenGNDEntityConverter.SEPARATOR + "GNDID"
        + NormdatenGNDEntityConverter.SEPARATOR + "Konrad";

    assertEquals(gndEntityStringNoType, converter.getAsString(null, null, autorNoType));
  }
}
