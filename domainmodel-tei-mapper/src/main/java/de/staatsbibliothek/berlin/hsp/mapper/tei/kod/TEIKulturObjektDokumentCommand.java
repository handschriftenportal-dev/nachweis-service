package de.staatsbibliothek.berlin.hsp.mapper.tei.kod;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEICommandException;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIUpdatePURLsCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.AltIdentifierFactory;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.MsIdentifierFactory;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.SurrogatesFactory;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import jakarta.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.Additional;
import org.tei_c.ns._1.AltIdentifier;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.MsIdentifier;
import org.tei_c.ns._1.SourceDesc;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.10.2020.
 * @version 1.0
 */
public class TEIKulturObjektDokumentCommand implements TEIUpdatePURLsCommand {

  private static final Logger logger =
      LoggerFactory.getLogger(TEIKulturObjektDokumentCommand.class);

  private TEIKulturObjektDokumentCommand() {
  }

  public static void updateSurrogatesDigitalisate(KulturObjektDokument kod) throws Exception {

    if (Objects.nonNull(kod.getTeiXML())
        && !kod.getTeiXML().isEmpty()
        && kod.getDigitalisate() != null) {

      List<TEI> tei =
          TEIObjectFactory.unmarshal(
              new ByteArrayInputStream(kod.getTeiXML().getBytes(StandardCharsets.UTF_8)));

      if (tei.size() == 1) {
        List<Additional> additionals = new ArrayList<>();
        TEI teikod = tei.getFirst();

        TEICommon.findAll(Additional.class, teikod, additionals);
        additionals.stream()
            .findFirst()
            .ifPresent(
                additional ->
                    additional.setSurrogates(
                        SurrogatesFactory.build(new ArrayList<>(kod.getDigitalisate()))));

        updateKODTEI(kod, teikod);
      }
    }
  }

  public static void updateIndentInMsIdentifier(KulturObjektDokument kod) throws Exception {
    Optional<TEI> tei = openValidatedTEIDokument(kod);
    Optional<MsIdentifier> msIdentifier = TEICommon.findFirst(MsIdentifier.class, tei.orElse(null));

    if (msIdentifier.isPresent() && msIdentifier.get().getIdnos().size() == 1) {

      adjustIdentifier(kod, msIdentifier.get());

      updateKODTEI(kod, tei.orElse(null));
    }
  }

  private static void adjustIdentifier(KulturObjektDokument kod, MsIdentifier msIdentifier) {
    msIdentifier.getIdnos().getFirst().getContent().clear();
    msIdentifier.getIdnos().getFirst().getContent().add(kod.getGueltigeSignatur());
    msIdentifier.getIdnos().getFirst().setSortKey(kod.getSortKey());
    msIdentifier
        .getMsNamesAndObjectNamesAndAltIdentifiers()
        .removeIf(o -> o instanceof AltIdentifier);

    kod.getAlternativeIdentifikationen()
        .forEach(
            alternativeIdentifikation -> {
              AltIdentifier altIdentifier = AltIdentifierFactory.build(alternativeIdentifikation);
              msIdentifier.getMsNamesAndObjectNamesAndAltIdentifiers().add(altIdentifier);
            });
  }

  protected static void updateKODTEI(KulturObjektDokument kod, TEI teikod) throws JAXBException {
    if (kod != null && teikod != null) {
      String xml = TEIObjectFactory.marshal(teikod);
      kod.setTeiXML(xml);
    }
  }

