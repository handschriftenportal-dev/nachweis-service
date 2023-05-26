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

import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactory.getCopyOfValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Head;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.Term;

class AttributsReferenzFactoryTest {

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";

  static Term createTerm(String termType, String termContent) {
    Term term = new Term();
    term.setType(termType);
    Optional.ofNullable(termContent).ifPresent(term.getContent()::add);
    return term;
  }

  static Index createIndex(String indexName, String id, String url) {
    Index index = new Index();
    index.setIndexName(indexName);
    index.setCopyOf(TEIValues.TEI_ANKER_TAG+id);
    index.getSources().add(url);
    return index;
  }

  static void addTermToIndex(Index index, String termType, String termContent) {
    Term term = new Term();
    term.setType(termType);
    Optional.ofNullable(termContent).ifPresent(term.getContent()::add);
    index.getTermsAndIndices().add(term);
  }

  @Test
  void testFindIndices() {
    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    msDesc.getHeads().add(head);
    head.getContent().add(createIndex(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName(),"HSP-123",HTTP_RESOLVER_SPK_DE_PURL));
    head.getContent().add(createIndex(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName(),"HSP-123",HTTP_RESOLVER_SPK_DE_PURL));
    head.getContent().add(createIndex(AttributsTyp.ENTSTEHUNGSORT.getIndexName(),"HSP-123",HTTP_RESOLVER_SPK_DE_PURL));

    List<Index> zeiten = AttributsReferenzFactory.findIndices(msDesc, AttributsTyp.ENTSTEHUNGSZEIT.getIndexName());
    assertNotNull(zeiten);
    assertEquals(2, zeiten.size());

    List<Index> orte = AttributsReferenzFactory.findIndices(msDesc, AttributsTyp.ENTSTEHUNGSORT.getIndexName());
    assertNotNull(orte);
    assertEquals(1, orte.size());

    List<Index> other = AttributsReferenzFactory.findIndices(msDesc, "other");
    assertNotNull(other);
    assertTrue(other.isEmpty());
  }

  @Test
  void testFindIndex() {
    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    msDesc.getHeads().add(head);
    head.getContent().add(createIndex(AttributsTyp.ENTSTEHUNGSORT.getIndexName(),"HSP-123",HTTP_RESOLVER_SPK_DE_PURL));

    Optional<Index> indexOrt = AttributsReferenzFactory.findIndex(msDesc, AttributsTyp.ENTSTEHUNGSORT.getIndexName());
    assertNotNull(indexOrt);
    assertTrue(indexOrt.isPresent());

    Optional<Index> indexOther = AttributsReferenzFactory.findIndex(msDesc, "other");
    assertNotNull(indexOther);
    assertTrue(indexOther.isEmpty());
  }

  @Test
  void testFindTerme() {
    Index index = createIndex(AttributsTyp.ENTSTEHUNGSORT.getIndexName(),"HSP-123",HTTP_RESOLVER_SPK_DE_PURL);
    index.getTermsAndIndices().add(createTerm("origPlace", "Hildesheim"));
    index.getTermsAndIndices().add(createIndex("other","HSP-123", HTTP_RESOLVER_SPK_DE_PURL));

    List<Term> terme = AttributsReferenzFactory.findTerme(index);
    assertNotNull(terme);
    assertEquals(1, terme.size());
  }

  @Test
  void testMapTermText() {
    Index index = new Index();
    index.setIndexName("indexName");

    Term term = new Term();
    term.setType("termType");
    term.getContent().add("termContent");
    index.getTermsAndIndices().add(term);

    assertEquals("termContent", AttributsReferenzFactory.mapTermText(index, "termType"));
    assertNull(AttributsReferenzFactory.mapTermText(index, "otherType"));
  }

  @Test
  void testGetCopyOfValue() {
    MsDesc msDesc = new MsDesc();
    msDesc.setType(DokumentObjektTyp.HSP_DESCRIPTION.toString());
    msDesc.setId("MSDesc1");

    assertEquals("MSDesc1", getCopyOfValue("",msDesc));
    assertEquals("CP1",getCopyOfValue("CP1",msDesc));
    assertEquals("CP1",getCopyOfValue("#CP1",msDesc));
  }
}
