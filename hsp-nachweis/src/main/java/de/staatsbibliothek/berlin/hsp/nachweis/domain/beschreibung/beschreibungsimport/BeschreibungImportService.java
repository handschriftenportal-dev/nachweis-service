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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport;

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsKomponentenTyp.KOPF;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary.SYSTEM_USERNAME;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForImportJob;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEI2BeschreibungMapper;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIBeschreibungCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIKODAttributsReferenzCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIKulturObjektDokumentCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIQuery;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamObjectTagId;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.neueImporte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.sendIndexMessage;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.konfiguration.KonfigurationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectTagDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.transformation.TeiXmlTransformationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.DataEntity;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportFile;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgang;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgangBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenUpdateService;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.NonUniqueResultException;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.tei_c.ns._1.TEI;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */
@Slf4j
@ApplicationScoped
public class BeschreibungImportService implements ImportBoundary, Serializable {

  private static final long serialVersionUID = 43621061554989457L;

  private static final String CONFIG_AUTO_UERBERNAHME = "automatische.uebernahme.aktiv";

  ImportRepository importRepository;

  private transient KafkaIndexingProducer kafkaIndexingProducer;

  private transient BeschreibungsRepository beschreibungsRepository;

  private transient KulturObjektDokumentRepository kulturObjektDokumentRepository;

  private transient SuchDokumentService suchDokumentService;

  private transient Event<BeschreibungImport> sendIndexMessageEvent;

  private transient NormdatenReferenzBoundary normdatenReferenzBoundary;

  private String addBeschreibungMsDescInKodSourceDesc;

  private transient ImportVorgangBoundary importVorgangBoundary;

  private transient TeiXmlValidationBoundary teiXmlValidationBoundary;

  private transient TeiXmlTransformationBoundary teiXmlTransformationBoundary;

  private transient ResourceBundle resourceBundle = ResourceBundle
      .getBundle(I18NController.APPLICATION_MESSAGES, I18NController.getLocale());

  private String praesentationBaseUrl;

  private transient NormdatenUpdateService normdatenUpdateService;

  private transient PURLBoundary purlBoundary;
  private boolean purlAutogenerateEnabled;

  private transient KonfigurationBoundary konfigurationBoundary;

  public BeschreibungImportService() {
  }

  @Override
  public List<Beschreibung> convertTEI2Beschreibung(ActivityStreamObjectDTO streamObject)
      throws Exception {
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = new ArrayList<>();

    if (streamObject.getContent() != null) {
      List<TEI> content = TEIObjectFactory
          .unmarshal(new ByteArrayInputStream(streamObject.getContent()));
      for (TEI tei : content) {
        List<Beschreibung> mappedBeschreibungen = beschreibungMapper.map(tei);
        for (Beschreibung beschreibung : mappedBeschreibungen) {
          enhanceNormdatenReferenzSprache(beschreibung);
        }
        beschreibungen.addAll(mappedBeschreibungen);
      }
    }
    return beschreibungen;
  }

  @Override
  public List<BeschreibungImport> alleImporteAnzeigen() {
    return importRepository.listAll();
  }

  @Override
  public Optional<BeschreibungImport> findById(String id) {
    return importRepository.findByIdOptional(id);
  }

  @Override
  public boolean isAutomatischenUebernahmeAktiv() {
    return konfigurationBoundary.getWert(CONFIG_AUTO_UERBERNAHME)
        .map("true"::equals)
        .orElse(Boolean.FALSE);
  }

