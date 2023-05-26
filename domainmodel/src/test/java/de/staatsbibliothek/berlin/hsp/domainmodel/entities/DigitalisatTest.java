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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 08.10.2020.
 * @version 1.0
 */
public class DigitalisatTest {

  @Test
  void testMinimalConstruction() {

    Digitalisat digitalisat = Digitalisat.DigitalisatBuilder()
        .build();
    assertNotNull(digitalisat);

  }

  @Test
  void testAdvancedConstruction() {

    Digitalisat digitalisat = Digitalisat.DigitalisatBuilder()
        .withErstellungsdatum(LocalDate.now())
        .withTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL)
        .withManifest("http://manifest.json")
        .build();
    assertNotNull(digitalisat);
    assertNotNull(digitalisat.getErstellungsDatum());

    assertEquals(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL, digitalisat.getTyp());
    assertEquals("http://manifest.json", digitalisat.getManifestURL().toASCIIString());

  }

  @Test
  void testDigitalisatTypfromString() {

    assertEquals(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL, DigitalisatTyp.fromString("completeFromOriginal"));
    assertEquals(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL, DigitalisatTyp.fromString("komplettVomOriginal"));

    assertEquals(DigitalisatTyp.KOMPLETT_VON_REPRODUKTION, DigitalisatTyp.fromString("komplettVonReproduktion"));
    assertEquals(DigitalisatTyp.KOMPLETT_VON_REPRODUKTION, DigitalisatTyp.fromString("completeFromSurrogate"));

    assertEquals(DigitalisatTyp.WASSERZEICHEN, DigitalisatTyp.fromString("Wasserzeichen"));
    assertEquals(DigitalisatTyp.WASSERZEICHEN, DigitalisatTyp.fromString("watermark"));
  }
}
