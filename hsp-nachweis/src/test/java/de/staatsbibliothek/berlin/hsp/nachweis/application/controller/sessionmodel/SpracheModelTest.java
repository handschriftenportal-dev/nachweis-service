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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author konstantin.goerlitz@sbb.spk-berlin.de
 */
class SpracheModelTest {

  NormdatenReferenzBoundary normdatenReferenzBoundaryMock;

  SpracheModel spracheModel;

  NormdatenReferenz english;
  NormdatenReferenz german;

  @BeforeEach
  void setup() {

    english = new NormdatenReferenzBuilder()
        .withId("12")
        .withName("Englisch")
        .withGndID("gnd-123457")
        .addVarianterName(new VarianterName("english", "en"))
        .addVarianterName(new VarianterName("Englische Sprache", "de"))
        .addIdentifikator(new Identifikator("cs", "http://id.loc.gov/vocabulary/iso639-1/cs", "ISO_639-1"))
        .addIdentifikator(new Identifikator("ces", "http://id.loc.gov/vocabulary/iso639-2/ces", "ISO_639-2"))
        .build();

    german = new NormdatenReferenzBuilder()
        .withId("13")
        .withName("Deutsch")
        .withGndID("gnd-7654321")
        .addVarianterName(new VarianterName("deutsch", "de"))
        .addIdentifikator(new Identifikator("de", "http://id.loc.gov/vocabulary/iso639-1/de", "ISO_639-1"))
        .addIdentifikator(new Identifikator("deu", "http://id.loc.gov/vocabulary/iso639-2/deu", "ISO_639-2"))
        .addIdentifikator(new Identifikator("ger", "http://id.loc.gov/vocabulary/iso639-2/ger", "ISO_639-2"))
        .build();

    normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);
    when(normdatenReferenzBoundaryMock.findAllByIdOrNameAndType(null, NormdatenReferenz.SPRACHE_TYPE_NAME, true))
        .thenReturn(Set.of(english, german));

    spracheModel = new SpracheModel(normdatenReferenzBoundaryMock);
  }

  @Test
  void getAllSprachenTest() {
    spracheModel.setup();
    assertEquals(2, spracheModel.getAllSprachen().size());
  }

  @Test
  void setAllSprachenTest() {
    spracheModel.setup();
    assertEquals(2, spracheModel.getAllSprachen().size());
  }

  @Test
  void filteredSprachenTest() {
    spracheModel.setup();
    spracheModel.setFilteredSprachen(Arrays.asList(english, german));
    assertEquals(2, spracheModel.getFilteredSprachen().size());
  }

  @Test
  void iso6391IdentifierTest() {
    assertEquals(1, spracheModel.iso6391Identifikatoren(english).size());
    assertEquals(0, spracheModel.iso6391Identifikatoren(null).size());
  }

  @Test
  void iso6392IdentifierTest() {
    assertEquals(1, spracheModel.iso6392Identifikatoren(english).size());
    assertEquals(2, spracheModel.iso6392Identifikatoren(german).size());
    assertEquals(0, spracheModel.iso6392Identifikatoren(null).size());
  }

  @Test
  void testIdentifikatorenAsString() {
    assertEquals("ces",
        spracheModel.identifikatorenAsString(spracheModel.iso6392Identifikatoren(english)));
    assertEquals("deu ger",
        spracheModel.identifikatorenAsString(spracheModel.iso6392Identifikatoren(german)));
  }

}
