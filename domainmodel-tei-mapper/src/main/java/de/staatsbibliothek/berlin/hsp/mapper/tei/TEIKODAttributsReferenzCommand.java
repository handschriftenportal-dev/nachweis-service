/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.mapper.tei;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
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
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPCommandException;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactory;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.Head;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.SourceDesc;
import org.tei_c.ns._1.TEI;

public class TEIKODAttributsReferenzCommand {

  private static final String MSG_REQ_KOD = "kod is required!";

  private static final Logger logger = LoggerFactory.getLogger(TEIKODAttributsReferenzCommand.class);

  static Map<AttributsTyp, Function<AttributsTyp, AttributsReferenz>> emptyBuildersMap
      = new EnumMap<>(AttributsTyp.class);

  static {
    emptyBuildersMap.put(AttributsTyp.TITEL, (attributsTyp -> Titel.builder().build()));
    emptyBuildersMap.put(AttributsTyp.BESCHREIBSTOFF, (attributsTyp -> Beschreibstoff.builder().build()));
    emptyBuildersMap.put(AttributsTyp.UMFANG, (attributsTyp -> Umfang.builder().build()));
    emptyBuildersMap.put(AttributsTyp.ABMESSUNG, (attributsTyp -> Abmessung.builder().build()));
    emptyBuildersMap.put(AttributsTyp.FORMAT, (attributsTyp -> Format.builder().build()));
    emptyBuildersMap.put(AttributsTyp.ENTSTEHUNGSORT, (attributsTyp -> Entstehungsort.builder().build()));
    emptyBuildersMap.put(AttributsTyp.ENTSTEHUNGSZEIT, (attributsTyp -> Entstehungszeit.builder().build()));
    emptyBuildersMap.put(AttributsTyp.GRUNDSPRACHE, (attributsTyp -> Grundsprache.builder().build()));
    emptyBuildersMap.put(AttributsTyp.HANDSCHRIFTENTYP, (attributsTyp -> Handschriftentyp.builder().build()));
    emptyBuildersMap.put(AttributsTyp.STATUS, (attributsTyp -> Status.builder().build()));
    emptyBuildersMap.put(AttributsTyp.BUCHSCHMUCK, (attributsTyp -> Buchschmuck.builder().build()));
    emptyBuildersMap.put(AttributsTyp.MUSIKNOTATION, (attributsTyp -> Musiknotation.builder().build()));
  }

  private TEIKODAttributsReferenzCommand() {
    //do nothing
  }

  public static void updateAttributsReferenzen(KulturObjektDokument kod, Beschreibung beschreibung) throws HSPCommandException, HSPMapperException {

    Objects.requireNonNull(kod, MSG_REQ_KOD);
    Objects.requireNonNull(kod.getTeiXML(), "kod.teiXML is required!");
    Objects.requireNonNull(kod.getBeschreibungenIDs(), "kod.beschreibungenIDs is required!");
    Objects.requireNonNull(beschreibung, "beschreibung is required!");
    Objects.requireNonNull(beschreibung.getTeiXML(), "beschreibung.teiXML is required!");

    if (!kod.getBeschreibungenIDs().contains(beschreibung.getId())
        || VerwaltungsTyp.EXTERN != beschreibung.getVerwaltungsTyp()) {
      return;
    }

    TEI kodTEI = openTEIDokument(kod.getTeiXML());
    MsDesc kodMsDesc = findFirstMsDesc(kodTEI);

    Map<AttributsTyp, AttributsReferenz> kodReferenzen = TEI2AttributsReferenzMapper.map(kodTEI, kodMsDesc);

    Map<AttributsTyp, AttributsReferenz> beschreibungReferenzen = TEI2AttributsReferenzMapper.map(
        beschreibung.getTeiXML());

    addPURLToAttributsreferenzen(beschreibung, beschreibungReferenzen);

    boolean forceNew = kod.getBeschreibungenIDs().size() == 1;

    Set<AttributsReferenz> mergedReferenzen =
        mergeAttributsReferenzen(kodReferenzen, beschreibungReferenzen, forceNew, new HashMap<>());

    updateIndices(mergedReferenzen, kodMsDesc);

    kod.setTeiXML(marshalTEI(kodTEI));
  }

