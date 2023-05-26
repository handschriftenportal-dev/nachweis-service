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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.detail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konstantin.goerlitz@sbb.spk-berlin.de
 */
public class SpracheDetailControllerTest {
  SpracheDetailController spracheDetailController;

  @BeforeEach
  void setup() {
    spracheDetailController = new SpracheDetailController(Mockito.mock(NormdatenReferenzBoundary.class));
    spracheDetailController.setModel(createLanguageNormdatenReferenz());
  }

  @Test
  void testSetup() {
    assertEquals("13", spracheDetailController.getModel().getId());
    assertEquals("Deutsch", spracheDetailController.getModel().getName());
    assertEquals("4113292-0", spracheDetailController.getModel().getGndID());
    assertEquals("Hochdeutsch; Neuhochdeutsch; Deutsche Sprache; Deutsch",
        spracheDetailController.getModel().getVarianteNamenAlsString());

    assertEquals(1, spracheDetailController.getIso6391Identifikatoren().size());
    assertEquals(2, spracheDetailController.getIso6392Identifikatoren().size());
  }

  private NormdatenReferenz createLanguageNormdatenReferenz() {
    return new NormdatenReferenzBuilder()
        .withId("13")
        .withTypeName("Language")
        .withName("Deutsch")
        .withGndID("4113292-0")
        .addIdentifikator(
            new Identifikator("deu", "http://id.loc.gov/vocabulary/iso639-1/de", "ISO_639-1"))
        .addIdentifikator(
            new Identifikator("deu", "http://id.loc.gov/vocabulary/iso639-2/deu", "ISO_639-2"))
        .addIdentifikator(
            new Identifikator("ger", "http://id.loc.gov/vocabulary/iso639-2/ger", "ISO_639-2"))
        .addIdentifikator(new Identifikator("de", "", ""))
        .addVarianterName(new VarianterName("Neuhochdeutsch", "de"))
        .addVarianterName(new VarianterName("Deutsche Sprache", "de"))
        .addVarianterName(new VarianterName("Hochdeutsch", "de"))
        .addVarianterName(new VarianterName("Deutsch", "de"))
        .build();
  }
}
