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

class HandschriftentypTest {

  @Test
  void testBuilder() {
    Handschriftentyp handschriftentyp = Handschriftentyp.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("composite")
        .build();

    assertNotNull(handschriftentyp);
    assertNotNull(handschriftentyp.getId());
    assertEquals("HSP-123", handschriftentyp.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, handschriftentyp.getReferenzTyp());
    assertEquals(AttributsTyp.HANDSCHRIFTENTYP, handschriftentyp.getAttributsTyp());
    assertEquals("composite", handschriftentyp.getText());
  }

  @Test
  void testToString() {
    String toString = Handschriftentyp.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(new String[]{Handschriftentyp.class.getSimpleName(),
                "id", "attributsTyp", "referenzId", "referenzTyp", "text"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Handschriftentyp handschriftentyp_1 = Handschriftentyp.builder()
        .withReferenzId("HSP-123")
        .build();

    Handschriftentyp handschriftentyp_2 = Handschriftentyp.builder()
        .withReferenzId("HSP-123")
        .build();

    Handschriftentyp handschriftentyp_3 = Handschriftentyp.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(handschriftentyp_1, handschriftentyp_2);

    assertNotEquals(handschriftentyp_1, handschriftentyp_3);
  }

}
