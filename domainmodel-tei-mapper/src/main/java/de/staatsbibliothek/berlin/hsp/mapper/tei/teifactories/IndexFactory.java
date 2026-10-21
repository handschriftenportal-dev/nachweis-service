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

  private static final Map<AttributsTyp, FactoryConsumer> FACTORIES = new EnumMap<>(
      AttributsTyp.class);

  static {
    FACTORIES.put(AttributsTyp.TITEL, (referenz, ergebnis)
        -> IndexTitleFactory.build((Titel) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.BESCHREIBSTOFF, (referenz, ergebnis)
        -> IndexMaterialFactory.getInstance().build((Beschreibstoff) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.UMFANG, (referenz, ergebnis)
        -> IndexMeasureFactory.build((Umfang) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.ABMESSUNG, (referenz, ergebnis)
        -> IndexDimensionsFactory.build((Abmessung) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.FORMAT, (referenz, ergebnis)
        -> IndexFormatFactory.getInstance().build((Format) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.ENTSTEHUNGSORT, (referenz, ergebnis)
        -> IndexOrigPlaceFactory.build((Entstehungsort) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.ENTSTEHUNGSZEIT, (referenz, ergebnis)
        -> IndexOrigDateFactory.build((Entstehungszeit) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.GRUNDSPRACHE, (referenz, ergebnis)
        -> IndexTextLangFactory.getInstance().build((Grundsprache) referenz, ergebnis));
    FACTORIES.put(AttributsTyp.HANDSCHRIFTENTYP, (referenz, ergebnis)
        -> IndexFormFactory.getInstance().build((Handschriftentyp) referenz, ergebnis));
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
      if (referenzURL != null && !referenzURL.isEmpty()) {
        index.getSources().add(referenzURL);
      }

      index.setCopyOf(TEIValues.TEI_ANKER_TAG + referenzId.replaceAll(TEIValues.TEI_ANKER_TAG, ""));
    }
  }

  public static void setChangeID(Index index, AttributsReferenz referenz) {
    if (referenz.getChangeID() != null) {
      index.getChanges().clear();
      index.getChanges().add(TEIValues.TEI_ANKER_TAG + referenz.getChangeID());
    }
  }

  @FunctionalInterface
  interface FactoryConsumer {

    void accept(AttributsReferenz attributsReferenz, List<Index> result);
  }

}
