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

package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 25.02.2022.
 * @version 1.0
 */
public class DokumentObjektTypTest {

  @Test
  void testToString() {
    assertEquals("hsp:object", DokumentObjektTyp.HSP_OBJECT.toString());
    assertEquals("hsp:description", DokumentObjektTyp.HSP_DESCRIPTION.toString());
    assertEquals("hsp:description_retro", DokumentObjektTyp.HSP_DESCRIPTION_RETRO.toString());
  }

  @Test
  void testFromString() {
    assertEquals(DokumentObjektTyp.HSP_OBJECT, DokumentObjektTyp.fromString("hsp:object"));
    assertEquals(DokumentObjektTyp.HSP_DESCRIPTION,
        DokumentObjektTyp.fromString("hsp:description"));
    assertEquals(DokumentObjektTyp.HSP_DESCRIPTION_RETRO,
        DokumentObjektTyp.fromString("hsp:description_retro"));
  }
}
