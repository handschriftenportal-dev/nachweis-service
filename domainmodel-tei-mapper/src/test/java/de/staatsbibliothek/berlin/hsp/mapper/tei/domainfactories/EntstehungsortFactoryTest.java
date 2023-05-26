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

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Head;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.Term;

class EntstehungsortFactoryTest {

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";
  @Test
  void testBuild() {
    TEI tei = new TEI();

    EntstehungsortFactory entstehungsortFactory = new EntstehungsortFactory();

    MsDesc msDesc = new MsDesc();
    assertNull(entstehungsortFactory.build(msDesc, tei));

    msDesc = createMsDesc();
    Entstehungsort entstehungsort = entstehungsortFactory.build(msDesc, tei);

    assertNotNull(entstehungsort);
    assertEquals("HSP-f12c2125-44b7-3050-b37e-4e71da8ab10f", entstehungsort.getId());
    assertSame(AttributsTyp.ENTSTEHUNGSORT, entstehungsort.getAttributsTyp());
    assertEquals("HSP-123", entstehungsort.getReferenzId());
    assertSame(AttributsReferenzTyp.BESCHREIBUNG, entstehungsort.getReferenzTyp());
    assertEquals("Berlin (Fasz. 1), Potsdam (Fasz. 2)", entstehungsort.getText());
    assertNotNull(entstehungsort.getNormdatenReferenzen());
    assertEquals(2, entstehungsort.getNormdatenReferenzen().size());

    assertTrue(checkNormdatenReferenz(entstehungsort.getNormdatenReferenzen(),
        "NORM-123", "4005728-8", "Berlin"));

    assertTrue(checkNormdatenReferenz(entstehungsort.getNormdatenReferenzen(),
        "NORM-456", "4046948-7", "Potsdam"));
    assertEquals(HTTP_RESOLVER_SPK_DE_PURL,entstehungsort.getReferenzURL());
  }

  @Test
  void testMapToNormdatenReferenz() {
    Term term = new Term();
    term.setKey("NORM-123");
    term.getReves().add("https://d-nb.info/gnd/4046948-7");
    term.getContent().add("Potsdam");

    EntstehungsortFactory entstehungsortFactory = new EntstehungsortFactory();
    NormdatenReferenz normdatenReferenz =
        entstehungsortFactory.mapToNormdatenReferenz(term, NormdatenReferenz.ORT_TYPE_NAME);

    assertNotNull(normdatenReferenz);
    assertEquals("NORM-123", normdatenReferenz.getId());
    assertEquals("4046948-7", normdatenReferenz.getGndID());
    assertEquals("Potsdam", normdatenReferenz.getName());
  }

  private boolean checkNormdatenReferenz(List<NormdatenReferenz> normdatenReferenzen,
      String id, String gndId, String name) {
    return normdatenReferenzen.stream()
        .anyMatch(nr -> id.equals(nr.getId())
            && gndId.equals(nr.getGndID())
            && name.equals(nr.getName())
            && NormdatenReferenz.ORT_TYPE_NAME.equals(nr.getTypeName()));
  }

  private MsDesc createMsDesc() {
    MsDesc msDesc = new MsDesc();
    msDesc.setId("HSP-123");

    Head head = new Head();
    msDesc.getHeads().add(head);

    Index index = new Index();
    index.setIndexName(AttributsTyp.ENTSTEHUNGSORT.getIndexName());
    index.getSources().add(HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index);

    Term origPlace = createTerm(TEIValues.ENTSTEHUNGSORT_TERM_TYP_TEXT,
        "Berlin (Fasz. 1), Potsdam (Fasz. 2)");
    index.getTermsAndIndices().add(origPlace);

    Term normBerlin = createTerm(TEIValues.ENTSTEHUNGSORT_TERM_TYP_NORM, "Berlin");
    normBerlin.setKey("NORM-123");
    normBerlin.getReves().add("https://d-nb.info/gnd/4005728-8");
    index.getTermsAndIndices().add(normBerlin);

    Term normPotsdam = createTerm(TEIValues.ENTSTEHUNGSORT_TERM_TYP_NORM, "Potsdam");
    normPotsdam.setKey("NORM-456");
    normPotsdam.getReves().add("https://d-nb.info/gnd/4046948-7");
    index.getTermsAndIndices().add(normPotsdam);

    return msDesc;
  }

}
