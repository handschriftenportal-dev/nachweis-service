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

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.TITEL_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.wildfly.common.Assert.assertFalse;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;
import org.wildfly.common.Assert;

class IndexTitleFactoryTest {

  private static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";
  static Titel createTitel() {
    return Titel.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("TI-1")
        .withReferenzURL(REFERENZ_URL)
        .withText("Titel der Veschreibung")
        .build();
  }

  @Test
  void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexTitleFactory.build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Titel titel = createTitel();
    IndexTitleFactory.build(titel, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.get(0);

    Assert.assertNotNull(index);
    assertEquals(AttributsTyp.TITEL.getIndexName(), index.getIndexName());
    Assert.assertNotNull(index.getSources());
    assertEquals(REFERENZ_URL,index.getSources().get(0));
    assertEquals(TEIValues.TEI_ANKER_TAG+titel.getReferenzId(),index.getCopyOf());
    assertTerm(index, TITEL_TERM_TYP_TEXT, titel.getText());
  }

  @Test
  void testBuild_empty() {
    List<Index> ergebnis = new ArrayList<>();

    Titel titel = Titel.builder().build();
    IndexTitleFactory.build(titel, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.get(0);

    Assert.assertNotNull(index);
    Assert.assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());
    assertTerm(index, TITEL_TERM_TYP_TEXT, null);
  }

  @Test
  void testsetIndexSourceAndCopyOf() {
    Index index = new Index();

    Assertions.assertNotNull(index);

    IndexFactory.setIndexSourceAndCopyOf(index,"BS1","");

    assertEquals("#BS1",index.getCopyOf());
  }
}
