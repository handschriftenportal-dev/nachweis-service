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

class AttributsTypTest {

  @Test
  void testGetIndexName() {
    assertEquals("norm_title", AttributsTyp.TITEL.getIndexName());
    assertEquals("norm_material", AttributsTyp.BESCHREIBSTOFF.getIndexName());
    assertEquals("norm_measure", AttributsTyp.UMFANG.getIndexName());
    assertEquals("norm_dimensions", AttributsTyp.ABMESSUNG.getIndexName());
    assertEquals("norm_format", AttributsTyp.FORMAT.getIndexName());
    assertEquals("norm_origPlace", AttributsTyp.ENTSTEHUNGSORT.getIndexName());
    assertEquals("norm_origDate", AttributsTyp.ENTSTEHUNGSZEIT.getIndexName());
    assertEquals("norm_textLang", AttributsTyp.GRUNDSPRACHE.getIndexName());
    assertEquals("norm_form", AttributsTyp.HANDSCHRIFTENTYP.getIndexName());
    assertEquals("norm_status", AttributsTyp.STATUS.getIndexName());
    assertEquals("norm_decoration", AttributsTyp.BUCHSCHMUCK.getIndexName());
    assertEquals("norm_musicNotation", AttributsTyp.MUSIKNOTATION.getIndexName());
  }

}
