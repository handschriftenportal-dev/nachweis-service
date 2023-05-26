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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.digitalisatimport.DigitalisatImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.neueImporte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.katalogimport.KatalogImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodimport.KulturObjektDokumentImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgang;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgangBoundary;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 05.06.2019.
 */
@ApplicationScoped
public class KafkaImportConsumer {

  public static final Duration POLL_DURATION = Duration.ofSeconds(15);
  private static final Duration MAX_POLL_INTERVAL = Duration.ofSeconds(7200L);
  private static final Logger logger = LoggerFactory.getLogger(KafkaImportConsumer.class);
  private String topic;
  private String bootstrapServer;
  private String groupId;
  private long startOffset;
  private int maxPollRecords;
  private Consumer<String, ActivityStream> consumer;
  private volatile boolean running = true;
  private BearbeiterBoundary bearbeiterBoundary;
  private Event<BeschreibungImport> neueBeschreibungImporteEvent;
  private Event<DigitalisatImport> neueDigitalisatImporteEvent;
  private Event<KulturObjektDokumentImport> neueKulturObjektDokumentImportEvent;
  private Event<KatalogImport> neueKatalogImporteEvent;
  private ManagedExecutor managedExecutor;
  private ImportVorgangBoundary importVorgangBoundary;

  KafkaImportConsumer() {
  }


  @Inject
  public KafkaImportConsumer(@ConfigProperty(name = "kafka.import.topic") String topic,
      @ConfigProperty(name = "kafka.bootstrapserver") String bootstrapserver,
      @ConfigProperty(name = "kafka.import.groupid") String groupid,
      @ConfigProperty(name = "kafka.normdaten.startOffset", defaultValue = "-1") long startOffset,
      @ConfigProperty(name = "kafka.nachweisconsumer.maxpollrecords", defaultValue = "1") int maxPollRecords,
      @neueImporte Event<BeschreibungImport> neueBeschreibungImporteEvent,
      @neueImporte Event<DigitalisatImport> neueDigitalisatImporteEvent,
      @neueImporte Event<KulturObjektDokumentImport> neueKulturObjektDokumentImportEvent,
      @neueImporte Event<KatalogImport> neueKatalogImporteEvent,
      ManagedExecutor managedExecutor, BearbeiterBoundary bearbeiterBoundary, ImportVorgangBoundary importVorgangBoundary) {
    this.topic = topic;
    this.bootstrapServer = bootstrapserver;
    this.groupId = groupid;
    this.startOffset = startOffset;
    this.neueBeschreibungImporteEvent = neueBeschreibungImporteEvent;
    this.neueDigitalisatImporteEvent = neueDigitalisatImporteEvent;
    this.neueKulturObjektDokumentImportEvent = neueKulturObjektDokumentImportEvent;
    this.neueKatalogImporteEvent = neueKatalogImporteEvent;
    this.managedExecutor = managedExecutor;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.maxPollRecords = maxPollRecords;
    this.importVorgangBoundary = importVorgangBoundary;
  }


