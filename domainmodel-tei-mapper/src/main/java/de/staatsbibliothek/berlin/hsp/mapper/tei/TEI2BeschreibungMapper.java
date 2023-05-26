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

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.PUBLICATION_DATE_PRIMARY;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.PUBLICATION_DATE_SECONDARY;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.ElementFactoryRegistry.buildElement;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GlobaleBeschreibungsKomponente;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz.LizenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.api.Mapper;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AutorenFactory;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.PtrFactory;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.AltIdentifier;
import org.tei_c.ns._1.Creation;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.FileDesc;
import org.tei_c.ns._1.Idno;
import org.tei_c.ns._1.Licence;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.ProfileDesc;
import org.tei_c.ns._1.Ptr;
import org.tei_c.ns._1.PubPlace;
import org.tei_c.ns._1.PublicationStmt;
import org.tei_c.ns._1.RevisionDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.TeiHeader;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 20.04.20
 */
public class TEI2BeschreibungMapper implements Mapper<TEI, List<Beschreibung>> {

  public static final String IDNO_HSK_TYPE = "hsk";
  private static final Logger log = LoggerFactory.getLogger(TEI2BeschreibungMapper.class);

  public static VerwaltungsTyp findVerwaltungsTyp(MsDesc msDesc) {
    if (msDesc.getStatus() != null && !msDesc.getStatus().isEmpty() && !"draft"
        .equals(msDesc.getStatus())) {
      return VerwaltungsTyp.valueOf(msDesc.getStatus().toUpperCase());
    }

    return VerwaltungsTyp.INTERN;
  }

  public static DokumentObjektTyp findDokumentObjektTyp(MsDesc msDesc) {
    if (msDesc.getType() != null && !msDesc.getType().isEmpty()) {
      return DokumentObjektTyp.fromString(msDesc.getType());
    }

    return null;
  }

  public static BeschreibungsTyp findBeschreibungsTyp(MsDesc msDesc) {
    if (msDesc.getSubtype() != null && !msDesc.getSubtype().isEmpty()) {
      return BeschreibungsTyp.valueOf(msDesc.getSubtype().toUpperCase());
    }

    return BeschreibungsTyp.MEDIEVAL;
  }

  public static Lizenz findLizenz(List<Licence> licences) {
    Lizenz lizenz = null;
    if (Objects.nonNull(licences) && !licences.isEmpty()) {
      Licence licence = licences.get(0);

      Set<String> uris = null;
      if (licence.getTargets() != null && !licence.getTargets().isEmpty()) {
        uris = new LinkedHashSet<>(licence.getTargets());
      }
      String beschreibungsText = null;
      if (licence.getContent() != null && !licence.getContent().isEmpty()) {
        beschreibungsText = StringUtils.normalizeSpace(
            TEICommon.getContentAsString(licence.getContent()));
      }

      if (uris != null || beschreibungsText != null) {
        lizenz = new LizenzBuilder()
            .withBeschreibungsText(beschreibungsText)
            .withUris(uris)
            .build();
      }

    }
    return lizenz;
  }

  public static NormdatenReferenz getSchreibsprache(String local, String global) {
    String msDescLang2 = getLocalOrGlobal(local, global);
    NormdatenReferenz beschreibungsSprache;
    if (Objects.isNull(msDescLang2)) {
      beschreibungsSprache = null;
    } else {
      beschreibungsSprache = new NormdatenReferenz(msDescLang2, msDescLang2, "");
    }
    return beschreibungsSprache;
  }

  public static String getLocalOrGlobal(String local, String global) {
    if (Objects.nonNull(local) && !local.isEmpty()) {
      return local;
    }
    if (Objects.nonNull(global) && !global.isEmpty()) {
      return global;
    }
    return null;
  }

  public static LocalDateTime convertTEIDate(String datum) {
    LocalDateTime result = null;
    try {
      if (datum != null && !datum.isEmpty()) {
        result = LocalDateTime
            .from(LocalDate.parse(datum, DateTimeFormatter.ISO_DATE).atStartOfDay());
      }
    } catch (DateTimeParseException dtPEx) {
      if (datum.matches("\\d{4}")) {
        result = LocalDate.of(Integer.valueOf(datum), 12, 31).atStartOfDay();
      }
    }
    return result;
  }

