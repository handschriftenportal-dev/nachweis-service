package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaImportConsumer.POLL_DURATION;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.05.24.
 * @version 1.0
 */

@ApplicationScoped
@Getter
@Slf4j
public class KafkaNormdatenConsumer {

  private final String topic;
  private final String bootstrapServer;
  private final String groupId;
  private Consumer<String, ActivityStream> kafkaConsumerClient;
  private final AtomicBoolean polling = new AtomicBoolean(true);
  private final ManagedExecutor managedExecutor;
  private final NormdatenReferenzBoundary normdatenReferenzBoundary;

  @Inject
  public KafkaNormdatenConsumer(@ConfigProperty(name = "kafka.normdatenupdate.topic") String topic,
      @ConfigProperty(name = "kafka.bootstrapserver") String bootstrapServer,
      @ConfigProperty(name = "kafka.normdatenupdate.groupid") String groupId,
      ManagedExecutor managedExecutor, NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this.topic = topic;
    this.bootstrapServer = bootstrapServer;
    this.groupId = groupId;
    this.managedExecutor = managedExecutor;
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @PostConstruct
  public void setup() {
    log.info("Setup KafkaNormdatenConsumer with reading properties ....");

    this.kafkaConsumerClient = new KafkaConsumer<>(
        KafkaConsumerTool.createConsumerProperties(bootstrapServer, groupId, 1));
  }

  public void stopPollingMessages() {
    polling.set(false);
  }

  public void startPollingMessages() {
    log.info("Start polling messages for Normdaten-Updates");

    managedExecutor.runAsync(() -> {
      try {

        kafkaConsumerClient.subscribe(Collections.singletonList(topic),
            new KafkaPartitionRebalanceListener(kafkaConsumerClient, -1));

        while (polling.get()) {

          final ConsumerRecords<String, ActivityStream> consumerRecords =
              kafkaConsumerClient.poll(POLL_DURATION);

          for (ConsumerRecord<String, ActivityStream> record : consumerRecords) {
            try {
              log.info("Kafka Process Message Topic {} Partition {} Offset {} ",
                  record.topic(),
                  record.partition(),
                  record.offset());

              updateNormdatenReferenz(record.value());

            } catch (Exception ex) {
              log.error("Problem with the kafka messages {}", ex.getMessage(), ex);
            } finally {
              kafkaConsumerClient.commitSync(KafkaConsumerTool.createOffset(record));
            }
          }

        }
      } finally {
        kafkaConsumerClient.close();
        kafkaConsumerClient = null;
        log.info("Closed kafka consumer");
      }
    });
  }

  protected void updateNormdatenReferenz(ActivityStream message) {
    log.info("Update Normdatenreferenz with message {}", message);
    message.getObjects().forEach(
        contentObject -> normdatenReferenzBoundary.updateNormdatenReferenzWithID(
            contentObject.getId()));
  }
}
