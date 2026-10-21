package de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre;

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp.BESCHREIBUNG;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp.KOD;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusTest
class SperrenCleanupSchedulerTest {

  private final String TRANSACTION_SPERRE_ID = "Sperre_Transaction";
  private final String MANUAL_SPERRE_ID = "Sperre_MANUAL";
  private final String MANUAL_SPERRE_ID2 = "Sperre_MANUAL2";
  private final String TX_ID = "TX_ID_1";
  private final Set<SperreEintrag> GESPERRT_TRANSACTION =
      Set.of(new SperreEintrag("KOD_1", KOD), new SperreEintrag("BES_1_1", BESCHREIBUNG));
  private final Set<SperreEintrag> GESPERRT_MANUAL =
      Set.of(new SperreEintrag("KOD_2", KOD), new SperreEintrag("BES_2_1", BESCHREIBUNG));
  private final Set<SperreEintrag> GESPERRT_TRANSACTION_ONLY_KOD =
      Set.of(new SperreEintrag("KOD_1", KOD), new SperreEintrag("BES_1_2", BESCHREIBUNG));
  private final Set<SperreEintrag> GESPERRT_MANUAL_ONLY_KOD =
      Set.of(new SperreEintrag("KOD_2", KOD), new SperreEintrag("BES_2_2", BESCHREIBUNG));
  private final Set<SperreEintrag> NICHT_GESPERRT =
      Set.of(new SperreEintrag("KOD_3", KOD), new SperreEintrag("BES_3_1", BESCHREIBUNG));
  private final Bearbeiter unbekannt = new Bearbeiter("1", "Unbekannter Benutzer");
  Logger log = LoggerFactory.getLogger(SperrenCleanupSchedulerTest.class);
  @Inject
  SperreRepository sperreRepository;
  @Inject
  BearbeiterRepository bearbeiterRepository;
  @Inject
  SperrenCleanupScheduler scheduler;

  @BeforeEach
  @Transactional(TxType.REQUIRES_NEW)
  void setup() {
    bearbeiterRepository.saveAndFlush(unbekannt);

    Sperre sperreTransaction =
        Sperre.newBuilder()
            .withId(TRANSACTION_SPERRE_ID)
            .withBearbeiter(unbekannt)
            .withTxId(TX_ID)
            .withSperreGrund("Unit-Test")
            .withSperreTyp(SperreTyp.IN_TRANSACTION)
            .withStartDatum(LocalDateTime.now().minusMinutes(20))
            .withSperreEintraege(GESPERRT_TRANSACTION)
            .build();

    sperreRepository.saveAndFlush(sperreTransaction);

    Sperre sperreManual =
        Sperre.newBuilder()
            .withId(MANUAL_SPERRE_ID)
            .withBearbeiter(unbekannt)
            .withSperreGrund("Unit-Test")
            .withSperreTyp(SperreTyp.MANUAL)
            .withStartDatum(LocalDateTime.now().minusMinutes(30))
            .withSperreEintraege(GESPERRT_MANUAL)
            .build();

    sperreRepository.saveAndFlush(sperreManual);

    Sperre sperreManual2 =
        Sperre.newBuilder()
            .withId(MANUAL_SPERRE_ID2)
            .withBearbeiter(unbekannt)
            .withSperreGrund("Unit-Test")
            .withSperreTyp(SperreTyp.MANUAL)
            .withStartDatum(LocalDateTime.now().minusMinutes(35))
            .withSperreEintraege(GESPERRT_MANUAL)
            .build();

    sperreRepository.saveAndFlush(sperreManual2);
  }

  @AfterEach
  @Transactional(TxType.REQUIRES_NEW)
  void cleanup() {
    sperreRepository.deleteByIdAndFlush(TRANSACTION_SPERRE_ID);
    sperreRepository.deleteByIdAndFlush(MANUAL_SPERRE_ID);
    sperreRepository.deleteByIdAndFlush(MANUAL_SPERRE_ID2);
    bearbeiterRepository.deleteByIdAndFlush(unbekannt.getId());
  }

  @Test
  @TestTransaction
  void cleanupSperren() {
    sperreRepository.findAll().stream().forEach(
        s -> log.info("Sperre in DB: {} - {} - {}", s.getId(), s.getSperreTyp(),
            s.getStartDatum()));
    assertEquals(3, sperreRepository.findAll().count());
    scheduler.cleanupSperren();
    assertEquals(1, sperreRepository.findAll().count());
  }
}
