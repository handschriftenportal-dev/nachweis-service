package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import java.util.Collection;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 19.06.20
 */
public class KafkaPartitionRebalanceListener implements ConsumerRebalanceListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      KafkaPartitionRebalanceListener.class);

  private final Consumer<?, ?> consumer;

  private final long startOffset;

  public KafkaPartitionRebalanceListener(Consumer<?, ?> consumer, long startOffset) {
    this.consumer = consumer;
    this.startOffset = startOffset;
  }

  @Override
  public void onPartitionsRevoked(Collection<TopicPartition> topicPartitions) {
    LOGGER.info("Topic partitions {} revoked from consumer", topicPartitions);
  }

  @Override
  public void onPartitionsAssigned(Collection<TopicPartition> topicPartitions) {
    switch (String.valueOf(startOffset)) {
      case "-999":
        consumer.seekToEnd(topicPartitions);
        LOGGER.info("Kafka offset set to the end (startOffset={})", startOffset);
        break;
      case "-1":
        LOGGER.info("Kafka offset will be not touch (startOffset={})", startOffset);
        break;
      case "0":
        consumer.seekToBeginning(topicPartitions);
        LOGGER.info("Kafka offset set to the beginning (startOffset={})", startOffset);
        break;
      default:
        for (TopicPartition topicPartition : topicPartitions) {
          consumer.seek(topicPartition, startOffset);
        }
        LOGGER.info("Kafka offset set to the startOffset={}", startOffset);
        break;
    }
  }

  @Override
  public void onPartitionsLost(Collection<TopicPartition> partitions) {
    ConsumerRebalanceListener.super.onPartitionsLost(partitions);
    LOGGER.info("Topic partitions {} lost from consumer", partitions);
  }
}
