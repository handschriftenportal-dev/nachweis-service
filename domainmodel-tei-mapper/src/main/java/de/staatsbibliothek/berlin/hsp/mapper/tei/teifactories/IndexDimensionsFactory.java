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

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_DEPTH;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_HEIGHT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_TYPE;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_WIDTH;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.createTerm;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setIndexSourceAndCopyOf;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.List;
import java.util.Objects;
import org.tei_c.ns._1.Index;

class IndexDimensionsFactory {

  private IndexDimensionsFactory() {
    // do nothing
  }

  public static void build(Abmessung abmessung, List<Index> ergebnis) {
    if (Objects.isNull(abmessung)) {
      return;
    }

    if (Objects.isNull(abmessung.getAbmessungWerte()) || abmessung.getAbmessungWerte().isEmpty()) {
      ergebnis.add(buildIndex(null, abmessung.getReferenzId(), null, null,
          null, null, abmessung.getReferenzURL()));
    } else {
      abmessung.getAbmessungWerte().forEach(
          abmessungWert -> ergebnis.add(
              buildIndex(abmessung.getReferenzId(),
                  abmessungWert.getText(),
                  abmessungWert.getHoehe(),
                  abmessungWert.getBreite(),
                  abmessungWert.getTiefe(),
                  abmessungWert.getTyp(),
                  abmessung.getReferenzURL())));
    }
  }

  static Index buildIndex(String referenzId, String text, String hoehe, String breite, String tiefe, String type,
      String referenzBaseUrl) {
    Index index = new Index();
    index.setIndexName(AttributsTyp.ABMESSUNG.getIndexName());
    setIndexSourceAndCopyOf(index, referenzId, referenzBaseUrl);
    index.getTermsAndIndices().add(createTerm(ABMESSUNG_TERM_TYP_TEXT, text));
    index.getTermsAndIndices().add(createTerm(ABMESSUNG_TERM_TYP_HEIGHT, hoehe));
    index.getTermsAndIndices().add(createTerm(ABMESSUNG_TERM_TYP_WIDTH, breite));
    index.getTermsAndIndices().add(createTerm(ABMESSUNG_TERM_TYP_DEPTH, tiefe));
    index.getTermsAndIndices().add(createTerm(ABMESSUNG_TERM_TYP_TYPE, type));
    return index;
  }

}
