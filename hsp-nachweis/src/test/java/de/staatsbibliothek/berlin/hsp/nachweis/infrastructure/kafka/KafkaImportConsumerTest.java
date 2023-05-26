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

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaImportConsumer.POLL_DURATION;
import static org.mockito.Mockito.mock;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.digitalisatimport.DigitalisatImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.katalogimport.KatalogImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodimport.KulturObjektDokumentImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgangBoundary;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.enterprise.event.Event;
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
public class KafkaImportConsumerTest {

  private Event<BeschreibungImport> neueBeschreibungImporteEvent = (Event<BeschreibungImport>) mock(
      Event.class);

  private Event<DigitalisatImport> neueDigitalisatImporteEvent = (Event<DigitalisatImport>) mock(
      Event.class);

  private Event<KulturObjektDokumentImport> neueKulturObjektDokumentImporteEvent =
      (Event<KulturObjektDokumentImport>) mock(Event.class);

  private Event<KatalogImport> neueKatalogImporteEvent = (Event<KatalogImport>) mock(Event.class);

  private Consumer<String, ActivityStream> consumer = mock(KafkaConsumer.class);

  private ManagedExecutor managedExecutor = ManagedExecutor.builder().build();

  private ActivityStream importBeschreibungMessage;

  private ActivityStream importDigitalisatMessage;

  private ActivityStream importKODMessage;

  private ActivityStream importKatalogMessage;

  private ImportVorgangBoundary importVorgangBoundary = mock(ImportVorgangBoundary.class);

  @BeforeEach
  public void setUp() throws Exception {

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("40170459")
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
  public void testCreationConsumer() {

    KafkaImportConsumer kafkaImportConsumer = new KafkaImportConsumer("MyTopic", "localhost:9092",
        "nachweis", -1l, 1, neueBeschreibungImporteEvent,
        neueDigitalisatImporteEvent, neueKulturObjektDokumentImporteEvent, neueKatalogImporteEvent,
        managedExecutor, mock(BearbeiterBoundary.class),importVorgangBoundary);

    Assertions.assertEquals("MyTopic", kafkaImportConsumer.getTopic());

    Assertions.assertEquals("nachweis", kafkaImportConsumer.getGroupId());

    Assertions.assertEquals("localhost:9092", kafkaImportConsumer.getBootstrapServer());

    Assertions.assertTrue(kafkaImportConsumer.isRunning());

    Assertions.assertNull(kafkaImportConsumer.getConsumer());
  }

  @Test
  public void test_GivenSetup_CreateConsumer() {

    KafkaImportConsumer kafkaImportConsumer = new KafkaImportConsumer("MyTopic", "localhost:9092",
        "nachweis", -1l, 1, neueBeschreibungImporteEvent,
        neueDigitalisatImporteEvent, neueKulturObjektDokumentImporteEvent, neueKatalogImporteEvent,
        managedExecutor, mock(BearbeiterBoundary.class),importVorgangBoundary);

    kafkaImportConsumer.createConsumer();

    Assertions.assertNotNull(kafkaImportConsumer.getConsumer());
  }

  @Test
  public void testGivenSetup_ConsumeBeschreibungsImportMessage()
      throws ExecutionException, InterruptedException {

    KafkaImportConsumer kafkaImportConsumer = new KafkaImportConsumer("MyTopic", "localhost:9092",
        "nachweis", -1l, 1, neueBeschreibungImporteEvent,
        neueDigitalisatImporteEvent, neueKulturObjektDokumentImporteEvent, neueKatalogImporteEvent,
        managedExecutor, mock(BearbeiterBoundary.class),importVorgangBoundary);

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
  public void testGivenSetup_ConsumeDigitalisatImportMessage()
      throws ExecutionException, InterruptedException {

    KafkaImportConsumer kafkaImportConsumer = new KafkaImportConsumer("MyTopic", "localhost:9092",
        "nachweis", -1l, 1, neueBeschreibungImporteEvent,
        neueDigitalisatImporteEvent, neueKulturObjektDokumentImporteEvent, neueKatalogImporteEvent,
        managedExecutor, mock(BearbeiterBoundary.class),importVorgangBoundary);

    kafkaImportConsumer.setConsumer(consumer);

    Mockito.when(consumer.poll(POLL_DURATION))
        .thenReturn(createConsumerRecords(1, importDigitalisatMessage));

    CompletableFuture<Void> cf1 = CompletableFuture
        .runAsync(() -> kafkaImportConsumer.startConsumer());

    Thread.sleep(100);

    kafkaImportConsumer.stopConsumer();

    cf1.get();

    Mockito.verify(consumer, Mockito.atLeast(1)).poll(POLL_DURATION);
  }

  @Test
  public void testGivenSetup_ConsumeKODImportMessage()
      throws ExecutionException, InterruptedException {

    KafkaImportConsumer kafkaImportConsumer = new KafkaImportConsumer("MyTopic", "localhost:9092",
        "nachweis", -1l, 1, neueBeschreibungImporteEvent,
        neueDigitalisatImporteEvent, neueKulturObjektDokumentImporteEvent, neueKatalogImporteEvent,
        managedExecutor, mock(BearbeiterBoundary.class),importVorgangBoundary);

    kafkaImportConsumer.setConsumer(consumer);

    Mockito.when(consumer.poll(POLL_DURATION))
        .thenReturn(createConsumerRecords(1, importKODMessage));

    CompletableFuture<Void> cf1 = CompletableFuture
        .runAsync(() -> kafkaImportConsumer.startConsumer());

    Thread.sleep(100);

    kafkaImportConsumer.stopConsumer();

    cf1.get();

    Mockito.verify(consumer, Mockito.atLeast(1)).poll(POLL_DURATION);
  }

  @Test
  public void testGivenSetup_ConsumeKatalogImportMessage()
      throws ExecutionException, InterruptedException {

    KafkaImportConsumer kafkaImportConsumer = new KafkaImportConsumer("MyTopic", "localhost:9092",
        "nachweis", -1l, 1, neueBeschreibungImporteEvent,
        neueDigitalisatImporteEvent, neueKulturObjektDokumentImporteEvent, neueKatalogImporteEvent,
        managedExecutor, mock(BearbeiterBoundary.class),importVorgangBoundary);

    kafkaImportConsumer.setConsumer(consumer);

    Mockito.when(consumer.poll(POLL_DURATION))
        .thenReturn(createConsumerRecords(1, importKatalogMessage));

    CompletableFuture<Void> cf1 = CompletableFuture
        .runAsync(() -> kafkaImportConsumer.startConsumer());

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
