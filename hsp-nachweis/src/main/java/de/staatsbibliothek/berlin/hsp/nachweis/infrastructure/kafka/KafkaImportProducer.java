package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForUpload;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObjectTag;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportUploadDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportVorgang;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.05.2020.
 * @version 1.0
 * <p>: Kafka Producer Class for sending all xml data import messages to import service module.
 * These messages contain data for Kulturobjektdokumente, Beschreibungen and all Recherchedaten.
 */
@ApplicationScoped
public class KafkaImportProducer extends KafkaTransactionalProducer {

  KafkaImportProducer() {
    super(null, null, null, null, -1);
  }

  @Inject
  public KafkaImportProducer(
      TransactionManager transactionManager,
      @ConfigProperty(name = "kafka.dataimport.topic") String topic,
      @ConfigProperty(name = "kafka.bootstrapserver") String bootstrapserver,
      @ConfigProperty(name = "kafka.dataimport.groupid") String groupid,
      @ConfigProperty(name = "kafka.transaction.timeout") int transactionTimeout) {
    super(transactionManager, topic, bootstrapserver, groupid, transactionTimeout);
  }

  @Transactional
  public void sendImportDateiAsActivityStreamMessage(
      ImportUploadDatei datei,
      ActivityStreamsDokumentTyp dokumentTyp,
      ActivityStreamAction action,
      List<ActivityStreamObjectTag> tags,
      String actor)
      throws ActivityStreamsException {

    ActivityStream message = createMessageForUpload(datei, dokumentTyp, action, tags, actor);

    send(message);
  }

  public void sendImportJobAsActivityStreamMessage(ImportVorgang importVorgang)
      throws JsonProcessingException, ActivityStreamsException {
    ActivityStream message = ActivityStreamMessageFactory.createMessageForImportJob(importVorgang);
    send(message);
  }
}
