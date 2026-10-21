package de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 01.09.21
 */
@QuarkusTest
@TestTransaction
@TestInstance(Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
class DokumentSperreServiceTest extends SperreCommon {

  @Inject
  DokumentSperreBoundary boundary;

  @Inject
  BearbeiterRepository bearbeiterRepository;

  Bearbeiter michael = new Bearbeiter("2", "Michael");
  Bearbeiter konrad = new Bearbeiter("3", "Konrad");

  @BeforeEach
  @Transactional(TxType.REQUIRES_NEW)
  void setup() {
    Arrays.asList(michael, konrad).forEach(b -> bearbeiterRepository.saveAndFlush(b));
  }

  @AfterEach
  @Transactional(TxType.REQUIRES_NEW)
  void cleanup() {
    Arrays.asList(michael, konrad).forEach(b -> bearbeiterRepository.deleteByIdAndFlush(b.getId()));
  }

  @Test
  void testAcuireManualSperreWithoutBearbeiter() {
    assertThrows(
        DokumentSperreException.class,
        () -> {
          boundary.acquireSperre(null, SperreTyp.MANUAL, "Test", new SperreEintrag[0]);
        });
  }

  @ParameterizedTest
  @Transactional()
  @ValueSource(strings = {"2"})
  void testAcuireManuelSperreWithConflict(String bearbeiterID) throws DokumentSperreException {
    int start = new Random().nextInt();
    final SperreEintrag sperreEintrag = new SperreEintrag("ckm" + start,
        SperreDokumentTyp.BESCHREIBUNG);

    Sperre alreadyExistingSperre =
        new Sperre.SperreBuilder()
            .withSperreTyp(SperreTyp.MANUAL)
            .withBearbeiter(konrad)
            .withSperreGrund("Test")
            .addSperreEintrag(sperreEintrag)
            .build();

    ((DokumentSperreService) boundary).saveSperreInNewTransaction(alreadyExistingSperre);

    if ("2".equals(bearbeiterID)) {
      assertThrows(
          SperreAlreadyExistException.class,
          () -> {
            boundary.acquireSperre(
                michael,
                SperreTyp.MANUAL,
                "Test",
                Arrays.asList(sperreEintrag).toArray(new SperreEintrag[1]));
          });
    } else {

      Sperre newSperre =
          boundary.acquireSperre(
              konrad,
              SperreTyp.MANUAL,
              "Test",
              Arrays.asList(sperreEintrag).toArray(new SperreEintrag[1]));
      assertNotNull(newSperre);

      assertEquals(alreadyExistingSperre, newSperre);
    }

    ((DokumentSperreService) boundary).deleteSperreInNewTransaction(alreadyExistingSperre.getId());
  }

  @Test
  @Transactional
  void testAcquireReleaseSperreManual()
      throws SperreAlreadyExistException, DokumentSperreException {
    Sperre sperre = getTestSperre(bearbeiterRepository, SperreTyp.MANUAL);

    ((DokumentSperreService) boundary).saveSperreInNewTransaction(sperre);

    boundary.releaseSperre(sperre);

    assertTrue(
        boundary.findAll().stream()
            .filter(s -> s.getId().equals(sperre.getId()))
            .findFirst()
            .isEmpty());
  }

  @RepeatedTest(10)
  @Transactional()
  void testAcquireReleaseSperreInTransaction()
      throws SperreAlreadyExistException, DokumentSperreException {
    Sperre sperre = getTestSperre(bearbeiterRepository, SperreTyp.IN_TRANSACTION);
    ArrayList<SperreEintrag> sperreEintraege = new ArrayList<>(sperre.getSperreEintraege());

    assertFalse(boundary.isGesperrt(sperreEintraege.get(0)));

    Sperre acquiredSperre =
        boundary.acquireSperre(
            michael,
            sperre.getSperreTyp(),
            "Unittest",
            sperreEintraege.toArray(new SperreEintrag[0]));

    assertNotNull(acquiredSperre);

    assertTrue(boundary.isGesperrt(sperreEintraege.get(0)));
  }
}
