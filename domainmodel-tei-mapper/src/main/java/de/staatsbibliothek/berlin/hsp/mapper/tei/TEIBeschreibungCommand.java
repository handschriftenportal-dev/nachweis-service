package de.staatsbibliothek.berlin.hsp.mapper.tei;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIQuery.ORIG_PLACE_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIQuery.TEXT_LANG_ID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIQuery.findGNDIDinListOfStrings;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.DATE_TIME_PATTERN;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.LOCAL_DATE_FORMAT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.MSPART_TYPE_OTHER;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.PTR_TYPE_HSP;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.PUBLICATION_DATE_SECONDARY;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ROLE_AUTHOR;
import static de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory.unmarshalOne;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.AltIdentifierFactory;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import jakarta.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.BiblScope;
import org.tei_c.ns._1.Change;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.Div;
import org.tei_c.ns._1.Idno;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.MsIdentifier;
import org.tei_c.ns._1.MsPart;
import org.tei_c.ns._1.Name;
import org.tei_c.ns._1.P;
import org.tei_c.ns._1.PersName;
import org.tei_c.ns._1.Ptr;
import org.tei_c.ns._1.PubPlace;
import org.tei_c.ns._1.PublicationStmt;
import org.tei_c.ns._1.Publisher;
import org.tei_c.ns._1.Resp;
import org.tei_c.ns._1.RespStmt;
import org.tei_c.ns._1.RevisionDesc;
import org.tei_c.ns._1.SourceDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.TeiHeader;
import org.tei_c.ns._1.Term;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 12.05.2020.
 * @version 1.0
 */
public class TEIBeschreibungCommand implements TEIUpdatePURLsCommand {

  private static final Logger logger = LoggerFactory.getLogger(
      TEIBeschreibungCommand.class);

  private static final String DNB_URL = "http://d-nb.info/gnd/";

  protected static Optional<TEI> TEI_XML = Optional.empty();

  private TEIBeschreibungCommand() {
  }

  public static void updateBeschreibungsXML(Beschreibung beschreibung,
      List<EntstehungsortParameter> entstehungsorte) throws Exception {

    openTEIDokument(beschreibung);
    if (TEI_XML.isPresent()) {
      updatePublikationsdatumHSP(beschreibung);
      updateAenderungsdatum(beschreibung);
      updateBeschreibungsIDAndType(beschreibung);
      updateSettlementAndRepositoryIDs(beschreibung);
      if (!beschreibung.getPURLs().isEmpty()) {
        updatePURLs(beschreibung);
      }

      updateEntstehungsortID(beschreibung, entstehungsorte);
      updatePersonAsAutoren(beschreibung);
      updateGrundsprachenIDs(beschreibung);
      writeXML(beschreibung);
    }
  }

  public static void writeXML(Beschreibung beschreibung)
      throws JAXBException {
    if (TEI_XML.isPresent()) {
      beschreibung.setTeiXML(TEIObjectFactory.marshal(TEI_XML.get()));
    }
  }

  protected static void updateBeschreibungsIDAndType(
      Beschreibung beschreibung) {

    if (Objects.nonNull(beschreibung)
        && Objects.nonNull(beschreibung.getTeiXML())
        && Objects.nonNull(beschreibung.getId())) {

      logger.info("Update Beschreibungen ID {} ", beschreibung.getId());

      if (TEI_XML.isPresent()) {

        TEI t = TEI_XML.get();

        List<MsDesc> msDescList = TEIQuery.queryForMsDescAsBeschreibung(t);

        for (MsDesc msDesc : msDescList) {
          MsIdentifier msIdentifier = msDesc.getMsIdentifier();

          if (Objects.isNull(msDesc.getMsIdentifier())) {
            msIdentifier = new MsIdentifier();
          }

          msIdentifier
              .getMsNamesAndObjectNamesAndAltIdentifiers()
              .add(
                  AltIdentifierFactory.build(
                      new Identifikation.IdentifikationBuilder()
                          .withIdent(msDesc.getId())
                          .withIdentTyp(IdentTyp.HSP_ALTIDENTIFIER_MXML)
                          .build()));

          msDesc.setId(beschreibung.getId());

          if (msDesc.getType() == null || msDesc.getType().isEmpty()) {
            msDesc.setType(DokumentObjektTyp.HSP_DESCRIPTION.toString());
          }

          if (beschreibung.getVerwaltungsTyp() != null) {
            msDesc.setStatus(
                beschreibung.getVerwaltungsTyp().name().toLowerCase());
          }

          if (beschreibung.getBeschreibungsTyp() != null) {
            msDesc.setSubtype(beschreibung.getBeschreibungsTyp().toString());
          }
        }
      }
    }
  }

