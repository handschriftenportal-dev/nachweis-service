package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.GRUNDSPRACHE_TERM_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.GRUNDSPRACHE_TERM_TEXT;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import org.tei_c.ns._1.Index;

class IndexTextLangFactory extends IndexConceptFactory<Grundsprache> {

  private IndexTextLangFactory() {
    // do nothing
  }

  public static IndexTextLangFactory getInstance() {
    return new IndexTextLangFactory();
  }

  @Override
  String getIndexType() {
    return AttributsTyp.GRUNDSPRACHE.getIndexName();
  }

  @Override
  void addTermsToIndex(Index index, Grundsprache attributsReferenz) {
    var textTerm = createTextTerm(GRUNDSPRACHE_TERM_TEXT, attributsReferenz.getText());
    addTermToIndex(index, textTerm);

    var normdatenTerms = createNormdatenTerms(GRUNDSPRACHE_TERM_NORM,
        attributsReferenz.getNormdatenReferenzen());
    normdatenTerms.forEach(term -> addTermToIndex(index, term));
  }
}
