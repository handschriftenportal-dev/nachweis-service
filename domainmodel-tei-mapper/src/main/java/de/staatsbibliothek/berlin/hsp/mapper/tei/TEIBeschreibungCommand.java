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

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIQuery.ORIG_PLACE_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.DATE_TIME_PATTERN;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.LOCAL_DATE_FORMAT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.MSPART_TYPE_OTHER;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.PTR_TYPE_HSP;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.PUBLICATION_DATE_SECONDARY;
import static de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory.unmarshalOne;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.AltIdentifierFactory;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import javax.xml.bind.JAXBException;
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
import org.tei_c.ns._1.P;
import org.tei_c.ns._1.Ptr;
import org.tei_c.ns._1.PubPlace;
import org.tei_c.ns._1.PublicationStmt;
import org.tei_c.ns._1.Publisher;
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

  private static final Logger logger = LoggerFactory.getLogger(TEIBeschreibungCommand.class);

  private TEIBeschreibungCommand() {
  }

  public static void updateBeschreibungsIDAndType(Beschreibung beschreibung) throws JAXBException {

    if (Objects.nonNull(beschreibung) && Objects.nonNull(beschreibung.getTeiXML()) && Objects
        .nonNull(beschreibung.getId())) {

      logger.info("Update Beschreibungen ID {} ", beschreibung.getId());

      List<TEI> tei = TEIObjectFactory
          .unmarshal(
              new ByteArrayInputStream(beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8)));

      if (tei.size() == 1) {

        TEI t = tei.get(0);

        List<MsDesc> msDescList = TEIQuery.queryForMsDescAsBeschreibung(t);

        for (MsDesc msDesc : msDescList) {
          MsIdentifier msIdentifier = msDesc.getMsIdentifier();

          if (Objects.isNull(msDesc.getMsIdentifier())) {
            msIdentifier = new MsIdentifier();
          }

          msIdentifier.getMsNamesAndObjectNamesAndAltIdentifiers()
              .add(AltIdentifierFactory.build(msDesc.getId(), TEIValues.HSP_ALTIDENTIFIER_MXML));

          msIdentifier.getMsNamesAndObjectNamesAndAltIdentifiers().add(AltIdentifierFactory.build(
              beschreibung.getKodID(), TEIValues.HSP_ALTIDENTIFIER_HSPID,
              TEIValues.KOD_ID_COLLECTION));

          msDesc.setId(beschreibung.getId());

          if (msDesc.getType() == null || msDesc.getType().isEmpty()) {
            msDesc.setType(DokumentObjektTyp.HSP_DESCRIPTION.toString());
          }

          if (beschreibung.getVerwaltungsTyp() != null) {
            msDesc.setStatus(beschreibung.getVerwaltungsTyp().name().toLowerCase());
          }

          if (beschreibung.getBeschreibungsTyp() != null) {
            msDesc.setSubtype(beschreibung.getBeschreibungsTyp().toString());
          }

          beschreibung.setTeiXML(TEIObjectFactory.marshal(t));
        }
      }
    }
  }


  public static void updateSettlementAndRepositoryIDs(Beschreibung beschreibung,
      KulturObjektDokument kod) throws JAXBException {

    if (Objects.nonNull(beschreibung) && Objects.nonNull(beschreibung.getTeiXML())) {

      logger.info("Update settlement and repository for beschreibung {} ", beschreibung.getId());

      List<TEI> tei = TEIObjectFactory
          .unmarshal(
              new ByteArrayInputStream(beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8)));

      if (tei.size() == 1) {

        TEI t = tei.get(0);

        List<MsDesc> msDescList = TEIQuery.queryForMsDescAsBeschreibung(t);

        for (MsDesc m : msDescList) {
          MsIdentifier msIdentifier = m.getMsIdentifier();

          Identifikation KODIdentifikation = kod.getGueltigeIdentifikation();

          if (Objects.nonNull(msIdentifier) && KODIdentifikation != null && Objects
              .nonNull(KODIdentifikation.getBesitzer()) && Objects
              .nonNull(KODIdentifikation.getAufbewahrungsOrt())) {

            logger.debug("Trying to add settlement and repository ids {} , {}",
                KODIdentifikation.getAufbewahrungsOrt().getName(), KODIdentifikation.getBesitzer()
                    .getName());

            msIdentifier.getSettlement().setKey(KODIdentifikation.getAufbewahrungsOrt().getId());

            beschreibung.getGueltigeIdentifikation()
                .ifPresent(i -> i.setAufbewahrungsOrt(KODIdentifikation.getAufbewahrungsOrt()));

            msIdentifier.getRepository().setKey(KODIdentifikation.getBesitzer().getId());

            beschreibung.getGueltigeIdentifikation()
                .ifPresent(i -> i.setBesitzer(KODIdentifikation.getBesitzer()));

            beschreibung.setTeiXML(TEIObjectFactory.marshal(t));
          }
        }
      }
    }
  }

  public static void updateKatalogId(Beschreibung beschreibung, String katalogID) throws Exception {
    if (Objects.nonNull(beschreibung)
        && Objects.nonNull(beschreibung.getTeiXML())
        && Objects.nonNull(katalogID)) {

      List<TEI> teis = TEIObjectFactory
          .unmarshal(
              new ByteArrayInputStream(beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8)));

      if (teis.size() == 1) {
        logger.info("Update katalogId {} for beschreibung {} ", katalogID, beschreibung.getId());

        TEI tei = teis.get(0);

        PubPlace pubPlace;
        Optional<PubPlace> pubPlaceOptional = TEICommon.findFirst(PubPlace.class, tei);
        if (pubPlaceOptional.isPresent()) {
          pubPlace = pubPlaceOptional.get();
        } else {
          PublicationStmt publicationStmt = TEICommon.findFirst(PublicationStmt.class, tei)
              .orElseThrow(() -> new Exception("TEI contains no publicationStmt"));
          pubPlace = new PubPlace();
          publicationStmt.getPublishersAndDistributorsAndAuthorities().add(pubPlace);
        }

        List<Ptr> ptrs = new ArrayList<>();
        TEICommon.findAll(Ptr.class, pubPlace, ptrs);

        Optional<Ptr> hspPtr = ptrs.stream()
            .filter(ptr -> PTR_TYPE_HSP.equals(ptr.getType()))
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

  public static void updateVolltext(Beschreibung beschreibung, Div volltext) throws Exception {
    if (Objects.nonNull(beschreibung)
        && Objects.nonNull(beschreibung.getTeiXML())
        && Objects.nonNull(volltext)) {

      Optional<TEI> optionalTEI = openTEIDokument(beschreibung);

      if (optionalTEI.isPresent()) {
        logger.info("Update volltext for beschreibung {} ", beschreibung.getId());

        TEI tei = optionalTEI.get();

        MsDesc msDesc = TEICommon.findFirst(MsDesc.class, tei)
            .orElseThrow(() -> new Exception(
                "TEI for Beschreibung " + beschreibung.getId() + " contains no msDesc"));

        MsPart msPartOther = findAndClearMsPartOther(msDesc);

        volltext.getMeetingsAndBylinesAndDatelines()
            .stream()
            .filter(P.class::isInstance)
            .map(P.class::cast)
            .forEachOrdered(p -> msPartOther.getPSAndAbs().add(p));

        beschreibung.setTeiXML(TEIObjectFactory.marshal(tei));
      }
    }
  }

  private static MsPart findAndClearMsPartOther(MsDesc msDesc) {
    MsPart msPartOther = msDesc.getMsParts()
        .stream()
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
      msDesc.getMsParts().add(msPartOther);
    } else {
      msPartOther.getPSAndAbs().removeIf(element ->
          Optional.ofNullable(element)
              .filter(P.class::isInstance)
              .map(P.class::cast)
              .map(P::getN)
              .filter(p -> p.startsWith("Page"))
              .isPresent());
    }
    return msPartOther;
  }

  public static void updateKatalogReferences(URI rangeIdentifier,
      Beschreibung beschreibung, URI katalogManifestURI) throws Exception {

    checkKatalogRequirements(rangeIdentifier, beschreibung, katalogManifestURI);

    TEI teiXML = unmarshalOne(
            new ByteArrayInputStream(
                beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8)))
        .orElseThrow(()-> new IllegalArgumentException("No TEI XML Found"));

    Optional<SourceDesc> sourceDesc = TEICommon.findFirst(SourceDesc.class,
        teiXML);

    sourceDesc.flatMap(sourceD -> sourceD.getBiblsAndBiblStructsAndListBibls().stream()
        .filter(Bibl.class::isInstance)
        .map(Bibl.class::cast)
        .findFirst()).ifPresent(bibl -> {
      updateBiblScope(rangeIdentifier, bibl);

      updateBibl(katalogManifestURI, bibl);
    });

    beschreibung.setTeiXML(TEIObjectFactory.marshal(teiXML));
  }

  static void checkKatalogRequirements(URI rangeIdentifier, Beschreibung beschreibung, URI katalogManifestURI) {
    if (Objects.isNull(beschreibung) || Objects.isNull(beschreibung.getTeiXML())) {
      throw new IllegalArgumentException(
          "Update Katalog failed because of missing needed parameter beschreibung-tei.");
    }

    if (Objects.isNull(rangeIdentifier)) {
      AtomicReference<String> ident = new AtomicReference<>(".");
      beschreibung.getGueltigeIdentifikation().ifPresent(identifikation -> ident.set(identifikation.getIdent()));
      throw new IllegalArgumentException(String.format(
          "Update Katalog failed because of missing needed parameter rangeIdentifier for %s", ident.get()));
    }

    if (Objects.isNull(katalogManifestURI)) {
      throw new IllegalArgumentException(
          "Update Katalog failed because of missing needed parameter katalogManifestURI.");
    }
  }

  static void updateBibl(URI katalogManifestURI, Bibl bibl) {
    bibl.getContent().stream().filter(
            content -> content instanceof Ptr && ((Ptr) content).getType()
                .equals(PTR_TYPE_HSP))
        .findFirst().ifPresentOrElse(ptr -> {
          ((Ptr) ptr).getTargets().clear();
          ((Ptr) ptr).getTargets().add(katalogManifestURI.toASCIIString());
            }, () -> {
              Ptr ptr = new Ptr();
              ptr.setType(PTR_TYPE_HSP);
              ptr.getTargets().add(katalogManifestURI.toASCIIString());
              bibl.getContent().add(ptr);
            }

        );
  }

  static void updateBiblScope(URI rangeIdentifier, Bibl bibl) {
    bibl.getContent().stream()
        .filter(BiblScope.class::isInstance)
        .findFirst().ifPresentOrElse(
            biblScope -> {
              ((BiblScope) biblScope).getFacs().clear();
              ((BiblScope) biblScope).getFacs().add(rangeIdentifier.toASCIIString());
            },
            () -> {
              BiblScope biblScope = new BiblScope();
              biblScope.getFacs().add(rangeIdentifier.toASCIIString());
              bibl.getContent().add(biblScope);
            });
  }

  public static void updateAenderungsdatum(Beschreibung beschreibung) throws Exception {
    if (Objects.nonNull(beschreibung)
        && Objects.nonNull(beschreibung.getTeiXML())
        && Objects.nonNull(beschreibung.getAenderungsDatum())) {

      List<TEI> teis = TEIObjectFactory
          .unmarshal(
              new ByteArrayInputStream(beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8)));

      if (teis.size() == 1) {
        logger.info("Update aenderungsdatum for beschreibung {} ", beschreibung.getId());

        TeiHeader teiHeader = teis.stream()
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
        Change change = revisionDesc.getRevisionChangeAttribute().get(0);

        Date changeDate = change.getContent().stream()
            .filter(Date.class::isInstance)
            .map(Date.class::cast)
            .findFirst()
            .orElseGet(() -> {
              Date date = new Date();
              change.getContent().add(date);
              return date;
            });

        updateDate(changeDate, beschreibung.getAenderungsDatum());
        beschreibung.setTeiXML(TEIObjectFactory.marshal(teis.get(0)));
      }
    }
  }

  public static void updatePublikationsdatumHSP(Beschreibung beschreibung) throws Exception {
    if (Objects.nonNull(beschreibung)
        && Objects.nonNull(beschreibung.getTeiXML())
        && Objects.nonNull(beschreibung.getPublikationen())) {

      Optional<Publikation> publikationHsp = beschreibung.getPublikationen()
          .stream()
          .filter(p -> PublikationsTyp.PUBLIKATION_HSP == p.getPublikationsTyp())
          .findFirst();

      if (publikationHsp.isEmpty()) {
        logger.info("No publikationsDatumHSP to update for beschreibung {} ", beschreibung.getId());
        return;
      }

      Optional<TEI> optionalTEI = openTEIDokument(beschreibung);

      if (optionalTEI.isPresent()) {
        TEI tei = optionalTEI.get();
        logger.info("Update publikationsDatumHSP for beschreibung {} ", beschreibung.getId());

        PublicationStmt publicationStmt = findPublicationStmt(tei, beschreibung.getId());

        Date publicationDate = findPublicationDate(publicationStmt);

        updateDate(publicationDate, publikationHsp.get().getDatumDerVeroeffentlichung());
        beschreibung.setTeiXML(TEIObjectFactory.marshal(tei));
      }
    }
  }

  private static Date findPublicationDate(PublicationStmt publicationStmt) {
    return publicationStmt.getPublishersAndDistributorsAndAuthorities().stream()
        .filter(object -> Date.class.isAssignableFrom(object.getClass()))
        .map(Date.class::cast)
        .filter(date -> PUBLICATION_DATE_SECONDARY.equals(date.getType()))
        .findFirst()
        .orElseGet(() -> {
          Date date = new Date();
          date.setType(PUBLICATION_DATE_SECONDARY);
          publicationStmt.getPublishersAndDistributorsAndAuthorities().add(date);
          return date;
        });
  }

  private static PublicationStmt findPublicationStmt(TEI tei, String beschreibungId) throws Exception {
    return Stream.ofNullable(tei.getTeiHeader())
        .flatMap(teiHeader -> Stream.ofNullable(teiHeader.getFileDesc()))
        .flatMap(fileDesc -> Stream.ofNullable(fileDesc.getPublicationStmt()))
        .filter(publicationStmt -> publicationStmt.getPublishersAndDistributorsAndAuthorities().stream()
            .anyMatch(Publisher.class::isInstance))
        .findFirst()
        .orElseThrow(() -> new Exception("TEI contains no valid PublicationStmt, beschreibungId=" + beschreibungId));
  }

  private static void updateDate(Date date, LocalDateTime dateTime) {
    date.setWhen(dateTime.format(DateTimeFormatter.ofPattern(LOCAL_DATE_FORMAT)));
    date.getContent().clear();
    date.getContent().add(dateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
  }

  public static void updateEntstehungsortID(Beschreibung beschreibung, String placeID,
      String preferredName)
      throws Exception {

    List<Term> termList = new ArrayList<>();
    List<MsDesc> msDescList = new ArrayList<>();
    List<TEI> teis = TEIObjectFactory
        .unmarshal(
            new ByteArrayInputStream(beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8)));

    TEICommon.findAll(MsDesc.class, teis.get(0), msDescList);
    TEICommon.findAll(Term.class, msDescList.get(0).getHeads().get(0), termList);
    termList.stream().filter(t -> ORIG_PLACE_NORM.equals(t.getType())).findFirst()
        .ifPresent(term -> {
          term.setKey(placeID);
          term.getContent().clear();
          term.getContent().add(preferredName);
        });

    beschreibung.setTeiXML(TEIObjectFactory.marshal(teis.get(0)));

  }

  static Optional<TEI> openTEIDokument(Beschreibung beschreibung) throws IOException, JAXBException {
    if (Objects.nonNull(beschreibung) && Objects.nonNull(beschreibung.getTeiXML())) {
      try (ByteArrayInputStream inputStream = new ByteArrayInputStream(
          beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8))) {
        return unmarshalOne(inputStream);
      }
    }
    return Optional.empty();
  }

  public static void updatePURLs(Beschreibung beschreibung) throws Exception {
    Optional<TEI> teiOptional = openTEIDokument(beschreibung);

    if (teiOptional.isPresent()) {
      TEI tei = teiOptional.get();
      TEIUpdatePURLsCommand.updatePURLs(teiOptional.get(), beschreibung.getPURLs());
      beschreibung.setTeiXML(TEIObjectFactory.marshal(tei));
    }
  }

}
