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

class AbmessungWertTest {

  @Test
  void testBuilder() {
    AbmessungWert abmessungWert = AbmessungWert.builder()
        .withText("23,5 × 16,5 x 2,5")
        .withHoehe("23,5")
        .withBreite("16,5")
        .withTiefe("2,5")
        .withTyp("factual")
        .build();

    assertNotNull(abmessungWert);
    assertNotNull(abmessungWert.getId());
    assertEquals("23,5 × 16,5 x 2,5", abmessungWert.getText());
    assertEquals("23,5", abmessungWert.getHoehe());
    assertEquals("16,5", abmessungWert.getBreite());
    assertEquals("2,5", abmessungWert.getTiefe());
    assertEquals("factual", abmessungWert.getTyp());
  }

  @Test
  void testToString() {
    String toString = AbmessungWert.builder().build().toString();
    assertNotNull(toString);
    assertTrue(Arrays.stream(
            new String[]{AbmessungWert.class.getSimpleName(), "id", "text", "hoehe", "breite", "tiefe", "typ"})
        .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    assertNotEquals(AbmessungWert.builder().build(), AbmessungWert.builder().build());
  }
}
