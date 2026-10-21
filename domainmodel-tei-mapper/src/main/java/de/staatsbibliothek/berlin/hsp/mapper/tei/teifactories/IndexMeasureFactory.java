package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.UMFANG_TERM_TYP_NO_OF_LEAVES;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.UMFANG_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.createTerm;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setChangeID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory.setIndexSourceAndCopyOf;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.List;
import java.util.Objects;
import org.tei_c.ns._1.Index;

class IndexMeasureFactory {

  private IndexMeasureFactory() {
    // do nothing
  }

  public static void build(Umfang umfang, List<Index> ergebnis) {
    if (Objects.isNull(umfang)) {
      return;
    }

    Index index = new Index();
    index.setIndexName(AttributsTyp.UMFANG.getIndexName());
    setChangeID(index,umfang);
    setIndexSourceAndCopyOf(index, umfang.getReferenzId(), umfang.getReferenzURL());

    index.getTermsAndIndices().add(createTerm(UMFANG_TERM_TYP_TEXT, umfang.getText()));
    index.getTermsAndIndices().add(createTerm(UMFANG_TERM_TYP_NO_OF_LEAVES, umfang.getBlattzahl()));

    ergebnis.add(index);
  }

}
