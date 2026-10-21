package de.staatsbibliothek.berlin.hsp.mapper.tei;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.PUBLICATION_DATE_PRIMARY;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.PUBLICATION_DATE_SECONDARY;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.ElementFactoryRegistry.buildElement;
import static java.util.Objects.requireNonNullElse;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GlobaleBeschreibungsKomponente;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation.PublikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz.LizenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
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

  public static Lizenz findLizenz(TEI tei) throws Exception {
    List<Licence> licences = new ArrayList<>();
    TEICommon.findAll(Licence.class, tei, licences);
    return findLizenz(licences);
  }

  public static Lizenz findLizenz(List<Licence> licences) {
    Lizenz lizenz = null;
    if (Objects.nonNull(licences) && !licences.isEmpty()) {
      Licence licence = licences.getFirst();

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
        result = LocalDate.of(Integer.parseInt(datum), 12, 31).atStartOfDay();
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
        .flatMap(publisher -> publisher.stream()
            .filter(element -> Idno.class.isAssignableFrom(element.getClass()))
            .filter(idno -> IDNO_HSK_TYPE.equals(((Idno) idno).getType())).findFirst())
        .ifPresent(hsk -> hskNumber.set(TEICommon.getContentAsString(((Idno) hsk).getContent())));

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

    for (MsDesc msDesc : TEIQuery.queryForMsDescAsBeschreibung(tei)) {
      Identifikation gueltigeIdentifikation = buildElement(Identifikation.class, msDesc, tei);
      Optional<String> optMsDesc = findKODIdentifier(msDesc);
      String kodid = optMsDesc.orElse(null);
      Beschreibung beschreibung = new BeschreibungsBuilder(msDesc.getId(), kodid,
          gueltigeIdentifikation)
          .withTEIXml(TEIObjectFactory.marshal(tei))
          .withAenderungsDatum(findAenderungsdatumInTEIHeaderAndFiledesc(tei))
          .withBeschreibungssprache(getSchreibsprache(tei.getLang(), msDesc.getLang()))
          .withErstellungsDatum(findErstellungsDatum(tei))
          .withVerwaltungsTyp(findVerwaltungsTyp(msDesc))
          .withBeschreibungsTyp(findBeschreibungsTyp(msDesc))
          .withDokumentObjectTyp(findDokumentObjektTyp(msDesc))
          .withPublikation(findPublikation(tei))
          .addAutoren(AutorenFactory.buildAutoren(TEICommon.findFirst(FileDesc.class, tei)))
          .build();

      addAltIdentifier(beschreibung, msDesc);

      optMsDesc.ifPresent(id -> beschreibung.getAltIdentifier().remove(id));

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

  public List<Beschreibung> map(List<TEI> tei) throws Exception {
    List<Beschreibung> beschreibungList = new ArrayList<>();
    for (TEI teiItem : tei) {
      beschreibungList.addAll(map(teiItem));
    }
    return beschreibungList;
  }

  public Optional<String> findKODIdentifier(MsDesc msDesc) {

    return msDesc.getMsIdentifier().getMsNamesAndObjectNamesAndAltIdentifiers().stream()
        .filter(o -> o instanceof AltIdentifier altidentifier
            && altidentifier.getCollection() != null
            && altidentifier.getCollection().getContent() != null && altidentifier
            .getCollection().getContent().getFirst().equals(TEIValues.KOD_ID_COLLECTION))
        .map(
            altIdentifier -> (String) ((AltIdentifier) altIdentifier).getIdno().getContent().getFirst())
        .findFirst();
  }

  public void addAltIdentifier(Beschreibung beschreibung, MsDesc msDesc) {

    List<String> altIdentifier = msDesc.getMsIdentifier()
        .getMsNamesAndObjectNamesAndAltIdentifiers().stream()
        .filter(AltIdentifier.class::isInstance)
        .map(o -> TEICommon.getContentAsString(((AltIdentifier) o).getIdno().getContent()))
        .toList();

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
            .forEach(dates::add));

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
        .flatMap(profileDesc -> profileDesc.getAbstractsAndCreationsAndLangUsages().stream())
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

  Publikation findPublikation(TEI tei) throws Exception {
    PublicationStmt publicationStmt = Optional.ofNullable(tei)
        .map(TEI::getTeiHeader)
        .map(TeiHeader::getFileDesc)
        .map(FileDesc::getPublicationStmt)
        .orElse(null);

    if (publicationStmt == null) {
      return null;
    }

    // Extract primary and secondary publication dates
    LocalDateTime erstpublikationsDatum = null;
    LocalDateTime publikationsDatumHSP = null;

    for (Object obj : publicationStmt.getPublishersAndDistributorsAndAuthorities()) {
      if (obj instanceof Date date && Objects.nonNull(date.getWhen())) {
        var defaultDate = LocalDateTime.of(1900, 1, 1, 0, 0);
        var actualDate = requireNonNullElse(convertTEIDate(date.getWhen()),
            defaultDate);
        if (PUBLICATION_DATE_PRIMARY.equals(date.getType())) {
          erstpublikationsDatum = actualDate;
        } else if (PUBLICATION_DATE_SECONDARY.equals(date.getType())) {
          publikationsDatumHSP = actualDate;
        }
      }
    }

    // Extract PURLs from pubPlace elements
    Set<PURL> purls = findPurls(publicationStmt);

    // Extract Lizenz
    Lizenz lizenz = findLizenz(tei);

    // Build Publikation with all data
    PublikationBuilder builder = new PublikationBuilder();

    if (erstpublikationsDatum != null) {
      builder.withErstpublikationsDatum(erstpublikationsDatum);
    }

    if (publikationsDatumHSP != null) {
      builder.withPublikationsDatumHSP(publikationsDatumHSP);
    }

    if (lizenz != null) {
      builder.withLizenz(lizenz);
    }

    if (!purls.isEmpty()) {
      builder.withPURLs(purls);
    }

    return builder.build();
  }

  Set<PURL> findPurls(PublicationStmt publicationStmt) {
    return publicationStmt.getPublishersAndDistributorsAndAuthorities().stream()
        .filter(PubPlace.class::isInstance)
        .map(PubPlace.class::cast)
        .flatMap(pubPlace -> pubPlace.getContent().stream())
        .filter(Ptr.class::isInstance)
        .map(Ptr.class::cast)
        .filter(ptr -> PtrFactory.TYPE.equals(ptr.getType()) && !ptr.getTargets().isEmpty())
        .map(this::mapPtrToPURL)
        .collect(Collectors.toSet());
  }

  PURL mapPtrToPURL(Ptr ptr) {
    Optional<String> id = Optional.ofNullable(ptr.getId());
    URI purl = URI.create(ptr.getTargets().iterator().next());
    PURLTyp purlTyp =
        PtrFactory.SUBTYPE.equals(ptr.getSubtype()) ? PURLTyp.INTERNAL : PURLTyp.EXTERNAL;
    return id.map(s -> new PURL(s, purl, purl, purlTyp))
        .orElseGet(() -> new PURL(purl, purl, purlTyp));
  }
}
