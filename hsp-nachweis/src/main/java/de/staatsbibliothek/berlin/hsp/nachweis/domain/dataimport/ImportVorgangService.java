package de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObjectTag;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.ImportUpdate;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServicePort;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportVorgang;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaImportProducer;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.05.2020.
 * @version 1.0
 * <p>Class responsible for all Import Vorgänge and for handling the message sending to the import
 * module.
 */
@Dependent
public class ImportVorgangService implements ImportVorgangBoundary, Serializable {

  private static final Logger logger = LoggerFactory.getLogger(ImportVorgangService.class);

  @Serial
  private static final long serialVersionUID = 5130810203010338330L;

  private KafkaImportProducer kafkaImportProducer;

  private ImportServicePort importServicePort;

  private transient ObjectMapper objectMapper;

  private Event<ImportVorgang> importVorgangEvent;

  ImportVorgangService() {
  }

  @Inject
  public ImportVorgangService(
      KafkaImportProducer kafkaImportProducer,
      @RestClient ImportServicePort importServicePort,
      ObjectMapper objectMapper,
      @ImportUpdate Event<ImportVorgang> importVorgangEvent) {
    this.kafkaImportProducer = kafkaImportProducer;
    this.importServicePort = importServicePort;
    this.objectMapper = objectMapper;
    this.importVorgangEvent = importVorgangEvent;
  }

  @Override
  public void sendDateiUploadMessageToImportService(
      ImportUploadDatei datei, ActivityStreamsDokumentTyp dokumentTyp, Bearbeiter bearbeiter)
      throws ImportVorgangException {

    sendKafkaMessage(datei, dokumentTyp, bearbeiter, null);
  }

  @Override
  public Optional<ImportVorgang> findById(String id) {
    if (Objects.isNull(id) || id.isBlank()) {
      logger.info("findById: id is null or blank.");
      return Optional.empty();
    }
    try {
      ImportVorgang importVorgang = importServicePort.findById(id);
      return Optional.ofNullable(importVorgang);
    } catch (WebApplicationException wae) {
      logger.error("findById: error finding ImportVorgang for id {}: {}", id, wae.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public void sendDateiUploadMessageToImportService(
      ImportUploadDatei datei,
      ActivityStreamsDokumentTyp dokumentTyp,
      Bearbeiter bearbeiter,
      List<ActivityStreamObjectTag> tag)
      throws ImportVorgangException {
    sendKafkaMessage(datei, dokumentTyp, bearbeiter, tag);
  }

  @Transactional
  void sendKafkaMessage(
      ImportUploadDatei datei,
      ActivityStreamsDokumentTyp dokumentTyp,
      Bearbeiter bearbeiter,
      List<ActivityStreamObjectTag> tag)
      throws ImportVorgangException {
    if (datei == null || dokumentTyp == null || bearbeiter == null) {
      throw new IllegalArgumentException(
          "Fehlende Informationen, Daten können nicht gesendet werden!");
    }

    logger.info(
        "Sending input to import service for type {} , datei {} content {} ",
        dokumentTyp,
        datei.getName(),
        datei.getContentType());

    try {

      kafkaImportProducer.sendImportDateiAsActivityStreamMessage(
          datei, dokumentTyp, ActivityStreamAction.ADD, tag, bearbeiter.getBearbeitername());

    } catch (ActivityStreamsException e) {
      logger.error("Error during sending message to import ", e);
      throw new ImportVorgangException("Die Datei konnte nicht angenommen werden.", e);
    }
  }

  @Override
  public Set<ImportVorgang> findAll() {
    return new LinkedHashSet<>(importServicePort.findAll());
  }

  @Override
  public ImportVorgang getImportVorgang(ActivityStreamMessageDTO activityStream)
      throws ActivityStreamsException, JsonProcessingException {
    ImportVorgang importVorgang = null;
    for (ActivityStreamObjectDTO activityStreamObject : activityStream.getObjects()) {
      if (ActivityStreamsDokumentTyp.IMPORT.equals(activityStreamObject.getType())) {
        String content = new String(activityStreamObject.getContent(), StandardCharsets.UTF_8);
        importVorgang = objectMapper.readValue(content, ImportVorgang.class);
        break;
      }
    }
    return importVorgang;
  }

  @Override
  public void sendImportJobToKafka(ImportVorgang importVorgang)
      throws ActivityStreamsException, JsonProcessingException {
    kafkaImportProducer.sendImportJobAsActivityStreamMessage(importVorgang);
    importVorgangEvent.fire(importVorgang);
  }

  @Override
  public void deleteVorgaenge(List<ImportVorgang> importVorgang) throws ImportVorgangException {

    try {
      importServicePort.deleteByIDs(importVorgang.stream().map(ImportVorgang::getId).toList());
    } catch (Exception error) {
      logger.error("Error during deletion of vorgang {}", importVorgang, error);
      throw new ImportVorgangException(error);
    }
  }

  void setKafkaImportProducer(KafkaImportProducer kafkaImportProducer) {
    this.kafkaImportProducer = kafkaImportProducer;
  }

  void setImportServicePort(ImportServicePort importServicePort) {
    this.importServicePort = importServicePort;
  }

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
}
