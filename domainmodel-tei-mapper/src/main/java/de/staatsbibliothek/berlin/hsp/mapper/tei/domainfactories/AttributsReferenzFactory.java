package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.mapper.api.factory.ElementFactory;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIQuery;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.Change;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.PersName;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.Term;

public abstract class AttributsReferenzFactory<T extends AttributsReferenz> implements
    ElementFactory<T, MsDesc, TEI> {

  private static final Logger log = LoggerFactory.getLogger(
      AttributsReferenzFactory.class);

  protected AttributsReferenzFactory() {
    // do nothing
  }

  protected static List<Index> findIndices(MsDesc msDesc, String indexName) {
    return findFilteredIndexStream(msDesc, indexName).collect(
        Collectors.toList());
  }

  protected static Optional<Index> findIndex(MsDesc msDesc, String indexName) {
    return findFilteredIndexStream(msDesc, indexName).findFirst();
  }

  protected static List<Term> findTerme(Index index) {
    return Stream.ofNullable(index)
        .flatMap(idx -> idx.getTermsAndIndices().stream())
        .filter(Term.class::isInstance)
        .map(Term.class::cast)
        .toList();
  }

  protected static String mapTermText(Index index, String termType) {
    return findTerme(index).stream()
        .filter(term -> termType.equals(term.getType()))
        .map(term -> TEICommon.getContentAsString(term.getContent()))
        .findFirst()
        .orElse(null);
  }

  protected static Optional<String> findChangeID(Index index) {
    return index.getChanges().stream()
        .findFirst()
        .map(c -> c.replaceAll(TEIValues.TEI_ANKER_TAG, ""));
  }

  private static Stream<Index> findFilteredIndexStream(MsDesc msDesc,
      String indexName) {
    return Stream.ofNullable(msDesc)
        .flatMap(m -> m.getHeads().stream())
        .findFirst()
        .stream()
        .flatMap(head -> head.getContent().stream())
        .filter(Index.class::isInstance)
        .map(Index.class::cast)
        .filter(index -> indexName.equals(index.getIndexName()));
  }

  protected static String getCopyOfValue(String copyOf, MsDesc msDesc) {
    if (StringUtils.isBlank(copyOf) && !DokumentObjektTyp.HSP_OBJECT.equals(
        DokumentObjektTyp.fromString(msDesc.getType()))) {
      return msDesc.getId();
    }

    return copyOf != null ? copyOf.replaceAll(TEIValues.TEI_ANKER_TAG, "")
        : copyOf;
  }

  protected static Optional<Change> findChangeWithID(TEI tei, String id) {
    if (tei == null || tei.getTeiHeader() == null
        || tei.getTeiHeader().getRevisionDesc() == null) {
      throw new IllegalArgumentException(
          "Ungültiges TEI-Dokument: TEI, TeiHeader oder RevisionDesc fehlt!");
    }
    return tei.getTeiHeader().getRevisionDesc().getRevisionChangeAttribute()
        .stream()
        .filter(c -> c.getId().equals(id))
        .findFirst();
  }

  protected static Optional<LocalDateTime> mapModificationDate(TEI tei,
      Index index) {
    if (tei == null || index == null) {
      throw new IllegalArgumentException(
          "TEI-Dokument oder Index darf nicht null sein!");
    }
    return findChangeID(index)
        .flatMap(i -> findChangeWithID(tei, i))
        .map(c -> Optional.ofNullable(c.getWhen()))
        .filter(Optional::isPresent)
        .map(w -> {
          try {
            return LocalDateTime.parse(w.get(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
          } catch (Exception e) {
            log.error("Error during parsing Date of Change Element");
          }
          return null;
        });
  }

  protected static Optional<Bearbeiter> mapBearbeiter(TEI tei, Index index) {
    if (tei == null || index == null) {
      throw new IllegalArgumentException(
          "TEI-Dokument oder Index darf nicht null sein!");
    }
    return findChangeID(index)
        .flatMap(i -> findChangeWithID(tei, i))
        .flatMap(c -> c.getContent().stream()
            .filter(node -> node instanceof PersName)
            .map(node -> {
              PersName persName = (PersName) node;
              return Bearbeiter.newBuilder()
                  .withBearbeitername(
                      TEICommon.getContentAsString(persName.getContent()))
                  .withId(persName.getKey())
                  .build();
            })
            .findFirst());
  }

  protected static Optional<AttributsReferenzStatusTyp> mapAttributsReferenzStatusTyp(
      TEI tei, Index index) {

    return findChangeID(index)
        .flatMap(i -> findChangeWithID(tei, i))
        .map(c -> {

          if (StringUtils.isEmpty(c.getN())) {
            return AttributsReferenzStatusTyp.LEER;
          }

          return AttributsReferenzStatusTyp.valueOf(c.getN());
        });
  }

  public NormdatenReferenz mapTermToNormdatenReferenz(Term term,
      String typeName) {

    if (term == null || typeName == null) {
      throw new IllegalArgumentException(
          "Term  or typeName is null and not allowed!");
    }

    return new NormdatenReferenzBuilder()
        .withId(term.getKey() != null ? term.getKey()
            : TEICommon.getContentAsString(term.getContent()))
        .withGndID(TEIQuery.findGNDIDinListOfStrings(term.getReves()))
        .withTypeName(typeName)
        .withName(TEICommon.getContentAsString(term.getContent()))
        .withUri(TEICommon.getContentAsString(term.getReves()))
        .build();
  }
}
