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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.MockedTransactionManager;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.transaction.SystemException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 13.09.2019.
 */
public class KafkaIndexingProducerTest extends MockedTransactionManager {

  private BeschreibungsRepository beschreibungsRepository = Mockito.mock(BeschreibungsRepository.class);
  @Test
  public void testGivenValuesCreation() {

    KafkaIndexingProducer producer = new KafkaIndexingProducer(transactionManagerMOCK,"index",
        "localhost:9092,localhost:9092,localhost:9092", "nachweis", 900,beschreibungsRepository);

    Assertions.assertNotNull(producer);

  }

  @Test
  public void testGivenNullSend() {

    KafkaIndexingProducer producer = new KafkaIndexingProducer(transactionManagerMOCK,"index",
        "localhost:9092,localhost:9092,localhost:9092", "nachweis", 900, beschreibungsRepository);

    String txId = "testTxId";
    KafkaProducer<String, ActivityStream> kafkaProducer = mock(KafkaProducer.class);

    producer.addProducer(txId, kafkaProducer);

    producer.send(null, null);

    verify(kafkaProducer, times(0)).send(any());
  }

  @Test
  public void testGivenActivityMessageSend()
      throws ExecutionException, InterruptedException, TimeoutException, ActivityStreamsException, SystemException {

    when(xidImpleMOCK.getTransactionUid()).thenReturn(uid);
    when(transactionManagerMOCK.getTransaction()).thenReturn(transactionMOCK);
    when(transactionMOCK.getTxId()).thenReturn(xidImpleMOCK);

    KafkaIndexingProducer producer = new KafkaIndexingProducer(transactionManagerMOCK, "index",
        "localhost:9092,localhost:9092,localhost:9092", "nachweis", 900,beschreibungsRepository);

    ActivityStream message = ActivityStream
        .builder()
        .withId("1")
        .withType(ActivityStreamAction.ADD)
        .withActorName("konrad")
        .build();

    KafkaProducer<String, ActivityStream> kafkaProducer = mock(KafkaProducer.class);

    CompletableFuture<RecordMetadata> record = Mockito.mock(CompletableFuture.class);

    when(record.get(10, TimeUnit.SECONDS)).thenReturn(null);
    when(record.isDone()).thenReturn(true);


    producer.addProducer(txId, kafkaProducer);

    BeschreibungImport beschreibungImport1 = new BeschreibungImport(ImportStatus.OFFEN, message);

    producer.send(message, beschreibungImport1);
  }

  @Test
  void testsSendKulturobjektDokumentAsActivityStreamMessage()
      throws ActivityStreamsException, ExecutionException, InterruptedException, TimeoutException {
    KafkaIndexingProducer producer = new KafkaIndexingProducer(transactionManagerMOCK, "index",
        "localhost:9092,localhost:9092,localhost:9092", "nachweis", 900,beschreibungsRepository);
    KulturObjektDokument kulturObjektDokument = new KulturObjektDokumentBuilder().withId("KOD1")
        .withTEIXml("<xml></xml>")
        .build();
    Beschreibung external = new BeschreibungsBuilder().withId("BESCHREIBUNG1").withVerwaltungsTyp(
        VerwaltungsTyp.EXTERN).withTEIXml("<xml></xml>").build();
    Beschreibung internal= new BeschreibungsBuilder().withId("BESCHREIBUNG2").withVerwaltungsTyp(
        VerwaltungsTyp.INTERN).withTEIXml("<xml></xml>").build();
    kulturObjektDokument.addBeschreibungsdokument(external.getId());
    kulturObjektDokument.addBeschreibungsdokument(internal.getId());
    when(beschreibungsRepository.findByIdOptional(external.getId())).thenReturn(
        Optional.of(external));
    when(beschreibungsRepository.findByIdOptional(internal.getId())).thenReturn(
        Optional.of(internal));

    KafkaProducer<String, ActivityStream> kafkaProducer = mock(KafkaProducer.class);
    CompletableFuture<RecordMetadata> record = Mockito.mock(CompletableFuture.class);
    when(record.get(10, TimeUnit.SECONDS)).thenReturn(null);
    when(record.isDone()).thenReturn(true);
    producer.addProducer(txId, kafkaProducer);

    ActivityStream message = producer.sendKulturobjektDokumentAsActivityStreamMessage(kulturObjektDokument,ActivityStreamAction.ADD,true,"Konrad");

    assertEquals(ActivityStreamAction.ADD,message.getAction());
    assertTrue(message.getObjects().get(0).isCompressed());
    assertEquals(2, message.getObjects().size());
    assertEquals("Konrad",message.getActor().getName());

    verify(kafkaProducer,times(1)).send(any(),any());
  }
}
