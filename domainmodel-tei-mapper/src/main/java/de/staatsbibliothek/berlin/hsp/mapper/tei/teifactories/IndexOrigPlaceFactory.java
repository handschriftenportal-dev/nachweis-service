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

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSORT_TERM_TYP_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSORT_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setIndexSourceAndCopyOf;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.Term;

class IndexOrigPlaceFactory {

  private IndexOrigPlaceFactory() {
    // do nothing
  }

  public static void build(Entstehungsort entstehungsort, List<Index> ergebnis) {
    if (Objects.isNull(entstehungsort)) {
      return;
    }

    Index index = new Index();
    index.setIndexName(AttributsTyp.ENTSTEHUNGSORT.getIndexName());
    setIndexSourceAndCopyOf(index, entstehungsort.getReferenzId(), entstehungsort.getReferenzURL());

    index.getTermsAndIndices().add(IndexFactory.createTerm(ENTSTEHUNGSORT_TERM_TYP_TEXT, entstehungsort.getText()));

    if (Objects.isNull(entstehungsort.getNormdatenReferenzen())
        || entstehungsort.getNormdatenReferenzen().isEmpty()) {
      index.getTermsAndIndices().add(IndexFactory.createTerm(ENTSTEHUNGSORT_TERM_TYP_NORM, null));
    } else {
      index.getTermsAndIndices().addAll(buildTermsFromNormdatenReferenzen(entstehungsort.getNormdatenReferenzen()));
    }
    ergebnis.add(index);
  }

  static List<Term> buildTermsFromNormdatenReferenzen(List<NormdatenReferenz> normdatenReferenzen) {
    return Stream.ofNullable(normdatenReferenzen)
        .flatMap(Collection::stream)
        .map(IndexOrigPlaceFactory::createTerm)
        .collect(Collectors.toList());
  }

  private static Term createTerm(NormdatenReferenz normdatenReferenzen) {
    Objects.requireNonNull(normdatenReferenzen, "normdatenReferenzen is required!");
    Term term = new Term();
    term.setType(ENTSTEHUNGSORT_TERM_TYP_NORM);
    term.setKey(normdatenReferenzen.getId());
    Optional.ofNullable(normdatenReferenzen.getGndUrl()).ifPresent(value -> term.getReves().add(value));
    Optional.ofNullable(normdatenReferenzen.getName()).ifPresent(value -> term.getContent().add(value));
    return term;
  }

}
