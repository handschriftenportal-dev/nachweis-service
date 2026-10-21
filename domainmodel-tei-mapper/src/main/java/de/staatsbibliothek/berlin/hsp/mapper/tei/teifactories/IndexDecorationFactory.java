package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.BUCHSCHMUCK_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.createTerm;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setChangeID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setIndexSourceAndCopyOf;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.List;
import java.util.Objects;
import org.tei_c.ns._1.Index;

class IndexDecorationFactory {

  private IndexDecorationFactory() {
    // do nothing
  }

  public static void build(Buchschmuck buchschmuck, List<Index> ergebnis) {
    if (Objects.isNull(buchschmuck)) {
      return;
    }

    Index index = new Index();
    index.setIndexName(AttributsTyp.BUCHSCHMUCK.getIndexName());
    setChangeID(index, buchschmuck);
    setIndexSourceAndCopyOf(index, buchschmuck.getReferenzId(), buchschmuck.getReferenzURL());
    index.getTermsAndIndices().add(createTerm(BUCHSCHMUCK_TERM_TYP_TEXT, buchschmuck.getText()));

    ergebnis.add(index);
  }
}
