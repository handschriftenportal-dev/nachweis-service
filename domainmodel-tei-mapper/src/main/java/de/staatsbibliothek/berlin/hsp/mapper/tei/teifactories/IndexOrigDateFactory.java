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
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.createTerm;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setIndexSourceAndCopyOf;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.List;
import java.util.Objects;
import org.tei_c.ns._1.Index;

class IndexOrigDateFactory {

  private IndexOrigDateFactory() {
    // do nothing
  }

  public static void build(Entstehungszeit entstehungszeit, List<Index> ergebnis) {
    if (Objects.isNull(entstehungszeit)) {
      return;
    }

    if (Objects.isNull(entstehungszeit.getEntstehungszeitWerte()) || entstehungszeit.getEntstehungszeitWerte()
        .isEmpty()) {
      ergebnis.add(buildIndex(null, entstehungszeit.getReferenzId(), null, null, null,
          entstehungszeit.getReferenzURL()));
    } else {
      entstehungszeit.getEntstehungszeitWerte().forEach(
          entstehungszeitWert -> ergebnis.add(
              buildIndex(entstehungszeitWert.getText(),
                  entstehungszeit.getReferenzId(),
                  entstehungszeitWert.getNichtVor(),
                  entstehungszeitWert.getNichtNach(),
                  entstehungszeitWert.getTyp(),
                  entstehungszeit.getReferenzURL())
          ));
    }
  }

  static Index buildIndex(String text, String source, String notBefore, String notAfter, String type,
      String referenzBaseUrl) {
    Index index = new Index();
    index.setIndexName(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName());
    setIndexSourceAndCopyOf(index, source, referenzBaseUrl);
    index.getTermsAndIndices().add(createTerm(ENTSTEHUNGSZEIT_TERM_TYP_TEXT, text));
    index.getTermsAndIndices().add(createTerm(ENTSTEHUNGSZEIT_TERM_TYP_NOT_BEFORE, notBefore));
    index.getTermsAndIndices().add(createTerm(ENTSTEHUNGSZEIT_TERM_TYP_NOT_AFTER, notAfter));
    index.getTermsAndIndices().add(createTerm(ENTSTEHUNGSZEIT_TERM_TYP_TYPE, type));
    return index;
  }

}
