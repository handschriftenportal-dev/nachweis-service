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
import org.junit.jupiter.api.Test;

class TitelTest {

  @Test
  void testBuilder() {
    Titel titel = Titel.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("Titel der Beschreibung")
        .build();

    assertNotNull(titel);
    assertNotNull(titel.getId());
    assertEquals("HSP-123", titel.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, titel.getReferenzTyp());
    assertEquals(AttributsTyp.TITEL, titel.getAttributsTyp());
    assertEquals("Titel der Beschreibung", titel.getText());
  }

  @Test
  void testToString() {
    String toString = Titel.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(new String[]{
                Titel.class.getSimpleName(), "id", "attributsTyp", "referenzId", "referenzTyp", "text"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Titel titel_1 = Titel.builder()
        .withReferenzId("HSP-123")
        .build();

    Titel titel_2 = Titel.builder()
        .withReferenzId("HSP-123")
        .build();

    Titel titel_3 = Titel.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(titel_1, titel_2);

    assertNotEquals(titel_1, titel_3);
  }

}
