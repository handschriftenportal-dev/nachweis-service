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

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.Term;

public class IndexFactory {

  private static final Map<AttributsTyp, FactoryConsumer> FACTORIES = new EnumMap<>(AttributsTyp.class);

  static {
    FACTORIES.put(AttributsTyp.TITEL, (referenz, ergebnis)
        -> IndexTitleFactory.build((Titel) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.BESCHREIBSTOFF, (referenz,  ergebnis)
        -> IndexMaterialFactory.build((Beschreibstoff) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.UMFANG, (referenz, ergebnis)
        -> IndexMeasureFactory.build((Umfang) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.ABMESSUNG, (referenz, ergebnis)
        -> IndexDimensionsFactory.build((Abmessung) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.FORMAT, (referenz, ergebnis)
        -> IndexFormatFactory.build((Format) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.ENTSTEHUNGSORT, (referenz, ergebnis)
        -> IndexOrigPlaceFactory.build((Entstehungsort) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.ENTSTEHUNGSZEIT, (referenz, ergebnis)
        -> IndexOrigDateFactory.build((Entstehungszeit) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.GRUNDSPRACHE, (referenz, ergebnis)
        -> IndexTextLangFactory.build((Grundsprache) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.HANDSCHRIFTENTYP, (referenz, ergebnis)
        -> IndexFormFactory.build((Handschriftentyp) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.STATUS, (referenz, ergebnis)
        -> IndexStatusFactory.build((Status) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.BUCHSCHMUCK, (referenz, ergebnis)
        -> IndexDecorationFactory.build((Buchschmuck) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.MUSIKNOTATION, (referenz, ergebnis)
        -> IndexMusicNotationFactory.build((Musiknotation) referenz, ergebnis));
  }

  private IndexFactory() {
    // do nothing
  }

  public static List<Index> build(Set<AttributsReferenz> attributsReferenzen) {
    List<Index> ergebnis = new ArrayList<>();

    Stream.ofNullable(attributsReferenzen)
        .flatMap(Collection::stream)
        .filter(Objects::nonNull)
        .forEach(referenz -> Optional.ofNullable(FACTORIES.get(referenz.getAttributsTyp()))
            .ifPresent(factoryConsumer -> factoryConsumer.accept(referenz, ergebnis)));

    return ergebnis;
  }

  static Term createTerm(String type, String content) {
    Objects.requireNonNull(type, "type is required!");
    Term term = new Term();
    term.setType(type);
    if (Objects.nonNull(content) && !content.isEmpty()) {
      term.getContent().add(content);
    }
    return term;
  }

  public static void setIndexSourceAndCopyOf(Index index, String referenzId, String referenzURL) {
    if (Stream.of(index, referenzId).allMatch(Objects::nonNull)
        && !referenzId.isEmpty()) {
      if(referenzURL != null && !referenzURL.isEmpty()) {
        index.getSources().add(referenzURL);
      }

      index.setCopyOf(TEIValues.TEI_ANKER_TAG+referenzId);
    }
  }

  @FunctionalInterface
  interface FactoryConsumer {
    void accept(AttributsReferenz attributsReferenz, List<Index> result);
  }

}