  protected static void updatePersonAsAutoren(Beschreibung beschreibung)
      throws Exception {

    if (TEI_XML.isPresent()) {

      TEI tei = TEI_XML.get();

      List<PersName> fullMappedPersNames = new ArrayList<>();

      TEICommon.findFirst(RespStmt.class, tei)
          .ifPresent(
              respStmt -> {
                respStmt.getRespsAndNamesAndOrgNames().stream()
                    .filter(TEIBeschreibungCommand::isPersNameAuthor)
                    .map(o -> (PersName) o)
                    .forEach(
                        persName ->
                            beschreibung.getAutoren().stream()
                                .filter(
                                    autor ->
                                        autor
                                            .getName()
                                            .contains(
                                                StringUtils.substringAfterLast(
                                                    TEICommon.getContentAsString(
                                                        persName.getContent()),
                                                    " ")))
                                .findFirst()
                                .ifPresentOrElse(
                                    autor ->
                                        fullMappedPersNames.add(
                                            createPersName(
                                                autor.getId(),
                                                List.of(autor.getName()),
                                                List.of(
                                                    NormdatenReferenz.DNB_BASE_URL
                                                        + autor.getGndID()))),
                                    () ->
                                        fullMappedPersNames.add(
                                            createPersName(
                                                persName.getKey(),
                                                persName.getContent(),
                                                List.of()))));
                respStmt
                    .getRespsAndNamesAndOrgNames()
                    .removeIf(TEIBeschreibungCommand::isPersNameAuthor);
                respStmt.getRespsAndNamesAndOrgNames()
                    .addAll(fullMappedPersNames);
              });
    }
  }

  private static boolean isPersNameAuthor(Object o) {
    return o instanceof PersName && ((PersName) o).isSetReves()
        && ((PersName) o).isSetRoles() && ((PersName) o).getRoles()
        .contains(ROLE_AUTHOR);
  }

  private static PersName createPersName(String key, List<Object> content,
      List<String> reves) {
    PersName p = new PersName();
    p.getRoles().add(ROLE_AUTHOR);
    p.setKey(key);
    p.getContent().clear();
    p.getContent().addAll(content);

    if (!reves.isEmpty()) {
      p.getReves().addAll(reves);
    }

    return p;
  }

