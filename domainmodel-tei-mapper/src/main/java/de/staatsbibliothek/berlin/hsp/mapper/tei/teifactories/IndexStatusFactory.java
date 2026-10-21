package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.STATUS_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.createTerm;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setChangeID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setIndexSourceAndCopyOf;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.List;
import java.util.Objects;
import org.tei_c.ns._1.Index;

class IndexStatusFactory {

  private IndexStatusFactory() {
    // do nothing
  }

  public static void build(Status status, List<Index> ergebnis) {
    if (Objects.isNull(status)) {
      return;
    }

    Index index = new Index();
    index.setIndexName(AttributsTyp.STATUS.getIndexName());
    setChangeID(index,status);
    setIndexSourceAndCopyOf(index, status.getReferenzId(), status.getReferenzURL());
    index.getTermsAndIndices().add(createTerm(STATUS_TERM_TYP_TEXT, status.getText()));

    ergebnis.add(index);
  }
}
