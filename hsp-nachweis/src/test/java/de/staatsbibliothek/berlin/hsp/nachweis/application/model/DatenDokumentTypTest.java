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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 16.09.2020.
 * @version 1.0
 */
public class DatenDokumentTypTest {

  @Test
  void testValues() {
    Assertions.assertEquals(9, DatenDokumentTyp.values().length);
  }

  @Test
  void testToString() {
    Assertions.assertEquals("Kulturobjektdokument", DatenDokumentTyp.KOD.toString());
    Assertions.assertEquals("Beschreibung", DatenDokumentTyp.BESCHREIBUNG.toString());
    Assertions.assertEquals("Katalog", DatenDokumentTyp.KATALOG.toString());
    Assertions.assertEquals("Ort", DatenDokumentTyp.ORT.toString());
    Assertions.assertEquals("Körperschaft", DatenDokumentTyp.KOERPERSCHAFT.toString());
    Assertions.assertEquals("Beziehung", DatenDokumentTyp.BEZIEHUNG.toString());
    Assertions.assertEquals("Digitalisat", DatenDokumentTyp.DIGITALISAT.toString());
    Assertions.assertEquals("Person", DatenDokumentTyp.PERSON.toString());
    Assertions.assertEquals("Sprache", DatenDokumentTyp.SPRACHE.toString());
  }
}
