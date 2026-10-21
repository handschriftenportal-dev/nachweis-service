package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.TEI_ANKER_TAG;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.TITEL_TERM_TYP_TEXT;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.Objects;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;

public class TitelFactory extends AttributsReferenzFactory<Titel> {

  @Override
  public Titel build(MsDesc msDesc, TEI tei) {
    Objects.requireNonNull(msDesc, "msDesc is required!");
    Objects.requireNonNull(tei, "tei is required!");

    return findIndex(msDesc, AttributsTyp.TITEL.getIndexName())
        .map(index -> Titel.builder()
            .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
            .withReferenzId(getCopyOfValue(index.getCopyOf(), msDesc))
            .withReferenzURL(index.getSources().stream().findFirst().orElse(""))
            .withText(mapTermText(index, TITEL_TERM_TYP_TEXT))
            .withModificationDatum(mapModificationDate(tei, index).orElse(null))
            .withChangeId(index.getChanges().stream().findFirst()
                .map(c -> c.replace(TEI_ANKER_TAG,"")).orElse(null))
            .withBearbeiter(mapBearbeiter(tei, index).orElse(null))
            .withAttributsReferenzStatusTyp(mapAttributsReferenzStatusTyp(tei, index).orElse(null))
            .build())
        .orElse(null);
  }
}
