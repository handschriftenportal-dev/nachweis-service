package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.HANDSCHRIFTENTYP_TERM_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.TEI_ANKER_TAG;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp.HandschriftentypBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.Objects;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.Term;

public class HandschriftentypFactory extends AttributsReferenzFactory<Handschriftentyp> {

  @Override
  public Handschriftentyp build(MsDesc msDesc, TEI tei) {
    Objects.requireNonNull(msDesc, "msDesc is required!");
    Objects.requireNonNull(tei, "tei is required!");

    return findIndex(msDesc, AttributsTyp.HANDSCHRIFTENTYP.getIndexName())
        .map(index -> {
          HandschriftentypBuilder builder = Handschriftentyp.builder()
              .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
              .withReferenzId(getCopyOfValue(index.getCopyOf(), msDesc))
              .withReferenzURL(index.getSources().stream().findFirst().orElse(""))
              .withModificationDatum(mapModificationDate(tei, index).orElse(null))
              .withBearbeiter(mapBearbeiter(tei, index).orElse(null))
              .withChangeId(index.getChanges().stream().findFirst()
                  .map(c -> c.replace(TEI_ANKER_TAG, "")).orElse(null))
              .withAttributsReferenzStatusTyp(
                  mapAttributsReferenzStatusTyp(tei, index).orElse(null));
          mapTerme(index, builder);

          return builder.build();
        })
        .orElse(null);
  }

  private void mapTerme(Index index, HandschriftentypBuilder builder) {

    for (Term term : findTerme(index)) {
      if (!HANDSCHRIFTENTYP_TERM_NORM.equals(term.getType())
          && !TEIValues.HANDSCHRIFTENTYP_TERM_NORM_KEY_FEATURE.equals(term.getType())) {
        continue;
      }

      String termText = TEICommon.getContentAsString(term.getContent());
      boolean hasKeyOrRef = term.getKey() != null
          || (term.getReves() != null && !term.getReves().isEmpty());

      if (termText != null && !termText.isBlank()) {
        builder.withText(termText);
      }

      if (hasKeyOrRef) {
        builder.addReferenz(mapTermToNormdatenReferenz(term, NormdatenReferenz.CONCEPT_TYPE));
      }
    }
  }
}
