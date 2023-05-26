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

class BeschreibstoffTest {

  @Test
  void testBuilder() {

    Beschreibstoff beschreibstoff = Beschreibstoff.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("Pergament, Papyrus")
        .withTypen(null)
        .withTypen(List.of("parchment"))
        .addTyp(null)
        .addTyp("papyrus")
        .build();

    assertNotNull(beschreibstoff);
    assertNotNull(beschreibstoff.getId());
    assertEquals("HSP-123", beschreibstoff.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, beschreibstoff.getReferenzTyp());
    assertEquals(AttributsTyp.BESCHREIBSTOFF, beschreibstoff.getAttributsTyp());
    assertEquals("Pergament, Papyrus", beschreibstoff.getText());
    assertNotNull(beschreibstoff.getTypen());
    assertEquals(2, beschreibstoff.getTypen().size());
    assertTrue(beschreibstoff.getTypen().contains("parchment"));
    assertTrue(beschreibstoff.getTypen().contains("papyrus"));
  }

  @Test
  void testToString() {
    String toString = Beschreibstoff.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(
                new String[]{Beschreibstoff.class.getSimpleName(),
                    "id", "attributsTyp", "referenzId", "referenzTyp", "text", "typen"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Beschreibstoff beschreibstoff_1 = Beschreibstoff.builder()
        .withReferenzId("HSP-123")
        .build();

    Beschreibstoff beschreibstoff_2 = Beschreibstoff.builder()
        .withReferenzId("HSP-123")
        .build();

    Beschreibstoff beschreibstoff_3 = Beschreibstoff.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(beschreibstoff_1, beschreibstoff_2);

    assertNotEquals(beschreibstoff_1, beschreibstoff_3);
  }
}
