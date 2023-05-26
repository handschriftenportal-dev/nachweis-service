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
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.BiblFactory;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.SurrogatesFactory;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.Additional;
import org.tei_c.ns._1.ListBibl;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.SourceDesc;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.10.2020.
 * @version 1.0
 */
public class TEIKulturObjektDokumentCommand implements TEIUpdatePURLsCommand {

  private static final Logger logger = LoggerFactory
      .getLogger(TEIKulturObjektDokumentCommand.class);

  private TEIKulturObjektDokumentCommand() {
  }

  public static void updateSurrogatesDigitalisate(KulturObjektDokument kod) throws Exception {

    if (Objects.nonNull(kod.getTeiXML()) && !kod.getTeiXML().isEmpty()
        && kod.getDigitalisate() != null) {

      List<TEI> tei = TEIObjectFactory
          .unmarshal(new ByteArrayInputStream(kod.getTeiXML().getBytes(StandardCharsets.UTF_8)));

      if (tei.size() == 1) {
        List<Additional> additionals = new ArrayList<>();
        TEI teikod = tei.get(0);

        TEICommon.findAll(Additional.class, teikod, additionals);
        additionals.stream().findFirst().ifPresent(additional -> additional
            .setSurrogates(SurrogatesFactory.build(new ArrayList<>(kod.getDigitalisate()))));

        updateKODTEI(kod, teikod);

      }
    }
  }

  protected static void updateKODTEI(KulturObjektDokument kod, TEI teikod)
      throws JAXBException {
    String xml = TEIObjectFactory.marshal(teikod);
    kod.setTeiXML(xml);
  }

