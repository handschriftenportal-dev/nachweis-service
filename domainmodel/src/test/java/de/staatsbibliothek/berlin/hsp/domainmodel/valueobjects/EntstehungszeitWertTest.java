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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class EntstehungszeitWertTest {

  @Test
  void testBuilder() {
    EntstehungszeitWert entstehungszeitWert = EntstehungszeitWert.builder()
        .withText("ca. 1220 (Fasz. I)")
        .withNichtVor("1210")
        .withNichtNach("1230")
        .withTyp("datable")
        .build();

    assertNotNull(entstehungszeitWert);
    assertNotNull(entstehungszeitWert.getId());
    assertEquals("ca. 1220 (Fasz. I)", entstehungszeitWert.getText());
    assertEquals("1210", entstehungszeitWert.getNichtVor());
    assertEquals("1230", entstehungszeitWert.getNichtNach());
    assertEquals("datable", entstehungszeitWert.getTyp());
  }

  @Test
  void testToString() {
    String toString = EntstehungszeitWert.builder().build().toString();
    assertNotNull(toString);
    assertTrue(Arrays.stream(new String[]{
            EntstehungszeitWert.class.getSimpleName(), "id", "text", "nichtVor", "nichtNach", "typ"})
        .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    assertNotEquals(EntstehungszeitWert.builder().build(), EntstehungszeitWert.builder().build());
  }

}
