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

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForKulturobjektDokument;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.KafkaIndexProducerBoundary;
import java.util.Objects;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.10.22.
 * @version 1.0
 */

@ApplicationScoped
public class KafkaIndexingNoneTransactionalProducer implements KafkaIndexProducerBoundary {

  private static final Logger logger = LoggerFactory.getLogger(KafkaIndexingNoneTransactionalProducer.class);
  private String topic;

  private String bootstrapserver;

  private String groupid;

  private KafkaProducer<String, ActivityStream> producer;

  private BeschreibungsRepository beschreibungsRepository;

  @Inject
  public KafkaIndexingNoneTransactionalProducer(@ConfigProperty(name = "kafka.index.topic") String topic,
      @ConfigProperty(name = "kafka.bootstrapserver") String bootstrapserver,
      @ConfigProperty(name = "kafka.index.groupid") String groupid, BeschreibungsRepository beschreibungsRepository) {
    this.topic = topic;
    this.bootstrapserver = bootstrapserver;
    this.groupid = groupid;
    this.beschreibungsRepository = beschreibungsRepository;
  }

  @PostConstruct
  public void setup() {

    Properties producerProperties = new Properties();
    producerProperties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapserver);
    producerProperties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        StringSerializer.class.getName());
    producerProperties.setProperty(ProducerConfig.CLIENT_ID_CONFIG, groupid);
    producerProperties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        KafkaActivityStreamMessageSerializer.class.getName());
    producerProperties.setProperty(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "500000000");
    producerProperties.setProperty(ProducerConfig.BUFFER_MEMORY_CONFIG, "500000000");
    producerProperties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
    producerProperties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    producerProperties
        .setProperty(ProducerConfig.RETRIES_CONFIG, String.valueOf(Integer.MAX_VALUE));
    producerProperties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

    producer = new KafkaProducer<>(producerProperties);
  }

  public void send(ActivityStream message) {

    if (Objects.nonNull(message)) {
      logger.info("Sending message to kafka topic {} with id {} ", topic, message.getId());

      final ProducerRecord<String, ActivityStream> record = new ProducerRecord(topic,
          message.getId(), message);

      producer.send(record, new KafkaLoggingCallback());
      producer.flush();
    }
  }

  public ActivityStream sendKulturobjektDokumentAsActivityStreamMessage(KulturObjektDokument kod, ActivityStreamAction action, boolean compressed, String actor)
      throws ActivityStreamsException {

    ActivityStream message = createMessageForKulturobjektDokument(kod,action,compressed,actor,beschreibungsRepository);

    send(message);

    return message;
  }

}
