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

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_NOT_AFTER;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_NOT_BEFORE;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TYPE;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.wildfly.common.Assert.assertFalse;
import static org.wildfly.common.Assert.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.EntstehungszeitWert;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;

class IndexOrigDateFactoryTest {

  private static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";

  static Entstehungszeit createEntstehungszeit() {
    return Entstehungszeit.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("OD-1")
        .withReferenzURL(REFERENZ_URL)
        .addEntstehungszeitWert(EntstehungszeitWert.builder()
            .withText("1446 oder vorher (Fasz. I)")
            .withNichtVor("1426")
            .withNichtNach("1446")
            .withTyp("datable")
            .build())
        .addEntstehungszeitWert(
            EntstehungszeitWert.builder()
                .withText("1446 (Fasz. II)")
                .withNichtVor("1446")
                .withNichtNach("1446")
                .withTyp("dated")
                .build())
        .build();
  }

  @Test
  void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexOrigDateFactory.build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Entstehungszeit entstehungszeit = createEntstehungszeit();

    IndexOrigDateFactory.build(entstehungszeit, ergebnis);

    assertFalse(ergebnis.isEmpty());
    assertEquals(2, ergebnis.size());

    Index index = ergebnis.get(0);

    assertNotNull(index);
    assertEquals(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(TEIValues.TEI_ANKER_TAG+entstehungszeit.getReferenzId(),index.getCopyOf());
    assertEquals(REFERENZ_URL,index.getSources().get(0));

    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_TEXT,
        entstehungszeit.getEntstehungszeitWerte().get(0).getText());
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_NOT_BEFORE,
        entstehungszeit.getEntstehungszeitWerte().get(0).getNichtVor());
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_NOT_AFTER,
        entstehungszeit.getEntstehungszeitWerte().get(0).getNichtNach());
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_TYPE,
        entstehungszeit.getEntstehungszeitWerte().get(0).getTyp());

    index = ergebnis.get(1);

    assertNotNull(index);
    assertEquals(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());

    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_TEXT,
        entstehungszeit.getEntstehungszeitWerte().get(1).getText());
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_NOT_BEFORE,
        entstehungszeit.getEntstehungszeitWerte().get(1).getNichtVor());
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_NOT_AFTER,
        entstehungszeit.getEntstehungszeitWerte().get(1).getNichtNach());
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_TYPE,
        entstehungszeit.getEntstehungszeitWerte().get(1).getTyp());
  }

  @Test
  void testBuild_empty() {
    Entstehungszeit entstehungszeit = Entstehungszeit.builder().build();

    List<Index> ergebnis = new ArrayList<>();

    IndexOrigDateFactory.build(entstehungszeit, ergebnis);

    assertFalse(ergebnis.isEmpty());
    assertEquals(1, ergebnis.size());

    Index index = ergebnis.get(0);

    assertNotNull(index);
    assertEquals(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_TEXT, null);
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_NOT_BEFORE, null);
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_NOT_AFTER, null);
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_TYPE, null);
  }

}
