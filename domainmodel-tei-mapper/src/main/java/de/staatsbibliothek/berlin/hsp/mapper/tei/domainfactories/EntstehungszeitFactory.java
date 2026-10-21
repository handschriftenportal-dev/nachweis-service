package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_NOT_AFTER;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_NOT_BEFORE;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TYPE;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.TEI_ANKER_TAG;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.EntstehungszeitWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.EntstehungszeitWert.EntstehungszeitWertBuilder;
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

public class EntstehungszeitFactory extends AttributsReferenzFactory<Entstehungszeit> {

  static Map<String, BiConsumer<Term, EntstehungszeitWertBuilder>> termBuilders = new HashMap<>();

  static {
    termBuilders.put(ENTSTEHUNGSZEIT_TERM_TYP_TEXT,
        (term, builder) -> builder.withText(TEICommon.getContentAsString(term.getContent())));
    termBuilders.put(ENTSTEHUNGSZEIT_TERM_TYP_NOT_BEFORE,
        (term, builder) -> builder.withNichtVor(TEICommon.getContentAsString(term.getContent())));
    termBuilders.put(ENTSTEHUNGSZEIT_TERM_TYP_NOT_AFTER,
        (term, builder) -> builder.withNichtNach(TEICommon.getContentAsString(term.getContent())));
    termBuilders.put(ENTSTEHUNGSZEIT_TERM_TYP_TYPE,
        (term, builder) -> builder.withTyp(TEICommon.getContentAsString(term.getContent())));
  }

  @Override
  public Entstehungszeit build(MsDesc msDesc, TEI tei) {
    Objects.requireNonNull(msDesc, "msDesc is required!");
    Objects.requireNonNull(tei, "tei is required!");

    List<Index> indices = findIndices(msDesc, AttributsTyp.ENTSTEHUNGSZEIT.getIndexName());

    if (!indices.isEmpty()) {
      Index index = indices.iterator().next();
      return Entstehungszeit.builder()
          .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
          .withReferenzURL(index.getSources().stream().findFirst().orElse(""))
          .withReferenzId(getCopyOfValue(index.getCopyOf(), msDesc))
          .withEntstehungszeitWerte(mapEntstehungszeitWerte(indices))
          .withModificationDatum(mapModificationDate(tei, index).orElse(null))
          .withBearbeiter(mapBearbeiter(tei, index).orElse(null))
          .withChangeId(index.getChanges().stream().findFirst()
              .map(c -> c.replace(TEI_ANKER_TAG,"")).orElse(null))
          .withAttributsReferenzStatusTyp(mapAttributsReferenzStatusTyp(tei, index).orElse(null))
          .build();
    }
    return null;
  }

  private List<EntstehungszeitWert> mapEntstehungszeitWerte(List<Index> indices) {
    return indices.stream().map(AttributsReferenzFactory::findTerme)
        .map(this::mapEntstehungszeitWert)
        .collect(Collectors.toList());
  }

  private EntstehungszeitWert mapEntstehungszeitWert(List<Term> terme) {
    EntstehungszeitWertBuilder builder = EntstehungszeitWert.builder();
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