  public void createConsumer() {

    final Properties props = new Properties();
    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
    props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getName());
    props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        KafkaActivityStreamMessageDeserializer.class.getName());
    //Transaction settings
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
    props.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, String.valueOf(MAX_POLL_INTERVAL.toMillis()));

    this.consumer = new KafkaConsumer<>(props);

    consumer.subscribe(Collections.singletonList(topic),
        new KafkaPartitionRebalanceListener(consumer, startOffset));

    logger
        .info("Kafka Import consumer created with following properties {} ", props);
  }

  public void startConsumer() {
    managedExecutor.runAsync(() -> {
      if (Objects.nonNull(consumer)) {

        logger
            .info("Kafka Import consumer started reading topic");

        while (running) {
          final ConsumerRecords<String, ActivityStream> consumerRecords =
              consumer.poll(POLL_DURATION);
          if (consumerRecords != null && !consumerRecords.isEmpty()) {
            for (ConsumerRecord<String, ActivityStream> record : consumerRecords) {
                try {
                  logger.info("Kafka Process Message Topic {} Partition {} Offset {} ",
                      record.topic(),
                      record.partition(),
                      record.offset());

                  processImportInProgress(record);

                  processImportItem(record);

                } catch (Exception ex) {
                  logger.error("Problem with the kafka messages {}", ex.getMessage(), ex);
                  processImportError(record.value(), String.format("%s\n%s", ex.getMessage(),
                      Arrays.stream(ex.getSuppressed()).map(Throwable::getMessage).collect(
                          Collectors.joining("\n"))));
                } finally {
                  Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();
                  TopicPartition topicPartition = new TopicPartition(record.topic(),
                      record.partition());
                  OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(record.offset() + 1);
                  offsetsToCommit.put(topicPartition, offsetAndMetadata);
                  logger.info("Kafka Commit Message Partition {} Offset {} ",
                      topicPartition.partition(),
                      offsetAndMetadata.offset());
                  consumer.commitSync(offsetsToCommit);
                }
              }
            }
          }
          consumer.close();
          logger.info("Closed kafka consumer");
        }
    });
  }

  @Transactional(rollbackOn = {Exception.class}, value = TxType.REQUIRES_NEW)
  void processImportInProgress(ConsumerRecord<String, ActivityStream> record)
      throws ActivityStreamsException, JsonProcessingException {
    ImportVorgang importVorgang = importVorgangBoundary.getImportVorgang(
        new ActivityStreamMessageDTO(record.value()));
    importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.IN_PROGRESS);
    importVorgangBoundary.sendImportJobToKafka(importVorgang);
  }

  @Transactional(rollbackOn = {Exception.class}, value = TxType.REQUIRES_NEW)
  void processImportError(ActivityStream activityStream, String errorMessage) {
    try {
      ActivityStreamMessageDTO messageDTO = new ActivityStreamMessageDTO(activityStream);
      ImportVorgang importVorgang = importVorgangBoundary.getImportVorgang(messageDTO);
      importVorgang.setFehler(errorMessage);
      importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.FAILED);
      importVorgangBoundary.sendImportJobToKafka(importVorgang);

    } catch (Exception e) {
      logger.error("Error during process import error message", e);
    }
  }

  @Transactional(rollbackOn = {Exception.class}, value = TxType.REQUIRES_NEW)
  @TransactionConfiguration(timeout = 7200)
  void processImportItem(ConsumerRecord<String, ActivityStream> record)
      throws Exception {
    logger
        .info("Consumer Record: {} {} {}",
            record.key(), record.partition(), record.offset());

    if (Objects.nonNull(record.value()) && record.value().getObjects().stream()
        .anyMatch(o -> ActivityStreamsDokumentTyp.BESCHREIBUNG.equals(o.getType()))) {
      BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN,
          record.value());
      neueBeschreibungImporteEvent.fire(beschreibungImportJob);
    }

    if (Objects.nonNull(record.value()) && record.value().getObjects().stream()
        .anyMatch(o -> ActivityStreamsDokumentTyp.DIGITALISAT.equals(o.getType()))) {
      DigitalisatImport digitalisatImport = new DigitalisatImport(record.value(),
          bearbeiterBoundary.findBearbeiterWithName(record.value().getActor().getName()));
      neueDigitalisatImporteEvent.fire(digitalisatImport);
    }

    if (Objects.nonNull(record.value()) && record.value().getObjects().stream()
        .anyMatch(o -> ActivityStreamsDokumentTyp.KOD.equals(o.getType()))) {
      KulturObjektDokumentImport kodImport = new KulturObjektDokumentImport(record.value());
      neueKulturObjektDokumentImportEvent.fire(kodImport);
    }

    if (Objects.nonNull(record.value()) && record.value().getObjects().stream()
        .anyMatch(o -> ActivityStreamsDokumentTyp.KATALOG.equals(o.getType()))) {
      KatalogImport katalogImport = new KatalogImport(record.value(),
          bearbeiterBoundary.findBearbeiterWithName(record.value().getActor().getName()));
      neueKatalogImporteEvent.fire(katalogImport);
    }
  }

  public void stopConsumer() {
    this.running = false;
  }


  public String getTopic() {
    return topic;
  }

  public String getBootstrapServer() {
    return bootstrapServer;
  }

  public Consumer<String, ActivityStream> getConsumer() {
    return consumer;
  }

  void setConsumer(
      Consumer<String, ActivityStream> consumer) {
    this.consumer = consumer;
  }

  public String getGroupId() {
    return groupId;
  }

  public boolean isRunning() {
    return running;
  }
}