  protected static void addPURLToAttributsreferenzen(Beschreibung beschreibung,
      Map<AttributsTyp, AttributsReferenz> beschreibungReferenzen) {
    if(beschreibung != null && beschreibung.getPURLs() != null && beschreibungReferenzen != null) {
      beschreibung.getPURLs().stream().filter(purl -> purl.getTyp().equals(PURLTyp.INTERNAL))
          .findFirst().ifPresent(internalPURL -> beschreibungReferenzen.values()
              .forEach(attributsReferenz -> {
                if (internalPURL.getPurl() != null) {
                  attributsReferenz.setReferenzURL(internalPURL.getPurl().toString());
                }
              }));
    }
  }

  public static void updateAttributsReferenzen(KulturObjektDokument kod, Set<AttributsReferenz> attributsReferenzen, Map<String, PURL> purlMap) throws HSPCommandException, HSPMapperException {

    Objects.requireNonNull(kod, MSG_REQ_KOD);
    Objects.requireNonNull(kod.getTeiXML(), "kod.teiXML is required!");
    Objects.requireNonNull(attributsReferenzen, "attributsReferenzen is required!");

    TEI kodTEI = openTEIDokument(kod.getTeiXML());
    MsDesc kodMsDesc = findFirstMsDesc(kodTEI);

    Map<AttributsTyp, AttributsReferenz> kodReferenzen = TEI2AttributsReferenzMapper.map(kodTEI, kodMsDesc);

    Map<AttributsTyp, AttributsReferenz> beschreibungsReferenzen = attributsReferenzen.stream()
        .collect(Collectors.toMap(AttributsReferenz::getAttributsTyp, Function.identity()));

    Set<AttributsReferenz> mergedReferenzen = mergeAttributsReferenzen(kodReferenzen, beschreibungsReferenzen, true,  purlMap);

    updateIndices(mergedReferenzen, kodMsDesc);

    kod.setTeiXML(marshalTEI(kodTEI));
  }

  public static void deleteAttributsReferenzenForBeschreibung(KulturObjektDokument kod, String beschreibungId) throws HSPCommandException, HSPMapperException {

    Objects.requireNonNull(kod, MSG_REQ_KOD);
    Objects.requireNonNull(kod.getTeiXML(), "beschreibungId is required!");

    logger.info("Delete Attributsrefrenzen for KOD {} and Beschreibung {}",  kod.getId(), beschreibungId);

    TEI kodTEI = openTEIDokument(kod.getTeiXML());
    MsDesc kodMsDesc = findFirstMsDesc(kodTEI);

    Map<AttributsTyp, AttributsReferenz> kodReferenzen = TEI2AttributsReferenzMapper.map(kodTEI, kodMsDesc);

    Set<AttributsReferenz> cleanedReferenzen = kodReferenzen.values().stream()
        .map(referenz -> beschreibungId.equals(referenz.getReferenzId()) ?
            buildEmptyAttributsReferenz(referenz.getAttributsTyp()) : null)
        .filter(Objects::nonNull).collect(Collectors.toSet());

    if(!cleanedReferenzen.isEmpty()) {

      logger.debug("Clean Attributsreferenzen {}", cleanedReferenzen.stream()
          .map(attributsReferenz -> attributsReferenz.toString()).
          collect(Collectors.joining(",")));

      updateIndices(cleanedReferenzen, kodMsDesc);

      String tei = marshalTEI(kodTEI);
      logger.debug("Delete Attributsreferenzen in TEI {} ", tei);
      kod.setTeiXML(tei);
    }
  }

