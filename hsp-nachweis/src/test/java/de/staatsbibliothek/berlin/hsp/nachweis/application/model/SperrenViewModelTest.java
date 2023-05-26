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

package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.SperrenViewModel.SperrenViewModelBuilder;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 30.08.2021
 */
public class SperrenViewModelTest {

  @Test
  public void testBuilder() {
    LocalDateTime testDate = LocalDateTime.of(2021, 1, 1, 12, 0, 0);
    SperrenViewModel model = new SperrenViewModelBuilder()
        .withId("1")
        .withBearbeitername("Tester")
        .withStartDatum(testDate)
        .withSperreTyp(SperreTyp.MANUAL)
        .withSperreGrund("Testsperre")
        .withTxId("123xyz")
        .withSperreEintraege(List.of(new SperreEintragViewModel("1", SperreDokumentTyp.BESCHREIBUNG, "HS. 1_1")))
        .addSperreEintrag(new SperreEintragViewModel("2", SperreDokumentTyp.BESCHREIBUNG, "HS. 1"))
        .build();

    assertNotNull(model);
    assertEquals("Tester", model.getBearbeitername());
    assertEquals(testDate, model.getStartDatum());
    assertEquals(SperreTyp.MANUAL, model.getSperreTyp());
    assertEquals("Testsperre", model.getSperreGrund());
    assertEquals("123xyz", model.getTxId());
    assertNotNull(model.getSperreEintraege());
    assertEquals(2, model.getSperreEintraege().size());
    assertNotNull(model.getSperreEintraegeSignaturen());
    assertTrue(model.getSperreEintraegeSignaturen().contains("HS. 1_1"));
    assertNotNull(model.getSperreEintraegeIds());
    assertTrue(model.getSperreEintraegeIds().contains("2"));
  }

  @Test
  public void testEquals() {
    LocalDateTime testDate = LocalDateTime.of(2021, 1, 1, 12, 0, 0);
    SperrenViewModel model1 = new SperrenViewModelBuilder()
        .withId("1")
        .withBearbeitername("Tester")
        .withStartDatum(testDate)
        .withSperreTyp(SperreTyp.MANUAL)
        .withSperreGrund("Testsperre")
        .withTxId("123xyz")
        .withSperreEintraege(List.of(new SperreEintragViewModel("1", SperreDokumentTyp.BESCHREIBUNG, "HS. 1_1")))
        .addSperreEintrag(new SperreEintragViewModel("2", SperreDokumentTyp.BESCHREIBUNG, "HS. 1"))
        .build();

    SperrenViewModel model2 = new SperrenViewModelBuilder()
        .withId("1")
        .withBearbeitername("Tester")
        .withStartDatum(testDate)
        .build();

    SperrenViewModel model3 = new SperrenViewModelBuilder()
        .withId("2")
        .withBearbeitername("Tester 2")
        .withStartDatum(testDate)
        .build();

    SperrenViewModel model4 = new SperrenViewModelBuilder()
        .withId("3")
        .withBearbeitername("Tester")
        .withStartDatum(LocalDateTime.now())
        .build();

    assertEquals(model1, model2);
    assertNotEquals(model1, model3);
    assertNotEquals(model1, model4);
  }
}
