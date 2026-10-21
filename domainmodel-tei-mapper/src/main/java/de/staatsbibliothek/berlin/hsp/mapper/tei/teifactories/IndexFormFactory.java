package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.HANDSCHRIFTENTYP_TERM_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.HANDSCHRIFTENTYP_TERM_NORM_KEY_FEATURE;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.util.List;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.Term;

class IndexFormFactory extends IndexConceptFactory<Handschriftentyp> {

  private IndexFormFactory() {
    // do nothing
  }

  public static IndexFormFactory getInstance() {
    return new IndexFormFactory();
  }

  @Override
  String getIndexType() {
    return AttributsTyp.HANDSCHRIFTENTYP.getIndexName();
  }

  @Override
  void addTermsToIndex(Index index, Handschriftentyp attributsReferenz) {
    List<NormdatenReferenz> normdatenReferenzen = attributsReferenz.getNormdatenReferenzen();

    if (normdatenReferenzen == null || normdatenReferenzen.isEmpty()) {
      Term emptyTerm = createEmptyTerm(HANDSCHRIFTENTYP_TERM_NORM);
      addTermToIndex(index, emptyTerm);
      return;
    }

    Term firstTerm = createNormdatenTerm(normdatenReferenzen.getFirst(),
        HANDSCHRIFTENTYP_TERM_NORM, true);
    addTermToIndex(index, firstTerm);

    normdatenReferenzen.stream()
        .skip(1)
        .map(term -> createNormdatenTerm(term, HANDSCHRIFTENTYP_TERM_NORM_KEY_FEATURE, false))
        .forEach(term -> addTermToIndex(index, term));
  }

}
