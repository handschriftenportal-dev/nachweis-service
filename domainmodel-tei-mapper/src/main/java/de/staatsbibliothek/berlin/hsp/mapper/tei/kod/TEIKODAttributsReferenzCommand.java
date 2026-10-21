package de.staatsbibliothek.berlin.hsp.mapper.tei.kod;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPCommandException;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEI2AttributsReferenzMapper;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.Change;
import org.tei_c.ns._1.Head;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.PersName;
import org.tei_c.ns._1.RevisionDesc;
import org.tei_c.ns._1.SourceDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.TeiHeader;

public class TEIKODAttributsReferenzCommand {

  private static final String MSG_REQ_KOD = "kod is required!";

  private static final Logger logger = LoggerFactory.getLogger(
      TEIKODAttributsReferenzCommand.class);

  static Map<AttributsTyp, Function<AttributsTyp, AttributsReferenz>> emptyBuildersMap
      = new EnumMap<>(AttributsTyp.class);

  static {
    emptyBuildersMap.put(AttributsTyp.TITEL,
        (_ -> Titel.builder().build()));
    emptyBuildersMap.put(AttributsTyp.BESCHREIBSTOFF,
        (attributsTyp -> Beschreibstoff.builder().build()));
    emptyBuildersMap.put(AttributsTyp.UMFANG,
        (attributsTyp -> Umfang.builder().build()));
    emptyBuildersMap.put(AttributsTyp.ABMESSUNG,
        (attributsTyp -> Abmessung.builder().build()));
    emptyBuildersMap.put(AttributsTyp.FORMAT,
        (attributsTyp -> Format.builder().build()));
    emptyBuildersMap.put(AttributsTyp.ENTSTEHUNGSORT,
        (attributsTyp -> Entstehungsort.builder().build()));
    emptyBuildersMap.put(AttributsTyp.ENTSTEHUNGSZEIT,
        (attributsTyp -> Entstehungszeit.builder().build()));
    emptyBuildersMap.put(AttributsTyp.GRUNDSPRACHE,
        (attributsTyp -> Grundsprache.builder().build()));
    emptyBuildersMap.put(AttributsTyp.HANDSCHRIFTENTYP,
        (attributsTyp -> Handschriftentyp.builder().build()));
    emptyBuildersMap.put(AttributsTyp.STATUS,
        (attributsTyp -> Status.builder().build()));
    emptyBuildersMap.put(AttributsTyp.BUCHSCHMUCK,
        (attributsTyp -> Buchschmuck.builder().build()));
    emptyBuildersMap.put(AttributsTyp.MUSIKNOTATION,
        (attributsTyp -> Musiknotation.builder().build()));
  }

  private TEIKODAttributsReferenzCommand() {
    //do nothing
  }

  public static void updateAttributsReferenzen(KulturObjektDokument kod,
      Change change)
      throws Exception {

    Objects.requireNonNull(kod, MSG_REQ_KOD);
    Objects.requireNonNull(change, "Change must not be null!");

    TEI kodTEI = openTEIDokument(kod.getTeiXML());
    MsDesc kodMsDesc = findFirstMsDesc(kodTEI);

    updateTEIIndices(kod.getAttributsReferenzen(), kodMsDesc);

    addChangeRevisionStatement(kodTEI, change);

    kod.setTeiXML(marshalTEI(kodTEI));
  }

  static void updateTEIIndices(
      Map<AttributsTyp, AttributsReferenz> attributsReferenzMap,
      MsDesc msDesc) {
    Head head = msDesc.getHeads().stream().findFirst().orElse(null);
    if (Objects.isNull(head)) {
      head = new Head();
      msDesc.getHeads().add(head);
    }

    attributsReferenzMap.entrySet().stream()
        .filter(entry -> entry.getValue() == null)
        .forEach(entry -> entry.setValue(
            buildEmptyAttributsReferenz(entry.getKey())));

    head.getContent().clear();

    head.getContent().addAll(
        IndexFactory.build(new HashSet<>(attributsReferenzMap.values())));
  }

