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
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class EntstehungsortTest {

  @Test
  void testBuilder() {
    NormdatenReferenz berlin = new NormdatenReferenz("NORM-123", "Berlin", "4005728-8",
        NormdatenReferenz.ORT_TYPE_NAME);
    NormdatenReferenz potsdam = new NormdatenReferenz("NORM-456", "Berlin", "4046948-7",
        NormdatenReferenz.ORT_TYPE_NAME);

    Entstehungsort entstehungsort = Entstehungsort.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("Berlin")
        .withNormdatenReferenzen(null)
        .withNormdatenReferenzen(List.of(berlin))
        .addNormdatenReferenz(null)
        .addNormdatenReferenz(potsdam)
        .build();

    assertNotNull(entstehungsort);
    assertNotNull(entstehungsort.getId());
    assertEquals("HSP-123", entstehungsort.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, entstehungsort.getReferenzTyp());
    assertEquals(AttributsTyp.ENTSTEHUNGSORT, entstehungsort.getAttributsTyp());
    assertEquals("Berlin", entstehungsort.getText());
    assertNotNull(entstehungsort.getNormdatenReferenzen());
    assertEquals(2, entstehungsort.getNormdatenReferenzen().size());
    assertTrue(entstehungsort.getNormdatenReferenzen().contains(berlin));
    assertTrue(entstehungsort.getNormdatenReferenzen().contains(potsdam));
  }

  @Test
  void testToString() {
    String toString = Entstehungsort.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(
                new String[]{
                    Entstehungsort.class.getSimpleName(), "id", "attributsTyp", "referenzId", "referenzTyp", "text",
                    "normdatenReferenzen"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Entstehungsort entstehungsort_1 = Entstehungsort.builder()
        .withReferenzId("HSP-123")
        .build();

    Entstehungsort entstehungsort_2 = Entstehungsort.builder()
        .withReferenzId("HSP-123")
        .build();

    Entstehungsort entstehungsort_3 = Entstehungsort.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(entstehungsort_1, entstehungsort_2);

    assertNotEquals(entstehungsort_1, entstehungsort_3);
  }
}
