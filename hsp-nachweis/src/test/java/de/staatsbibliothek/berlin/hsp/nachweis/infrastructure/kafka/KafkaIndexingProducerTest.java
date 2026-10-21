package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaTransactionalProducer.KAFKA_LOGGING_CALLBACK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.MockedTransactionManager;
import jakarta.transaction.SystemException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 13.09.2019.
 */
@Execution(ExecutionMode.SAME_THREAD)
class KafkaIndexingProducerTest extends MockedTransactionManager {

  private static final Identifikation IDENTIFIKATION =
      new IdentifikationBuilder()
          .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
          .withIdent("1-1-1")
          .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
          .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
          .build();

  private final BeschreibungsRepository beschreibungsRepository =
      mock(BeschreibungsRepository.class);

  @Test
  void testGivenValuesCreation() {

    KafkaIndexingProducer producer =
        new KafkaIndexingProducer(
            transactionManagerMOCK,
            "index",
            "localhost:9092,localhost:9092,localhost:9092",
            "de/staatsbibliothek/berlin/hsp/nachweis",
            900,
            beschreibungsRepository);

    assertNotNull(producer);
  }

  @Test
  void testGivenNullSend() {

    KafkaIndexingProducer producer =
        new KafkaIndexingProducer(
            transactionManagerMOCK,
            "index",
            "localhost:9092,localhost:9092,localhost:9092",
            "de/staatsbibliothek/berlin/hsp/nachweis",
            900,
            beschreibungsRepository);

    String txId = "testTxId";
    KafkaProducer<String, ActivityStream> kafkaProducer = mock(KafkaProducer.class);

    producer.addProducer(txId, kafkaProducer);

    producer.send(null, null);

    verify(kafkaProducer, times(0)).send(any());
  }

  @Test
  void testGivenActivityMessageSend()
      throws ExecutionException,
      InterruptedException,
      TimeoutException,
      ActivityStreamsException,
      SystemException {

    when(xidImpleMOCK.getTransactionUid()).thenReturn(uid);
    when(transactionManagerMOCK.getTransaction()).thenReturn(transactionMOCK);
    when(transactionMOCK.getTxId()).thenReturn(xidImpleMOCK);

    KafkaIndexingProducer producer =
        new KafkaIndexingProducer(
            transactionManagerMOCK,
            "index",
            "localhost:9092,localhost:9092,localhost:9092",
            "de/staatsbibliothek/berlin/hsp/nachweis",
            900,
            beschreibungsRepository);

    ActivityStream message =
        ActivityStream.builder()
            .withId("1")
            .withType(ActivityStreamAction.ADD)
            .withActorName("konrad")
            .build();

    KafkaProducer<String, ActivityStream> kafkaProducer = mock(KafkaProducer.class);

    CompletableFuture<RecordMetadata> record = mock(CompletableFuture.class);

    when(record.get(10, TimeUnit.SECONDS)).thenReturn(null);
    when(record.isDone()).thenReturn(true);

    producer.addProducer(txId, kafkaProducer);

    BeschreibungImport beschreibungImport1 = new BeschreibungImport(ImportStatus.OFFEN, message);

    producer.send(message, beschreibungImport1);
  }