  protected static void updateSettlementAndRepositoryIDs(
      Beschreibung beschreibung) {

    if (Objects.nonNull(beschreibung) && Objects.nonNull(
        beschreibung.getTeiXML())) {

      logger.info("Update settlement and repository for beschreibung {} ",
          beschreibung.getId());

      if (TEI_XML.isPresent()) {

        TEI t = TEI_XML.get();

        List<MsDesc> msDescList = TEIQuery.queryForMsDescAsBeschreibung(t);

        for (MsDesc m : msDescList) {
          MsIdentifier msIdentifier = m.getMsIdentifier();

          if (Objects.nonNull(msIdentifier)
              && beschreibung.isGueltigeIdentifikationValid()) {

            if (beschreibung.getGueltigeIdentifikation().isPresent()) {
              logger.debug(
                  "Trying to add settlement and repository ids {} , {}",
                  beschreibung.getGueltigeIdentifikation().get()
                      .getAufbewahrungsOrt().getName(),
                  beschreibung.getGueltigeIdentifikation().get().getBesitzer()
                      .getName());
            }

            msIdentifier
                .getSettlement()
                .setKey(
                    beschreibung.getGueltigeIdentifikation().get()
                        .getAufbewahrungsOrt().getId());

            if (msIdentifier.getSettlement().getReves().isEmpty()) {
              msIdentifier
                  .getSettlement()
                  .getReves()
                  .add(
                      DNB_URL
                          + beschreibung
                          .getGueltigeIdentifikation()
                          .get()
                          .getAufbewahrungsOrt()
                          .getGndID());
            }

            msIdentifier
                .getRepository()
                .setKey(
                    beschreibung.getGueltigeIdentifikation().get().getBesitzer()
                        .getId());

            if (msIdentifier.getRepository().getReves().isEmpty()) {
              msIdentifier
                  .getRepository()
                  .getReves()
                  .add(
                      DNB_URL
                          + beschreibung
                          .getGueltigeIdentifikation()
                          .get()
                          .getBesitzer()
                          .getGndID());
            }
          }
        }
      }
    }
  }

