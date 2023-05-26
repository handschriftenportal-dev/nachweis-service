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

import static javax.transaction.Status.STATUS_ACTIVE;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 12.02.21
 */
public class KafkaTransactionalProducer {

  public static final KafkaLoggingCallback KAFKA_LOGGING_CALLBACK = new KafkaLoggingCallback();
  final static Logger logger = LoggerFactory.getLogger(KafkaTransactionalProducer.class);
  private static final Map<String, KafkaProducer<String, ActivityStream>> activeProducers = new ConcurrentHashMap<>();

  TransactionManager transactionManager;

  String topic;

  String bootstrapserver;

  String groupid;

  int transactionTimeout;

  KafkaTransactionalProducer(TransactionManager transactionManager, String topic,
      String bootstrapserver,
      String groupid, int transactionTimeout) {
    this.transactionManager = transactionManager;
    this.topic = topic;
    this.bootstrapserver = bootstrapserver;
    this.groupid = groupid;
    this.transactionTimeout = transactionTimeout;
  }

  @PreDestroy
  public void destroy() {
    for (Producer<String, ActivityStream> producer : activeProducers.values()) {
      CompletableFuture.runAsync(() -> producer.close(Duration.ofSeconds(15l)));
    }
  }


  @Transactional(rollbackOn = {Exception.class})
  protected void send(ActivityStream message) {

    if (Objects.nonNull(message)) {
      logger.info("Sending message to kafka topic {} with id {} ", topic, message.getId());
      String txId = getTransactionId();

      KafkaProducer<String, ActivityStream> producer = getProducer(txId);

      final ProducerRecord<String, ActivityStream> record = new ProducerRecord<>(topic,
          message.getId(), message);

      producer.send(record, KAFKA_LOGGING_CALLBACK);
    }
  }

  String beginTransaction() {
    return beginTransaction(UUID.randomUUID().toString());
  }

  String beginTransaction(String txId) {
    KafkaProducer<String, ActivityStream> producer = getProducer(txId);
    producer.beginTransaction();
    return txId;
  }

  void commitTransaction(String txId) throws Exception {
    KafkaProducer<String, ActivityStream> producer = getProducer(txId);
    producer.commitTransaction();
    closeProducer(txId);
  }

  public void rollbackTransaction(String txId) throws Exception {
    KafkaProducer<String, ActivityStream> producer = getProducer(txId);
    producer.abortTransaction();
    closeProducer(txId);
  }

  private void closeProducer(String txId) throws Exception {
    KafkaProducer<String, ActivityStream> producer = activeProducers.remove(txId);
    if (producer != null) {
      producer.close(Duration.ofSeconds(15L));
    }
  }

  public String getTopic() {
    return topic;
  }

  public String getBootstrapserver() {
    return bootstrapserver;
  }

  public String getGroupid() {
    return groupid;
  }

  private KafkaProducer<String, ActivityStream> getProducer(String txId) {
    String producerTransactionKey = String.valueOf(txId);
    if (!activeProducers.containsKey(producerTransactionKey)) {
      Properties producerProperties = new Properties();
      producerProperties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapserver);
      producerProperties.setProperty(ProducerConfig.CLIENT_ID_CONFIG, groupid + producerTransactionKey);
      producerProperties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
          StringSerializer.class.getName());
      producerProperties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
          KafkaActivityStreamMessageSerializer.class.getName());
      producerProperties.setProperty(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "500000000");
      producerProperties.setProperty(ProducerConfig.BUFFER_MEMORY_CONFIG, "500000000");
      producerProperties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
      producerProperties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
      producerProperties
          .setProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG, producerTransactionKey);
      producerProperties
          .setProperty(ProducerConfig.RETRIES_CONFIG, String.valueOf(Integer.MAX_VALUE));
      producerProperties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

      // Batch producing at compression to  encrease the throuthput
      // Without Batch sending 1000 TEI XML in 90 Seconds
      // With Batch sending 1000 TEI XML in 120 Seconds :(???
      // Active MQ sending 1000 TEI XML in 4 Minutes

      producerProperties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
      producerProperties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "30");
      producerProperties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32 * 1024));
      producerProperties.setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG,
          String.valueOf(Duration.ofSeconds(transactionTimeout).toMillis()));
      producerProperties.setProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG,
          String.valueOf(Duration.ofSeconds(900).toMillis()));

      KafkaProducer<String, ActivityStream> producer = new KafkaProducer<>(producerProperties);
      producer.initTransactions();
      addProducer(producerTransactionKey, producer);
      logger.debug("Created kafka producer with transactionalId={} {}", producerTransactionKey, producerProperties.entrySet().stream()
          .map(objectObjectEntry -> objectObjectEntry.getKey() + ":" + objectObjectEntry.getValue())
          .collect(Collectors.joining(";")));
    }
    return activeProducers.get(producerTransactionKey);
  }

  @Transactional
  String getTransactionId() {
    try {
      Transaction jtaTransaction = transactionManager.getTransaction();
      String kafkaTransactionId = KafkaProducerXAResource.getTransactionId(jtaTransaction);
      if (!activeProducers.containsKey(kafkaTransactionId)) {
        KafkaProducerXAResource xaResource = new KafkaProducerXAResource(this);
        if (jtaTransaction.getStatus() == STATUS_ACTIVE) {
          jtaTransaction.enlistResource(xaResource);
          logger.info("KafkaProducerXAResource for transactionID= {} registered.",
              kafkaTransactionId);
        } else {
          logger.warn(
              "Unable to register KafkaProducerXAResource for transactionID= {} due to transaction status={}",
              kafkaTransactionId, jtaTransaction.getStatus());
        }
      }
      return kafkaTransactionId;
    } catch (Exception e) {
      throw new RuntimeException("Unable to enlistResource " + e.getMessage(), e);
    }
  }

  void addProducer(String txId, KafkaProducer<String, ActivityStream> producer) {
    activeProducers.put(txId, producer);
  }

  public int getTransactionTimeout() {
    return transactionTimeout;
  }

  public void setTransactionTimeout(int transactionTimeout) {
    this.transactionTimeout = transactionTimeout;
  }
}
