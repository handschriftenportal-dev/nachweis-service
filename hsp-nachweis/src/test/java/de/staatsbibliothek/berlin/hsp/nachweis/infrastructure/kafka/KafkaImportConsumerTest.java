package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaImportConsumer.POLL_DURATION;
import static org.mockito.Mockito.mock;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportVorgangBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.digitalisatimport.DigitalisatImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.katalogimport.KatalogImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodimport.KulturObjektDokumentImport;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.event.Event;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 05.06.2019.
 */
@ExtendWith(MockitoExtension.class)
@QuarkusTest
@TestTransaction
class KafkaImportConsumerTest {

  private static final Identifikation IDENTIFIKATION = new IdentifikationBuilder()
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .withIdent("1-1-1")
      .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
      .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
      .build();
  private final Event<BeschreibungImport> neueBeschreibungImporteEvent = (Event<BeschreibungImport>) mock(
      Event.class);
  private final Event<DigitalisatImport> neueDigitalisatImporteEvent = (Event<DigitalisatImport>) mock(
      Event.class);
  private final Event<KulturObjektDokumentImport> neueKulturObjektDokumentImporteEvent =
      (Event<KulturObjektDokumentImport>) mock(Event.class);
  private final Event<KatalogImport> neueKatalogImporteEvent = (Event<KatalogImport>) mock(
      Event.class);
  private final Consumer<String, ActivityStream> consumer = mock(KafkaConsumer.class);
  private final ManagedExecutor managedExecutor = ManagedExecutor.builder().build();
  private final ImportVorgangBoundary importVorgangBoundary = mock(ImportVorgangBoundary.class);
  private ActivityStream importBeschreibungMessage;
  private ActivityStream importDigitalisatMessage;
  private ActivityStream importKODMessage;
  private ActivityStream importKatalogMessage;

  @BeforeEach
  public void setUp() throws Exception {

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("40170459", "1-1-1",
        IDENTIFIKATION)
        .build();

    ActivityStreamObject activityStreamObjectBeschreibung = ActivityStreamObject
        .builder()
        .withType(ActivityStreamsDokumentTyp.BESCHREIBUNG)
        .withId(beschreibung.getId())
        .build();

    ActivityStreamObject activityStreamObjectDigitalisat = ActivityStreamObject
        .builder()
        .withType(ActivityStreamsDokumentTyp.DIGITALISAT)
        .build();

    ActivityStreamObject activityStreamObjectKOD = ActivityStreamObject
        .builder()
        .withType(ActivityStreamsDokumentTyp.KOD)
        .build();

    ActivityStreamObject activityStreamObjectKatalog = ActivityStreamObject
        .builder()
        .withType(ActivityStreamsDokumentTyp.KATALOG)
        .build();

    importBeschreibungMessage = ActivityStream.builder()
        .withId("Berlin_test_4_190405.fmt.xml")
        .withType(ActivityStreamAction.ADD)
        .withPublished(LocalDateTime.now())
        .withActorName("Konrad Eichstädt")
        .addObject(activityStreamObjectBeschreibung)
        .build();

    importDigitalisatMessage = ActivityStream.builder()
        .withId("tei_digitalisat_test_4_190405.fmt.xml")
        .withType(ActivityStreamAction.ADD)
        .withPublished(LocalDateTime.now())
        .withActorName("Konrad Eichstädt")
        .addObject(activityStreamObjectDigitalisat)
        .build();

    importKODMessage = ActivityStream.builder()
        .withId("tei_kulturobjektdokument_test_4_190405.fmt.xml")
        .withType(ActivityStreamAction.ADD)
        .withPublished(LocalDateTime.now())
        .withActorName("Konrad Eichstädt")
        .addObject(activityStreamObjectKOD)
        .build();

    importKatalogMessage = ActivityStream.builder()
        .withId("tei_katalog_test_4_190405.fmt.xml")
        .withType(ActivityStreamAction.ADD)
        .withPublished(LocalDateTime.now())
        .withActorName("Konrad Eichstädt")
        .addObject(activityStreamObjectKatalog)
        .build();
  }

  @Test
  void testCreationConsumer() {

    KafkaImportConsumer kafkaImportConsumer = new KafkaImportConsumer("MyTopic", "localhost:9092",
        "de/staatsbibliothek/berlin/hsp/nachweis", -1L, 1, neueBeschreibungImporteEvent,
        neueDigitalisatImporteEvent, neueKulturObjektDokumentImporteEvent, neueKatalogImporteEvent,
        managedExecutor, mock(BearbeiterBoundary.class), importVorgangBoundary);

    Assertions.assertEquals("MyTopic", kafkaImportConsumer.getTopic());

    Assertions.assertEquals("de/staatsbibliothek/berlin/hsp/nachweis", kafkaImportConsumer.getGroupId());

    Assertions.assertEquals("localhost:9092", kafkaImportConsumer.getBootstrapServer());

    Assertions.assertTrue(kafkaImportConsumer.isRunning());

    Assertions.assertNull(kafkaImportConsumer.getConsumer());
  }

  @Test
  void test_GivenSetup_CreateConsumer() {

    KafkaImportConsumer kafkaImportConsumer = new KafkaImportConsumer("MyTopic", "localhost:9092",
        "de/staatsbibliothek/berlin/hsp/nachweis", -1L, 1, neueBeschreibungImporteEvent,
        neueDigitalisatImporteEvent, neueKulturObjektDokumentImporteEvent, neueKatalogImporteEvent,
        managedExecutor, mock(BearbeiterBoundary.class), importVorgangBoundary);

    kafkaImportConsumer.createConsumer();

    Assertions.assertNotNull(kafkaImportConsumer.getConsumer());
  }

