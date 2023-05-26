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
    objectMapper.findAndRegisterModules();
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
