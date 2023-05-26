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

package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class GrundspracheTest {

  @Test
  void testBuilder() {

    Grundsprache grundsprache = Grundsprache.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("Alt- und Mittelhochdeutsch")
        .withNormdatenIds(null)
        .withNormdatenIds(List.of("4001523-3"))
        .addNormdatenId(null)
        .addNormdatenId("4039687-3")
        .build();

    assertNotNull(grundsprache);
    assertNotNull(grundsprache.getId());
    assertEquals("HSP-123", grundsprache.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, grundsprache.getReferenzTyp());
    assertEquals(AttributsTyp.GRUNDSPRACHE, grundsprache.getAttributsTyp());
    assertEquals("Alt- und Mittelhochdeutsch", grundsprache.getText());
    assertNotNull(grundsprache.getNormdatenIds());
    assertEquals(2, grundsprache.getNormdatenIds().size());
    assertTrue(grundsprache.getNormdatenIds().contains("4001523-3"));
    assertTrue(grundsprache.getNormdatenIds().contains("4039687-3"));
  }

  @Test
  void testToString() {
    String toString = Grundsprache.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(new String[]{Grundsprache.class.getSimpleName(),
                "id", "attributsTyp", "referenzId", "referenzTyp", "text", "normdatenIds"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Grundsprache grundsprache_1 = Grundsprache.builder()
        .withReferenzId("HSP-123")
        .build();

    Grundsprache grundsprache_2 = Grundsprache.builder()
        .withReferenzId("HSP-123")
        .build();

    Grundsprache grundsprache_3 = Grundsprache.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(grundsprache_1, grundsprache_2);

    assertNotEquals(grundsprache_1, grundsprache_3);
  }

}