  public static void migrateAttributsReferenzen(KulturObjektDokument kod, Map<String,PURL> purlMap)
      throws HSPCommandException, HSPMapperException {

    TEI kodTEI = openTEIDokument(kod.getTeiXML());

    SourceDesc sourceDesc = findSourceDesc(kodTEI, kod.getId());

    Map<AttributsTyp, AttributsReferenz> beschreibungsReferenzen = new EnumMap<>(AttributsTyp.class);

    Optional<MsDesc> beschreibungMsDesc = findBeschreibungMsDescInSourceDesc(sourceDesc);

    if (beschreibungMsDesc.isPresent()) {
      beschreibungsReferenzen = TEI2AttributsReferenzMapper.map(kodTEI, beschreibungMsDesc.get());
    }

    Set<AttributsReferenz> mergedReferenzen =
        mergeAttributsReferenzen(new EnumMap<>(AttributsTyp.class), beschreibungsReferenzen, true, purlMap);

    TEI reOpenedKodTEI = openTEIDokument(kod.getTeiXML());
    SourceDesc reOpenedSourceDesc = findSourceDesc(reOpenedKodTEI, kod.getId());
    MsDesc reOpenedKodMsDesc = findKodMsDescInSourceDesc(reOpenedSourceDesc)
        .orElseThrow(() -> new HSPCommandException("No KOD-MsDesc in TEI of KOD " + kod.getId()));

    updateIndices(mergedReferenzen, reOpenedKodMsDesc);

    kod.setTeiXML(marshalTEI(reOpenedKodTEI));
  }

  static void updateIndices(Set<AttributsReferenz> attributsReferenzen, MsDesc msDesc) {

    Head head = msDesc.getHeads().stream().findFirst().orElse(null);
    if (Objects.isNull(head)) {
      head = new Head();
      msDesc.getHeads().add(head);
    }

    logger.info("Add Attributsreferenzen to Head {}", attributsReferenzen.stream()
        .map(attributsReferenz -> attributsReferenz.toString()).
        collect(Collectors.joining(",")));

    replaceAllHeadIndexElements(attributsReferenzen, head);
  }

  protected static void replaceAllHeadIndexElements(Set<AttributsReferenz> attributsReferenzen, Head head) {

    Set<String> indexNamesToReplace = getIndexNames(attributsReferenzen);

    logger.debug("Replace in Head{} ", indexNamesToReplace.stream()
        .collect(Collectors.joining(", ")));

    head.getContent().removeIf(content -> Objects.nonNull(content)
        && content instanceof Index
        && indexNamesToReplace.contains(((Index) content).getIndexName()));

    /**
     * This part is needed only because of serialization and desialization of XML using JAXB
     * Index Element contains source as ArrayList and will be added always as empty source which is not allowed
     */

    final List<Index> indexElementsToRemove = new ArrayList<>();
    final List<Index> indexElementsToAdd = new ArrayList<>();
    for (Object headElement : head.getContent()) {
      if (headElement instanceof Index) {
        Index headIndexElement = (Index) headElement;
        if (!indexNamesToReplace.contains(headIndexElement.getIndexName())) {
          Index newIndex = new Index();
          newIndex.setIndexName(headIndexElement.getIndexName());
          IndexFactory.setIndexSourceAndCopyOf(newIndex, headIndexElement.getCopyOf(),
              headIndexElement.getSources().stream().findFirst().orElse(""));
          indexElementsToRemove.add((Index) headElement);
          indexElementsToAdd.add(newIndex);
        }
      }
    }

    head.getContent().removeAll(indexElementsToRemove);
    //Index Elements created based on old onces
    head.getContent().addAll(indexElementsToAdd);
    //Index Elemtns created based on Attributsreferenzes
    head.getContent().addAll(IndexFactory.build(attributsReferenzen));
  }

  static AttributsReferenz buildEmptyAttributsReferenz(AttributsTyp attributsTyp) {
    Objects.requireNonNull(attributsTyp, "attributsTyp is required!");

    return Optional.ofNullable(emptyBuildersMap.get(attributsTyp))
        .map(function -> function.apply(attributsTyp))
        .orElseThrow(() -> new IllegalArgumentException("Unmapped attributsTyp: " + attributsTyp));
  }

