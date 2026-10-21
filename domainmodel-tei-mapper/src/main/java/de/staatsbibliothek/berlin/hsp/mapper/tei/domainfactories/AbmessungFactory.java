package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_DEPTH;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_HEIGHT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_TYPE;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_WIDTH;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.TEI_ANKER_TAG;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AbmessungWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AbmessungWert.AbmessungWertBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.Term;

public class AbmessungFactory extends AttributsReferenzFactory<Abmessung> {

  static Map<String, BiConsumer<Term, AbmessungWertBuilder>> termBuilders = new HashMap<>();

  static {
    termBuilders.put(ABMESSUNG_TERM_TYP_TEXT,
        (term, builder) -> builder.withText(TEICommon.getContentAsString(term.getContent())));
    termBuilders.put(ABMESSUNG_TERM_TYP_HEIGHT,
        (term, builder) -> builder.withHoehe(TEICommon.getContentAsString(term.getContent())));
    termBuilders.put(ABMESSUNG_TERM_TYP_WIDTH,
        (term, builder) -> builder.withBreite(TEICommon.getContentAsString(term.getContent())));
    termBuilders.put(ABMESSUNG_TERM_TYP_DEPTH,
        (term, builder) -> builder.withTiefe(TEICommon.getContentAsString(term.getContent())));
    termBuilders.put(ABMESSUNG_TERM_TYP_TYPE,
        (term, builder) -> builder.withTyp(TEICommon.getContentAsString(term.getContent())));
  }

  @Override
  public Abmessung build(MsDesc msDesc, TEI tei) {
    Objects.requireNonNull(msDesc, "msDesc is required!");
    Objects.requireNonNull(tei, "tei is required!");

    List<Index> indices = findIndices(msDesc, AttributsTyp.ABMESSUNG.getIndexName());

    if (!indices.isEmpty()) {
      Index index = indices.getFirst();
      return Abmessung.builder()
          .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
          .withReferenzId(getCopyOfValue(index.getCopyOf(),msDesc))
          .withReferenzURL(index.getSources().stream().findFirst().orElse(""))
          .withAbmessungWerte(mapAbmessungWerte(indices))
          .withModificationDatum(mapModificationDate(tei, index).orElse(null))
          .withChangeId(index.getChanges().stream().findFirst()
              .map(c -> c.replace(TEI_ANKER_TAG,"")).orElse(null))
          .withBearbeiter(mapBearbeiter(tei, index).orElse(null))
          .withAttributsReferenzStatusTyp(mapAttributsReferenzStatusTyp(tei, index).orElse(null))
          .build();
    } else {
      return null;
    }
  }

  private List<AbmessungWert> mapAbmessungWerte(List<Index> indices) {
    return indices.stream().map(AttributsReferenzFactory::findTerme)
        .map(this::mapAbmessungWert)
        .collect(Collectors.toList());
  }

  private AbmessungWert mapAbmessungWert(List<Term> terme) {
    AbmessungWertBuilder builder = AbmessungWert.builder();
    terme.forEach(term ->
        Optional.ofNullable(termBuilders.get(term.getType()))
            .ifPresentOrElse(
                consumer -> consumer.accept(term, builder),
                () -> {
                  throw new IllegalArgumentException("Unmapped Term: " + term);
                }));
    return builder.build();
  }

}