  @Test
  void testGivenSetup_ConsumeBeschreibungsImportMessage()
      throws ExecutionException, InterruptedException {

    KafkaImportConsumer kafkaImportConsumer = new KafkaImportConsumer("MyTopic", "localhost:9092",
        "de/staatsbibliothek/berlin/hsp/nachweis", -1L, 1, neueBeschreibungImporteEvent,
        neueDigitalisatImporteEvent, neueKulturObjektDokumentImporteEvent, neueKatalogImporteEvent,
        managedExecutor, mock(BearbeiterBoundary.class), importVorgangBoundary);

    kafkaImportConsumer.setConsumer(consumer);

    Mockito.when(consumer.poll(POLL_DURATION))
        .thenReturn(createConsumerRecords(1, importBeschreibungMessage));

    CompletableFuture<Void> cf1 = CompletableFuture
        .runAsync(kafkaImportConsumer::startConsumer);

    Thread.sleep(100);

    kafkaImportConsumer.stopConsumer();

    cf1.get();

    Mockito.verify(consumer, Mockito.atLeast(1)).poll(POLL_DURATION);
  }

  @Test
  void testGivenSetup_ConsumeDigitalisatImportMessage()
      throws ExecutionException, InterruptedException {

    KafkaImportConsumer kafkaImportConsumer = new KafkaImportConsumer("MyTopic", "localhost:9092",
        "de/staatsbibliothek/berlin/hsp/nachweis", -1L, 1, neueBeschreibungImporteEvent,
        neueDigitalisatImporteEvent, neueKulturObjektDokumentImporteEvent, neueKatalogImporteEvent,
        managedExecutor, mock(BearbeiterBoundary.class), importVorgangBoundary);

    kafkaImportConsumer.setConsumer(consumer);

    Mockito.when(consumer.poll(POLL_DURATION))
        .thenReturn(createConsumerRecords(1, importDigitalisatMessage));

    CompletableFuture<Void> cf1 = CompletableFuture
        .runAsync(kafkaImportConsumer::startConsumer);

    Thread.sleep(100);

    kafkaImportConsumer.stopConsumer();

    cf1.get();

    Mockito.verify(consumer, Mockito.atLeast(1)).poll(POLL_DURATION);
  }

  @Test
  void testGivenSetup_ConsumeKODImportMessage()
      throws ExecutionException, InterruptedException {

    KafkaImportConsumer kafkaImportConsumer = new KafkaImportConsumer("MyTopic", "localhost:9092",
        "de/staatsbibliothek/berlin/hsp/nachweis", -1L, 1, neueBeschreibungImporteEvent,
        neueDigitalisatImporteEvent, neueKulturObjektDokumentImporteEvent, neueKatalogImporteEvent,
        managedExecutor, mock(BearbeiterBoundary.class), importVorgangBoundary);

    kafkaImportConsumer.setConsumer(consumer);

    Mockito.when(consumer.poll(POLL_DURATION))
        .thenReturn(createConsumerRecords(1, importKODMessage));

    CompletableFuture<Void> cf1 = CompletableFuture
        .runAsync(kafkaImportConsumer::startConsumer);

    Thread.sleep(100);

    kafkaImportConsumer.stopConsumer();

    cf1.get();

    Mockito.verify(consumer, Mockito.atLeast(1)).poll(POLL_DURATION);
  }

  @Test
  void testGivenSetup_ConsumeKatalogImportMessage()
      throws ExecutionException, InterruptedException {

    KafkaImportConsumer kafkaImportConsumer = new KafkaImportConsumer("MyTopic", "localhost:9092",
        "de/staatsbibliothek/berlin/hsp/nachweis", -1L, 1, neueBeschreibungImporteEvent,
        neueDigitalisatImporteEvent, neueKulturObjektDokumentImporteEvent, neueKatalogImporteEvent,
        managedExecutor, mock(BearbeiterBoundary.class), importVorgangBoundary);

    kafkaImportConsumer.setConsumer(consumer);

    Mockito.when(consumer.poll(POLL_DURATION))
        .thenReturn(createConsumerRecords(1, importKatalogMessage));

    CompletableFuture<Void> cf1 = CompletableFuture
        .runAsync(kafkaImportConsumer::startConsumer);

    Thread.sleep(100);

    kafkaImportConsumer.stopConsumer();

    cf1.get();

    Mockito.verify(consumer, Mockito.atLeast(1)).poll(POLL_DURATION);
  }

  private ConsumerRecords createConsumerRecords(final int count, ActivityStream message) {
    final String topic = "MyTopic";
    final int partition = 0;

    final Map<TopicPartition, List<ConsumerRecord>> recordsMap = new HashMap<>();
    final TopicPartition topicPartition = new TopicPartition(topic, partition);
    final List<ConsumerRecord> consumerRecords = new ArrayList<>();

    for (int x = 0; x < count; x++) {
      consumerRecords.add(
          new ConsumerRecord(topic, partition, x, "Key", message)
      );
    }
    recordsMap.put(topicPartition, consumerRecords);

    return new ConsumerRecords(recordsMap);
  }
}
