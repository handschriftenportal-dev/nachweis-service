package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import java.util.Map;
import java.util.Objects;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 13.09.2019.
 */
public class KafkaActivityStreamMessageSerializer implements Serializer<ActivityStream> {

  private final Logger logger = LoggerFactory.getLogger(KafkaActivityStreamMessageSerializer.class);

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {

  }

  @Override
  public byte[] serialize(String topic, ActivityStream data) {

    if (Objects.isNull(data)) {
      return new byte[0];
    }

    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {
      return objectMapper.writeValueAsBytes(data);
    } catch (JsonProcessingException e) {
      logger.info("Error during serialization of activity stream message", e);
    }

    return new byte[0];
  }

  @Override
  public void close() {

  }
}
