package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 19.06.2020.
 * @version 1.0
 */

public class KafkaLoggingCallback implements Callback {

  private static final Logger logger = LoggerFactory.getLogger(KafkaLoggingCallback.class);

  protected KafkaLoggingCallback() {
  }

  @Override
  public void onCompletion(RecordMetadata metadata, Exception exception) {

    if (exception != null) {
      logger.error("Error during sending message to kafka {} ", exception.getMessage(), exception);
    }

    if (metadata != null) {
      logger.info("Sending Message to Kafka topic {} , partition {} , offset {}", metadata.topic(),
          metadata.partition(),
          metadata.offset());
    }

  }
}
