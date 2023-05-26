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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.MockedTransactionManager;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.transaction.SystemException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 02.03.22
 */
class KafkaKatalogProducerTest extends MockedTransactionManager {

  @Test
  void testCreation() {

    KafkaKatalogProducer kafkaKatalogProducer = new KafkaKatalogProducer(transactionManagerMOCK, "katalog-index",
        "localhost:9092", "katalog", 900);

    assertNotNull(kafkaKatalogProducer);
    assertEquals("katalog-index", kafkaKatalogProducer.getTopic());
    assertEquals("localhost:9092", kafkaKatalogProducer.getBootstrapserver());
    assertEquals("katalog", kafkaKatalogProducer.getGroupid());
  }

  @Test
  void testSendingMessage() throws ActivityStreamsException, SystemException {

    when(xidImpleMOCK.getTransactionUid()).thenReturn(uid);
    when(transactionManagerMOCK.getTransaction()).thenReturn(transactionMOCK);
    when(transactionMOCK.getTxId()).thenReturn(xidImpleMOCK);

    KafkaKatalogProducer kafkaKatalogProducer = new KafkaKatalogProducer(transactionManagerMOCK, "katalog-index",
        "localhost:9092", "katalog", 900);

    KafkaProducer<String, ActivityStream> producer = (KafkaProducer<String, ActivityStream>) Mockito
        .mock(KafkaProducer.class);

    kafkaKatalogProducer.addProducer(txId, producer);

    ActivityStreamObject uploadObject = ActivityStreamObject.builder()
        .withCompressed(true)
        .withType(ActivityStreamsDokumentTyp.KATALOG)
        .withContent("Katalog")
        .build();

    ActivityStream message = ActivityStream
        .builder()
        .withId(ActivityStreamsDokumentTyp.KATALOG.name())
        .withType(ActivityStreamAction.ADD)
        .withPublished(LocalDateTime.now())
        .withActorName("Konrad Eichstädt")
        .addObject(uploadObject)
        .build();

    kafkaKatalogProducer.send(message);

    verify(producer).send(any(), any());
  }

  @Test
  void testsSendKatalogAsActivityStreamMessage()
      throws ActivityStreamsException, ExecutionException, InterruptedException, TimeoutException {
    KafkaKatalogProducer kafkaKatalogProducer = new KafkaKatalogProducer(transactionManagerMOCK, "katalog-index",
        "localhost:9092", "katalog", 900);
    Katalog katalog = new KatalogBuilder().withId("KOD1")
        .withTEIXML("<xml></xml>")
        .build();

    KafkaProducer<String, ActivityStream> kafkaProducer = mock(KafkaProducer.class);
    CompletableFuture<RecordMetadata> record = Mockito.mock(CompletableFuture.class);
    when(record.get(10, TimeUnit.SECONDS)).thenReturn(null);
    when(record.isDone()).thenReturn(true);
    kafkaKatalogProducer.addProducer(txId, kafkaProducer);

    ActivityStream message = kafkaKatalogProducer.sendKatalogAsActivityStreamMessage(katalog,ActivityStreamAction.ADD,true,"Konrad");

    assertEquals(ActivityStreamAction.ADD,message.getAction());
    assertTrue(message.getObjects().get(0).isCompressed());
    assertEquals(1, message.getObjects().size());
    assertEquals("Konrad",message.getActor().getName());

    verify(kafkaProducer,times(1)).send(any(),any());
  }
}
