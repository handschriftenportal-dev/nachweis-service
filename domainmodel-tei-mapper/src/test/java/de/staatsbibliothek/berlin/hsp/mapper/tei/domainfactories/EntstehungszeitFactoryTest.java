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

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.EntstehungszeitWert;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Head;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;

class EntstehungszeitFactoryTest {

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";

  @Test
  void testBuild() {
    TEI tei = new TEI();

    EntstehungszeitFactory entstehungszeitFactory = new EntstehungszeitFactory();

    MsDesc msDesc = new MsDesc();
    assertNull(entstehungszeitFactory.build(msDesc, tei));

    msDesc = createMsDesc();
    Entstehungszeit entstehungszeit = entstehungszeitFactory.build(msDesc, tei);

    assertNotNull(entstehungszeit);
    assertEquals("HSP-15ade495-213e-393c-a3c1-f82bd70c2de2", entstehungszeit.getId());
    assertSame(AttributsTyp.ENTSTEHUNGSZEIT, entstehungszeit.getAttributsTyp());
    assertEquals("HSP-123", entstehungszeit.getReferenzId());
    assertSame(AttributsReferenzTyp.BESCHREIBUNG, entstehungszeit.getReferenzTyp());
    assertEquals("um 1210 (Fasz. I); um 1240 (Fasz. II)", entstehungszeit.getText());

    assertNotNull(entstehungszeit.getEntstehungszeitWerte());
    assertEquals(2, entstehungszeit.getEntstehungszeitWerte().size());
    assertTrue(checkEntstehungszeitWert(entstehungszeit.getEntstehungszeitWerte(),
        "um 1210 (Fasz. I)", "1200", "1220", "datable"));
    assertTrue(checkEntstehungszeitWert(entstehungszeit.getEntstehungszeitWerte(),
        "um 1240 (Fasz. II)", "1230", "1250", "dated"));
    assertEquals(HTTP_RESOLVER_SPK_DE_PURL,entstehungszeit.getReferenzURL());
  }

  private boolean checkEntstehungszeitWert(List<EntstehungszeitWert> werte,
      String text, String nichtVor, String nichtNach, String typ) {
    return werte.stream().anyMatch(wert ->
        text.equals(wert.getText())
            && nichtVor.equals(wert.getNichtVor())
            && nichtNach.equals(wert.getNichtNach())
            && typ.equals(wert.getTyp()));
  }

  private MsDesc createMsDesc() {
    MsDesc msDesc = new MsDesc();
    msDesc.setId("HSP-123");

    Head head = new Head();
    msDesc.getHeads().add(head);

    Index index_1 = createIndex(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName(),"HSP-123",HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index_1);

    addTermToIndex(index_1, TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TEXT, "um 1210 (Fasz. I)");
    addTermToIndex(index_1, TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_NOT_BEFORE, "1200");
    addTermToIndex(index_1, TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_NOT_AFTER, "1220");
    addTermToIndex(index_1, TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TYPE, "datable");

    Index index_2 = createIndex(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName(),"HSP-123",HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index_2);

    addTermToIndex(index_2, TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TEXT, "um 1240 (Fasz. II)");
    addTermToIndex(index_2, TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_NOT_BEFORE, "1230");
    addTermToIndex(index_2, TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_NOT_AFTER, "1250");
    addTermToIndex(index_2, TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TYPE, "dated");

    return msDesc;
  }

}