  public static void updateKODSourceDescWithAddBeschreibungMsDesc(KulturObjektDokument kod,
      Beschreibung beschreibung)
      throws Exception {

    if (Objects.nonNull(kod.getTeiXML()) && beschreibung.getTeiXML() != null && !beschreibung
        .getTeiXML().isEmpty()) {

      try (ByteArrayInputStream kodInputStream = new ByteArrayInputStream(
          kod.getTeiXML().getBytes(StandardCharsets.UTF_8));
          ByteArrayInputStream beschreibungInputStream = new ByteArrayInputStream(
              beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8))) {

        List<TEI> kodTEIList = TEIObjectFactory
            .unmarshal(kodInputStream);

        List<TEI> beschreibungTEIList = TEIObjectFactory
            .unmarshal(beschreibungInputStream);

        logger
            .info("Update KOD sourceDesc Add Beschreibungen msDesc KOD ID {} Beschreibungs ID {} ",
                kod.getId(),
                beschreibung.getId());

        if (kodTEIList.size() == 1 && !(beschreibungTEIList.isEmpty())) {
          List<MsDesc> msDescList = new ArrayList<>();
          List<SourceDesc> listSourceDesc = new ArrayList<>();
          TEI kodTei = kodTEIList.get(0);
          TEI firstBeschreibungTei = beschreibungTEIList.get(0);

          TEICommon.findAll(SourceDesc.class, kodTei, listSourceDesc);

          if (!msDescWithTypeDescriptionIsAllreadyIncluded(listSourceDesc)) {
            TEICommon.findAll(MsDesc.class, firstBeschreibungTei, msDescList);
            listSourceDesc.stream().findFirst().ifPresent(sourceDesc ->
                sourceDesc.getBiblsAndBiblStructsAndListBibls().add(msDescList.get(0)));
          } else {
            if (kod.getBeschreibungenIDs().isEmpty()) {
              listSourceDesc.stream().findFirst().ifPresent(sourceDesc -> {

                Iterator<Object> iterator = sourceDesc.getBiblsAndBiblStructsAndListBibls()
                    .iterator();
                while (iterator.hasNext()) {
                  Object sourceObject = iterator.next();
                  if (sourceObject instanceof MsDesc && (
                      DokumentObjektTyp.HSP_DESCRIPTION.toString()
                          .equals(((MsDesc) sourceObject).getType())
                          || DokumentObjektTyp.HSP_DESCRIPTION_RETRO.toString()
                          .equals(((MsDesc) sourceObject).getType()))) {
                    iterator.remove();
                  }
                }
              });
            }
          }
          updateKODTEI(kod, kodTei);
        }
      }
    }
  }

  public static void updateKODSourceDescWithRemoveBeschreibungMsDesc(KulturObjektDokument kod, String beschreibungId)
      throws Exception {
    if (Objects.nonNull(kod.getTeiXML()) && beschreibungId != null && !beschreibungId.isEmpty()) {
      try (ByteArrayInputStream kodInputStream = new ByteArrayInputStream(
          kod.getTeiXML().getBytes(StandardCharsets.UTF_8))) {

        List<TEI> kodTEIList = TEIObjectFactory.unmarshal(kodInputStream);
        logger.info("Update KOD sourceDesc Remove Beschreibung msDesc KOD ID {} Beschreibungs ID {} ",
            kod.getId(), beschreibungId);

        if (kodTEIList.size() == 1) {
          final AtomicReference<Boolean> kodChanged = new AtomicReference<>(Boolean.FALSE);
          TEI kodTei = kodTEIList.get(0);

          List<SourceDesc> listSourceDesc = new ArrayList<>();
          TEICommon.findAll(SourceDesc.class, kodTei, listSourceDesc);

          listSourceDesc.stream().findFirst().ifPresent(sourceDesc -> {
            Iterator<Object> iterator = sourceDesc.getBiblsAndBiblStructsAndListBibls().iterator();
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
            logger.info("msDesc for Beschreibungs ID {} has been removed from KOD ID {}.",
                kod.getId(), beschreibungId);
            updateKODTEI(kod, kodTei);
          } else {
            logger.info("no changes made to TEI for KOD ID {} and beschreibung {}.",
                kod.getId(), beschreibungId);
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
        .anyMatch(msDesc -> (DokumentObjektTyp.HSP_DESCRIPTION.toString().equals(msDesc.getType())
            || DokumentObjektTyp.HSP_DESCRIPTION_RETRO.toString().equals(msDesc.getType())));
  }

  private static boolean msDescWithTypeDescriptionIsAllreadyIncluded(
      List<SourceDesc> listSourceDesc) {
    return listSourceDesc
        .stream()
        .findFirst()
        .map(TEIKulturObjektDokumentCommand::checkSourceDescContainsMsDescWithTypeDescription)
        .orElse(false);
  }

  public static void updateKODBeschreibungenReferenzes(KulturObjektDokument kod) throws Exception {

    if (Objects.nonNull(kod.getTeiXML())) {

      try (ByteArrayInputStream inputStream = new ByteArrayInputStream(
          kod.getTeiXML().getBytes(StandardCharsets.UTF_8))) {

        List<TEI> tei = TEIObjectFactory
            .unmarshal(inputStream);

        logger.info("Update KOD List Bibl Add Beschreibungen ID {} ", kod.getId());

        if (tei.size() == 1) {
          List<Additional> additionalList = new ArrayList<>();
          TEI teikod = tei.get(0);

          TEICommon.findAll(Additional.class, teikod, additionalList);

          if(additionalList.isEmpty()) {
            throw new IllegalArgumentException("Missing Additional in TEI XML");
          }

          if (kod.getBeschreibungenIDs().isEmpty()) {
            additionalList.stream().findFirst().ifPresent(a -> a.setListBibl(null));
          } else {
            additionalList.stream().findFirst().ifPresent(a -> {

              if (a.getListBibl() == null) {
                a.setListBibl(new ListBibl());
              }

              a.getListBibl().getBiblsAndBiblStructsAndListBibls().clear();

              kod.getBeschreibungenIDs()
                  .forEach(b -> a.getListBibl().getBiblsAndBiblStructsAndListBibls().add(BiblFactory.build(b)));
            });
          }

          updateKODTEI(kod, teikod);
        }
      }
    }
  }

  static Optional<TEI> openTEIDokument(KulturObjektDokument kod) throws IOException, JAXBException {
    return Objects.nonNull(kod) ? openTEIDokument(kod.getTeiXML()) : Optional.empty();
  }

  static Optional<TEI> openTEIDokument(String teiXML) throws IOException, JAXBException {
    if (Objects.nonNull(teiXML)) {
      try (ByteArrayInputStream inputStream = new ByteArrayInputStream(teiXML.getBytes(StandardCharsets.UTF_8))) {
        return TEIObjectFactory.unmarshalOne(inputStream);
      }
    }
    return Optional.empty();
  }

  public static void updatePURLs(KulturObjektDokument kod) throws Exception {
    Optional<TEI> teiOptional = openTEIDokument(kod);

    if (teiOptional.isPresent()) {
      TEI tei = teiOptional.get();
      TEIUpdatePURLsCommand.updatePURLs(teiOptional.get(), kod.getPURLs());
      updateKODTEI(kod, tei);
    }
  }


}
