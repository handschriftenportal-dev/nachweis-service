package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 31.05.24.
 * @version 1.0
 */
@Slf4j
public class KafkaConsumerTool {

  private static final Duration MAX_POLL_INTERVAL = Duration.ofSeconds(7200L);

  public static Properties createConsumerProperties(String bootstrapServer, String groupId,
      int maxPollRecords) {
    final Properties props = new Properties();
    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
    props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getName());
    props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        KafkaActivityStreamMessageDeserializer.class.getName());
    //Transaction settings
    props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    props.setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    props.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, String.valueOf(maxPollRecords));
    props.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,
        String.valueOf(MAX_POLL_INTERVAL.toMillis()));

    return props;
  }

  public static Map<TopicPartition, OffsetAndMetadata> createOffset(
      ConsumerRecord<String, ActivityStream> record) {

    if (record == null) {
      throw new IllegalArgumentException("Missing record parameter");
    }

    Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();
    TopicPartition topicPartition = new TopicPartition(record.topic(),
        record.partition());
    OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(record.offset() + 1);
    offsetsToCommit.put(topicPartition, offsetAndMetadata);
    log.info("Kafka Commit Message Partition {} Offset {} ",
        topicPartition.partition(),
        offsetAndMetadata.offset());

    return offsetsToCommit;
  }

}
