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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AbmessungWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AbmessungTest {

  @Test
  void testBuilder() {
    AbmessungWert abmessungWert_1 = AbmessungWert.builder()
        .withText("23,5 × 16,5 x 2,5")
        .withHoehe("23,5")
        .withBreite("16,5")
        .withTiefe("2,5")
        .withTyp("factual")
        .build();

    AbmessungWert abmessungWert_2 = AbmessungWert.builder()
        .withText("25 × 15 x 5")
        .withHoehe("25")
        .withBreite("15")
        .withTiefe("5")
        .withTyp("deduced")
        .build();

    Abmessung abmessung = Abmessung.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withAbmessungWerte(null)
        .withAbmessungWerte(List.of(abmessungWert_1))
        .addAbmessungWert(null)
        .addAbmessungWert(abmessungWert_2)
        .build();

    assertNotNull(abmessung);
    assertNotNull(abmessung.getId());
    assertEquals("HSP-123", abmessung.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, abmessung.getReferenzTyp());
    assertEquals(AttributsTyp.ABMESSUNG, abmessung.getAttributsTyp());
    assertEquals("23,5 × 16,5 x 2,5; 25 × 15 x 5", abmessung.getText());
    assertNotNull(abmessung.getAbmessungWerte());
    assertEquals(2, abmessung.getAbmessungWerte().size());
    assertTrue(abmessung.getAbmessungWerte().contains(abmessungWert_1));
    assertTrue(abmessung.getAbmessungWerte().contains(abmessungWert_2));

    assertNotNull(abmessung.toString());
  }

  @Test
  void testToString() {
    String toString = Abmessung.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(
                new String[]{
                    Abmessung.class.getSimpleName(), "id", "attributsTyp", "referenzId", "referenzTyp",
                    "text", "abmessungWerte"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Abmessung abmessung_1 = Abmessung.builder()
        .withReferenzId("HSP-123")
        .build();

    Abmessung abmessung_2 = Abmessung.builder()
        .withReferenzId("HSP-123")
        .build();

    Abmessung abmessung_3 = Abmessung.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(abmessung_1, abmessung_2);
    assertNotEquals(abmessung_1, abmessung_3);
  }
}
