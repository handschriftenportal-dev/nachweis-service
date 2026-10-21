package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.ActivityStreamMessage;
import java.io.IOException;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */
public class KafkaActivityStreamMessageDeserializer implements Deserializer<ActivityStreamMessage> {


  private static final Logger logger = LoggerFactory
      .getLogger(KafkaActivityStreamMessageDeserializer.class);

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {

  }

  @Override
  public ActivityStreamMessage deserialize(String topic, byte[] data) {

    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {

      return objectMapper.readValue(data, ActivityStreamMessage.class);

    } catch (IOException e) {

      logger.error("Error during reading JSON Message from queue", e);

      return null;
    }
  }

  @Override
  public void close() {

  }
}
