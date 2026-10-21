package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.SAVE_ERROR;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResult.Status.FEHLER;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExterneRessourcenImportStateServiceTest {

  private static final String BEARBEITER = "b-1";
  private static final String ANDERER_BEARBEITER = "b-2";
  private static final ExterneRessourcenImportResult RESULT =
      ExterneRessourcenImportResult.builder()
          .kodUuid("HSP-1")
          .ressource("hsc")
          .link("https://example.org/1")
          .status(FEHLER)
          .errorMessageKey(SAVE_ERROR)
          .errorMessageArgs(new Object[]{"original"})
          .lineNumber(1)
          .build();
  private static final List<ExterneRessourcenImportResult> RESULTS = List.of(RESULT);

  private ExterneRessourcenImportStateService stateService;

  @BeforeEach
  void setUp() {
    stateService = new ExterneRessourcenImportStateService();
  }

  @Test
  void shouldStartImportAndPreventConcurrentImport() {
    assertTrue(stateService.tryStartImport(BEARBEITER));

    assertTrue(stateService.isRunning());
    assertEquals(ExterneRessourcenImportStatus.RUNNING, stateService.getStatus(BEARBEITER));
    assertNotNull(stateService.getStartTime(BEARBEITER));
    assertNull(stateService.getFinishTime(BEARBEITER));
    assertFalse(stateService.tryStartImport(ANDERER_BEARBEITER));
  }

  @Test
  void shouldRejectImportWithoutBearbeiterId() {
    assertThrows(NullPointerException.class, () -> stateService.tryStartImport(null));

    assertFalse(stateService.isRunning());
  }

  @Test
  void shouldExposeSuccessfulResultOnlyToStartingBearbeiter() {
    stateService.tryStartImport(BEARBEITER);

    stateService.finishSuccessfully(RESULTS, "result.csv");

    assertFalse(stateService.isRunning());
    assertEquals(ExterneRessourcenImportStatus.SUCCESS, stateService.getStatus(BEARBEITER));
    assertNotNull(stateService.getFinishTime(BEARBEITER));
    assertNull(stateService.getResults(ANDERER_BEARBEITER));
    assertNull(stateService.getResultFileName(ANDERER_BEARBEITER));
    assertEquals(RESULTS, stateService.getResults(BEARBEITER));
    assertEquals("result.csv", stateService.getResultFileName(BEARBEITER));
  }

  @Test
  void shouldExposeFailureOnlyToStartingBearbeiter() {
    stateService.tryStartImport(BEARBEITER);

    stateService.finishWithError(SAVE_ERROR, "details");

    assertEquals(ExterneRessourcenImportStatus.FAILED, stateService.getStatus(BEARBEITER));
    assertNotNull(stateService.getFinishTime(BEARBEITER));
    assertNull(stateService.getErrorMessageKey(ANDERER_BEARBEITER));
    assertNull(stateService.getErrorMessageArgs(ANDERER_BEARBEITER));
    assertEquals(SAVE_ERROR, stateService.getErrorMessageKey(BEARBEITER));
    assertArrayEquals(new Object[]{"details"}, stateService.getErrorMessageArgs(BEARBEITER));
    assertNull(stateService.getResults(BEARBEITER));
  }

  @Test
  void shouldKeepCompletedResultWhileAnotherBearbeiterImports() {
    stateService.tryStartImport(BEARBEITER);
    stateService.finishSuccessfully(RESULTS, "result.csv");

    assertTrue(stateService.tryStartImport(ANDERER_BEARBEITER));

    assertTrue(stateService.isRunning());
    assertEquals(ExterneRessourcenImportStatus.SUCCESS, stateService.getStatus(BEARBEITER));
    assertEquals(RESULTS, stateService.getResults(BEARBEITER));
    assertEquals(
        ExterneRessourcenImportStatus.RUNNING, stateService.getStatus(ANDERER_BEARBEITER));
  }

  @Test
  void shouldResetOnlyCompletedResultOfGivenBearbeiter() {
    stateService.tryStartImport(BEARBEITER);
    stateService.finishSuccessfully(RESULTS, "result.csv");

    stateService.reset(ANDERER_BEARBEITER);
    assertEquals(ExterneRessourcenImportStatus.SUCCESS, stateService.getStatus(BEARBEITER));

    stateService.reset(BEARBEITER);
    assertNull(stateService.getStatus(BEARBEITER));
    assertNull(stateService.getStartTime(BEARBEITER));
    assertNull(stateService.getFinishTime(BEARBEITER));
    assertNull(stateService.getResults(BEARBEITER));
  }

  @Test
  void shouldNotResetRunningImport() {
    stateService.tryStartImport(BEARBEITER);

    stateService.reset(BEARBEITER);

    assertTrue(stateService.isRunning());
    assertEquals(ExterneRessourcenImportStatus.RUNNING, stateService.getStatus(BEARBEITER));
  }

  @Test
  void shouldClearPreviousResultWhenSameBearbeiterStartsNextImport() {
    stateService.tryStartImport(BEARBEITER);
    stateService.finishSuccessfully(RESULTS, "result.csv");

    assertTrue(stateService.tryStartImport(BEARBEITER));

    assertEquals(ExterneRessourcenImportStatus.RUNNING, stateService.getStatus(BEARBEITER));
    assertNull(stateService.getResults(BEARBEITER));
    assertNull(stateService.getFinishTime(BEARBEITER));
  }

  @Test
  void shouldProtectResultsFromExternalModification() {
    List<ExterneRessourcenImportResult> mutableResults = new ArrayList<>(RESULTS);
    stateService.tryStartImport(BEARBEITER);
    stateService.finishSuccessfully(mutableResults, "result.csv");

    mutableResults.clear();

    assertEquals(RESULTS, stateService.getResults(BEARBEITER));
    assertThrows(
        UnsupportedOperationException.class,
        () -> stateService.getResults(BEARBEITER).clear());
  }

  @Test
  void shouldProtectErrorArgumentsFromExternalModification() {
    Object[] errorArgs = {"original"};
    stateService.tryStartImport(BEARBEITER);
    stateService.finishWithError(SAVE_ERROR, errorArgs);

    errorArgs[0] = "changed";
    Object[] returnedArgs = stateService.getErrorMessageArgs(BEARBEITER);
    returnedArgs[0] = "changed again";

    assertArrayEquals(
        new Object[]{"original"}, stateService.getErrorMessageArgs(BEARBEITER));
  }
}
