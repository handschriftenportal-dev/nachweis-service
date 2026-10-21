package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.FORMAT_TERM_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.FORMAT_TERM_TEXT_TYPE;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.TEI_ANKER_TAG;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.Term;

public class FormatFactory extends AttributsReferenzFactory<Format> {

  @Override
  public Format build(MsDesc msDesc, TEI tei) {
    Objects.requireNonNull(msDesc, "msDesc is required!");
    Objects.requireNonNull(tei, "tei is required!");

    List<Index> indices = findIndices(msDesc, AttributsTyp.FORMAT.getIndexName());

    if (indices.isEmpty()) {
      return null;
    }

    Index firstIndex = indices.get(0);

    return Format.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzURL(firstIndex.getSources().stream().findFirst().orElse(""))
        .withReferenzId(getCopyOfValue(firstIndex.getCopyOf(), msDesc))
        .withFormatWerte(mapFormatWerte(indices))
        .withModificationDatum(mapModificationDate(tei, firstIndex).orElse(null))
        .withBearbeiter(mapBearbeiter(tei, firstIndex).orElse(null))
        .withAttributsReferenzStatusTyp(mapAttributsReferenzStatusTyp(tei, firstIndex).orElse(null))
        .withChangeId(firstIndex.getChanges().stream()
            .findFirst()
            .map(c -> c.replace(TEI_ANKER_TAG, "")).orElse(null))
        .build();
  }

  private List<FormatWert> mapFormatWerte(List<Index> indices) {
    return indices.stream()
        .map(this::mapFormatWert)
        .collect(Collectors.toList());
  }

  private FormatWert mapFormatWert(Index index) {
    List<Term> terme = findTerme(index);

    Term formatTerm = terme.stream()
        .filter(term -> FORMAT_TERM_NORM.equals(term.getType()))
        .findFirst()
        .orElse(null);

    Term typeOfInfoTerm = terme.stream()
        .filter(term -> FORMAT_TERM_TEXT_TYPE.equals(term.getType()))
        .findFirst()
        .orElse(null);

    NormdatenReferenz normdatenReferenz = formatTerm != null
        ? mapTermToNormdatenReferenz(formatTerm, NormdatenReferenz.CONCEPT_TYPE)
        : null;

    String typ = typeOfInfoTerm != null
        ? TEICommon.getContentAsString(typeOfInfoTerm.getContent())
        : null;

    return FormatWert.builder()
        .withReferenz(normdatenReferenz)
        .withTyp(typ)
        .build();
  }
}
