package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.TITEL_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.createTerm;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setChangeID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setIndexSourceAndCopyOf;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.List;
import java.util.Objects;
import org.tei_c.ns._1.Index;

class IndexTitleFactory {

  private IndexTitleFactory() {
    // do nothing
  }

  public static void build(Titel titel, List<Index> ergebnis) {
    if (Objects.isNull(titel)) {
      return;
    }

    Index index = new Index();
    index.setIndexName(AttributsTyp.TITEL.getIndexName());
    setChangeID(index,titel);
    setIndexSourceAndCopyOf(index, titel.getReferenzId(), titel.getReferenzURL());
    index.getTermsAndIndices().add(createTerm(TITEL_TERM_TYP_TEXT, titel.getText()));

    ergebnis.add(index);
  }
}