  static Set<AttributsReferenz> mergeAttributsReferenzen(Map<AttributsTyp, AttributsReferenz> kodReferenzen,
      Map<AttributsTyp, AttributsReferenz> beschreibungReferenzen, boolean forceNew, Map<String,PURL> purlMap) {

    Set<AttributsReferenz> mergedReferenzen = new LinkedHashSet<>();

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      AttributsReferenz kodReferenz = Optional.ofNullable(kodReferenzen.get(attributsTyp))
          .orElse(buildEmptyAttributsReferenz(attributsTyp));
      AttributsReferenz beschreibungsReferenz = Optional.ofNullable(beschreibungReferenzen.get(attributsTyp))
          .orElse(buildEmptyAttributsReferenz(attributsTyp));

      AttributsReferenz mergedReferenz;

      if (forceNew || Objects.equals(kodReferenz.getReferenzId(), beschreibungsReferenz.getReferenzId())) {
        mergedReferenz = beschreibungsReferenz;
      } else {
        mergedReferenz = kodReferenz;
      }

      if(StringUtils.isBlank(mergedReferenz.getReferenzURL()) && purlMap.containsKey(mergedReferenz.getReferenzId()))
      {
        if(purlMap.get(mergedReferenz.getReferenzId()).getPurl() != null) {
          mergedReferenz.setReferenzURL(
              purlMap.get(mergedReferenz.getReferenzId()).getPurl().toString());
        }
      }

      mergedReferenzen.add(mergedReferenz);
    }

    return mergedReferenzen;
  }

  static Set<String> getIndexNames(Set<AttributsReferenz> attributsReferenzen) {
    return Stream.ofNullable(attributsReferenzen)
        .flatMap(Collection::stream)
        .map(AttributsReferenz::getAttributsTyp)
        .map(AttributsTyp::getIndexName)
        .collect(Collectors.toSet());
  }

  static SourceDesc findSourceDesc(TEI tei, String kodId) throws HSPCommandException {
    Optional<SourceDesc> optionalSourceDesc;
    try {
      optionalSourceDesc = TEICommon.findFirst(SourceDesc.class, tei);
    } catch (Exception e) {
      throw new HSPCommandException("Error finding sourceDesc in TEI of KOD " + kodId, e);
    }
    return optionalSourceDesc.orElseThrow(() -> new HSPCommandException("No sourceDesc in TEI of KOD " + kodId));
  }

  static Optional<MsDesc> findKodMsDescInSourceDesc(SourceDesc sourceDesc) {
    return sourceDesc.getBiblsAndBiblStructsAndListBibls().stream()
        .filter(MsDesc.class::isInstance)
        .map(MsDesc.class::cast)
        .filter(msDesc -> DokumentObjektTyp.HSP_OBJECT.toString().equals(msDesc.getType()))
        .findFirst();
  }

  static Optional<MsDesc> findBeschreibungMsDescInSourceDesc(SourceDesc sourceDesc) {
    return sourceDesc.getBiblsAndBiblStructsAndListBibls().stream()
        .filter(MsDesc.class::isInstance)
        .map(MsDesc.class::cast)
        .filter(msDesc -> (DokumentObjektTyp.HSP_DESCRIPTION.toString().equals(msDesc.getType())
            || DokumentObjektTyp.HSP_DESCRIPTION_RETRO.toString().equals(msDesc.getType())))
        .findFirst();
  }

  private static TEI openTEIDokument(String teiXML) throws HSPCommandException {
    Optional<TEI> tei;

    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(teiXML.getBytes(StandardCharsets.UTF_8))) {
      tei = TEIObjectFactory.unmarshalOne(inputStream);
    } catch (Exception e) {
      throw new HSPCommandException("Error reading TEI-XML: " + e.getMessage(), e);
    }
    return tei.orElseThrow(() -> new HSPCommandException("TEI-XML contains no TEI!"));
  }

  private static MsDesc findFirstMsDesc(TEI tei) throws HSPCommandException {
    Optional<MsDesc> msDesc;
    try {
      msDesc = TEICommon.findFirst(MsDesc.class, tei);
    } catch (Exception e) {
      throw new HSPCommandException("Error findig first MsDesc in TEI: " + e.getMessage(), e);
    }

    return msDesc.orElseThrow(() -> new HSPCommandException("TEI contains no MsDesc!"));
  }

  private static String marshalTEI(TEI tei) throws HSPCommandException {
    try {
      return TEIObjectFactory.marshal(tei);
    } catch (Exception e) {
      throw new HSPCommandException("Error marshalling TEI: " + e.getMessage(), e);
    }
  }

}
