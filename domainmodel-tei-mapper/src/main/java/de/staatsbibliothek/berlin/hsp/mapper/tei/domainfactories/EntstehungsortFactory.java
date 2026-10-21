package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSORT_TERM_TYP_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSORT_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.TEI_ANKER_TAG;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort.EntstehungsortBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.Objects;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.Term;

public class EntstehungsortFactory extends AttributsReferenzFactory<Entstehungsort> {

  @Override
  public Entstehungsort build(MsDesc msDesc, TEI tei) {
    Objects.requireNonNull(msDesc, "msDesc is required!");
    Objects.requireNonNull(tei, "tei is required!");

    return findIndex(msDesc, AttributsTyp.ENTSTEHUNGSORT.getIndexName())
        .map(index -> {
          EntstehungsortBuilder builder = Entstehungsort.builder()
              .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
              .withReferenzURL(index.getSources().stream().findFirst().orElse(""))
              .withReferenzId(getCopyOfValue(index.getCopyOf(), msDesc))
              .withModificationDatum(mapModificationDate(tei, index).orElse(null))
              .withBearbeiter(mapBearbeiter(tei, index).orElse(null))
              .withChangeId(index.getChanges().stream().findFirst()
                  .map(c -> c.replace(TEI_ANKER_TAG, "")).orElse(null))
              .withAttributsReferenzStatusTyp(
                  mapAttributsReferenzStatusTyp(tei, index).orElse(null));
          mapTerme(index, builder);
          return builder.build();
        }).orElse(null);
  }

  private void mapTerme(Index index, EntstehungsortBuilder builder) {
    for (Term term : findTerme(index)) {
      switch (term.getType()) {
        case ENTSTEHUNGSORT_TERM_TYP_TEXT:
          builder.withText(TEICommon.getContentAsString(term.getContent()));
          break;

        case ENTSTEHUNGSORT_TERM_TYP_NORM:
          builder.addNormdatenReferenz(
              mapTermToNormdatenReferenz(term, NormdatenReferenz.ORT_TYPE_NAME));
          break;

        default:
          throw new IllegalArgumentException("Unmapped Term: " + term);
      }
    }
  }

}
