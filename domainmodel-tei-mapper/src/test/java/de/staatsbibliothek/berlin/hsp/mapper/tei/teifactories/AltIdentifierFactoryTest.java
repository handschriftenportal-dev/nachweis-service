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

package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.AltIdentifier;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.05.2020.
 * @version 1.0
 */
public class AltIdentifierFactoryTest {

  @Test
  void testBuild() {

    AltIdentifier altIdentifier = AltIdentifierFactory.build("123",
        TEIValues.HSP_ALTIDENTIFIER_MXML);

    assertEquals(TEIValues.HSP_ALTIDENTIFIER_MXML, altIdentifier.getType());

    assertEquals("123", altIdentifier.getIdno().getContent().get(0));
  }

  @Test
  void testBuildWithCollection() {

    AltIdentifier altIdentifier = AltIdentifierFactory.build("123",
        TEIValues.HSP_ALTIDENTIFIER_MXML, TEIValues.KOD_ID_COLLECTION);

    assertEquals(TEIValues.HSP_ALTIDENTIFIER_MXML, altIdentifier.getType());

    assertEquals("123", altIdentifier.getIdno().getContent().get(0));

    assertEquals(TEIValues.KOD_ID_COLLECTION, altIdentifier.getCollection().getContent().get(0));
  }
}
