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

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Head;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;

class FormatFactoryTest {

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";

  @Test
  void testBuild() {
    TEI tei = new TEI();

    FormatFactory formatFactory = new FormatFactory();

    MsDesc msDesc = new MsDesc();
    assertNull(formatFactory.build(msDesc, tei));

    msDesc = createMsDesc();
    Format format = formatFactory.build(msDesc, tei);

    assertNotNull(format);
    assertEquals("HSP-a44d2fd1-3ce6-34cf-8501-2e8833ba964f", format.getId());
    assertSame(AttributsTyp.FORMAT, format.getAttributsTyp());
    assertEquals("HSP-123", format.getReferenzId());
    assertSame(AttributsReferenzTyp.BESCHREIBUNG, format.getReferenzTyp());
    assertEquals("smaller_than_octavo;long_and_narrow", format.getText());

    assertNotNull(format.getFormatWerte());
    assertEquals(2, format.getFormatWerte().size());
    assertTrue(checkFormatWert(format.getFormatWerte(), "smaller_than_octavo", "deduced"));
    assertTrue(checkFormatWert(format.getFormatWerte(), "long_and_narrow", "factual"));
    assertEquals(HTTP_RESOLVER_SPK_DE_PURL,format.getReferenzURL());
  }

  private boolean checkFormatWert(List<FormatWert> werte, String text, String typ) {
    return werte.stream().anyMatch(wert -> text.equals(wert.getText()) && typ.equals(wert.getTyp()));
  }

  private MsDesc createMsDesc() {
    MsDesc msDesc = new MsDesc();
    msDesc.setId("HSP-123");

    Head head = new Head();
    msDesc.getHeads().add(head);

    Index index_1 = createIndex(AttributsTyp.FORMAT.getIndexName(),"HSP-123",HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index_1);

    addTermToIndex(index_1, TEIValues.FORMAT_TERM_TYP_TEXT, "smaller than octavo");
    addTermToIndex(index_1, TEIValues.FORMAT_TERM_TYP_TYPE, "deduced");

    Index index_2 = createIndex(AttributsTyp.FORMAT.getIndexName(),"HSP-123",HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index_2);

    addTermToIndex(index_2, TEIValues.FORMAT_TERM_TYP_TEXT, "long and narrow");
    addTermToIndex(index_2, TEIValues.FORMAT_TERM_TYP_TYPE, "factual");

    return msDesc;
  }
}
