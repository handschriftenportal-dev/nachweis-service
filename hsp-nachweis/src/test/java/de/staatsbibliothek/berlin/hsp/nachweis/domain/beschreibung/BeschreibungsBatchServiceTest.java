package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BeschreibungsBatchServiceTest {

  private KulturObjektDokumentRepository kodRepository;
  private BeschreibungsRepository beschreibungsRepository;
  private SuchDokumentService suchDokumentService;
  private KafkaIndexingProducer kafkaIndexingProducer;
  private BeschreibungsBatchService service;

  @BeforeEach
  void setUp() {
    kodRepository = mock(KulturObjektDokumentRepository.class);
    beschreibungsRepository = mock(BeschreibungsRepository.class);
    suchDokumentService = mock(SuchDokumentService.class);
    kafkaIndexingProducer = mock(KafkaIndexingProducer.class);
    service = new BeschreibungsBatchService(
        kodRepository, beschreibungsRepository, suchDokumentService, kafkaIndexingProducer);
  }

  @Test
  void publizierenBatch_happyPath_persistsAndIndexesAllItems() throws Exception {
    KulturObjektDokument kod = kodWithBeschreibung("KOD-B1", "B1");
    Beschreibung beschreibung = beschreibung("B1", "KOD-B1");

    service.publizierenBatch(List.of(kod), List.of(beschreibung));

    verify(kodRepository).saveAll(List.of(kod));
    verify(beschreibungsRepository).saveAll(List.of(beschreibung));
    verify(suchDokumentService).allKODsUebernehmen(List.of(kod));
    verify(suchDokumentService).allBeschreibungenUebernehmen(List.of(beschreibung));
    verify(kafkaIndexingProducer).sendKulturobjektDokumentAsActivityStreamMessage(
        eq(kod), eq(ActivityStreamAction.UPDATE), eq(true), eq("System"));
  }

  @Test
  void publizierenBatch_kodWithoutTei_skipsKafkaButStillPersists() throws Exception {
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("KOD-NOTEI")
        .build();
    kod.getBeschreibungenIDs().add("B2");
    Beschreibung beschreibung = beschreibung("B2", "KOD-NOTEI");

    service.publizierenBatch(List.of(kod), List.of(beschreibung));

    verify(kodRepository).saveAll(List.of(kod));
    verify(beschreibungsRepository).saveAll(List.of(beschreibung));
    verify(suchDokumentService).allKODsUebernehmen(List.of(kod));
    verify(suchDokumentService).allBeschreibungenUebernehmen(List.of(beschreibung));
    verify(kafkaIndexingProducer, never()).sendKulturobjektDokumentAsActivityStreamMessage(
        any(), any(), anyBoolean(), anyString());
  }

  @Test
  void publizierenBatch_kodWithoutBeschreibungen_skipsKafka() throws Exception {
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("KOD-EMPTY")
        .withTEIXml("<xml/>")
        .build();

    service.publizierenBatch(List.of(kod), List.of());

    verify(kodRepository).saveAll(List.of(kod));
    verify(beschreibungsRepository).saveAll(List.of());
    verify(kafkaIndexingProducer, never()).sendKulturobjektDokumentAsActivityStreamMessage(
        any(), any(), anyBoolean(), anyString());
  }

  @Test
  void publizierenBatch_emptyBatch_noInteractionsBeyondEmptySaves() throws Exception {
    service.publizierenBatch(List.of(), List.of());

    verify(kodRepository).saveAll(List.of());
    verify(beschreibungsRepository).saveAll(List.of());
    verify(suchDokumentService).allKODsUebernehmen(List.of());
    verify(suchDokumentService).allBeschreibungenUebernehmen(List.of());
    verify(kafkaIndexingProducer, never()).sendKulturobjektDokumentAsActivityStreamMessage(
        any(), any(), anyBoolean(), anyString());
  }

  @Test
  void publizierenBatch_kafkaFailure_propagates() throws Exception {
    KulturObjektDokument kod = kodWithBeschreibung("KOD-FAIL", "B3");
    Beschreibung beschreibung = beschreibung("B3", "KOD-FAIL");

    doThrow(new RuntimeException("kafka down"))
        .when(kafkaIndexingProducer).sendKulturobjektDokumentAsActivityStreamMessage(
            any(), any(), anyBoolean(), anyString());

    assertThrows(RuntimeException.class,
        () -> service.publizierenBatch(List.of(kod), List.of(beschreibung)));

    verify(kodRepository).saveAll(List.of(kod));
    verify(beschreibungsRepository).saveAll(List.of(beschreibung));
  }

  @Test
  void publizierenBatch_multipleKods_sendsOneKafkaMessagePerKod() throws Exception {
    KulturObjektDokument kod1 = kodWithBeschreibung("KOD-M1", "B-M1");
    KulturObjektDokument kod2 = kodWithBeschreibung("KOD-M2", "B-M2");
    Beschreibung b1 = beschreibung("B-M1", "KOD-M1");
    Beschreibung b2 = beschreibung("B-M2", "KOD-M2");

    when(kafkaIndexingProducer.sendKulturobjektDokumentAsActivityStreamMessage(
        any(), any(), anyBoolean(), anyString())).thenReturn(null);

    service.publizierenBatch(List.of(kod1, kod2), List.of(b1, b2));

    verify(kafkaIndexingProducer).sendKulturobjektDokumentAsActivityStreamMessage(
        eq(kod1), eq(ActivityStreamAction.UPDATE), eq(true), eq("System"));
    verify(kafkaIndexingProducer).sendKulturobjektDokumentAsActivityStreamMessage(
        eq(kod2), eq(ActivityStreamAction.UPDATE), eq(true), eq("System"));
  }

  /**
   * Pins partial-batch-failure semantics: when Kafka throws on the second KOD, the first KOD was
   * already sent via the transactional producer. Because {@code KafkaIndexingProducer} is
   * transactional, both sends participate in the JTA transaction — the rollback on failure also
   * discards the first send at broker commit time. This test documents the send-order and
   * propagation contract (rollback safety relies on the producer being transactional).
   */
  @Test
  void publizierenBatch_kafkaFailsOnSecondKod_firstAlreadySentThenPropagates() throws Exception {
    KulturObjektDokument kod1 = kodWithBeschreibung("KOD-P1", "B-P1");
    KulturObjektDokument kod2 = kodWithBeschreibung("KOD-P2", "B-P2");
    Beschreibung b1 = beschreibung("B-P1", "KOD-P1");
    Beschreibung b2 = beschreibung("B-P2", "KOD-P2");

    when(kafkaIndexingProducer.sendKulturobjektDokumentAsActivityStreamMessage(
        eq(kod1), any(), anyBoolean(), anyString())).thenReturn(null);
    doThrow(new RuntimeException("kafka down on kod2"))
        .when(kafkaIndexingProducer).sendKulturobjektDokumentAsActivityStreamMessage(
            eq(kod2), any(), anyBoolean(), anyString());

    assertThrows(RuntimeException.class,
        () -> service.publizierenBatch(List.of(kod1, kod2), List.of(b1, b2)));

    // first KOD send was attempted before the failure on the second
    verify(kafkaIndexingProducer).sendKulturobjektDokumentAsActivityStreamMessage(
        eq(kod1), eq(ActivityStreamAction.UPDATE), eq(true), eq("System"));
    verify(kafkaIndexingProducer).sendKulturobjektDokumentAsActivityStreamMessage(
        eq(kod2), eq(ActivityStreamAction.UPDATE), eq(true), eq("System"));
    // DB saves happened before the Kafka loop → both ran, @Transactional rollback discards them
    verify(kodRepository).saveAll(List.of(kod1, kod2));
    verify(beschreibungsRepository).saveAll(List.of(b1, b2));
  }

  private KulturObjektDokument kodWithBeschreibung(String kodId, String beschreibungId) {
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId(kodId)
        .withTEIXml("<xml/>")
        .build();
    kod.getBeschreibungenIDs().add(beschreibungId);
    return kod;
  }

  private Beschreibung beschreibung(String id, String kodId) {
    Identifikation identifikation = new IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withIdent("1-1-1")
        .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
        .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
        .build();
    return new BeschreibungsBuilder(id, kodId, identifikation)
        .withTEIXml("<xml/>")
        .build();
  }
}