  public static void updateKatalogId(Beschreibung beschreibung,
      String katalogID) throws Exception {
    if (Objects.nonNull(beschreibung)
        && Objects.nonNull(beschreibung.getTeiXML())
        && Objects.nonNull(katalogID)) {

      List<TEI> teis =
          TEIObjectFactory.unmarshal(
              new ByteArrayInputStream(
                  beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8)));

      if (teis.size() == 1) {
        logger.info("Update katalogId {} for beschreibung {} ", katalogID,
            beschreibung.getId());

        TEI tei = teis.getFirst();

        PubPlace pubPlace;
        Optional<PubPlace> pubPlaceOptional = TEICommon.findFirst(
            PubPlace.class, tei);
        if (pubPlaceOptional.isPresent()) {
          pubPlace = pubPlaceOptional.get();
        } else {
          PublicationStmt publicationStmt =
              TEICommon.findFirst(PublicationStmt.class, tei)
                  .orElseThrow(
                      () -> new Exception("TEI contains no publicationStmt"));
          pubPlace = new PubPlace();
          publicationStmt.getPublishersAndDistributorsAndAuthorities()
              .add(pubPlace);
        }

        List<Ptr> ptrs = new ArrayList<>();
        TEICommon.findAll(Ptr.class, pubPlace, ptrs);

        Optional<Ptr> hspPtr =
            ptrs.stream().filter(ptr -> PTR_TYPE_HSP.equals(ptr.getType()))
                .findFirst();

        if (hspPtr.isPresent()) {
          hspPtr.get().getTargets().clear();
          hspPtr.get().getTargets().add(katalogID);
        } else {
          Ptr ptr = new Ptr();
          ptr.setType(PTR_TYPE_HSP);
          ptr.getTargets().add(katalogID);
          pubPlace.getContent().add(ptr);
        }

        beschreibung.setTeiXML(TEIObjectFactory.marshal(tei));
        beschreibung.setKatalogID(katalogID);
      }
    }
  }

  public static void updateVolltext(Beschreibung beschreibung, Div volltext)
      throws Exception {
    if (Objects.nonNull(beschreibung)
        && Objects.nonNull(beschreibung.getTeiXML())
        && Objects.nonNull(volltext)) {
      openTEIDokument(beschreibung);

      if (TEI_XML.isPresent()) {
        logger.info("Update volltext for beschreibung {} ",
            beschreibung.getId());

        TEI tei = TEI_XML.get();

        MsDesc msDesc =
            TEICommon.findFirst(MsDesc.class, tei)
                .orElseThrow(
                    () ->
                        new Exception(
                            "TEI for Beschreibung "
                                + beschreibung.getId()
                                + " contains no msDesc"));

        MsPart msPartOther = findAndClearMsPartOther(msDesc);

        volltext.getMeetingsAndBylinesAndDatelines().stream()
            .filter(P.class::isInstance)
            .map(P.class::cast)
            .forEachOrdered(p -> msPartOther.getPSAndAbs().add(p));

        beschreibung.setTeiXML(TEIObjectFactory.marshal(tei));
      }
    }
  }

  private static MsPart findAndClearMsPartOther(MsDesc msDesc) {
    MsPart msPartOther =
        msDesc.getMsContentsAndPhysDescsAndHistories().stream()
            .filter(o -> o instanceof MsPart)
            .map(o -> (MsPart) o)
            .filter(msPart -> MSPART_TYPE_OTHER.equals(msPart.getType()))
            .findFirst()
            .orElse(null);

    if (Objects.isNull(msPartOther)) {
      msPartOther = new MsPart();
      msPartOther.setType(MSPART_TYPE_OTHER);
      MsIdentifier msIdentifier = new MsIdentifier();
      msPartOther.setMsIdentifier(msIdentifier);
      Idno idno = new Idno();
      idno.getContent().add("Sonstiges");
      msIdentifier.getIdnos().add(idno);
      msDesc.getMsContentsAndPhysDescsAndHistories().add(msPartOther);
    } else {
      msPartOther
          .getPSAndAbs()
          .removeIf(
              element ->
                  Optional.ofNullable(element)
                      .filter(P.class::isInstance)
                      .map(P.class::cast)
                      .map(P::getN)
                      .filter(p -> p.startsWith("Page"))
                      .isPresent());
    }
    return msPartOther;
  }

  public static void updateKatalogReferences(
      URI rangeIdentifier, Beschreibung beschreibung, URI katalogManifestURI)
      throws Exception {

    checkKatalogRequirements(rangeIdentifier, beschreibung, katalogManifestURI);

    TEI teiXML =
        unmarshalOne(
            new ByteArrayInputStream(
                beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8)))
            .orElseThrow(
                () -> new IllegalArgumentException("No TEI XML Found"));

    Optional<SourceDesc> sourceDesc = TEICommon.findFirst(SourceDesc.class,
        teiXML);

    sourceDesc
        .flatMap(
            sourceD ->
                sourceD.getBiblsAndBiblStructsAndListBibls().stream()
                    .filter(Bibl.class::isInstance)
                    .map(Bibl.class::cast)
                    .findFirst())
        .ifPresent(
            bibl -> {
              updateBiblScope(rangeIdentifier, bibl);

              updateBibl(katalogManifestURI, bibl);
            });

    beschreibung.setTeiXML(TEIObjectFactory.marshal(teiXML));
  }

  static void checkKatalogRequirements(
      URI rangeIdentifier, Beschreibung beschreibung, URI katalogManifestURI) {
    if (Objects.isNull(beschreibung) || Objects.isNull(
        beschreibung.getTeiXML())) {
      throw new IllegalArgumentException(
          "Update Katalog failed because of missing needed parameter beschreibung-tei.");
    }

    if (Objects.isNull(rangeIdentifier)) {
      AtomicReference<String> ident = new AtomicReference<>(null);
      beschreibung
          .getGueltigeIdentifikation()
          .ifPresent(identifikation -> ident.set(identifikation.getIdent()));
      throw new IllegalArgumentException(
          String.format(
              "Update Katalog failed because of missing needed parameter rangeIdentifier for %s",
              ident.get()));
    }

    if (Objects.isNull(katalogManifestURI)) {
      throw new IllegalArgumentException(
          "Update Katalog failed because of missing needed parameter katalogManifestURI.");
    }
  }

  static void updateBibl(URI katalogManifestURI, Bibl bibl) {
    bibl.getContent().stream()
        .filter(content -> content instanceof Ptr && ((Ptr) content).getType()
            .equals(PTR_TYPE_HSP))
        .findFirst()
        .ifPresentOrElse(
            ptr -> {
              ((Ptr) ptr).getTargets().clear();
              ((Ptr) ptr).getTargets().add(katalogManifestURI.toASCIIString());
            },
            () -> {
              Ptr ptr = new Ptr();
              ptr.setType(PTR_TYPE_HSP);
              ptr.getTargets().add(katalogManifestURI.toASCIIString());
              bibl.getContent().add(ptr);
            });
  }

  static void updateBiblScope(URI rangeIdentifier, Bibl bibl) {
    bibl.getContent().stream()
        .filter(BiblScope.class::isInstance)
        .findFirst()
        .ifPresentOrElse(
            biblScope -> {
              ((BiblScope) biblScope).getFacs().clear();
              ((BiblScope) biblScope).getFacs()
                  .add(rangeIdentifier.toASCIIString());
            },
            () -> {
              BiblScope biblScope = new BiblScope();
              biblScope.getFacs().add(rangeIdentifier.toASCIIString());
              bibl.getContent().add(biblScope);
            });
  }

  public static void updateAenderungsdatum(Beschreibung beschreibung)
      throws Exception {
    if (Objects.nonNull(beschreibung)
        && Objects.nonNull(beschreibung.getTeiXML())
        && Objects.nonNull(beschreibung.getAenderungsDatum())) {

      List<TEI> teis =
          TEIObjectFactory.unmarshal(
              new ByteArrayInputStream(
                  beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8)));

      if (teis.size() == 1) {
        logger.info("Update aenderungsdatum for beschreibung {} ",
            beschreibung.getId());

        TeiHeader teiHeader =
            teis.stream()
                .findFirst()
                .map(TEI::getTeiHeader)
                .orElseThrow(() -> new Exception("TEI contains no header!"));

        if (Objects.isNull(teiHeader.getRevisionDesc())) {
          teiHeader.setRevisionDesc(new RevisionDesc());
        }
        RevisionDesc revisionDesc = teiHeader.getRevisionDesc();

        if (revisionDesc.getRevisionChangeAttribute().isEmpty()) {
          revisionDesc.getRevisionChangeAttribute().add(new Change());
        }
        Change change = revisionDesc.getRevisionChangeAttribute().getFirst();

        Date changeDate =
            change.getContent().stream()
                .filter(Date.class::isInstance)
                .map(Date.class::cast)
                .findFirst()
                .orElseGet(
                    () -> {
                      Date date = new Date();
                      change.getContent().add(date);
                      return date;
                    });

        updateDate(changeDate, beschreibung.getAenderungsDatum());
        logger.info("Update change date {}", TEICommon.getContentAsString(changeDate.getContent()));
        TEI_XML = Optional.of(teis.getFirst());
      }
    }
  }

  protected static void updatePublikationsdatumHSP(Beschreibung beschreibung)
      throws Exception {
    if (Objects.nonNull(beschreibung)
        && Objects.nonNull(beschreibung.getTeiXML())
        && Objects.nonNull(beschreibung.getPublikation())) {

      Publikation publikationHsp = beschreibung.getPublikation();

      if (publikationHsp.getPublikationsDatumHSP() == null) {
        logger.info("No publikationsDatumHSP to update for beschreibung {} ",
            beschreibung.getId());
        return;
      }

      if (TEI_XML.isPresent()) {
        TEI tei = TEI_XML.get();
        logger.info("Update publikationsDatumHSP for beschreibung {} ",
            beschreibung.getId());

        PublicationStmt publicationStmt = findPublicationStmt(tei,
            beschreibung.getId());

        Date publicationDate = findPublicationDate(publicationStmt);

        updateDate(publicationDate,
            publikationHsp.getPublikationsDatumHSP());
      }
    }
  }

  private static Date findPublicationDate(PublicationStmt publicationStmt) {
    return publicationStmt.getPublishersAndDistributorsAndAuthorities().stream()
        .filter(object -> Date.class.isAssignableFrom(object.getClass()))
        .map(Date.class::cast)
        .filter(date -> PUBLICATION_DATE_SECONDARY.equals(date.getType()))
        .findFirst()
        .orElseGet(
            () -> {
              Date date = new Date();
              date.setType(PUBLICATION_DATE_SECONDARY);
              publicationStmt.getPublishersAndDistributorsAndAuthorities()
                  .add(date);
              return date;
            });
  }

  private static PublicationStmt findPublicationStmt(TEI tei,
      String beschreibungId)
      throws Exception {
    return Stream.ofNullable(tei.getTeiHeader())
        .flatMap(teiHeader -> Stream.ofNullable(teiHeader.getFileDesc()))
        .flatMap(fileDesc -> Stream.ofNullable(fileDesc.getPublicationStmt()))
        .filter(
            publicationStmt ->
                publicationStmt.getPublishersAndDistributorsAndAuthorities()
                    .stream()
                    .anyMatch(Publisher.class::isInstance))
        .findFirst()
        .orElseThrow(
            () ->
                new Exception(
                    "TEI contains no valid PublicationStmt, beschreibungId="
                        + beschreibungId));
  }

  private static void updateDate(Date date, LocalDateTime dateTime) {
    date.setWhen(
        dateTime.format(DateTimeFormatter.ofPattern(LOCAL_DATE_FORMAT)));
    date.getContent().clear();
    date.getContent()
        .add(dateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
  }

  protected static void updateGrundsprachenIDs(Beschreibung beschreibung)
      throws Exception {

    if (TEI_XML.isPresent()) {

      List<Term> termList = new ArrayList<>();
      List<MsDesc> msDescList = new ArrayList<>();
      TEICommon.findAll(MsDesc.class, TEI_XML.get(), msDescList);
      TEICommon.findAll(Term.class, msDescList.getFirst().getHeads().getFirst(),
          termList);

      beschreibung.findKopf().ifPresent(kopf -> kopf.getGrundsprachen()
          .forEach(grundsprachen -> termList.stream().
              filter(
                  t ->
                      TEXT_LANG_ID.equals(t.getType())
                          && t.isSetReves()
                          && findGNDIDinListOfStrings(t.getReves()).equals(
                          grundsprachen.getGndID()))
              .findFirst()
              .ifPresent(term -> term.setKey(grundsprachen.getId()))));

    }
  }

  protected static void updateEntstehungsortID(
      Beschreibung beschreibung, List<EntstehungsortParameter> entstehungsorte)
      throws Exception {

    if (TEI_XML.isPresent()) {
      List<Term> termList = new ArrayList<>();
      List<MsDesc> msDescList = new ArrayList<>();

      TEICommon.findAll(MsDesc.class, TEI_XML.get(), msDescList);
      TEICommon.findAll(Term.class, msDescList.getFirst().getHeads().getFirst(),
          termList);

      entstehungsorte.forEach(entstehungsortParameter -> {
        termList.stream()
            .filter(
                t ->
                    ORIG_PLACE_NORM.equals(t.getType())
                        && t.isSetReves()
                        && findGNDIDinListOfStrings(t.getReves()).equals(
                        entstehungsortParameter.gndIdentifier()))
            .findFirst()
            .ifPresent(
                term -> {
                  term.setKey(entstehungsortParameter.id());
                  term.getContent().clear();
                  term.getContent()
                      .add(entstehungsortParameter.preferredName());
                });
      });
    }
  }

  public static void openTEIDokument(Beschreibung beschreibung)
      throws IOException, JAXBException {
    if (Objects.nonNull(beschreibung) && Objects.nonNull(
        beschreibung.getTeiXML())) {
      try (ByteArrayInputStream inputStream =
          new ByteArrayInputStream(
              beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8))) {
        TEI_XML = unmarshalOne(inputStream);
      }
    }
  }

  public static void updatePURLs(Beschreibung beschreibung)
      throws Exception {

    if (TEI_XML.isPresent()) {
      TEI tei = TEI_XML.get();
      TEIUpdatePURLsCommand.updatePURLs(tei,
          beschreibung.getPURLs());
    }
  }

}
