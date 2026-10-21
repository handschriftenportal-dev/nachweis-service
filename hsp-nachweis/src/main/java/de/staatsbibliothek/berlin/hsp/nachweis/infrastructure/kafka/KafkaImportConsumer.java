package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportVorgangBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.digitalisatimport.DigitalisatImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.neueImporte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.katalogimport.KatalogImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodimport.KulturObjektDokumentImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportVorgang;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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
  private static final Logger logger = LoggerFactory.getLogger(KafkaImportConsumer.class);
  private final AtomicBoolean running = new AtomicBoolean(true);
  private String topic;
  private String bootstrapServer;
  private String groupId;
  private long startOffset;
  private int maxPollRecords;
  private Consumer<String, ActivityStream> consumer;
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
  public KafkaImportConsumer(
      @ConfigProperty(name = "kafka.import.topic") String topic,
      @ConfigProperty(name = "kafka.bootstrapserver") String bootstrapserver,
      @ConfigProperty(name = "kafka.import.groupid") String groupid,
      @ConfigProperty(name = "kafka.normdaten.startOffset", defaultValue = "-1") long startOffset,
      @ConfigProperty(name = "kafka.import.maxpollrecords", defaultValue = "1")
      int maxPollRecords,
      @neueImporte Event<BeschreibungImport> neueBeschreibungImporteEvent,
      @neueImporte Event<DigitalisatImport> neueDigitalisatImporteEvent,
      @neueImporte Event<KulturObjektDokumentImport> neueKulturObjektDokumentImportEvent,
      @neueImporte Event<KatalogImport> neueKatalogImporteEvent,
      ManagedExecutor managedExecutor,
      BearbeiterBoundary bearbeiterBoundary,
      ImportVorgangBoundary importVorgangBoundary) {
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

    this.consumer =
        new KafkaConsumer<>(
            KafkaConsumerTool.createConsumerProperties(bootstrapServer, groupId, maxPollRecords));

    consumer.subscribe(
        Collections.singletonList(topic),
        new KafkaPartitionRebalanceListener(consumer, startOffset));

    logger.info("Kafka Import consumer created");
  }

  public void startConsumer() {
    managedExecutor.runAsync(
        () -> {
          try {
            while (running.get()) {
              if (consumer == null) {
                logger.info("Consumer is null... recreated");
                createConsumer();
              }

              logger.debug("Consumer is running ... ");

              final ConsumerRecords<String, ActivityStream> consumerRecords =
                  consumer.poll(POLL_DURATION);

              if (consumerRecords != null && !consumerRecords.isEmpty()) {
                for (ConsumerRecord<String, ActivityStream> record : consumerRecords) {
                  long messageStartNanos = System.nanoTime();
                  try {
                    logger.info(
                        "Kafka Process Message Topic {} Partition {} Offset {} ",
                        record.topic(),
                        record.partition(),
                        record.offset());

                    processImportInProgress(record);

                    processImportItem(record);

                    logger.info(
                        "Kafka Message verarbeitet Topic {} Partition {} Offset {} in {} ms",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        (System.nanoTime() - messageStartNanos) / 1_000_000L);

                  } catch (Exception ex) {
                    logger.error("Problem with the kafka messages {}", ex.getMessage(), ex);
                    processImportError(
                        record.value(),
                        String.format(
                            "%s\n%s",
                            ex.getMessage(),
                            Arrays.stream(ex.getSuppressed())
                                .map(Throwable::getMessage)
                                .collect(Collectors.joining("\n"))));
                  } finally {
                    consumer.commitSync(KafkaConsumerTool.createOffset(record));
                  }
                }
              }
            }
          } catch (Exception error) {
            logger.error("Problem with the kafka messages {}", error.getMessage(), error);
          } finally {
            consumer.close();
            consumer = null;
            logger.info("Closed kafka consumer");
          }
        });
  }

  @Transactional(
      rollbackOn = {Exception.class},
      value = TxType.REQUIRES_NEW)
  void processImportInProgress(ConsumerRecord<String, ActivityStream> record)
      throws ActivityStreamsException, JsonProcessingException {
    ImportVorgang importVorgang =
        importVorgangBoundary.getImportVorgang(new ActivityStreamMessageDTO(record.value()));
    if (importVorgang != null) {
      importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.IN_PROGRESS);
      importVorgangBoundary.sendImportJobToKafka(importVorgang);
    }
  }

  @Transactional(
      rollbackOn = {Exception.class},
      value = TxType.REQUIRES_NEW)
  void processImportError(ActivityStream activityStream, String errorMessage) {
    try {
      ActivityStreamMessageDTO messageDTO = new ActivityStreamMessageDTO(activityStream);
      ImportVorgang importVorgang = importVorgangBoundary.getImportVorgang(messageDTO);
      if (importVorgang != null) {
        importVorgang.setFehler(errorMessage);
        importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.FAILED);
        importVorgangBoundary.sendImportJobToKafka(importVorgang);
      }

    } catch (Exception e) {
      logger.error("Error during process import error message", e);
    }
  }

  @Transactional(
      rollbackOn = {Exception.class},
      value = TxType.REQUIRES_NEW)
  @TransactionConfiguration(timeout = 7200)
  void processImportItem(ConsumerRecord<String, ActivityStream> record) throws Exception {
    logger.info("Consumer Record: {} {} {}", record.key(), record.partition(), record.offset());

    if (Objects.nonNull(record.value())
        && record.value().getObjects().stream()
        .anyMatch(o -> ActivityStreamsDokumentTyp.BESCHREIBUNG.equals(o.getType()))) {
      BeschreibungImport beschreibungImportJob =
          new BeschreibungImport(ImportStatus.OFFEN, record.value());
      neueBeschreibungImporteEvent.fire(beschreibungImportJob);
    }

    if (Objects.nonNull(record.value())
        && record.value().getObjects().stream()
        .anyMatch(o -> ActivityStreamsDokumentTyp.DIGITALISAT.equals(o.getType()))) {
      DigitalisatImport digitalisatImport =
          new DigitalisatImport(
              record.value(),
              bearbeiterBoundary.findBearbeiterWithName(record.value().getActor().getName()));
      neueDigitalisatImporteEvent.fire(digitalisatImport);
    }

    if (Objects.nonNull(record.value())
        && record.value().getObjects().stream()
        .anyMatch(o -> ActivityStreamsDokumentTyp.KOD.equals(o.getType()))) {
      KulturObjektDokumentImport kodImport = new KulturObjektDokumentImport(record.value());
      neueKulturObjektDokumentImportEvent.fire(kodImport);
    }

    if (Objects.nonNull(record.value())
        && record.value().getObjects().stream()
        .anyMatch(o -> ActivityStreamsDokumentTyp.KATALOG.equals(o.getType()))) {
      KatalogImport katalogImport =
          new KatalogImport(
              record.value(),
              bearbeiterBoundary.findBearbeiterWithName(record.value().getActor().getName()));
      neueKatalogImporteEvent.fire(katalogImport);
    }
  }

  public void stopConsumer() {
    logger.info("Stop Consumer is calling ...");
    this.running.set(false);
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

  void setConsumer(Consumer<String, ActivityStream> consumer) {
    this.consumer = consumer;
  }

  public String getGroupId() {
    return groupId;
  }

  public boolean isRunning() {
    return running.get();
  }
}