  public static String findKatalogHSKID(TEI tei) {

    AtomicReference<String> hskNumber = new AtomicReference<>();

    Optional.ofNullable(tei)
        .map(TEI::getTeiHeader)
        .map(TeiHeader::getFileDesc)
        .map(FileDesc::getPublicationStmt)
        .map(PublicationStmt::getPublishersAndDistributorsAndAuthorities)
        .ifPresent(publisher -> publisher.stream()
            .filter(element -> Idno.class.isAssignableFrom(element.getClass()))
            .filter(idno -> IDNO_HSK_TYPE.equals(((Idno) idno).getType())).findFirst()
            .ifPresent(
                hsk -> hskNumber.set(TEICommon.getContentAsString(((Idno) hsk).getContent()))));

    return hskNumber.get();
  }

  /**
   * tei.beschreibungsAenderungsDatum=/TEI//revisionsDesc/change[last()/@when]/@when;/TEI/teiHeader/fileDesc/publicationStmt/date[@type='issued']/@when
   */
  @Override
  public List<Beschreibung> map(TEI tei) throws Exception {
    if (Objects.isNull(tei)) {
      throw new HSPMapperException("TEI tei is null");
    }
    log.info("Start mapping of the TEI to Beschreibung..");
    List<Beschreibung> beschreibungList = new ArrayList<>();

    List<Licence> licences = new ArrayList<>();
    TEICommon.findAll(Licence.class, tei, licences);

    for (MsDesc msDesc : TEIQuery.queryForMsDescAsBeschreibung(tei)) {
      Beschreibung beschreibung = new BeschreibungsBuilder()
          .withId(msDesc.getId())
          .withTEIXml(TEIObjectFactory.marshal(tei))
          .withAenderungsDatum(findAenderungsdatumInTEIHeaderAndFiledesc(tei))
          .withBeschreibungssprache(getSchreibsprache(tei.getLang(), msDesc.getLang()))
          .withErstellungsDatum(findErstellungsDatum(tei))
          .withVerwaltungsTyp(findVerwaltungsTyp(msDesc))
          .withBeschreibungsTyp(findBeschreibungsTyp(msDesc))
          .withDokumentObjectTyp(findDokumentObjektTyp(msDesc))
          .withLizenz(findLizenz(licences))
          .withPublikationen(findPublikationen(tei))
          .withPURLs(findPURLs(tei))
          .build();

      beschreibung.getBeschreibungsBeteiligte()
          .addAll(buildElement(NormdatenReferenz.class, msDesc, tei));
      addAltIdentifier(beschreibung, msDesc);

      findKODIdentifier(beschreibung, msDesc).ifPresent(kodid -> {
        beschreibung.setKodID(kodid);
        beschreibung.getAltIdentifier().remove(kodid);
      });

      beschreibung.getAutoren()
          .addAll(AutorenFactory.buildAutoren(TEICommon.findFirst(FileDesc.class, tei)));

      Set<GlobaleBeschreibungsKomponente> beschreibungsStruktur = beschreibung
          .getBeschreibungsStruktur();
      beschreibungsStruktur.add(buildElement(BeschreibungsKomponenteKopf.class, msDesc, tei));

      beschreibungList.add(beschreibung);

      if (beschreibung.getKatalogID() == null || beschreibung.getKatalogID().isEmpty()) {
        beschreibung.setKatalogID(findKatalogHSKID(tei));
      }

      log.debug("Imported Beschreibung {}", beschreibung);
    }
    return beschreibungList;
  }

  public Optional<String> findKODIdentifier(Beschreibung beschreibung, MsDesc msDesc) {

    return msDesc.getMsIdentifier().getMsNamesAndObjectNamesAndAltIdentifiers().stream()
        .filter(o -> o instanceof AltIdentifier
            && ((AltIdentifier) o).getCollection() != null
            && ((AltIdentifier) o).getCollection().getContent() != null && ((AltIdentifier) o)
            .getCollection().getContent().get(0).equals(TEIValues.KOD_ID_COLLECTION))
        .map(
            altIdentifier -> (String) ((AltIdentifier) altIdentifier).getIdno().getContent().get(0))
        .findFirst();
  }

  public void addAltIdentifier(Beschreibung beschreibung, MsDesc msDesc) {

    List<String> altIdentifier = msDesc.getMsIdentifier()
        .getMsNamesAndObjectNamesAndAltIdentifiers().stream()
        .filter(AltIdentifier.class::isInstance)
        .map(o -> TEICommon.getContentAsString(((AltIdentifier) o).getIdno().getContent()))
        .collect(Collectors.toList());

    altIdentifier.forEach(i -> beschreibung.getAltIdentifier().add(i));
  }

