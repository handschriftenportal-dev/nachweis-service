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

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.FORMAT_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.FORMAT_TERM_TYP_TYPE;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.wildfly.common.Assert.assertFalse;
import static org.wildfly.common.Assert.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;

class IndexFormatFactoryTest {

  public static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";

  static Format createFormat() {
    return Format.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("FO-1")
        .withReferenzURL(REFERENZ_URL)
        .addFormatWert(FormatWert.builder()
            .withText("long and narrow")
            .withTyp("factual")
            .build())
        .addFormatWert(
            FormatWert.builder()
                .withText("larger than folio")
                .withTyp("computed")
                .build())
        .build();
  }

  @Test
  void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexFormatFactory.build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Format format = createFormat();

    IndexFormatFactory.build(format, ergebnis);

    assertFalse(ergebnis.isEmpty());
    assertEquals(2, ergebnis.size());

    Index index = ergebnis.get(0);

    assertNotNull(index);
    assertEquals(AttributsTyp.FORMAT.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());

    assertTerm(index, FORMAT_TERM_TYP_TEXT, "long and narrow");
    assertTerm(index, FORMAT_TERM_TYP_TYPE, format.getFormatWerte().get(0).getTyp());

    index = ergebnis.get(1);

    assertNotNull(index);
    assertEquals(AttributsTyp.FORMAT.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(REFERENZ_URL,index.getSources().get(0));
    assertEquals(TEIValues.TEI_ANKER_TAG+format.getReferenzId(),index.getCopyOf());

    assertTerm(index, FORMAT_TERM_TYP_TEXT, "larger than folio");
    assertTerm(index, FORMAT_TERM_TYP_TYPE, format.getFormatWerte().get(1).getTyp());
  }

  @Test
  void testBuild_empty() {
    Format format = Format.builder().build();

    List<Index> ergebnis = new ArrayList<>();

    IndexFormatFactory.build(format, ergebnis);

    assertFalse(ergebnis.isEmpty());
    assertEquals(1, ergebnis.size());

    Index index = ergebnis.get(0);

    assertNotNull(index);
    assertEquals(AttributsTyp.FORMAT.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());
    assertTerm(index, FORMAT_TERM_TYP_TEXT, null);
    assertTerm(index, FORMAT_TERM_TYP_TYPE, null);
  }
}
