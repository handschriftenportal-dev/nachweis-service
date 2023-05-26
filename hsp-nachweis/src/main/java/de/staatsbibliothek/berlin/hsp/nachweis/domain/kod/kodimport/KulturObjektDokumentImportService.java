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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodimport;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.neueImporte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren.KulturObjektDokumentRegistry;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.DataEntity;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportFile;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgang;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgangBoundary;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.tei_c.ns._1.AltIdentifier;
import org.tei_c.ns._1.MsIdentifier;
import org.tei_c.ns._1.TEI;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 08.07.21
 */

@ApplicationScoped
@Slf4j
public class KulturObjektDokumentImportService {

  private final KulturObjektDokumentBoundary kulturObjektDokumentBoundary;

  private final NormdatenReferenzBoundary normdatenReferenzBoundary;

  private final ImportVorgangBoundary importVorgangBoundary;

  private final BearbeiterBoundary bearbeiterBoundary;

  private final String nachweisserverbaseurl;

  @Inject
  public KulturObjektDokumentImportService(
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      NormdatenReferenzBoundary normdatenReferenzBoundary,
      ImportVorgangBoundary importVorgangBoundary,
      BearbeiterBoundary bearbeiterBoundary,
      @ConfigProperty(name = "nachweisserverbaseurl") String nachweisserverbaseurl) {
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
    this.importVorgangBoundary = importVorgangBoundary;
    this.nachweisserverbaseurl = nachweisserverbaseurl;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {Exception.class})
  @TransactionConfiguration(timeout = 7200)
  public void onImportMessage(@Observes @neueImporte KulturObjektDokumentImport importe) {

    log.info("Neuer KulturObjektDokument Import erhalten {} ", importe.getMessageDTO());
    try {

      ImportVorgang importVorgang = importVorgangBoundary.getImportVorgang(importe.getMessageDTO());

      log.info("Starting ImportVorgang {} for KulturObjektDokument Import", importVorgang.getId());

      List<KulturObjektDokument> kods = new ArrayList<>();

      for (MsIdentifier msIdentifier : findAllMsIdentifiers(
          extractTEIFromMessage(importe.getMessageDTO()))) {
        NormdatenReferenz ort = Optional.ofNullable(msIdentifier.getSettlement())
            .flatMap(settlement -> Optional.ofNullable(settlement.getKey()))
            .flatMap(key -> normdatenReferenzBoundary.findOneByIdOrNameAndType(key, NormdatenReferenz.ORT_TYPE_NAME))
            .orElseThrow(() -> new Exception("Keine NormdatenReferenz Ort gefunden!"));

        NormdatenReferenz koerperschaft = Optional.ofNullable(msIdentifier.getRepository())
            .flatMap(repository -> Optional.ofNullable(repository.getKey()))
            .flatMap(key -> normdatenReferenzBoundary.findOneByIdOrNameAndType(key,
                NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
            .orElseThrow(() -> new Exception("Keine NormdatenReferenz Koerperschaft gefunden!"));

        List<String> signaturen = new ArrayList<>();

        signaturen.add(msIdentifier.getIdnos().stream()
            .findFirst()
            .map(idno -> TEICommon.getContentAsString(idno.getContent()))
            .orElseThrow(() -> new Exception("Keine GueltigeSignatur gefunden!")));

        msIdentifier.getMsNamesAndObjectNamesAndAltIdentifiers().stream()
            .filter(o -> o instanceof AltIdentifier)
            .map(ai -> ((AltIdentifier) ai).getIdno())
            .filter(Objects::nonNull)
            .forEach(aiIdno -> signaturen.add(TEICommon.getContentAsString(aiIdno.getContent())));

        kods.add(KulturObjektDokumentRegistry.create(ort, koerperschaft, signaturen));
      }

      log.info("ImportVorgang {} for KulturObjektDokument Import: registrieren started...", importVorgang.getId());

      List<KulturObjektDokument> registrierteKODs = kulturObjektDokumentBoundary.registrieren(
          bearbeiterBoundary.findBearbeiterWithName(importe.getMessageDTO().getActor().getName()),
          kods);

      log.info("ImportVorgang {} for KulturObjektDokument Import: registrieren finished, send successMessageToImport.",
          importVorgang.getId());

      sendSuccessMessageToImport(importe.getMessageDTO(), registrierteKODs, importVorgang);

      log.info("Finished ImportVorgang {} for KulturObjektDokument Import", importVorgang.getId());

    } catch (Exception error) {
      log.error("Error during KulturObjektDokument importieren!", error);
      sendFailureMessageToImport(importe.getMessageDTO(), error.getMessage());
    }
  }

  @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {Exception.class})
  void sendFailureMessageToImport(ActivityStreamMessageDTO message, String errorMessage) {
    try {
      ImportVorgang importVorgang = importVorgangBoundary.getImportVorgang(message);
      log.info("Sending Error Message for failed Import {} ",importVorgang);
      importVorgang.setFehler(errorMessage);
      importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.FAILED);
      importVorgangBoundary.sendImportJobToKafka(importVorgang);
    } catch (Exception e) {
      log.error("Error during Send Import Message!", e);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  void sendSuccessMessageToImport(ActivityStreamMessageDTO message,
      List<KulturObjektDokument> registrierteKODs,
      ImportVorgang importVorgang) throws ActivityStreamsException, JsonProcessingException {

    registrierteKODs.forEach(kod ->
        message.getObjects().stream()
            .filter(o -> ActivityStreamsDokumentTyp.KOD.equals(o.getType()))
            .findFirst()
            .ifPresent(o -> {
              ImportFile importFile = findImportFileForActivityStreamObject(importVorgang, o);
              importFile.getDataEntityList()
                  .add(new DataEntity(kod.getId(),
                      kod.getGueltigeIdentifikation().getIdent(),
                      nachweisserverbaseurl + "kulturObjektDokument/kulturObjektDokument.xhtml?id="
                          + kod.getId()));
            }));

    log.info("Sending Success to Kafka for importVorgang {}.", importVorgang.getId());

    importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.SUCCESS);
    importVorgangBoundary.sendImportJobToKafka(importVorgang);

  }

  ImportFile findImportFileForActivityStreamObject(ImportVorgang importVorgang,
      ActivityStreamObjectDTO activityStreamObject) {

    ImportFile importFile;
    Objects.requireNonNull(activityStreamObject.getName(), "ActivityStreamObject without name");
    importFile = importVorgang.getImportFiles().stream()
        .filter(searchForImportFile -> activityStreamObject.getName()
            .equals(searchForImportFile.getDateiName()))
        .findFirst()
        .orElseThrow(() -> new NullPointerException(
            "No importFile found for " + activityStreamObject.getName()));
    return importFile;
  }

  List<TEI> extractTEIFromMessage(ActivityStreamMessageDTO activityStreamMessage)
      throws ActivityStreamsException, JAXBException {

    List<TEI> teiList = new ArrayList<>();

    if (activityStreamMessage != null && activityStreamMessage.getObjects() != null) {
      for (ActivityStreamObjectDTO o : activityStreamMessage.getObjects()) {
        if (ActivityStreamsDokumentTyp.KOD.equals(o.getType())) {
          teiList.addAll(TEIObjectFactory.unmarshal(new ByteArrayInputStream(o.getContent())));
        }
      }
    }

    return teiList;
  }

  List<MsIdentifier> findAllMsIdentifiers(List<TEI> teiList) throws Exception {
    List<MsIdentifier> identifiers = new ArrayList<>();

    for (TEI tei : teiList) {
      TEICommon.findAll(MsIdentifier.class, tei, identifiers);
    }

    return identifiers;
  }

}
