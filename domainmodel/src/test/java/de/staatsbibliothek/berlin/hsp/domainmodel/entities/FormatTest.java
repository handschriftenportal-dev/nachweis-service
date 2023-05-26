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
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class FormatTest {

  @Test
  void testBuilder() {
    FormatWert formatWert_1 = FormatWert.builder()
        .withText("smaller than octavo")
        .withTyp("deduced")
        .build();

    FormatWert formatWert_2 = FormatWert.builder()
        .withText("long and narrow")
        .withTyp("factual")
        .build();

    Format format = Format.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withFormatWerte(null)
        .withFormatWerte(List.of(formatWert_1))
        .addFormatWert(null)
        .addFormatWert(formatWert_2)
        .build();

    assertNotNull(format);
    assertNotNull(format.getId());
    assertEquals("HSP-123", format.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, format.getReferenzTyp());
    assertEquals(AttributsTyp.FORMAT, format.getAttributsTyp());
    assertEquals("smaller_than_octavo;long_and_narrow", format.getText());
    assertNotNull(format.getFormatWerte());
    assertEquals(2, format.getFormatWerte().size());
    assertTrue(format.getFormatWerte().contains(formatWert_1));
    assertTrue(format.getFormatWerte().contains(formatWert_2));

    assertNotNull(format.toString());
  }

  @Test
  void testToString() {
    String toString = Format.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(
                new String[]{
                    Format.class.getSimpleName(), "id", "attributsTyp", "referenzId", "referenzTyp",
                    "text", "formatWerte"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Format format_1 = Format.builder()
        .withReferenzId("HSP-123")
        .build();

    Format format_2 = Format.builder()
        .withReferenzId("HSP-123")
        .build();

    Format format_3 = Format.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(format_1, format_2);
    assertNotEquals(format_1, format_3);
  }
}
