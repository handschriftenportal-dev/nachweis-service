package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.BESCHREIBSTOFF_TERM_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.BESCHREIBSTOFF_TERM_TEXT;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import org.tei_c.ns._1.Index;

class IndexMaterialFactory extends IndexConceptFactory<Beschreibstoff> {

  private IndexMaterialFactory() {
    // do nothing
  }

  public static IndexMaterialFactory getInstance() {
    return new IndexMaterialFactory();
  }

  @Override
  String getIndexType() {
    return AttributsTyp.BESCHREIBSTOFF.getIndexName();
  }

  @Override
  void addTermsToIndex(Index index, Beschreibstoff attributsReferenz) {
    var textTerm = createTextTerm(BESCHREIBSTOFF_TERM_TEXT, attributsReferenz.getText());
    addTermToIndex(index, textTerm);

    var normdatenTerms = createNormdatenTerms(BESCHREIBSTOFF_TERM_NORM,
        attributsReferenz.getNormdatenReferenzen());
    normdatenTerms.forEach(term -> addTermToIndex(index, term));
  }
}