  @Override
  public void setAutomatischenUebernahmeAktiv(boolean automatischenUebernahmeAktiv) {
    log.info("setAutomatischenUebernahmeAktiv: automatischenUebernahmeAktiv={}", automatischenUebernahmeAktiv);
    konfigurationBoundary.setWert(CONFIG_AUTO_UERBERNAHME, String.valueOf(automatischenUebernahmeAktiv));
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void onImportMessage(@Observes @neueImporte BeschreibungImport beschreibungImportJob) {
    log.info("onImportMessage: received new beschreibungImportJob {}",
        beschreibungImportJob.getId());

    if (!isAutomatischenUebernahmeAktiv()) {
      importRepository.save(beschreibungImportJob);
    } else {
      log.info("onImportMessage: start automatische Uebernahme for beschreibungImport {}",
          beschreibungImportJob.getId());

      ImportVorgang importVorgang = null;

      try {
        ActivityStreamMessageDTO activityStream = beschreibungImportJob.getMessageDTO();
        importVorgang = importVorgangBoundary.getImportVorgang(activityStream);

        VerwaltungsTyp verwaltungstyp = findInternExternTag(activityStream.getObjects())
            .flatMap(t -> VerwaltungsTyp.fromString(t.getName()))
            .orElseThrow(
                () -> new Exception("Automatische Übernahme ohne VerwaltungsTyp nicht möglich!"));

        for (ActivityStreamObjectDTO activityStreamObject : activityStream.getObjects()) {
          beschreibungUebernehmen(beschreibungImportJob, importVorgang, activityStreamObject,
              verwaltungstyp);
        }
        importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.SUCCESS);
        setImportVorgang(beschreibungImportJob.getMessageDTO(), importVorgang);
        importErfolgreich(beschreibungImportJob);
        importRepository.save(beschreibungImportJob);
        importVorgangBoundary.sendImportJobToKafka(importVorgang);
      } catch (Exception ex) {
        log.error("Unable to process import {}", ex.getMessage(), ex);

        processError(beschreibungImportJob, importVorgang, ex);
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  @TransactionConfiguration(timeout = 7200)
  @Override
  public void importeUebernehmen(String beschreibungImportId, String verwaltungsTyp)
      throws BeschreibungImportException {

    VerwaltungsTyp verwaltungstyp;

    try {
      if (Objects.isNull(beschreibungImportId) || beschreibungImportId.isEmpty()) {
        throw new IllegalArgumentException("Wrong beschreibungImportId");
      }

      if (Objects.isNull(verwaltungsTyp) || verwaltungsTyp.isEmpty()) {
        throw new IllegalArgumentException("Wrong Verwaltungstyp");
      }

      verwaltungstyp = VerwaltungsTyp.valueOf(verwaltungsTyp.toUpperCase());
    } catch (IllegalArgumentException error) {
      throw new BeschreibungImportException(
          resourceBundle.getString("datenuebernahme_couldnotstart"), error);
    }

    log.info("Importe übernehmen for job {}", beschreibungImportId);

    Optional<BeschreibungImport> optBeschreibungImportJob = importRepository.findByIdOptional(
        beschreibungImportId);

    if (optBeschreibungImportJob.isPresent()) {
      BeschreibungImport beschreibungImportJob = optBeschreibungImportJob.get();

      ImportVorgang importVorgang = null;

      try {
        ActivityStreamMessageDTO activityStream = beschreibungImportJob.getMessageDTO();
        importVorgang = importVorgangBoundary.getImportVorgang(activityStream);

        for (ActivityStreamObjectDTO activityStreamObject : activityStream.getObjects()) {
          beschreibungUebernehmen(beschreibungImportJob, importVorgang, activityStreamObject,
              verwaltungstyp);
        }
        importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.SUCCESS);
        setImportVorgang(beschreibungImportJob.getMessageDTO(), importVorgang);
        importErfolgreich(beschreibungImportJob);
        importVorgangBoundary.sendImportJobToKafka(importVorgang);
      } catch (Exception ex) {
        log.error("Unable to process import {}", ex.getMessage(), ex);

        processError(beschreibungImportJob, importVorgang, ex);

        throw new BeschreibungImportException("Beschreibungsimport fehlgeschlagen!", ex);
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  void beschreibungUebernehmen(BeschreibungImport beschreibungImportJob,
      ImportVorgang importVorgang,
      ActivityStreamObjectDTO activityStreamObject, VerwaltungsTyp verwaltungstyp)
      throws Exception {

    if (ActivityStreamsDokumentTyp.BESCHREIBUNG.equals(activityStreamObject.getType())) {

      List<Beschreibung> mappedBeschreibungen = convertTEI2Beschreibung(activityStreamObject);

      ImportFile importFile = findImportFileForActivityStreamObject(importVorgang,
          activityStreamObject);
      if (importFile != null) {

        for (Beschreibung beschreibung : mappedBeschreibungen) {

          Optional<Beschreibung> oldBeschreibungState = checkIfBeschreibungAlreadyExists(
              beschreibung);

          handleBeschreibungAlreadyExist(verwaltungstyp, beschreibung,
              oldBeschreibungState);

          Optional<KulturObjektDokument> optionalKulturObjektDokument = ermittleKOD(
              beschreibung);

          if (optionalKulturObjektDokument.isPresent()) {

            KulturObjektDokument kulturObjektDokument = optionalKulturObjektDokument.get();

            beschreibung.setKodID(kulturObjektDokument.getId());

            if (VerwaltungsTyp.INTERN.equals(verwaltungstyp)) {
              beschreibung.makeBeschreibungIntern();
            } else {
              beschreibung.makeBeschreibungExtern();
              beschreibung.getPublikationen()
                  .removeIf(publikation -> PublikationsTyp.PUBLIKATION_HSP
                      == publikation.getPublikationsTyp());
              beschreibung.getPublikationen()
                  .add(new Publikation(UUID.randomUUID().toString(), LocalDateTime.now(),
                      PublikationsTyp.PUBLIKATION_HSP));
            }

            if (Objects.isNull(beschreibung.getAenderungsDatum())) {
              beschreibung.setAenderungsDatum(beschreibung.getErstellungsDatum());
            }

            kulturObjektDokument.getBeschreibungenIDs().add(beschreibung.getId());

            createAndAddInternalPURL(beschreibung);

            updateTEIDocuments(kulturObjektDokument, beschreibung);

            saveKodAndBeschreibungenIntoSystem(beschreibungImportJob, beschreibung,
                kulturObjektDokument);

            if (VerwaltungsTyp.EXTERN.equals(verwaltungstyp)) {
              sendKafkaMessageToIndex(beschreibungImportJob, kulturObjektDokument);
            }

            updateImportJsonNodeForSuccess(beschreibung, importFile);
          }
        }
      }
    }
  }

  void handleBeschreibungAlreadyExist(VerwaltungsTyp verwaltungstyp, Beschreibung beschreibung,
      Optional<Beschreibung> oldBeschreibungState) throws BeschreibungAlreadyExistException {
    if (oldBeschreibungState.isPresent()) {
      if (VerwaltungsTyp.INTERN.equals(verwaltungstyp)) {
        throw new BeschreibungAlreadyExistException(
            resourceBundle.getString("datenuebernahme_descriptionalreadyexists")
                + oldBeschreibungState
                .get().getId());
      }

      if (VerwaltungsTyp.EXTERN.equals(verwaltungstyp)) {
        beschreibung.getAltIdentifier().add(beschreibung.getId());
        beschreibung.setId(oldBeschreibungState.get().getId());
        if (!oldBeschreibungState.get().getPURLs().isEmpty()) {
          oldBeschreibungState.get().getPURLs().stream()
              .filter(purl -> PURLTyp.INTERNAL == purl.getTyp())
              .forEach(purl -> beschreibung.getPURLs().add(purl));
          oldBeschreibungState.get().getPURLs()
              .removeIf(purl -> PURLTyp.INTERNAL == purl.getTyp());
        }
        beschreibungsRepository.deleteById(oldBeschreibungState.get().getId());
      }
    } else {
      beschreibung.getAltIdentifier().add(beschreibung.getId());
      beschreibung.setId(TEIValues.UUID_PREFIX + UUID
          .nameUUIDFromBytes(beschreibung.toString(false).getBytes(StandardCharsets.UTF_8)));
    }
  }

  void updateTEIDocuments(KulturObjektDokument kod, Beschreibung beschreibung)
      throws Exception {

    TEIBeschreibungCommand.updatePublikationsdatumHSP(beschreibung);
    TEIBeschreibungCommand.updateAenderungsdatum(beschreibung);
    TEIBeschreibungCommand.updateBeschreibungsIDAndType(beschreibung);
    TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(kod);
    TEIBeschreibungCommand.updateSettlementAndRepositoryIDs(beschreibung, kod);
    if (!beschreibung.getPURLs().isEmpty()) {
      TEIBeschreibungCommand.updatePURLs(beschreibung);
    }
    Optional<String> gndid = TEIQuery.queryForTermOriginGNDID(beschreibung);

    if (gndid.isPresent()) {
      Optional<GNDEntityFact> gndEntityFact = normdatenUpdateService.createPlaceIfNotExist(
          gndid.get());

      log.debug("Update Beschreibung with GNDEntity {0} " + gndEntityFact.isPresent());

      if (gndEntityFact.isPresent()) {
        log.debug("Update Beschreibung with GNDEntity {0} " + gndEntityFact.get());
        TEIBeschreibungCommand.updateEntstehungsortID(beschreibung,
            gndEntityFact.get().getId(), gndEntityFact.get().getPreferredName());
      }
    }

    teiXmlTransformationBoundary.transformTei2Hsp(beschreibung);

    if ("true".equals(addBeschreibungMsDescInKodSourceDesc)) {
      TEIKulturObjektDokumentCommand
          .updateKODSourceDescWithAddBeschreibungMsDesc(kod, beschreibung);

      TEIKODAttributsReferenzCommand.updateAttributsReferenzen(kod, beschreibung);

      teiXmlTransformationBoundary.transformTei2Hsp(kod);
    }
  }

  Optional<ActivityStreamObjectTagDTO> findInternExternTag(
      List<ActivityStreamObjectDTO> activityStreamObjects) {
    Optional<ActivityStreamObjectDTO> importStreamObjectDTO = activityStreamObjects.stream()
        .filter(o -> ActivityStreamsDokumentTyp.IMPORT.equals(o.getType()))
        .findFirst();

    return importStreamObjectDTO.flatMap(
        activityStreamObjectDTO -> activityStreamObjectDTO.getTag().stream()
            .filter(tag -> ActivityStreamObjectTagId.INTERN_EXTERN.name().equals(tag.getId()))
            .findFirst());
  }

  @Transactional(rollbackOn = {Exception.class}, value = TxType.REQUIRES_NEW)
  void processError(BeschreibungImport beschreibungImportJob, ImportVorgang importVorgang,
      Exception ex) {
    try {
      updateImportJsonNodeForFailure(importVorgang, ex.getMessage());
      setImportVorgang(beschreibungImportJob.getMessageDTO(), importVorgang);
      importFehlgeschlagen(beschreibungImportJob, ex);
      importVorgangBoundary.sendImportJobToKafka(importVorgang);
      importRepository.save(beschreibungImportJob);
    } catch (Exception e) {
      log.error("Unable to send error to importjob {}", e.getMessage(), e);
    }
  }

  private ImportFile findImportFileForActivityStreamObject(ImportVorgang importVorgang,
      ActivityStreamObjectDTO activityStreamObject) {

    ImportFile importFile;
    Objects.requireNonNull(activityStreamObject.getName(), "ActivityStreamObject without name");
    importFile = importVorgang.getImportFiles().stream()
        .filter(searchForImportFile -> activityStreamObject.getName()
            .equals(searchForImportFile.getDateiName()))
        .findFirst().orElse(null);
    return importFile;
  }


  private void setImportVorgang(ActivityStreamMessageDTO activityStream,
      ImportVorgang importVorgang)
      throws ActivityStreamsException, JsonProcessingException {
    ActivityStream importJobMessage = createMessageForImportJob(importVorgang);
    ActivityStreamObject newActivityStreamObject = importJobMessage.getObjects().get(0);
    List<ActivityStreamObjectDTO> objects = activityStream.getObjects();
    for (int i = 0, size = objects.size(); i < size; i++) {
      ActivityStreamObjectDTO activityStreamObject = objects.get(i);
      if (ActivityStreamsDokumentTyp.IMPORT.equals(activityStreamObject.getType())) {
        objects.remove(i);
        objects.add(new ActivityStreamObjectDTO(newActivityStreamObject));
        log.info("replaced old Object {} with new one {}", activityStreamObject,
            newActivityStreamObject);
        return;
      }
    }
    log.warn("The Activity Object with type IMPORT not found in the {}", activityStream);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void saveKodAndBeschreibungenIntoSystem(BeschreibungImport beschreibungImportJob,
      Beschreibung beschreibung,
      KulturObjektDokument kod) throws Exception {

    if (Objects.isNull(beschreibungImportJob) || Objects.isNull(beschreibung) || Objects
        .isNull(kod)) {
      throw new IllegalArgumentException("Missing argument to save KOD and Beschreibung");
    }

    try {
      teiXmlValidationBoundary.validateTeiXml(kod);
      teiXmlValidationBoundary.validateTeiXml(beschreibung);
      kulturObjektDokumentRepository.save(kod);
      suchDokumentService.kodUebernehmen(kod);
      beschreibungsRepository.save(beschreibung);
      suchDokumentService.beschreibungUebernehmen(beschreibung);
    } catch (Exception ex) {
      log.error("Unable to store KOD {}", kod);
      log.error("or Beschreibung {}", beschreibung);
      throw ex;
    }
  }

  void updateImportJsonNodeForFailure(ImportVorgang importVorgang, String errorMessage) {
    importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.FAILED);
    importVorgang.setFehler(errorMessage);
  }

  void updateImportJsonNodeForSuccess(Beschreibung beschreibung, ImportFile importFile) {
    AtomicReference<String> signatur = new AtomicReference<>("Signatur");
    beschreibung.getBeschreibungsStruktur().stream().filter(k -> k.getTyp().equals(KOPF))
        .findFirst().flatMap(
            kopf -> kopf.getIdentifikationen().stream()
                .filter(i -> i.getIdentTyp().equals(IdentTyp.GUELTIGE_SIGNATUR))
                .findFirst()
        ).ifPresent(i -> signatur.set(i.getIdent()));

    List<DataEntity> dataEntityList = importFile.getDataEntityList();
    DataEntity dataEntity = new DataEntity(beschreibung.getId(), signatur.get(),
        praesentationBaseUrl + "search?hspobjectid=" + beschreibung.getKodID() + "?q=%2A");
    if (dataEntityList.contains(dataEntity)) {
      int pos = dataEntityList.indexOf(dataEntity);
      DataEntity dataEntityInList = dataEntityList.get(pos);
      dataEntity.setUrl(dataEntityInList.getUrl());
      dataEntityList.remove(pos);
    }
    dataEntityList.add(dataEntity);
  }

  @Override
  public Optional<Beschreibung> checkIfBeschreibungAlreadyExists(Beschreibung beschreibung) {

    Optional<Beschreibung> alreadyExist = beschreibungsRepository
        .findByIdOptional(beschreibung.getId());

    if (alreadyExist.isPresent()) {
      return alreadyExist;
    }

    return beschreibungsRepository.findByAltIdentifierOptional(beschreibung.getId());
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void importErfolgreich(final BeschreibungImport beschreibungImportJob) {
    if (Objects.nonNull(beschreibungImportJob)) {
      beschreibungImportJob.setStatus(ImportStatus.UEBERNOMMEN);
      beschreibungImportJob.setSelectedForImport(false);
      importRepository.save(beschreibungImportJob);
      sendIndexMessageEvent.fire(beschreibungImportJob);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void importFehlgeschlagen(final BeschreibungImport beschreibungImportJob,
      Exception exception) {
    if (Objects.nonNull(beschreibungImportJob)) {
      beschreibungImportJob.setStatus(ImportStatus.FEHLGESCHLAGEN);
      beschreibungImportJob.setFehlerBeschreibung(exception.getMessage());
      beschreibungImportJob.setSelectedForImport(false);
      importRepository.save(beschreibungImportJob);
      sendIndexMessageEvent.fire(beschreibungImportJob);
    }
  }

  protected void sendKafkaMessageToIndex(BeschreibungImport beschreibungImportJob,
      KulturObjektDokument kulturObjektDokument)
      throws ActivityStreamsException {

    ActivityStream message;

    if (Objects.nonNull(kulturObjektDokument) && Objects.nonNull(beschreibungImportJob)
        && Objects.nonNull(kulturObjektDokument.getTeiXML()) && !kulturObjektDokument.getTeiXML()
        .isEmpty()) {

        message = kafkaIndexingProducer.sendKulturobjektDokumentAsActivityStreamMessage(kulturObjektDokument,ActivityStreamAction.UPDATE,true,SYSTEM_USERNAME);

        log.debug("Sending Message {}", message);

    } else {
      throw new MissingFormatArgumentException("Missing Elements for sending Kafka Message");
    }
  }


  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void importeAblehnen(List<String> importe) throws BeschreibungImportException {

    if (Objects.nonNull(importe) && !importe.isEmpty()) {

      Collection<BeschreibungImport> beschreibungImportJobs = importRepository.listAll().stream()
          .filter(j -> importe.contains(j.getId())).collect(
              Collectors.toList());

      for (BeschreibungImport j : beschreibungImportJobs) {
        try {
          j.setStatus(ImportStatus.ABGELEHNT);
          j.setSelectedForImport(false);
          importRepository.save(j);

          ImportVorgang importVorgang = importVorgangBoundary.getImportVorgang(j.getMessageDTO());
          if (importVorgang != null) {
            importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.FAILED);
            importVorgang.setFehler("Import abgelehnt");
            importVorgangBoundary.sendImportJobToKafka(importVorgang);
          }
        } catch (Exception e) {
          log.error("Problem updating importJob {}", e.getMessage(), e);
          throw new BeschreibungImportException(
              "Beschreibungsimport fehlgeschlagen!" + e.getMessage(), e);
        }
      }
    }

  }

  @Override
  public void selectForImport(List<String> ids) {

    importRepository.listAll().forEach(j -> {
      j.setSelectedForImport(ids.contains(j.getId()));
      importRepository.save(j);
    });

  }

  @Override
  public Optional<KulturObjektDokument> ermittleKOD(Beschreibung beschreibung)
      throws KulturObjektDokumentNotFoundException {

    beschreibung = beschreibungsRepository.findByIdOptional(beschreibung.getId())
        .orElse(beschreibung);

    Optional<KulturObjektDokument> optionalKulturObjektDokument = Optional.empty();

    if (beschreibung.getKodID() != null && !beschreibung.getKodID().isEmpty()) {
      optionalKulturObjektDokument = kulturObjektDokumentRepository
          .findByIdOptional(beschreibung.getKodID());
      if (optionalKulturObjektDokument.isPresent()) {
        return optionalKulturObjektDokument;
      }
    }

    if (beschreibung.getGueltigeIdentifikation().isPresent()) {
      Identifikation beschreibungsIdentifikation = beschreibung.getGueltigeIdentifikation().get();

      optionalKulturObjektDokument = findByIdentifikation(beschreibungsIdentifikation);

      log.info("Found By Gültige Signatur KOD {} for Beschreibung {}, {}, {}", optionalKulturObjektDokument.isPresent(),
          beschreibungsIdentifikation.getIdent(), beschreibungsIdentifikation.getBesitzer().getName(),
          beschreibungsIdentifikation.getAufbewahrungsOrt().getName());

      if (optionalKulturObjektDokument.isEmpty()) {
        optionalKulturObjektDokument = findByAlternativeIdentifikation(beschreibungsIdentifikation);

        log.info("Found By Alternative Signatur KOD {} for Beschreibung {}, {}, {}",
            optionalKulturObjektDokument.isPresent(),
            beschreibungsIdentifikation.getIdent(), beschreibungsIdentifikation.getBesitzer().getName(),
            beschreibungsIdentifikation.getAufbewahrungsOrt().getName());
      }
    }

    if (optionalKulturObjektDokument.isPresent()) {
      return optionalKulturObjektDokument;
    }

    throw new KulturObjektDokumentNotFoundException(
        "Kulturobjekt konnte mit Signature " + (beschreibung.getGueltigeIdentifikation().isPresent()
            ? beschreibung.getGueltigeIdentifikation().get().getIdent() : " ohne Signatur ")
            + " nicht gefunden werden!");
  }

  protected void enhanceNormdatenReferenzSprache(Beschreibung beschreibung)
      throws BeschreibungImportException {
    if (Objects.isNull(beschreibung.getBeschreibungsSprache())
        || Objects.isNull(beschreibung.getBeschreibungsSprache().getName())
        || beschreibung.getBeschreibungsSprache().getName().isBlank()) {
      log.info("No Beschreibungssprache available in Beschreibung {}.", beschreibung.getId());
      return;
    }

    NormdatenReferenz normdatenSprache = normdatenReferenzBoundary
        .findSpracheById(beschreibung.getBeschreibungsSprache().getName())
        .orElseThrow(
            () -> new BeschreibungImportException("Found no Language for Beschreibung " + beschreibung.getId()));

    beschreibung.setBeschreibungsSprache(normdatenSprache);
  }

  Optional<KulturObjektDokument> findByIdentifikation(Identifikation beschreibungsIdentifikation)
      throws KulturObjektDokumentNotFoundException {
    try {
      return kulturObjektDokumentRepository.findByIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName(
          beschreibungsIdentifikation.getIdent(),
          beschreibungsIdentifikation.getBesitzer().getName(),
          beschreibungsIdentifikation.getAufbewahrungsOrt().getName());
    } catch (NonUniqueResultException nonUniqueResultException) {
      throw new KulturObjektDokumentNotFoundException(
          "Es wurde mehr als eine übereinstimmende gültige KOD-Signatur zur BeschreibungsIdentifikation gefunden: "
              + beschreibungsIdentifikation.getIdent(), nonUniqueResultException);
    } catch (Exception exception) {
      throw new KulturObjektDokumentNotFoundException("Beim Abgleich der BeschreibungsIdentifikation "
          + beschreibungsIdentifikation.getIdent() + " mit gültigen KOD-Signaturen ist ein Fehler aufgetreten: "
          + exception.getMessage(), exception);
    }
  }

  Optional<KulturObjektDokument> findByAlternativeIdentifikation(Identifikation beschreibungsIdentifikation)
      throws KulturObjektDokumentNotFoundException {
    try {
      return kulturObjektDokumentRepository
          .findByAlternativeIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName(
              beschreibungsIdentifikation.getIdent(),
              beschreibungsIdentifikation.getBesitzer().getName(),
              beschreibungsIdentifikation.getAufbewahrungsOrt().getName());
    } catch (NonUniqueResultException nonUniqueResultException) {
      throw new KulturObjektDokumentNotFoundException(
          "Es wurde mehr als eine übereinstimmende alternative KOD-Signatur zur BeschreibungsIdentifikation gefunden: "
              + beschreibungsIdentifikation.getIdent(), nonUniqueResultException);
    } catch (Exception exception) {
      throw new KulturObjektDokumentNotFoundException("Beim Abgleich der BeschreibungsIdentifikation "
          + beschreibungsIdentifikation.getIdent() + " mit alternativen KOD-Signaturen ist ein Fehler aufgetreten: "
          + exception.getMessage(), exception);
    }
  }

  void createAndAddInternalPURL(Beschreibung beschreibung) {
    log.info("createAndAddInternalPURL: purlAutogenerateEnabled={}, beschreibung.id={}, "
            + "beschreibung.verwaltungsTyp={}, beschreibung.getDokumentObjektTyp={}",
        this.purlAutogenerateEnabled, beschreibung.getId(), beschreibung.getVerwaltungsTyp(),
        beschreibung.getDokumentObjektTyp());

    if (this.purlAutogenerateEnabled
        && VerwaltungsTyp.EXTERN == beschreibung.getVerwaltungsTyp()
        && beschreibung.getPURLs().stream().noneMatch(purl -> PURLTyp.INTERNAL == purl.getTyp())) {
      try {
        PURL purl = purlBoundary.createNewPURL(beschreibung.getId(), beschreibung.getDokumentObjektTyp());
        beschreibung.getPURLs().add(purl);
      } catch (Exception e) {
        log.error("createAndAddInternalPURLs: Error creating PURL for Beschreibung " + beschreibung.getId(), e);
      }
    }
  }

  @Inject
  public void setImportRepository(ImportRepository importRepository) {
    this.importRepository = importRepository;
  }

  @Inject
  public void setKafkaIndexingProducer(KafkaIndexingProducer kafkaIndexingProducer) {
    this.kafkaIndexingProducer = kafkaIndexingProducer;
  }

  @Inject
  protected void setBeschreibungsRepository(BeschreibungsRepository beschreibungsRepository) {
    this.beschreibungsRepository = beschreibungsRepository;
  }

  @Inject
  protected void setKulturObjektDokumentRepository(
      KulturObjektDokumentRepository kulturObjektDokumentRepository) {
    this.kulturObjektDokumentRepository = kulturObjektDokumentRepository;
  }

  @Inject
  protected void setSuchDokumentService(SuchDokumentService suchDokumentService) {
    this.suchDokumentService = suchDokumentService;
  }

  @Inject
  public void setSendIndexMessageEvent(
      @sendIndexMessage Event<BeschreibungImport> sendIndexMessageEvent) {
    this.sendIndexMessageEvent = sendIndexMessageEvent;
  }

  @Inject
  public void setAddBeschreibungMsDescInKodSourceDesc(
      @ConfigProperty(name = "workaround.ADD_BESCHREIBUNG_MSDESC_IN_KOD_SOURCE_DESC", defaultValue = "false") String addBeschreibungMsDescInKodSourceDesc) {
    this.addBeschreibungMsDescInKodSourceDesc = addBeschreibungMsDescInKodSourceDesc;
  }

  @Inject
  public void setImportVorgangBoundary(
      ImportVorgangBoundary importVorgangBoundary) {
    this.importVorgangBoundary = importVorgangBoundary;
  }

  @Inject
  public void setPraesentationBaseUrl(
      @ConfigProperty(name = "praesentationbaseurl") String praesentationBaseUrl) {
    this.praesentationBaseUrl = praesentationBaseUrl;
  }

  @Inject
  public void setNormdatenReferenzBoundary(NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @Inject
  public void setTeiXmlValidationBoundary(TeiXmlValidationBoundary teiXmlValidationBoundary) {
    this.teiXmlValidationBoundary = teiXmlValidationBoundary;
  }

  @Inject
  public void setTeiXmlTransformationBoundary(
      TeiXmlTransformationBoundary teiXmlTransformationBoundary) {
    this.teiXmlTransformationBoundary = teiXmlTransformationBoundary;
  }

  @Inject
  public void setNormdatenUpdateService(
      NormdatenUpdateService normdatenUpdateService) {
    this.normdatenUpdateService = normdatenUpdateService;
  }

  @Inject
  public void setPURLBoundary(
      PURLBoundary purlBoundary) {
    this.purlBoundary = purlBoundary;
  }

  @Inject
  public void setPurlAutogenerateEnabled(
      @ConfigProperty(name = "purl.autogenerate.enabled") boolean purlAutogenerateEnabled) {
    this.purlAutogenerateEnabled = purlAutogenerateEnabled;
  }

  @Inject
  public void setKonfigurationBoundary(KonfigurationBoundary konfigurationBoundary) {
    this.konfigurationBoundary = konfigurationBoundary;
  }

}
