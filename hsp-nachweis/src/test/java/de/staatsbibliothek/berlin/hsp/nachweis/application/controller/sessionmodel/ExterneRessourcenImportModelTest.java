package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.FILE_EMPTY;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.ROW_INVALID;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.UNEXPECTED_ERROR;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResult.Status.FEHLER;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResult;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportStateService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportStatus;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExterneRessourcenImportModelTest {

  private static final String BEARBEITER_ID = "b-1";
  private static final byte[] CONTENT = "content".getBytes(StandardCharsets.UTF_8);
  private ExterneRessourcenImportService externeRessourcenImportService;
  private ExterneRessourcenImportStateService stateService;
  private BearbeiterBoundary bearbeiterBoundary;
  private ExterneRessourcenImportModel model;

  @BeforeEach
  void setUp() {
    externeRessourcenImportService = mock(ExterneRessourcenImportService.class);
    stateService = new ExterneRessourcenImportStateService();
    bearbeiterBoundary = mock(BearbeiterBoundary.class);
    when(bearbeiterBoundary.getLoggedBearbeiter())
        .thenReturn(new Bearbeiter(BEARBEITER_ID, "Tester"));

    model = new ExterneRessourcenImportModel(
        externeRessourcenImportService,
        stateService,
        bearbeiterBoundary,
        null);
  }

  @Test
  void shouldImportAndStoreResults() throws Exception {
    List<ExterneRessourcenImportResult> results = List.of(failedResult());
    when(externeRessourcenImportService.importieren(CONTENT)).thenReturn(results);

    assertTrue(model.startImport(CONTENT));

    assertEquals(ExterneRessourcenImportStatus.SUCCESS, stateService.getStatus(BEARBEITER_ID));
    assertEquals(results, stateService.getResults(BEARBEITER_ID));
    assertEquals(
        ExterneRessourcenImportModel.RESULT_FILE_NAME,
        stateService.getResultFileName(BEARBEITER_ID));
  }

  @Test
  void shouldFinishWithImportError() throws Exception {
    when(externeRessourcenImportService.importieren(CONTENT))
        .thenThrow(new ExterneRessourcenImportException(FILE_EMPTY, "details"));

    assertTrue(model.startImport(CONTENT));

    assertEquals(ExterneRessourcenImportStatus.FAILED, stateService.getStatus(BEARBEITER_ID));
    assertEquals(FILE_EMPTY, stateService.getErrorMessageKey(BEARBEITER_ID));
    assertArrayEquals(new Object[]{"details"}, stateService.getErrorMessageArgs(BEARBEITER_ID));
  }

  @Test
  void shouldFinishWithUnexpectedError() throws Exception {
    when(externeRessourcenImportService.importieren(CONTENT))
        .thenThrow(new RuntimeException("unexpected"));

    assertTrue(model.startImport(CONTENT));

    assertEquals(ExterneRessourcenImportStatus.FAILED, stateService.getStatus(BEARBEITER_ID));
    assertEquals(
        UNEXPECTED_ERROR,
        stateService.getErrorMessageKey(BEARBEITER_ID));
  }

  @Test
  void shouldFinishWithErrorAndRethrowFatalError() throws Exception {
    AssertionError fatalError = new AssertionError("fatal");
    when(externeRessourcenImportService.importieren(CONTENT)).thenThrow(fatalError);

    AssertionError thrown = assertThrows(AssertionError.class, () -> model.startImport(CONTENT));

    assertEquals(fatalError, thrown);
    assertEquals(ExterneRessourcenImportStatus.FAILED, stateService.getStatus(BEARBEITER_ID));
    assertEquals(
        UNEXPECTED_ERROR,
        stateService.getErrorMessageKey(BEARBEITER_ID));
    assertFalse(stateService.isRunning());
  }

  @Test
  void shouldNotStartWhileAnotherImportIsRunning() throws Exception {
    assertTrue(stateService.tryStartImport("other"));

    assertFalse(model.startImport(CONTENT));

    verifyNoInteractions(externeRessourcenImportService);
    assertEquals(ExterneRessourcenImportStatus.RUNNING, stateService.getStatus(BEARBEITER_ID));
  }

  @Test
  void shouldRejectNullContentBeforeStartingImport() throws Exception {
    assertThrows(NullPointerException.class, () -> model.startImport(null));

    assertFalse(stateService.isRunning());
    verifyNoInteractions(externeRessourcenImportService);
  }

  @Test
  void shouldScheduleImportWithManagedExecutor() {
    ManagedExecutor managedExecutor = mock(ManagedExecutor.class);
    model = new ExterneRessourcenImportModel(
        externeRessourcenImportService,
        stateService,
        bearbeiterBoundary,
        managedExecutor);

    assertTrue(model.startImport(CONTENT));

    verify(managedExecutor).runAsync(any(Runnable.class));
    assertTrue(stateService.isRunning());
    verifyNoInteractions(externeRessourcenImportService);
  }

  @Test
  void shouldFinishWithErrorWhenManagedExecutorRejectsImport() {
    ManagedExecutor managedExecutor = mock(ManagedExecutor.class);
    when(managedExecutor.runAsync(any(Runnable.class)))
        .thenThrow(new RejectedExecutionException("executor is shutting down"));
    model = new ExterneRessourcenImportModel(
        externeRessourcenImportService,
        stateService,
        bearbeiterBoundary,
        managedExecutor);

    assertTrue(model.startImport(CONTENT));

    assertEquals(ExterneRessourcenImportStatus.FAILED, stateService.getStatus(BEARBEITER_ID));
    assertEquals(
        UNEXPECTED_ERROR,
        stateService.getErrorMessageKey(BEARBEITER_ID));
    assertFalse(stateService.isRunning());
    verifyNoInteractions(externeRessourcenImportService);
  }

  private static ExterneRessourcenImportResult failedResult() {
    return ExterneRessourcenImportResult.builder()
        .kodUuid("HSP-1")
        .ressource("hsc")
        .link("https://example.org/1")
        .status(FEHLER)
        .errorMessageKey(ROW_INVALID)
        .lineNumber(2)
        .build();
  }
}
