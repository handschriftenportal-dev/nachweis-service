package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.MUSIKNOTATION_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.createTerm;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setChangeID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setIndexSourceAndCopyOf;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.List;
import java.util.Objects;
import org.tei_c.ns._1.Index;

class IndexMusicNotationFactory {

  private IndexMusicNotationFactory() {
    // do nothing
  }

  public static void build(Musiknotation musiknotation, List<Index> ergebnis) {
    if (Objects.isNull(musiknotation)) {
      return;
    }

    Index index = new Index();
    index.setIndexName(AttributsTyp.MUSIKNOTATION.getIndexName());
    setChangeID(index,musiknotation);
    setIndexSourceAndCopyOf(index, musiknotation.getReferenzId(), musiknotation.getReferenzURL());
    index.getTermsAndIndices().add(createTerm(MUSIKNOTATION_TERM_TYP_TEXT, musiknotation.getText()));

    ergebnis.add(index);
  }
}