  public static void deleteAttributsReferenzenForBeschreibung(
      KulturObjektDokument kod,
      String beschreibungId, Change change) throws Exception {

    Objects.requireNonNull(kod, MSG_REQ_KOD);
    Objects.requireNonNull(kod.getTeiXML(), "beschreibungId is required!");

    logger.info("Delete Attributsrefrenzen for KOD {} and Beschreibung {}",
        kod.getId(),
        beschreibungId);

    TEI kodTEI = openTEIDokument(kod.getTeiXML());
    MsDesc kodMsDesc = findFirstMsDesc(kodTEI);

    Map<AttributsTyp, AttributsReferenz> kodReferenzen = TEI2AttributsReferenzMapper.map(
        kodTEI,
        kodMsDesc);

    kodReferenzen.forEach((typ, referenz) -> {
      if (referenz.getReferenzId() != null && referenz.getReferenzId()
          .equals(beschreibungId)) {
        var ref = buildEmptyAttributsReferenz(referenz.getAttributsTyp());
        ref.setChangeID(change.getId());
        ref.setAttributsReferenzStatusTyp(
            AttributsReferenzStatusTyp.valueOf(change.getN()));
        kodReferenzen.put(typ, ref);
      }
    });

    kod.setAttributsReferenzen(kodReferenzen);
    kod.updateAttributsReferenzStatusTyp();

    addChangeRevisionStatement(kodTEI, change);

    updateIndices(new HashSet<>(kod.getAttributsReferenzen().values()),
        kodMsDesc);

    String tei = marshalTEI(kodTEI);
    kod.setTeiXML(tei);

    logger.debug("Delete Attributsreferenzen in TEI {} ", tei);
  }

  static void updateIndices(Set<AttributsReferenz> attributsReferenzen,
      MsDesc msDesc) {

    Head head = msDesc.getHeads().stream().findFirst().orElse(null);
    if (Objects.isNull(head)) {
      head = new Head();
      msDesc.getHeads().add(head);
    }

    logger.info("Add Attributsreferenzen to Head {}",
        attributsReferenzen.stream()
            .filter(Objects::nonNull).map(AttributsReferenz::toString).
            collect(joining(",")));

    replaceAllHeadIndexElements(attributsReferenzen, head);
  }

  protected static void replaceAllHeadIndexElements(
      Set<AttributsReferenz> attributsReferenzen,
      Head head) {

    Set<String> indexNamesToReplace = getIndexNames(attributsReferenzen);

    logger.debug("Replace in Head {} ",
        !indexNamesToReplace.isEmpty() ? String.join(", ", indexNamesToReplace)
            : "No Attributsreferences");

    head.getContent().removeIf(content -> Objects.nonNull(content)
        && content instanceof Index
        && indexNamesToReplace.contains(((Index) content).getIndexName()));

    /*
     * This part is needed only because of serialization and desialization of XML using JAXB
     * Index Element contains source as ArrayList and will be added always as empty source which is not allowed
     */

    final List<Index> indexElementsToRemove = new ArrayList<>();
    final List<Index> indexElementsToAdd = new ArrayList<>();
    for (Object headElement : head.getContent()) {
      if (headElement instanceof Index headIndexElement
          && !indexNamesToReplace.contains(headIndexElement.getIndexName())) {
        Index newIndex = new Index();
        newIndex.setIndexName(headIndexElement.getIndexName());
        IndexFactory.setIndexSourceAndCopyOf(newIndex,
            headIndexElement.getCopyOf(),
            headIndexElement.getSources().stream().findFirst().orElse(""));
        indexElementsToRemove.add(headIndexElement);
        indexElementsToAdd.add(newIndex);
      }
    }

    head.getContent().removeAll(indexElementsToRemove);
    //Index Elements created based on old onces
    head.getContent().addAll(indexElementsToAdd);
    //Index Elemtns created based on Attributsreferenzes
    head.getContent().addAll(IndexFactory.build(attributsReferenzen));
  }

  static AttributsReferenz buildEmptyAttributsReferenz(
      AttributsTyp attributsTyp) {
    Objects.requireNonNull(attributsTyp, "attributsTyp is required!");

    return Optional.ofNullable(emptyBuildersMap.get(attributsTyp))
        .map(function -> function.apply(attributsTyp))
        .orElseThrow(() -> new IllegalArgumentException(
            "Unmapped attributsTyp: " + attributsTyp));
  }

  static Set<String> getIndexNames(Set<AttributsReferenz> attributsReferenzen) {
    return Stream.ofNullable(attributsReferenzen)
        .flatMap(Collection::stream)
        .map(AttributsReferenz::getAttributsTyp)
        .map(AttributsTyp::getIndexName)
        .collect(toSet());
  }

  static SourceDesc findSourceDesc(TEI tei, String kodId)
      throws HSPCommandException {
    Optional<SourceDesc> optionalSourceDesc;
    try {
      optionalSourceDesc = TEICommon.findFirst(SourceDesc.class, tei);
    } catch (Exception e) {
      throw new HSPCommandException(
          "Error finding sourceDesc in TEI of KOD " + kodId, e);
    }
    return optionalSourceDesc.orElseThrow(
        () -> new HSPCommandException("No sourceDesc in TEI of KOD " + kodId));
  }

  static Optional<MsDesc> findKodMsDescInSourceDesc(SourceDesc sourceDesc) {
    return sourceDesc.getBiblsAndBiblStructsAndListBibls().stream()
        .filter(MsDesc.class::isInstance)
        .map(MsDesc.class::cast)
        .filter(msDesc -> DokumentObjektTyp.HSP_OBJECT.toString()
            .equals(msDesc.getType()))
        .findFirst();
  }

