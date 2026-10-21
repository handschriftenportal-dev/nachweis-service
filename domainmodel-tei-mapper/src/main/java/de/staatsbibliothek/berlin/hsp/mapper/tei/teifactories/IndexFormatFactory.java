package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.FORMAT_TERM_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.FORMAT_TERM_TEXT_TYPE;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.util.List;
import java.util.Objects;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.Term;

class IndexFormatFactory extends IndexConceptFactory<Format> {

  private IndexFormatFactory() {
    // do nothing
  }

  public static IndexFormatFactory getInstance() {
    return new IndexFormatFactory();
  }

  @Override
  String getIndexType() {
    return AttributsTyp.FORMAT.getIndexName();
  }

  @Override
  void addTermsToIndex(Index index, Format format) {
    // This method is not used directly since Format overrides build()
    // to create multiple indices (one per FormatWert)
    throw new UnsupportedOperationException(
        "IndexFormatFactory creates multiple indices per Format, use build() instead");
  }

  @Override
  public void build(Format format, List<Index> result) {
    if (Objects.isNull(format)) {
      return;
    }

    List<FormatWert> formatWerte = format.getFormatWerte();

    if (Objects.isNull(formatWerte) || formatWerte.isEmpty()) {
      result.add(buildIndex(format, null));
    } else {
      formatWerte.stream()
          .map(formatWert -> buildIndex(format, formatWert))
          .forEach(result::add);
    }
  }

  private Index buildIndex(Format format, FormatWert formatWert) {
    Index index = createIndex(format);

    NormdatenReferenz normdatenReferenz =
        formatWert != null ? formatWert.getNormdatenReferenz() : null;
    String typeOfInformation = formatWert != null ? formatWert.getTyp() : null;

    Term formatTerm = createNormdatenTerm(normdatenReferenz, FORMAT_TERM_NORM);
    addTermToIndex(index, formatTerm);

    Term typeOfInfoTerm = createTextTerm(FORMAT_TERM_TEXT_TYPE, typeOfInformation);
    addTermToIndex(index, typeOfInfoTerm);

    return index;
  }
}
