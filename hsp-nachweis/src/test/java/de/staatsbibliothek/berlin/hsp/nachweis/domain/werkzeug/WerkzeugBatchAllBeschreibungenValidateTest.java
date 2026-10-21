package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungValidationReportItem;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import io.quarkus.test.junit.QuarkusTest;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 08.04.24.
 * @version 1.0
 */

@QuarkusTest
public class WerkzeugBatchAllBeschreibungenValidateTest {

  private BeschreibungsBoundary beschreibungsBoundary;
  private BearbeiterBoundary bearbeiterBoundary;
  private WerkzeugBatchAllBeschreibungenValidate sut;

  @BeforeEach
  void setUp() {
    beschreibungsBoundary = mock(BeschreibungsBoundary.class);
    bearbeiterBoundary = mock(BearbeiterBoundary.class);

    when(bearbeiterBoundary.getLoggedBearbeiter())
        .thenReturn(new Bearbeiter("1", "Test"));

    sut = new WerkzeugBatchAllBeschreibungenValidate(beschreibungsBoundary, bearbeiterBoundary);
  }

  @Test
  void odd_noInvalid_returnsAllValid_andNoCsv() {
    when(beschreibungsBoundary.findAllInvalidODD()).thenReturn(Set.of());

    sut.executeInternalForTests(WerkzeugBatchAllBeschreibungenValidate.Mode.ODD);

    var batch = sut.getBatchOdd();
    assertNotNull(batch);
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING, batch.getStatus());
    assertEquals(WerkzeugBatchAllBeschreibungenValidate.MSG_ODD_ALL_VALID,
        batch.getSuccessMessage());
    assertNull(batch.getDownload());
  }

  @Test
  void odd_hasInvalid_returnsSeveralInvalid_andCsv() {
    when(beschreibungsBoundary.findAllInvalidODD()).thenReturn(Set.of(
        BeschreibungValidationReportItem.builder()
            .beschreibungsID("HSP1")
            .nameBesitzer("Staabi")
            .nameAufbewahrungsort("Berlin")
            .signatur("Ms.1")
            .validationResult(new ValidationResult(false, "", "", "Fehler"))
            .build()
    ));

    sut.executeInternalForTests(WerkzeugBatchAllBeschreibungenValidate.Mode.ODD);

    var batch = sut.getBatchOdd();
    assertNotNull(batch);
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING, batch.getStatus());
    assertEquals(WerkzeugBatchAllBeschreibungenValidate.MSG_ODD_SOME_INVALID,
        batch.getSuccessMessage());

    byte[] dl = batch.getDownload();
    assertNotNull(dl);
    String csv = new String(dl, StandardCharsets.UTF_8);

    assertTrue(csv.startsWith("\uFEFF"), "CSV muss mit UTF-8 BOM beginnen");
    assertTrue(csv.contains("Fehler"), "CSV sollte die Fehlermeldung enthalten");
  }

  @Test
  void odd_serviceThrows_setsError_andNoCsv() {
    when(beschreibungsBoundary.findAllInvalidODD())
        .thenThrow(new RuntimeException("IMPORTSERVICE down"));

    sut.executeInternalForTests(WerkzeugBatchAllBeschreibungenValidate.Mode.ODD);

    var batch = sut.getBatchOdd();
    assertNotNull(batch);
    assertEquals(WerkzeugBatchJobStatus.ERROR, batch.getStatus());
    assertEquals(WerkzeugBatchAllBeschreibungenValidate.MSG_ODD_FAILED,
        batch.getSuccessMessage());
    assertNull(batch.getDownload());
  }

  @Test
  void all_noInvalid_returnsAllValid_andNoCsv() {
    when(beschreibungsBoundary.findAllInvalidALL()).thenReturn(Set.of());

    sut.executeInternalForTests(WerkzeugBatchAllBeschreibungenValidate.Mode.ALL);

    var batch = sut.getBatchAll();
    assertNotNull(batch);
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING, batch.getStatus());
    assertEquals(WerkzeugBatchAllBeschreibungenValidate.MSG_ALL_ALL_VALID,
        batch.getSuccessMessage());
    assertNull(batch.getDownload());
  }

  @Test
  void all_hasInvalid_returnsSeveralInvalid_andCsv() {
    when(beschreibungsBoundary.findAllInvalidALL()).thenReturn(Set.of(
        BeschreibungValidationReportItem.builder()
            .beschreibungsID("HSP2")
            .nameBesitzer("SBB")
            .nameAufbewahrungsort("Berlin")
            .signatur("Ms.2")
            .validationResult(new ValidationResult(false, "", "", "Fehler-ALL"))
            .build()
    ));

    sut.executeInternalForTests(WerkzeugBatchAllBeschreibungenValidate.Mode.ALL);

    var batch = sut.getBatchAll();
    assertNotNull(batch);
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING, batch.getStatus());
    assertEquals(WerkzeugBatchAllBeschreibungenValidate.MSG_ALL_SOME_INVALID,
        batch.getSuccessMessage());

    byte[] dl = batch.getDownload();
    assertNotNull(dl);
    String csv = new String(dl, StandardCharsets.UTF_8);
    assertTrue(csv.startsWith("\uFEFF"), "CSV muss mit UTF-8 BOM beginnen");
    assertTrue(csv.contains("Fehler-ALL"));
  }

  @Test
  void all_serviceThrows_setsError_andNoCsv() {
    when(beschreibungsBoundary.findAllInvalidALL())
        .thenThrow(new RuntimeException("IMPORTSERVICE down"));

    sut.executeInternalForTests(WerkzeugBatchAllBeschreibungenValidate.Mode.ALL);

    var batch = sut.getBatchAll();
    assertNotNull(batch);
    assertEquals(WerkzeugBatchJobStatus.ERROR, batch.getStatus());
    assertEquals(WerkzeugBatchAllBeschreibungenValidate.MSG_ALL_FAILED,
        batch.getSuccessMessage());
    assertNull(batch.getDownload());
  }

  @Test
  void startAndDispatchODD_startetBatchWennNichtRunning() {
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING,
        sut.getBatchOdd().getStatus());

    sut.startAndDispatchODD();

    assertNotNull(sut.getBatchOdd().getBearbeiter());
    assertEquals("Test", sut.getBatchOdd().getBearbeiter().getName());
  }

  @Test
  void startAndDispatchODD_startetNichtNeuWennSchonRunning() {
    Bearbeiter existing = new Bearbeiter("42", "AlreadyRunning");
    sut.getBatchOdd().start(existing);

    sut.startAndDispatchODD();

    assertEquals("AlreadyRunning",
        sut.getBatchOdd().getBearbeiter().getName());
  }

  @Test
  void startAndDispatchALL_startetBatchWennNichtRunning() {
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING,
        sut.getBatchAll().getStatus());

    sut.startAndDispatchALL();

    assertNotNull(sut.getBatchAll().getBearbeiter());
    assertEquals("Test", sut.getBatchAll().getBearbeiter().getName());
  }

  @Test
  void startAndDispatchALL_startetNichtNeuWennSchonRunning() {
    Bearbeiter existing = new Bearbeiter("99", "AlreadyRunningALL");
    sut.getBatchAll().start(existing);

    sut.startAndDispatchALL();

    assertEquals("AlreadyRunningALL",
        sut.getBatchAll().getBearbeiter().getName());
  }
}
