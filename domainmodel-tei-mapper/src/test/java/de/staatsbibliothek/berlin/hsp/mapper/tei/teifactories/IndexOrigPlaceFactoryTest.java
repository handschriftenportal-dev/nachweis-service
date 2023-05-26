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

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSORT_TERM_TYP_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSORT_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.findTerm;
import static de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon.getContentAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.wildfly.common.Assert.assertFalse;
import static org.wildfly.common.Assert.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.Term;

class IndexOrigPlaceFactoryTest {
  private static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";
  static Entstehungsort createEntstehungsort() {
    return Entstehungsort.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("OP-1")
        .withReferenzURL(REFERENZ_URL)
        .withText("Weißenburg")
        .addNormdatenReferenz(
            new NormdatenReferenz("NORM-f5c6758c-3c33-3785-933a-999b0fa2a4a0",
                "Weißenburg (Elsass)",
                "https://d-nb.info/gnd/4079134-8",
                NormdatenReferenz.ORT_TYPE_NAME)
        ).build();
  }

  @Test
  void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexOrigPlaceFactory.build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Entstehungsort entstehungsort = createEntstehungsort();
    IndexOrigPlaceFactory.build(entstehungsort, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.get(0);

    assertNotNull(index);
    assertEquals(AttributsTyp.ENTSTEHUNGSORT.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(REFERENZ_URL,index.getSources().get(0));
    assertEquals(TEIValues.TEI_ANKER_TAG+entstehungsort.getReferenzId(),index.getCopyOf());
    assertTerm(index, ENTSTEHUNGSORT_TERM_TYP_TEXT, entstehungsort.getText());

    assertNotNull(entstehungsort.getNormdatenReferenzen());
    assertEquals(1, entstehungsort.getNormdatenReferenzen().size());
    NormdatenReferenz normdatum = entstehungsort.getNormdatenReferenzen().get(0);

    Optional<Term> optNormTerm = findTerm(index, ENTSTEHUNGSORT_TERM_TYP_NORM);
    assertTrue(optNormTerm.isPresent());
    Term normTerm = optNormTerm.get();

    assertEquals(normdatum.getId(), normTerm.getKey());
    assertEquals(normdatum.getName(), getContentAsString(normTerm.getContent()));
    assertEquals(normdatum.getGndUrl(), getContentAsString(normTerm.getReves()));
  }

  @Test
  void testBuild_empty() {
    Entstehungsort entstehungsort = Entstehungsort.builder().build();

    List<Index> ergebnis = new ArrayList<>();

    IndexOrigPlaceFactory.build(entstehungsort, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.get(0);

    assertNotNull(index);
    assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());

    assertTerm(index, ENTSTEHUNGSORT_TERM_TYP_TEXT, null);
    assertTerm(index, ENTSTEHUNGSORT_TERM_TYP_NORM, null);
  }
}
