package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.MUSIKNOTATION_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.TEI_ANKER_TAG;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.Objects;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;

public class MusiknotationFactory extends AttributsReferenzFactory<Musiknotation> {

  @Override
  public Musiknotation build(MsDesc msDesc, TEI tei) {
    Objects.requireNonNull(msDesc, "msDesc is required!");
    Objects.requireNonNull(tei, "tei is required!");

    return findIndex(msDesc, AttributsTyp.MUSIKNOTATION.getIndexName())
        .map(index -> Musiknotation.builder()
            .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
            .withReferenzId(getCopyOfValue(index.getCopyOf(),msDesc))
            .withReferenzURL(index.getSources().stream().findFirst().orElse(""))
            .withText(mapTermText(index, MUSIKNOTATION_TERM_TYP_TEXT))
            .withModificationDatum(mapModificationDate(tei, index).orElse(null))
            .withChangeId(index.getChanges().stream().findFirst()
                .map(c -> c.replace(TEI_ANKER_TAG,"")).orElse(null))
            .withBearbeiter(mapBearbeiter(tei, index).orElse(null))
            .withAttributsReferenzStatusTyp(mapAttributsReferenzStatusTyp(tei, index).orElse(null))
            .build())
        .orElse(null);
  }

}
