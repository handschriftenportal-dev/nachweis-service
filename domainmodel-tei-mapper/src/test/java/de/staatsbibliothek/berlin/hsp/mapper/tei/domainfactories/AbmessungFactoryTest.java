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

package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactoryTest.addTermToIndex;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactoryTest.createIndex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AbmessungWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Head;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;

class AbmessungFactoryTest {

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";

  @Test
  void testBuild() {
    TEI tei = new TEI();

    AbmessungFactory abmessungFactory = new AbmessungFactory();

    MsDesc msDesc = new MsDesc();
    assertNull(abmessungFactory.build(msDesc, tei));

    msDesc = createMsDesc();
    Abmessung abmessung = abmessungFactory.build(msDesc, tei);

    assertNotNull(abmessung);
    assertEquals("HSP-a492b67e-78f7-3121-803a-ebadc1357ecf", abmessung.getId());
    assertSame(AttributsTyp.ABMESSUNG, abmessung.getAttributsTyp());
    assertEquals("HSP-123", abmessung.getReferenzId());
    assertSame(AttributsReferenzTyp.BESCHREIBUNG, abmessung.getReferenzTyp());
    assertEquals("23,5 × 16,5 x 2,5; 25 × 15 x 5", abmessung.getText());

    assertNotNull(abmessung.getAbmessungWerte());
    assertEquals(2, abmessung.getAbmessungWerte().size());
    assertTrue(checkAbmessungWert(abmessung.getAbmessungWerte(),
        "23,5 × 16,5 x 2,5", "23,5", "16,5", "2,5", "factual"));
    assertTrue(checkAbmessungWert(abmessung.getAbmessungWerte(),
        "25 × 15 x 5", "25", "15", "5", "deduced"));

    assertEquals(HTTP_RESOLVER_SPK_DE_PURL,abmessung.getReferenzURL());
  }

  private boolean checkAbmessungWert(List<AbmessungWert> werte,
      String text, String hoehe, String breite, String tiefe, String typ) {
    return werte.stream().anyMatch(wert ->
        text.equals(wert.getText())
            && hoehe.equals(wert.getHoehe())
            && breite.equals(wert.getBreite())
            && tiefe.equals(wert.getTiefe())
            && typ.equals(wert.getTyp()));
  }

  private MsDesc createMsDesc() {
    MsDesc msDesc = new MsDesc();
    msDesc.setId("HSP-123");

    Head head = new Head();
    msDesc.getHeads().add(head);

    Index index_1 = createIndex(AttributsTyp.ABMESSUNG.getIndexName(),"HSP-123",
        HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index_1);

    addTermToIndex(index_1, TEIValues.ABMESSUNG_TERM_TYP_TEXT, "23,5 × 16,5 x 2,5");
    addTermToIndex(index_1, TEIValues.ABMESSUNG_TERM_TYP_HEIGHT, "23,5");
    addTermToIndex(index_1, TEIValues.ABMESSUNG_TERM_TYP_WIDTH, "16,5");
    addTermToIndex(index_1, TEIValues.ABMESSUNG_TERM_TYP_DEPTH, "2,5");
    addTermToIndex(index_1, TEIValues.ABMESSUNG_TERM_TYP_TYPE, "factual");

    Index index_2 = createIndex(AttributsTyp.ABMESSUNG.getIndexName(),"HSP-123",HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index_2);

    addTermToIndex(index_2, TEIValues.ABMESSUNG_TERM_TYP_TEXT, "25 × 15 x 5");
    addTermToIndex(index_2, TEIValues.ABMESSUNG_TERM_TYP_HEIGHT, "25");
    addTermToIndex(index_2, TEIValues.ABMESSUNG_TERM_TYP_WIDTH, "15");
    addTermToIndex(index_2, TEIValues.ABMESSUNG_TERM_TYP_DEPTH, "5");
    addTermToIndex(index_2, TEIValues.ABMESSUNG_TERM_TYP_TYPE, "deduced");

    return msDesc;
  }

}
