package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.BESCHREIBSTOFF_TERM_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.BESCHREIBSTOFF_TERM_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.TEI_ANKER_TAG;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff.BeschreibstoffBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.Objects;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.Term;

public class BeschreibstoffFactory extends AttributsReferenzFactory<Beschreibstoff> {

  @Override
  public Beschreibstoff build(MsDesc msDesc, TEI tei) {
    Objects.requireNonNull(msDesc, "msDesc is required!");
    Objects.requireNonNull(tei, "tei is required!");

    return findIndex(msDesc, AttributsTyp.BESCHREIBSTOFF.getIndexName())
        .map(index -> {
          BeschreibstoffBuilder builder = Beschreibstoff.builder()
              .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
              .withReferenzURL(index.getSources().stream().findFirst().orElse(""))
              .withReferenzId(getCopyOfValue(index.getCopyOf(), msDesc))
              .withModificationDatum(mapModificationDate(tei, index).orElse(null))
              .withBearbeiter(mapBearbeiter(tei, index).orElse(null))
              .withChangeId(index.getChanges().stream().findFirst()
                  .map(c -> c.replace(TEI_ANKER_TAG, "")).orElse(null))
              .withAttributsReferenzStatusTyp(
                  mapAttributsReferenzStatusTyp(tei, index).orElse(null));
          mapTerm(index, builder);
          return builder.build();
        }).orElse(null);
  }

  private void mapTerm(Index index, BeschreibstoffBuilder builder) {
    for (Term term : findTerme(index)) {
      switch (term.getType()) {
        case BESCHREIBSTOFF_TERM_TEXT ->
            builder.withText(TEICommon.getContentAsString(term.getContent()));
        case BESCHREIBSTOFF_TERM_NORM -> builder.addReferenz(
            mapTermToNormdatenReferenz(term, NormdatenReferenz.CONCEPT_TYPE));
        default -> throw new IllegalArgumentException("Unmapped Term: " + term);
      }
    }
  }

}
