package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkeyupdate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.KulturObjektSortkeyUpdateReport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyImportFailure;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyImportStateService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyImportStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SortkeyImportStateServiceTest {

  private static final String BEARBEITER = "b-1";
  private static final String ANDERER_BEARBEITER = "b-2";

  private SortkeyImportStateService stateService;

  @BeforeEach
  void setUp() {
    stateService = new SortkeyImportStateService();
  }

  @Test
  void shouldDeliverReportOnlyToStartingBearbeiter() {

    KulturObjektSortkeyUpdateReport report =
        new KulturObjektSortkeyUpdateReport(3, List.of(), List.of());

    assertTrue(stateService.tryStartImport(BEARBEITER));
    stateService.finishSuccessfully(report);

    assertNull(stateService.consumeReport(ANDERER_BEARBEITER));
    assertEquals(SortkeyImportStatus.SUCCESS, stateService.getStatus());

    assertSame(report, stateService.consumeReport(BEARBEITER));
    assertEquals(SortkeyImportStatus.IDLE, stateService.getStatus());

    assertNull(stateService.consumeReport(BEARBEITER));
  }

  @Test
  void shouldDeliverFailureOnlyToStartingBearbeiter() {

    assertTrue(stateService.tryStartImport(BEARBEITER));
    stateService.finishWithError(800);

    assertNull(stateService.consumeReport(BEARBEITER));
    assertEquals(SortkeyImportStatus.FAILED, stateService.getStatus());

    assertNull(stateService.consumeFailure(ANDERER_BEARBEITER));
    assertEquals(SortkeyImportStatus.FAILED, stateService.getStatus());

    SortkeyImportFailure failure = stateService.consumeFailure(BEARBEITER);
    assertNotNull(failure);
    assertEquals(800, failure.updatedCountBeforeFailure());
    assertTrue(failure.isPartial());
    assertEquals(SortkeyImportStatus.IDLE, stateService.getStatus());

    assertNull(stateService.consumeFailure(BEARBEITER));
  }

  @Test
  void shouldReportNonPartialFailure() {

    assertTrue(stateService.tryStartImport(BEARBEITER));
    stateService.finishWithError(0);

    SortkeyImportFailure failure = stateService.consumeFailure(BEARBEITER);
    assertNotNull(failure);
    assertFalse(failure.isPartial());
  }

  @Test
  void shouldKeepRunningStateOnConsume() {

    assertTrue(stateService.tryStartImport(BEARBEITER));

    assertNull(stateService.consumeReport(BEARBEITER));
    assertNull(stateService.consumeFailure(BEARBEITER));
    assertEquals(SortkeyImportStatus.RUNNING, stateService.getStatus());
    assertTrue(stateService.isRunning());

    assertFalse(stateService.tryStartImport(ANDERER_BEARBEITER));
  }

  @Test
  void shouldKeepIdleStateOnConsume() {

    assertNull(stateService.consumeReport(BEARBEITER));
    assertNull(stateService.consumeFailure(BEARBEITER));
    assertEquals(SortkeyImportStatus.IDLE, stateService.getStatus());
  }

  @Test
  void shouldRecordStartTime() {

    assertNull(stateService.getStartTime());

    stateService.tryStartImport(BEARBEITER);
    assertNotNull(stateService.getStartTime());

    stateService.finishSuccessfully(
        new KulturObjektSortkeyUpdateReport(0, List.of(), List.of()));
    stateService.consumeReport(BEARBEITER);

    assertNull(stateService.getStartTime());
  }
}