  static Optional<MsDesc> findBeschreibungMsDescInSourceDesc(
      SourceDesc sourceDesc) {
    return sourceDesc.getBiblsAndBiblStructsAndListBibls().stream()
        .filter(MsDesc.class::isInstance)
        .map(MsDesc.class::cast)
        .filter(msDesc -> (DokumentObjektTyp.HSP_DESCRIPTION.toString()
            .equals(msDesc.getType())
            || DokumentObjektTyp.HSP_DESCRIPTION_RETRO.toString()
            .equals(msDesc.getType())))
        .findFirst();
  }

  protected static TEI openTEIDokument(String teiXML)
      throws HSPCommandException {
    Optional<TEI> tei;

    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(
        teiXML.getBytes(StandardCharsets.UTF_8))) {
      tei = TEIObjectFactory.unmarshalOne(inputStream);
    } catch (Exception e) {
      throw new HSPCommandException("Error reading TEI-XML: " + e.getMessage(),
          e);
    }
    return tei.orElseThrow(
        () -> new HSPCommandException("TEI-XML contains no TEI!"));
  }

  private static MsDesc findFirstMsDesc(TEI tei) throws HSPCommandException {
    Optional<MsDesc> msDesc;
    try {
      msDesc = TEICommon.findFirst(MsDesc.class, tei);
    } catch (Exception e) {
      throw new HSPCommandException(
          "Error findig first MsDesc in TEI: " + e.getMessage(), e);
    }

    return msDesc.orElseThrow(
        () -> new HSPCommandException("TEI contains no MsDesc!"));
  }

  protected static String marshalTEI(TEI tei) throws HSPCommandException {
    try {
      return TEIObjectFactory.marshal(tei);
    } catch (Exception e) {
      throw new HSPCommandException("Error marshalling TEI: " + e.getMessage(),
          e);
    }
  }

  public static void addChangeRevisionStatement(TEI tei, Change incommingChange)
      throws Exception {
    Objects.requireNonNull(tei, "TEI-Dokument darf nicht null sein!");

    TeiHeader header = TEICommon.findFirst(TeiHeader.class, tei)
        .orElseGet(() -> {
          TeiHeader newHeader = new TeiHeader();
          tei.setTeiHeader(newHeader);
          return newHeader;
        });

    RevisionDesc revisionDesc = TEICommon.findFirst(RevisionDesc.class, header)
        .orElseGet(() -> {
          RevisionDesc newRevisionDesc = new RevisionDesc();
          header.setRevisionDesc(newRevisionDesc);
          return newRevisionDesc;
        });

    revisionDesc.getRevisionChangeAttribute().removeIf(
        existingChange -> AttributsReferenzStatusTyp.AUTOMATISCH.name()
            .equals(existingChange.getN()) && incommingChange.getN()
            .equals(AttributsReferenzStatusTyp.AUTOMATISCH.name())
            || !findAllUsedChangeIDs(tei).contains(existingChange.getId()));

    revisionDesc.getRevisionChangeAttribute().add(incommingChange);
  }

  public static Set<String> findAllUsedChangeIDs(TEI tei) {
    try {
      MsDesc msDesc = findFirstMsDesc(tei);
      Set<String> set = msDesc.getHeads().getFirst().getContent().stream()
          .filter(o -> o instanceof Index)
          .map(o -> (Index) o).filter(i -> i.getChanges() != null && !i.getChanges().isEmpty())
          .map(i -> i.getChanges().getFirst()
              .replaceAll(TEIValues.TEI_ANKER_TAG, "")).collect(toSet());
      return set;

    } catch (Exception e) {
      return Set.of();
    }
  }

  public static Change createChangeElement(String changeId,
      LocalDateTime changeDate,
      AttributsReferenzStatusTyp changeStatus, String changeText,
      Bearbeiter bearbeiter) {
    Change change = new Change();
    change.setId(changeId);
    change.setWhen(
        changeDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    change.setN(changeStatus.name());
    change.getContent().add(changeText);

    if (bearbeiter != null) {
      PersName persName = new PersName();
      if (bearbeiter.getPerson() != null) {
        persName.setKey(bearbeiter.getPerson().getId());
        persName.getReves().add(bearbeiter.getPerson().getGndUrl());
        persName.getContent().add(bearbeiter.getPerson().getName());
      } else {
        persName.setKey(bearbeiter.getId());
        persName.getContent().add(bearbeiter.getName());
      }

      if (!persName.getContent().isEmpty()) {
        change.getContent().add(persName);
      }
    }
    return change;
  }
}