  LocalDateTime findAenderungsdatumInTEIHeaderAndFiledesc(TEI tei) {

    List<Date> dates = new ArrayList<>();

    Optional.ofNullable(tei)
        .map(TEI::getTeiHeader)
        .map(TeiHeader::getRevisionDesc).map(RevisionDesc::getRevisionChangeAttribute)
        .flatMap(changes -> changes.stream().findFirst())
        .ifPresent(change -> change.getContent().stream()
            .filter(Date.class::isInstance)
            .map(Date.class::cast)
            .forEach(date -> dates.add(date)));

    return dates.stream().findFirst().map(changeDate -> convertTEIDate(changeDate.getWhen()))
        .orElse(null);

  }

  LocalDateTime findErstellungsDatum(TEI tei) {
    return Optional.ofNullable(tei)
        .map(TEI::getTeiHeader)
        .stream()
        .flatMap(header -> header.getEncodingDescsAndProfileDescsAndXenoDatas().stream())
        .filter(ProfileDesc.class::isInstance)
        .findFirst()
        .map(ProfileDesc.class::cast)
        .stream()
        .flatMap(profileDesc -> profileDesc.getTextDescsAndParticDescsAndSettingDescs().stream())
        .filter(Creation.class::isInstance)
        .findFirst()
        .map(Creation.class::cast)
        .stream()
        .flatMap(creation -> creation.getContent().stream())
        .filter(Date.class::isInstance)
        .findFirst()
        .map(Date.class::cast)
        .map(date -> convertTEIDate(date.getWhen()))
        .orElse(null);
  }

  Set<Publikation> findPublikationen(TEI tei) {
    return Optional.ofNullable(tei)
        .map(TEI::getTeiHeader)
        .map(TeiHeader::getFileDesc)
        .map(FileDesc::getPublicationStmt)
        .stream()
        .flatMap(publicationStmt -> publicationStmt.getPublishersAndDistributorsAndAuthorities().stream())
        .filter(object -> Date.class.isAssignableFrom(object.getClass()))
        .map(Date.class::cast)
        .filter(date -> Objects.nonNull(date.getWhen())
            && (PUBLICATION_DATE_PRIMARY.equals(date.getType()) || PUBLICATION_DATE_SECONDARY.equals(date.getType())))
        .map(date -> {
          LocalDateTime publicationDate = convertTEIDate(date.getWhen());
          if (publicationDate == null) {
            publicationDate = LocalDateTime.of(1900, 1, 1, 0, 0);
          }

          PublikationsTyp typ = null;
          if (PUBLICATION_DATE_PRIMARY.equals(date.getType())) {
            typ = PublikationsTyp.ERSTPUBLIKATION;
          } else if (PUBLICATION_DATE_SECONDARY.equals(date.getType())) {
            typ = PublikationsTyp.PUBLIKATION_HSP;
          }

          return new Publikation(UUID.randomUUID().toString(), publicationDate, typ);
        })
        .collect(Collectors.toSet());
  }

  Set<PURL> findPURLs(TEI tei) {
    return Optional.ofNullable(tei)
        .map(TEI::getTeiHeader)
        .map(TeiHeader::getFileDesc)
        .map(FileDesc::getPublicationStmt)
        .stream()
        .flatMap(publicationStmt -> publicationStmt.getPublishersAndDistributorsAndAuthorities().stream())
        .filter(PubPlace.class::isInstance)
        .map(PubPlace.class::cast)
        .flatMap(pubPlace -> pubPlace.getContent().stream())
        .filter(Ptr.class::isInstance)
        .map(Ptr.class::cast)
        .filter(ptr -> PtrFactory.TYPE.equals(ptr.getType()) && !ptr.getTargets().isEmpty())
        .map(ptr -> mapPtrToPURL(ptr))
        .collect(Collectors.toSet());
  }

  PURL mapPtrToPURL(Ptr ptr) {
    Optional<String> id = Optional.ofNullable(ptr.getId());
    URI purl = URI.create(ptr.getTargets().iterator().next());
    PURLTyp purlTyp = PtrFactory.SUBTYPE.equals(ptr.getSubtype()) ? PURLTyp.INTERNAL : PURLTyp.EXTERNAL;
    return id.isPresent() ? new PURL(id.get(), purl, purl, purlTyp) : new PURL(purl, purl, purlTyp);
  }
}
