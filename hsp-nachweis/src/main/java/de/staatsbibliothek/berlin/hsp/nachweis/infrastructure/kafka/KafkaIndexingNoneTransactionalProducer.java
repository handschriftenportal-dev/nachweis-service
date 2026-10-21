package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForIndexRemoveAll;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForKatalog;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForKulturobjektDokument;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.TEIDokument;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.common.TEIUtility;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.KafkaIndexProducerBoundary;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.10.22.
 * @version 1.0
 */

@ApplicationScoped
public class KafkaIndexingNoneTransactionalProducer implements KafkaIndexProducerBoundary {

  private static final Logger logger = LoggerFactory.getLogger(
      KafkaIndexingNoneTransactionalProducer.class);
  private final String topic;

  private final String bootstrapserver;

  private final String groupid;
  private final BeschreibungsRepository beschreibungsRepository;
  private KafkaProducer<String, ActivityStream> producer;

  @Inject
  public KafkaIndexingNoneTransactionalProducer(
      @ConfigProperty(name = "kafka.index.topic") String topic,
      @ConfigProperty(name = "kafka.bootstrapserver") String bootstrapserver,
      @ConfigProperty(name = "kafka.index.groupid") String groupid,
      BeschreibungsRepository beschreibungsRepository) {
    this.topic = topic;
    this.bootstrapserver = bootstrapserver;
    this.groupid = groupid;
    this.beschreibungsRepository = beschreibungsRepository;
  }

  @PostConstruct
  public void setup() {

    Properties producerProperties = new Properties();
    producerProperties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapserver);
    producerProperties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        StringSerializer.class.getName());
    producerProperties.setProperty(ProducerConfig.CLIENT_ID_CONFIG, groupid);
    producerProperties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        KafkaActivityStreamMessageSerializer.class.getName());
    producerProperties.setProperty(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "500000000");
    producerProperties.setProperty(ProducerConfig.BUFFER_MEMORY_CONFIG, "500000000");
    producerProperties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
    producerProperties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    producerProperties
        .setProperty(ProducerConfig.RETRIES_CONFIG, String.valueOf(Integer.MAX_VALUE));
    producerProperties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

    producer = new KafkaProducer<>(producerProperties);
  }

  public void send(ActivityStream message) {

    if (Objects.nonNull(message)) {
      logger.info("Sending message to kafka topic {} with id {} ", topic, message.getId());

      final ProducerRecord<String, ActivityStream> producerRecord = new ProducerRecord<>(topic,
          message.getId(), message);

      producer.send(producerRecord, new KafkaLoggingCallback());
      producer.flush();
    }
  }

  public ActivityStream sendKulturobjektDokumentAsActivityStreamMessage(KulturObjektDokument kod,
      ActivityStreamAction action, boolean compressed, String actor)
      throws ActivityStreamsException {

    if (TEIUtility.isTEINullOrEmpty(kod)) {
      throw new IllegalArgumentException(
          "Can't send message because of needed parameter of KOD.");
    }

    ActivityStream message = createMessageForKulturobjektDokument(kod, action, compressed, actor,
        beschreibungsRepository);

    send(message);

    return message;
  }

  public void sendKatalogAsActivityStreamMessage(
      Katalog katalog, ActivityStreamAction action, boolean compressed, String actor)
      throws ActivityStreamsException {

    if (TEIUtility.isTEINullOrEmpty(katalog)) {
      throw new IllegalArgumentException(
          "Can't send message because of needed parameter of Katalog.");
    }

    ActivityStream message = createMessageForKatalog(katalog, action, actor, compressed);
    send(message);
  }

  public void sendTEIDocumentAsActivityStreamMessage(
      TEIDokument document, ActivityStreamAction action, boolean compressed, String actor)
      throws ActivityStreamsException {
    if (TEIUtility.isTEINullOrEmpty(document)) {
      throw new IllegalArgumentException(
          "Can't send message because of needed parameter of TEIDokument.");
    }

    if (document instanceof Katalog katalog) {
      send(createMessageForKatalog(katalog, action, actor, compressed));
    } else if (document instanceof KulturObjektDokument kod) {
      send(createMessageForKulturobjektDokument(kod, action, compressed, actor,
          beschreibungsRepository));
    }
  }

  @Override
  public ActivityStream sendRemoveAllAsActivityStreamMessage(String actor)
      throws ActivityStreamsException {

    ActivityStream message = createMessageForIndexRemoveAll(actor);

    send(message);

    return message;
  }

}
