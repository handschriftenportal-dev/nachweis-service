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

class UmfangTest {

  @Test
  void testBuilder() {

    Umfang umfang = Umfang.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("I + 394 Bl.")
        .withBlattzahl("394")
        .build();

    assertNotNull(umfang);
    assertNotNull(umfang.getId());
    assertEquals("HSP-123", umfang.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, umfang.getReferenzTyp());
    assertEquals(AttributsTyp.UMFANG, umfang.getAttributsTyp());
    assertEquals("I + 394 Bl.", umfang.getText());
    assertEquals("394", umfang.getBlattzahl());
  }

  @Test
  void testToString() {
    String toString = Umfang.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(
                new String[]{Umfang.class.getSimpleName(),
                    "id", "attributsTyp", "referenzId", "referenzTyp", "text", "blattzahl"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Umfang umfang_1 = Umfang.builder()
        .withReferenzId("HSP-123")
        .build();

    Umfang umfang_2 = Umfang.builder()
        .withReferenzId("HSP-123")
        .build();

    Umfang umfang_3 = Umfang.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(umfang_1, umfang_2);

    assertNotEquals(umfang_1, umfang_3);
  }

}
