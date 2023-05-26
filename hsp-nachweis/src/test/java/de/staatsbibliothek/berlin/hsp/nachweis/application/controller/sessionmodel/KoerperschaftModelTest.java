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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.07.2022
 */
class KoerperschaftModelTest {

  NormdatenReferenzBoundary normdatenReferenzBoundary = Mockito.mock(NormdatenReferenzBoundary.class);

  KoerperschaftModel koerperschaftModel;

  NormdatenReferenz sbb;
  NormdatenReferenz bsb;

  @BeforeEach
  void onSetup() {
    sbb = createSBB();
    bsb = createBSB();

    when(normdatenReferenzBoundary.findAllByIdOrNameAndType(null, NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME, true))
        .thenReturn(Set.of(sbb, bsb));

    koerperschaftModel = new KoerperschaftModel(normdatenReferenzBoundary);
  }

  @Test
  void testSetup() {
    koerperschaftModel.setup();
    assertEquals(2, koerperschaftModel.getAllKoerperschaften().size());
  }

  @Test
  void testFindIsil() {
    assertNotNull(koerperschaftModel.findIsil(sbb));
    assertEquals("DE-1", koerperschaftModel.findIsil(sbb).getText());
    assertNull(koerperschaftModel.findIsil(null));
  }

  private NormdatenReferenz createSBB() {
    return new NormdatenReferenzBuilder()
        .withId("1")
        .withName("Stabi")
        .withGndID("12122-X")
        .withTypeName(NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME)
        .addIdentifikator(new Identifikator("DE-1", "", "Isil"))
        .addVarianterName(new VarianterName("Staatsbibliothek zu Berlin", "de"))
        .build();
  }

  private NormdatenReferenz createBSB() {
    return new NormdatenReferenzBuilder()
        .withId("2")
        .withName("BSB")
        .withGndID("12122-Z")
        .withTypeName(NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME)
        .addIdentifikator(new Identifikator("DE-2", "", "Isil"))
        .addVarianterName(new VarianterName("Bayerische Staatsbibliothek", "de"))
        .build();
  }
}