  @Test
  void testsSendKulturobjektDokumentAsActivityStreamMessage()
      throws ActivityStreamsException {
    KafkaIndexingProducer kafkaIndexingProducer =
        new KafkaIndexingProducer(
            transactionManagerMOCK,
            "index",
            "localhost:9092,localhost:9092,localhost:9092",
            "de/staatsbibliothek/berlin/hsp/nachweis",
            900,
            beschreibungsRepository);
    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokumentBuilder().withId("KOD1").withTEIXml("<xml></xml>").build();
    Publikation publikation = new Publikation.PublikationBuilder()
        .withPublikationsDatumHSP(LocalDateTime.now())
        .addPURL(new PURL(
            URI.create("http://purl.test/1"),
            URI.create("http://dynamic.test/1"),
            PURLTyp.EXTERNAL))
        .build();
    publikation.onApproved();
    Beschreibung external =
        new BeschreibungsBuilder("BESCHREIBUNG1", "KOD1", IDENTIFIKATION)
            .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
            .withPublikation(publikation)
            .withTEIXml("<xml></xml>")
            .build();
    Beschreibung internal =
        new BeschreibungsBuilder("BESCHREIBUNG2", "KOD1", IDENTIFIKATION)
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .withTEIXml("<xml></xml>")
            .build();
    kulturObjektDokument.addBeschreibungsdokument(external.getId());
    kulturObjektDokument.addBeschreibungsdokument(internal.getId());
    when(beschreibungsRepository.findAllById(any()))
        .thenReturn(List.of(external, internal));

    KafkaProducer<String, ActivityStream> producer =
        (KafkaProducer<String, ActivityStream>) mock(KafkaProducer.class);

    kafkaIndexingProducer.addProducer(txId, producer);

    ActivityStream message =
        kafkaIndexingProducer.sendKulturobjektDokumentAsActivityStreamMessage(
            kulturObjektDokument, ActivityStreamAction.ADD, true, "Konrad");

    assertEquals(ActivityStreamAction.ADD, message.getAction());
    assertTrue(message.getObjects().getFirst().isCompressed());
    assertEquals(2, message.getObjects().size());
    assertEquals("Konrad", message.getActor().getName());

    verify(producer, times(1)).send(any(), eq(KAFKA_LOGGING_CALLBACK));
  }

  @Test
  void testsendRemoveAllAsActivityStreamMessage()
      throws ExecutionException, InterruptedException, TimeoutException, ActivityStreamsException {
    KafkaIndexingProducer producer =
        new KafkaIndexingProducer(
            transactionManagerMOCK,
            "index",
            "localhost:9092,localhost:9092,localhost:9092",
            "de/staatsbibliothek/berlin/hsp/nachweis",
            900,
            beschreibungsRepository);

    KafkaProducer<String, ActivityStream> kafkaProducer = mock(KafkaProducer.class);
    CompletableFuture<RecordMetadata> record = mock(CompletableFuture.class);
    when(record.get(10, TimeUnit.SECONDS)).thenReturn(null);
    when(record.isDone()).thenReturn(true);
    producer.addProducer(txId, kafkaProducer);

    ActivityStream message = producer.sendRemoveAllAsActivityStreamMessage("Konrad");

    assertNotNull(message);
    assertEquals(ActivityStreamAction.REMOVE_ALL, message.getAction());
    assertEquals(1, message.getObjects().size());
    assertEquals("Konrad", message.getActor().getName());
  }

  @Test
  void testSendingKatalogMessage() throws ActivityStreamsException, SystemException {

    when(xidImpleMOCK.getTransactionUid()).thenReturn(uid);
    when(transactionManagerMOCK.getTransaction()).thenReturn(transactionMOCK);
    when(transactionMOCK.getTxId()).thenReturn(xidImpleMOCK);

    KafkaIndexingProducer kafkaIndexingProducer =
        new KafkaIndexingProducer(
            transactionManagerMOCK,
            "katalog-index",
            "localhost:9092",
            "katalog",
            900,
            beschreibungsRepository);

    KafkaProducer<String, ActivityStream> producer =
        (KafkaProducer<String, ActivityStream>) mock(KafkaProducer.class);

    kafkaIndexingProducer.addProducer(txId, producer);

    Katalog katalog = new Katalog.KatalogBuilder().withId("k1").withTEIXML("<xml></xml>").build();

    kafkaIndexingProducer.sendKatalogAsActivityStreamMessage(
        katalog, ActivityStreamAction.ADD, true, "Konrad Eichstädt");

    verify(producer, times(1)).send(any(), eq(KAFKA_LOGGING_CALLBACK));
  }
}
