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
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.EntstehungszeitWert;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class EntstehungszeitTest {

  @Test
  void testBuilder() {
    EntstehungszeitWert zeitWert_1 = EntstehungszeitWert.builder()
        .withText("ca. 1220 (Fasz. I)")
        .withNichtVor("1210")
        .withNichtNach("1230")
        .withTyp("datable")
        .build();

    EntstehungszeitWert zeitWert_2 = EntstehungszeitWert.builder()
        .withText("ca. 1320 (Fasz. II)")
        .withNichtVor("1310")
        .withNichtNach("1330")
        .withTyp("dated")
        .build();

    Entstehungszeit entstehungszeit = Entstehungszeit.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withEntstehungszeitWerte(null)
        .withEntstehungszeitWerte(List.of(zeitWert_1))
        .addEntstehungszeitWert(null)
        .addEntstehungszeitWert(zeitWert_2)
        .build();

    assertNotNull(entstehungszeit);
    assertNotNull(entstehungszeit.getId());
    assertEquals("HSP-123", entstehungszeit.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, entstehungszeit.getReferenzTyp());
    assertEquals(AttributsTyp.ENTSTEHUNGSZEIT, entstehungszeit.getAttributsTyp());
    assertEquals("ca. 1220 (Fasz. I); ca. 1320 (Fasz. II)", entstehungszeit.getText());
    assertNotNull(entstehungszeit.getEntstehungszeitWerte());
    assertEquals(2, entstehungszeit.getEntstehungszeitWerte().size());
    assertTrue(entstehungszeit.getEntstehungszeitWerte().contains(zeitWert_1));
    assertTrue(entstehungszeit.getEntstehungszeitWerte().contains(zeitWert_2));

    assertNotNull(entstehungszeit.toString());
  }

  @Test
  void testToString() {
    String toString = Entstehungszeit.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(
                new String[]{
                    Entstehungszeit.class.getSimpleName(), "id", "attributsTyp", "referenzId", "referenzTyp",
                    "text", "entstehungszeitWerte"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Entstehungszeit entstehungszeit_1 = Entstehungszeit.builder()
        .withReferenzId("HSP-123")
        .build();

    Entstehungszeit entstehungszeit_2 = Entstehungszeit.builder()
        .withReferenzId("HSP-123")
        .build();

    Entstehungszeit entstehungszeit_3 = Entstehungszeit.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(entstehungszeit_1, entstehungszeit_2);
    assertNotEquals(entstehungszeit_1, entstehungszeit_3);
  }
}
