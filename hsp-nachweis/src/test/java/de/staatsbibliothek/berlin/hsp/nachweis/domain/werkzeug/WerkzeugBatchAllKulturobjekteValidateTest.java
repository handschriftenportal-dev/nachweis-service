package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjekteValidationReportItem;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import io.quarkus.test.junit.QuarkusTest;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WerkzeugBatchAllKulturobjekteValidateTest {

  private KulturObjektDokumentBoundary kodBoundary;
  private BearbeiterBoundary bearbeiterBoundary;
  private WerkzeugBatchAllKulturobjekteValidate kulturobjekteValidate;

  @BeforeEach
  void setUp() {
    kodBoundary = mock(KulturObjektDokumentBoundary.class);
    bearbeiterBoundary = mock(BearbeiterBoundary.class);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(new Bearbeiter("1", "Test"));

    kulturobjekteValidate = new WerkzeugBatchAllKulturobjekteValidate(kodBoundary, bearbeiterBoundary);
  }

  @Test
  void execute_keineUngueltigen_keinDownload() {
    when(kodBoundary.findAllInvalid()).thenReturn(Set.of());

    kulturobjekteValidate.execute();

    assertNotNull(kulturobjekteValidate.getBatch());
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING,
        kulturobjekteValidate.getBatch().getStatus());
    assertEquals(WerkzeugBatchAllKulturobjekteValidate.MSG_ALL_VALID,
        kulturobjekteValidate.getBatch().getSuccessMessage());
    assertNull(kulturobjekteValidate.getBatch().getDownload());

    verify(kodBoundary, times(1)).findAllInvalid();
  }

  @Test
  void execute_hatUngueltige_erzeugtCsvMitBomUndHeader() {
    var item = KulturObjekteValidationReportItem.builder()
        .kulturobjektHspID("HSP-123")
        .signatur("Dep. Erf., CA 4° 319")
        .bestandsHaltendeEinrichtung("Universitätsbibliothek Leipzig")
        .ort("Leipzig")
        .validationResult(new ValidationResult(false, null, null, "Fehler"))
        .build();

    when(kodBoundary.findAllInvalid()).thenReturn(Set.of(item));

    kulturobjekteValidate.execute();

    assertNotNull(kulturobjekteValidate.getBatch());
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING,
        kulturobjekteValidate.getBatch().getStatus());
    assertEquals(WerkzeugBatchAllKulturobjekteValidate.MSG_SOME_INVALID,
        kulturobjekteValidate.getBatch().getSuccessMessage());

    byte[] dl = kulturobjekteValidate.getBatch().getDownload();
    assertNotNull(dl);
    String csv = new String(dl, StandardCharsets.UTF_8);

    assertTrue(csv.startsWith("\uFEFF"), "CSV muss mit UTF-8 BOM beginnen");
    assertTrue(csv.contains("HSP-ID-Kulturobjekt;Signatur;Bestandshaltende Einrichtung;Ort;Fehlermeldung"),
        "CSV-Header fehlt/ist falsch");

    assertTrue(csv.contains("HSP-123"));
    assertTrue(csv.contains("Dep. Erf., CA 4° 319"));
    assertTrue(csv.contains("Universitätsbibliothek Leipzig"));
    assertTrue(csv.contains("Leipzig"));
    assertTrue(csv.contains("Fehler"));

    verify(kodBoundary, times(1)).findAllInvalid();
  }

  @Test
  void execute_wennFindAllInvalidWirft_setztFailUndKeinDownload() {
    when(kodBoundary.findAllInvalid())
        .thenThrow(new RuntimeException("IMPORTSERVICE down"));

    kulturobjekteValidate.execute();

    assertNotNull(kulturobjekteValidate.getBatch());
    assertEquals(WerkzeugBatchJobStatus.ERROR,
        kulturobjekteValidate.getBatch().getStatus(),
        "Bei Exception muss der Batch in ERROR enden");
    assertEquals(WerkzeugBatchAllKulturobjekteValidate.MSG_FAILED,
        kulturobjekteValidate.getBatch().getSuccessMessage(),
        "Fehlermeldung sollte als Key gesetzt werden");
    assertNull(kulturobjekteValidate.getBatch().getDownload(),
        "Bei Fehlern darf kein Download erzeugt werden");

    verify(kodBoundary, times(1)).findAllInvalid();
  }

  @Test
  void execute_finallySetztFailWennNochRunning() {
    when(kodBoundary.findAllInvalid())
        .thenAnswer(inv -> { throw new AssertionError("Simulierter schwerer Fehler"); });

    AssertionError err = assertThrows(
        AssertionError.class,
        () -> kulturobjekteValidate.execute()
    );
    assertTrue(err.getMessage().contains("Simulierter schwerer Fehler"));

    assertNotNull(kulturobjekteValidate.getBatch());
    assertEquals(WerkzeugBatchJobStatus.ERROR,
        kulturobjekteValidate.getBatch().getStatus());
    assertEquals(WerkzeugBatchAllKulturobjekteValidate.MSG_ABORTED,
        kulturobjekteValidate.getBatch().getSuccessMessage());
    assertNull(kulturobjekteValidate.getBatch().getDownload());

    verify(kodBoundary, times(1)).findAllInvalid();
  }

  @Test
  void startAndDispatch_startetBatchWennNichtRunning() {
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING,
        kulturobjekteValidate.getBatch().getStatus());

    kulturobjekteValidate.startAndDispatch();

    assertNotNull(kulturobjekteValidate.getBatch().getBearbeiter());
    assertEquals("Test", kulturobjekteValidate.getBatch().getBearbeiter().getName());
  }

  @Test
  void startAndDispatch_startetNichtWennBereitsRunning() {
    var existing = new Bearbeiter("77", "AlreadyRunning");
    kulturobjekteValidate.getBatch().start(existing);

    kulturobjekteValidate.startAndDispatch();

    assertEquals("AlreadyRunning",
        kulturobjekteValidate.getBatch().getBearbeiter().getName());
  }
}
