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

package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.BESCHREIBSTOFF_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.BESCHREIBSTOFF_TERM_TYP_TYPE;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.findTerms;
import static de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon.getContentAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.wildfly.common.Assert.assertFalse;
import static org.wildfly.common.Assert.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.Term;

class IndexMaterialFactoryTest {

  private static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";
  static Beschreibstoff createBeschreibstoff() {
    return Beschreibstoff.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("MA-1")
        .withReferenzURL(REFERENZ_URL)
        .withText("Pergament, Papyrus")
        .withTypen(List.of("parchment", "papyrus"))
        .build();
  }

  @Test
  void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexMaterialFactory.build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Beschreibstoff beschreibstoff = createBeschreibstoff();
    IndexMaterialFactory.build(beschreibstoff, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.get(0);

    assertNotNull(index);
    assertEquals(AttributsTyp.BESCHREIBSTOFF.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(REFERENZ_URL,index.getSources().get(0));
    assertEquals(TEIValues.TEI_ANKER_TAG+beschreibstoff.getReferenzId(),index.getCopyOf());

    assertTerm(index, BESCHREIBSTOFF_TERM_TYP_TEXT, beschreibstoff.getText());

    List<Term> terme = findTerms(index, BESCHREIBSTOFF_TERM_TYP_TYPE);

    assertTrue(terme.stream()
        .anyMatch(term -> beschreibstoff.getTypen().get(0).equals(getContentAsString(term.getContent()))));
    assertTrue(terme.stream()
        .anyMatch(term -> beschreibstoff.getTypen().get(1).equals(getContentAsString(term.getContent()))));
  }

  @Test
  void testBuild_empty() {
    Beschreibstoff beschreibstoff = Beschreibstoff.builder().build();

    List<Index> ergebnis = new ArrayList<>();

    IndexMaterialFactory.build(beschreibstoff, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.get(0);

    assertNotNull(index);
    assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());

    assertTerm(index, BESCHREIBSTOFF_TERM_TYP_TEXT, null);
    assertTerm(index, BESCHREIBSTOFF_TERM_TYP_TYPE, null);
  }

}
