package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogValidationReportItem;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import io.quarkus.test.junit.QuarkusTest;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WerkzeugBatchAllKatalogeValidateTest {

  private KatalogBoundary katalogBoundary;
  private BearbeiterBoundary bearbeiterBoundary;
  private WerkzeugBatchAllKatalogeValidate katalogeValidate;

  @BeforeEach
  void setUp() {
    katalogBoundary = mock(KatalogBoundary.class);
    bearbeiterBoundary = mock(BearbeiterBoundary.class);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(new Bearbeiter("1", "Test"));

    katalogeValidate = new WerkzeugBatchAllKatalogeValidate(katalogBoundary, bearbeiterBoundary);
  }

  @Test
  void execute_keineUngueltigen_keinDownload() {
    when(katalogBoundary.findAllInvalid()).thenReturn(Set.of());

    katalogeValidate.execute();

    assertNotNull(katalogeValidate.getBatch());
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING,
        katalogeValidate.getBatch().getStatus());
    assertEquals(WerkzeugBatchAllKatalogeValidate.MSG_ALL_VALID,
        katalogeValidate.getBatch().getSuccessMessage());
    assertNull(katalogeValidate.getBatch().getDownload());

    verify(katalogBoundary, times(1)).findAllInvalid();
  }

  @Test
  void execute_hatUngueltige_erzeugtCsvMitBomUndHeader() {
    var item = KatalogValidationReportItem.builder()
        .hspId("HSP-123")
        .titel("Katalogtitel, 23")
        .publikationsjahr("2024")
        .validationResult(new ValidationResult(false, null, null, "Fehler"))
        .build();

    when(katalogBoundary.findAllInvalid()).thenReturn(Set.of(item));

    katalogeValidate.execute();

    assertNotNull(katalogeValidate.getBatch());
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING,
        katalogeValidate.getBatch().getStatus());
    assertEquals(WerkzeugBatchAllKatalogeValidate.MSG_SOME_INVALID,
        katalogeValidate.getBatch().getSuccessMessage());

    byte[] dl = katalogeValidate.getBatch().getDownload();
    assertNotNull(dl);
    String csv = new String(dl, StandardCharsets.UTF_8);

    assertTrue(csv.startsWith("\uFEFF"), "CSV muss mit UTF-8 BOM beginnen");
    assertTrue(csv.contains("HSP-ID;Titel;Publikationsjahr;Fehlermeldung"), "CSV-Header fehlt/ist falsch");
    assertTrue(csv.contains("HSP-123"));
    assertTrue(csv.contains("Katalogtitel, 23"));
    assertTrue(csv.contains("2024"));
    assertTrue(csv.contains("Fehler"));

    verify(katalogBoundary, times(1)).findAllInvalid();
  }

  @Test
  void execute_wennFindAllInvalidWirft_setztFailUndKeinDownload() {
    when(katalogBoundary.findAllInvalid())
        .thenThrow(new RuntimeException("IMPORTSERVICE down"));

    katalogeValidate.execute();

    assertNotNull(katalogeValidate.getBatch());
    assertEquals(WerkzeugBatchJobStatus.ERROR,
        katalogeValidate.getBatch().getStatus(),
        "Bei Exception muss der Batch in ERROR enden");
    assertEquals(WerkzeugBatchAllKatalogeValidate.MSG_FAILED,
        katalogeValidate.getBatch().getSuccessMessage(),
        "Fehlermeldung sollte als Key gesetzt werden");
    assertNull(katalogeValidate.getBatch().getDownload(),
        "Bei Fehlern darf kein Download erzeugt werden");

    verify(katalogBoundary, times(1)).findAllInvalid();
  }

  @Test
  void execute_finallySetztFailWennNochRunning() {
    when(katalogBoundary.findAllInvalid())
        .thenAnswer(inv -> { throw new AssertionError("Simulierter schwerer Fehler"); });

    AssertionError err = assertThrows(
        AssertionError.class,
        () -> katalogeValidate.execute()
    );
    assertTrue(err.getMessage().contains("Simulierter schwerer Fehler"));

    assertNotNull(katalogeValidate.getBatch());
    assertEquals(WerkzeugBatchJobStatus.ERROR,
        katalogeValidate.getBatch().getStatus());
    assertEquals(WerkzeugBatchAllKatalogeValidate.MSG_ABORTED,
        katalogeValidate.getBatch().getSuccessMessage());
    assertNull(katalogeValidate.getBatch().getDownload());

    verify(katalogBoundary, times(1)).findAllInvalid();
  }

  @Test
  void startAndDispatch_startetBatchWennNichtRunning() {
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING,
        katalogeValidate.getBatch().getStatus());

    katalogeValidate.startAndDispatch();

    assertNotNull(katalogeValidate.getBatch().getBearbeiter());
    assertEquals("Test", katalogeValidate.getBatch().getBearbeiter().getName());
  }

  @Test
  void startAndDispatch_startetBatchNichtNeuWennSchonRunning() {
    Bearbeiter existing = new Bearbeiter("99", "AlreadyRunning");
    katalogeValidate.getBatch().start(existing);

    katalogeValidate.startAndDispatch();

    assertEquals("AlreadyRunning",
        katalogeValidate.getBatch().getBearbeiter().getName());
  }
}
