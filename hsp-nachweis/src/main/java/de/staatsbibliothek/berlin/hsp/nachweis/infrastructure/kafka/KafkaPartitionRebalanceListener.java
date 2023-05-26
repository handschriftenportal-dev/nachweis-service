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

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPartitionRebalanceListener.class);

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
}
