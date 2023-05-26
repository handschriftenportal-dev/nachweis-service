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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 29.06.2022
 */
public class NormdatenReferenzConverterTest {

  @Test
  void testGetAsObject() {

    String normdatenReferenzString = "123"
        + NormdatenReferenzConverter.SEPARATOR + "Konrad"
        + NormdatenReferenzConverter.SEPARATOR + "GNDID"
        + NormdatenReferenzConverter.SEPARATOR + GNDEntityFact.PERSON_TYPE_NAME;

    NormdatenReferenzConverter converter = new NormdatenReferenzConverter();

    NormdatenReferenz normdatenReferenz = converter.getAsObject(null, null, normdatenReferenzString);

    assertNotNull(normdatenReferenz);

    assertEquals("123", normdatenReferenz.getId());
    assertEquals("Konrad", normdatenReferenz.getName());
    assertEquals("GNDID", normdatenReferenz.getGndID());
    assertEquals(GNDEntityFact.PERSON_TYPE_NAME, normdatenReferenz.getTypeName());

    String normdatenReferenzStringNoGnd = "123"
        + NormdatenReferenzConverter.SEPARATOR + "Konrad"
        + NormdatenReferenzConverter.SEPARATOR
        + NormdatenReferenzConverter.SEPARATOR + GNDEntityFact.PERSON_TYPE_NAME;

    normdatenReferenz = converter.getAsObject(null, null, normdatenReferenzStringNoGnd);

    assertNotNull(normdatenReferenz);

    assertEquals("123", normdatenReferenz.getId());
    assertEquals("Konrad", normdatenReferenz.getName());
    assertNull(normdatenReferenz.getGndID());
    assertEquals(GNDEntityFact.PERSON_TYPE_NAME, normdatenReferenz.getTypeName());
  }

  @Test
  void testGetAsString() {

    NormdatenReferenzConverter converter = new NormdatenReferenzConverter();

    NormdatenReferenz normdatenReferenz = new NormdatenReferenz("123", "Konrad", "GNDID",
        GNDEntityFact.PERSON_TYPE_NAME);

    String normdatenReferenzString = "123"
        + NormdatenReferenzConverter.SEPARATOR + "Konrad"
        + NormdatenReferenzConverter.SEPARATOR + "GNDID"
        + NormdatenReferenzConverter.SEPARATOR + GNDEntityFact.PERSON_TYPE_NAME;

    assertEquals(normdatenReferenzString, converter.getAsString(null, null, normdatenReferenz));

    normdatenReferenz = new NormdatenReferenz("123", "Konrad", null, GNDEntityFact.PERSON_TYPE_NAME);

    String normdatenReferenzStringNoGnd = "123"
        + NormdatenReferenzConverter.SEPARATOR + "Konrad"
        + NormdatenReferenzConverter.SEPARATOR
        + NormdatenReferenzConverter.SEPARATOR + GNDEntityFact.PERSON_TYPE_NAME;

    assertEquals(normdatenReferenzStringNoGnd, converter.getAsString(null, null, normdatenReferenz));
  }
}
