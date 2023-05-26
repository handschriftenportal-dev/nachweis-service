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

import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactoryTest.createTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Head;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.Term;

class GrundSpracheFactoryTest {

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";
  @Test
  void testBuild() {
    TEI tei = new TEI();

    GrundspracheFactory grundspracheFactory = new GrundspracheFactory();

    MsDesc msDesc = new MsDesc();
    assertNull(grundspracheFactory.build(msDesc, tei));

    msDesc = createMsDesc();
    Grundsprache grundsprache = grundspracheFactory.build(msDesc, tei);

    assertNotNull(grundsprache);
    assertEquals("HSP-c78baa8d-9f4a-3aa7-a8f9-b429eea63244", grundsprache.getId());
    assertSame(AttributsTyp.GRUNDSPRACHE, grundsprache.getAttributsTyp());
    assertEquals("HSP-123", grundsprache.getReferenzId());
    assertSame(AttributsReferenzTyp.BESCHREIBUNG, grundsprache.getReferenzTyp());
    assertEquals("Alt- und Mittelhochdeutsch", grundsprache.getText());
    assertNotNull(grundsprache.getNormdatenIds());
    assertEquals(2, grundsprache.getNormdatenIds().size());
    assertTrue(grundsprache.getNormdatenIds().contains("4001523-3"));
    assertTrue(grundsprache.getNormdatenIds().contains("4039687-3"));
    assertEquals(HTTP_RESOLVER_SPK_DE_PURL,grundsprache.getReferenzURL());
  }

  private MsDesc createMsDesc() {
    MsDesc msDesc = new MsDesc();
    msDesc.setId("HSP-123");

    Head head = new Head();
    msDesc.getHeads().add(head);

    Index index = new Index();
    index.setIndexName(AttributsTyp.GRUNDSPRACHE.getIndexName());
    index.getSources().add(HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index);

    Term textLang = createTerm(TEIValues.GRUNDSPRACHE_TERM_TYP_TEXT, "Alt- und Mittelhochdeutsch");
    index.getTermsAndIndices().add(textLang);

    index.getTermsAndIndices().add(createTerm(TEIValues.GRUNDSPRACHE_TERM_TYP_NORMDATUM_ID, "4001523-3"));
    index.getTermsAndIndices().add(createTerm(TEIValues.GRUNDSPRACHE_TERM_TYP_NORMDATUM_ID, "4039687-3"));

    return msDesc;
  }
}
