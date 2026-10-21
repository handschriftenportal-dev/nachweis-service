package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_NOT_AFTER;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_NOT_BEFORE;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TYPE;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.createTerm;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setChangeID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setIndexSourceAndCopyOf;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.ArrayList;
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

    List<Index> indexes = new ArrayList<>();

    if (Objects.isNull(entstehungszeit.getEntstehungszeitWerte()) || entstehungszeit.getEntstehungszeitWerte().isEmpty()) {
      indexes.add(buildIndex(null, entstehungszeit.getReferenzId(), null, null, null, entstehungszeit.getReferenzURL()));
    } else {
      entstehungszeit.getEntstehungszeitWerte().forEach(
          entstehungszeitWert -> indexes.add(
              buildIndex(entstehungszeitWert.getText(),
                  entstehungszeit.getReferenzId(),
                  entstehungszeitWert.getNichtVor(),
                  entstehungszeitWert.getNichtNach(),
                  entstehungszeitWert.getTyp(),
                  entstehungszeit.getReferenzURL())
          ));
    }

    indexes.forEach(index -> setChangeID(index,entstehungszeit));

    ergebnis.addAll(indexes);
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