  public static void updateKODSourceDescWithAddBeschreibungMsDesc(
      KulturObjektDokument kod, Beschreibung beschreibung) throws Exception {

    if (Objects.nonNull(kod.getTeiXML())
        && beschreibung.getTeiXML() != null
        && !beschreibung.getTeiXML().isEmpty()) {

      try (ByteArrayInputStream kodInputStream =
          new ByteArrayInputStream(kod.getTeiXML().getBytes(StandardCharsets.UTF_8));
          ByteArrayInputStream beschreibungInputStream =
              new ByteArrayInputStream(beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8))) {

        List<TEI> kodTEIList = TEIObjectFactory.unmarshal(kodInputStream);

        List<TEI> beschreibungTEIList = TEIObjectFactory.unmarshal(beschreibungInputStream);

        logger.info(
            "Update KOD sourceDesc Add Beschreibungen msDesc KOD ID {} Beschreibungs ID {} ",
            kod.getId(),
            beschreibung.getId());

        if (kodTEIList.size() == 1 && !(beschreibungTEIList.isEmpty())) {
          List<MsDesc> msDescList = new ArrayList<>();
          List<SourceDesc> listSourceDesc = new ArrayList<>();
          TEI kodTei = kodTEIList.getFirst();
          TEI firstBeschreibungTei = beschreibungTEIList.getFirst();

          TEICommon.findAll(SourceDesc.class, kodTei, listSourceDesc);

          if (!msDescWithTypeDescriptionIsAllreadyIncluded(listSourceDesc)) {
            TEICommon.findAll(MsDesc.class, firstBeschreibungTei, msDescList);
            listSourceDesc.stream()
                .findFirst()
                .ifPresent(
                    sourceDesc ->
                        sourceDesc.getBiblsAndBiblStructsAndListBibls().add(msDescList.getFirst()));
          } else {
            if (kod.getBeschreibungenIDs().isEmpty()) {
              listSourceDesc.stream()
                  .findFirst()
                  .ifPresent(
                      sourceDesc ->
                          sourceDesc
                              .getBiblsAndBiblStructsAndListBibls()
                              .removeIf(
                                  sourceObject ->
                                      sourceObject instanceof MsDesc
                                          && (DokumentObjektTyp.HSP_DESCRIPTION
                                          .toString()
                                          .equals(((MsDesc) sourceObject).getType())
                                          || DokumentObjektTyp.HSP_DESCRIPTION_RETRO
                                          .toString()
                                          .equals(((MsDesc) sourceObject).getType()))));
            }
          }
          updateKODTEI(kod, kodTei);
        }
      }
    }
  }

  public static void updateKODSourceDescWithRemoveBeschreibungMsDesc(
      KulturObjektDokument kod, String beschreibungId) throws Exception {
    if (Objects.nonNull(kod.getTeiXML()) && beschreibungId != null && !beschreibungId.isEmpty()) {
      try (ByteArrayInputStream kodInputStream =
          new ByteArrayInputStream(kod.getTeiXML().getBytes(StandardCharsets.UTF_8))) {

        List<TEI> kodTEIList = TEIObjectFactory.unmarshal(kodInputStream);
        logger.info(
            "Update KOD sourceDesc Remove Beschreibung msDesc KOD ID {} Beschreibungs ID {} ",
            kod.getId(),
            beschreibungId);

        if (kodTEIList.size() == 1) {
          final AtomicReference<Boolean> kodChanged = new AtomicReference<>(Boolean.FALSE);
          TEI kodTei = kodTEIList.getFirst();

          List<SourceDesc> listSourceDesc = new ArrayList<>();
          TEICommon.findAll(SourceDesc.class, kodTei, listSourceDesc);

          listSourceDesc.stream()
              .findFirst()
              .ifPresent(
                  sourceDesc -> {
                    Iterator<Object> iterator =
                        sourceDesc.getBiblsAndBiblStructsAndListBibls().iterator();
                    while (iterator.hasNext()) {
                      Object sourceObject = iterator.next();
                      if (sourceObject instanceof MsDesc
                          && beschreibungId.equals(((MsDesc) sourceObject).getId())) {
                        iterator.remove();
                        kodChanged.set(Boolean.TRUE);
                        break;
                      }
                    }
                  });
          if (kodChanged.get()) {
            logger.info(
                "msDesc for Beschreibungs ID {} has been removed from KOD ID {}.",
                kod.getId(),
                beschreibungId);
            updateKODTEI(kod, kodTei);
          } else {
            logger.info(
                "no changes made to TEI for KOD ID {} and beschreibung {}.",
                kod.getId(),
                beschreibungId);
          }
        }
      }
    }
  }

  private static boolean checkSourceDescContainsMsDescWithTypeDescription(SourceDesc sourceDesc) {
    List<Object> biblsAndBiblStructsAndListBibls = sourceDesc.getBiblsAndBiblStructsAndListBibls();

    return biblsAndBiblStructsAndListBibls.stream()
        .filter(teiXMLObject -> teiXMLObject instanceof MsDesc)
        .map(teiXMLObject -> (MsDesc) teiXMLObject)
        .anyMatch(
            msDesc ->
                (DokumentObjektTyp.HSP_DESCRIPTION.toString().equals(msDesc.getType())
                    || DokumentObjektTyp.HSP_DESCRIPTION_RETRO
                    .toString()
                    .equals(msDesc.getType())));
  }

  private static boolean msDescWithTypeDescriptionIsAllreadyIncluded(
      List<SourceDesc> listSourceDesc) {
    return listSourceDesc.stream()
        .findFirst()
        .map(TEIKulturObjektDokumentCommand::checkSourceDescContainsMsDescWithTypeDescription)
        .orElse(false);
  }

  public static void updateKODBeschreibungenReferenzes(
      KulturObjektDokument kod) throws Exception {

    if (Objects.isNull(kod.getTeiXML())) {
      return;
    }

    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(
        kod.getTeiXML().getBytes(StandardCharsets.UTF_8))) {
      List<TEI> tei = TEIObjectFactory.unmarshal(inputStream);

      logger.info("Updating description references in TEI for KOD {}", kod.getId());

      if (tei.size() != 1) {
        return;
      }

      TEI teiKod = tei.getFirst();
      KODListBiblMapper.writeDescriptions(teiKod, kod.getBeschreibungenIDs());
      updateKODTEI(kod, teiKod);
    }
  }

  static Optional<TEI> openTEIDokument(KulturObjektDokument kod) throws IOException, JAXBException {
    return Objects.nonNull(kod) ? openTEIDokument(kod.getTeiXML()) : Optional.empty();
  }

  static Optional<TEI> openTEIDokument(String teiXML) throws IOException, JAXBException {
    if (Objects.nonNull(teiXML)) {
      try (ByteArrayInputStream inputStream =
          new ByteArrayInputStream(teiXML.getBytes(StandardCharsets.UTF_8))) {
        return TEIObjectFactory.unmarshalOne(inputStream);
      }
    }
    return Optional.empty();
  }

  private static Optional<TEI> openValidatedTEIDokument(
      KulturObjektDokument kod) throws Exception {
    if (Objects.isNull(kod) || Objects.isNull(kod.getTeiXML()) || kod.getTeiXML().isEmpty()) {

      throw new TEICommandException(
          "Missing needed parameter for XML manipulation");
    }
    return openTEIDokument(kod);
  }

  public static void updatePURLs(KulturObjektDokument kod) throws Exception {
    Optional<TEI> teiOptional = openTEIDokument(kod);

    if (teiOptional.isPresent()) {
      TEI tei = teiOptional.get();
      TEIUpdatePURLsCommand.updatePURLs(teiOptional.get(), kod.getPURLs());
      updateKODTEI(kod, tei);
    }
  }

  public static void updateMsIdentifierAndAllAltidentifier(
      KulturObjektDokument kulturObjektDokument) throws Exception {
    Optional<TEI> tei = openValidatedTEIDokument(kulturObjektDokument);
    Optional<MsIdentifier> msIdentifierAlt =
        TEICommon.findFirst(MsIdentifier.class, tei.orElse(null));

    if (msIdentifierAlt.isPresent() && tei.isPresent()) {
      MsIdentifier msIdentifierNeu =
          MsIdentifierFactory.build(
              kulturObjektDokument.getGueltigeIdentifikation(),
              kulturObjektDokument.getAlternativeIdentifikationen());

      msIdentifierAlt.get().getIdnos().clear();
      msIdentifierAlt.get().getIdnos().addAll(msIdentifierNeu.getIdnos());
      msIdentifierAlt.get().setRepository(msIdentifierNeu.getRepository());
      msIdentifierAlt.get().setSettlement(msIdentifierNeu.getSettlement());

      msIdentifierAlt.get().getMsNamesAndObjectNamesAndAltIdentifiers().clear();
      msIdentifierNeu.getMsNamesAndObjectNamesAndAltIdentifiers().stream()
          .filter(content -> content instanceof AltIdentifier)
          .map(content -> (AltIdentifier) content)
          .forEach(alt -> alt.setType(TEIValues.ALTERNATIVE_TYPE_FORMER));

      msIdentifierAlt
          .get()
          .getMsNamesAndObjectNamesAndAltIdentifiers()
          .addAll(msIdentifierNeu.getMsNamesAndObjectNamesAndAltIdentifiers());

      updateKODTEI(kulturObjektDokument, tei.get());
    }
  }
}
