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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 18.10.2022
 */

class DigitalisatViewModelTest {

  @Test
  void testMap() {

    assertNull(DigitalisatViewModel.map(null));

    Digitalisat digitalisat = Digitalisat.DigitalisatBuilder()
        .withAlternativeUrl("https://alternative.url")
        .withDigitalisierendeEinrichtung(
            new NormdatenReferenz("HSP-1", "StaBi", "GND-1", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .withDigitalisierungsdatum(LocalDate.of(2020, Month.MAY, 4))
        .withEntstehungsort(
            new NormdatenReferenz("HSP-2", "Berlin", "GND-2", NormdatenReferenz.ORT_TYPE_NAME))
        .withErstellungsdatum(LocalDate.of(2020, Month.MARCH, 14))
        .withID("D-1")
        .withManifest("https://hsp.de/manifest.json")
        .withThumbnail("https://hsp.de/thumbnail.jpg")
        .withTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL)
        .build();

    DigitalisatViewModel viewModel = DigitalisatViewModel.map(digitalisat);
    assertNotNull(viewModel);
    assertEquals(digitalisat.getId(), viewModel.getId());
    assertEquals(digitalisat.getAlternativeURL().toASCIIString(), viewModel.getAlternativeUrl());
    assertEquals(digitalisat.getDigitalisierungsDatum(), viewModel.getDigitalisierungsdatum());
    assertEquals(digitalisat.getDigitalisierendeEinrichtung(), viewModel.getEinrichtung());
    assertEquals(digitalisat.getEntstehungsOrt(), viewModel.getOrt());
    assertEquals(digitalisat.getManifestURL().toASCIIString(), viewModel.getManifestURL());
    assertEquals(digitalisat.getThumbnailURL().toASCIIString(), viewModel.getThumbnail());
    assertEquals(digitalisat.getTyp(), viewModel.getDigitalisatTyp());
    assertEquals(digitalisat.getErstellungsDatum(), viewModel.getErstellungsdatum());
  }

}
