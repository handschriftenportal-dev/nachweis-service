package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_DEPTH;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_HEIGHT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_TYPE;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_WIDTH;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.createTerm;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setChangeID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setIndexSourceAndCopyOf;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.ArrayList;
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

    List<Index> indexes = new ArrayList<>();

    if (Objects.isNull(abmessung.getAbmessungWerte()) || abmessung.getAbmessungWerte().isEmpty()) {
      indexes.add(buildIndex(null, abmessung.getReferenzId(), null, null,
          null, null, abmessung.getReferenzURL()));
    } else {
      abmessung.getAbmessungWerte().forEach(
          abmessungWert -> indexes.add(buildIndex(
              abmessung.getReferenzId(),
              abmessungWert.getText(),
              abmessungWert.getHoehe(),
              abmessungWert.getBreite(),
              abmessungWert.getTiefe(),
              abmessungWert.getTyp(),
              abmessung.getReferenzURL()))
      );
    }

    indexes.forEach(index -> setChangeID(index, abmessung));

    ergebnis.addAll(indexes);
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
