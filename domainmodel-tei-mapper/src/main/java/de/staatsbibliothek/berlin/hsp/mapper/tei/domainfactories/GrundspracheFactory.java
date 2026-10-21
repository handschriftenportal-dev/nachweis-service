package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.GRUNDSPRACHE_TERM_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.GRUNDSPRACHE_TERM_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.TEI_ANKER_TAG;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache.GrundspracheBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.Objects;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.Term;

public class GrundspracheFactory extends
    AttributsReferenzFactory<Grundsprache> {

  @Override
  public Grundsprache build(MsDesc msDesc, TEI tei) {
    Objects.requireNonNull(msDesc, "msDesc is required!");
    Objects.requireNonNull(tei, "tei is required!");

    return findIndex(msDesc, AttributsTyp.GRUNDSPRACHE.getIndexName())
        .map(index -> {
          GrundspracheBuilder builder = Grundsprache.builder()
              .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
              .withReferenzURL(
                  index.getSources().stream().findFirst().orElse(""))
              .withReferenzId(getCopyOfValue(index.getCopyOf(), msDesc))
              .withModificationDatum(
                  mapModificationDate(tei, index).orElse(null))
              .withBearbeiter(mapBearbeiter(tei, index).orElse(null))
              .withChangeId(index.getChanges().stream().findFirst()
                  .map(c -> c.replace(TEI_ANKER_TAG, "")).orElse(null))
              .withAttributsReferenzStatusTyp(
                  mapAttributsReferenzStatusTyp(tei, index).orElse(null));
          mapTerme(index, builder);
          return builder.build();
        }).orElse(null);
  }

  private void mapTerme(Index index, GrundspracheBuilder builder) {
    for (Term term : findTerme(index)) {
      switch (term.getType()) {
        case GRUNDSPRACHE_TERM_TEXT ->
            builder.withText(TEICommon.getContentAsString(term.getContent()));
        case GRUNDSPRACHE_TERM_NORM -> builder.addReferenz(
            mapTermToNormdatenReferenz(term, NormdatenReferenz.SPRACHE_TYPE_NAME));
        default -> throw new IllegalArgumentException("Unmapped Term: " + term);
      }
    }
  }
}
