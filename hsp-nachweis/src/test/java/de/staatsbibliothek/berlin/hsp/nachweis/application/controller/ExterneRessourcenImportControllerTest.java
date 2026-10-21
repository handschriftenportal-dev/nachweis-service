package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.FILE_EMPTY;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportStatus.FAILED;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportStatus.RUNNING;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportStatus.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.ExterneRessourcenImportModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportStateService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResult;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResultCSVWriter;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIViewRoot;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

class ExterneRessourcenImportControllerTest {

  private static final String BEARBEITER_ID = "bearbeiter-1";
  private static final byte[] CSV = "header\ndata".getBytes(StandardCharsets.UTF_8);

  private ExterneRessourcenImportModel importModel;
  private ExterneRessourcenImportStateService stateService;
  private FacesContext facesContext;
  private UIViewRoot viewRoot;
  private ExterneRessourcenImportResultCSVWriter resultCSVWriter;
  private MockedStatic<FacesContext> facesContextStatic;
  private ExterneRessourcenImportController controller;

  @BeforeEach
  void setUp() {
    importModel = mock(ExterneRessourcenImportModel.class);
    stateService = mock(ExterneRessourcenImportStateService.class);
    resultCSVWriter = mock(ExterneRessourcenImportResultCSVWriter.class);
    BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);
    when(bearbeiterBoundary.getLoggedBearbeiter())
        .thenReturn(new Bearbeiter(BEARBEITER_ID, "Bearbeiter"));

    facesContext = mock(FacesContext.class);
    viewRoot = mock(UIViewRoot.class);
    when(facesContext.getViewRoot()).thenReturn(viewRoot);
    when(viewRoot.getLocale()).thenReturn(Locale.GERMAN);
    facesContextStatic = mockStatic(FacesContext.class);
    facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

    controller = new ExterneRessourcenImportController(
        importModel, stateService, bearbeiterBoundary, resultCSVWriter);
  }

  @AfterEach
  void tearDown() {
    facesContextStatic.close();
  }

  @Test
  void shouldStartImportWhenFileIsUploaded() {
    FileUploadEvent event = uploadEvent(CSV);
    when(importModel.startImport(any())).thenReturn(true);

    controller.handleFileUpload(event);

    ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(importModel).startImport(contentCaptor.capture());
    assertArrayEquals(CSV, contentCaptor.getValue());
    assertTrue(controller.isPolling());
  }

  @Test
  void shouldRejectEmptyUploadedFile() {
    controller.handleFileUpload(uploadEvent(new byte[0]));

    verify(importModel, never()).startImport(any());
    verify(facesContext).addMessage(
        org.mockito.ArgumentMatchers.eq("globalmessages"), any(FacesMessage.class));
  }

  @Test
  void shouldReportWhenAnotherImportIsRunning() {
    when(importModel.startImport(any())).thenReturn(false);

    controller.handleFileUpload(uploadEvent(CSV));

    assertFalse(controller.isPolling());
    verify(facesContext).addMessage(
        org.mockito.ArgumentMatchers.eq("globalmessages"), any(FacesMessage.class));
  }

  @Test
  void shouldReportGlobalRunningJobEvenWhenBearbeiterHasSuccessfulResult() {
    when(stateService.getStatus(BEARBEITER_ID)).thenReturn(SUCCESS);
    when(stateService.isRunning()).thenReturn(true);

    assertTrue(controller.isSuccessful());
    assertTrue(controller.isRunning());
  }

  @Test
  void shouldInitializePollingForRunningAndStopItAfterSuccess() {
    when(stateService.getStatus(BEARBEITER_ID)).thenReturn(RUNNING, SUCCESS);

    controller.init();
    assertTrue(controller.isPolling());

    controller.checkImportStatus();
    assertFalse(controller.isPolling());
  }

  @Test
  void shouldNotInitializePollingForCompletedSuccessfulImport() {
    when(stateService.getStatus(BEARBEITER_ID)).thenReturn(SUCCESS);

    controller.init();

    assertFalse(controller.isPolling());
  }

  @Test
  void shouldInitializePollingForFailedImportToDeliverFailureMessage() {
    when(stateService.getStatus(BEARBEITER_ID)).thenReturn(FAILED);

    controller.init();

    assertTrue(controller.isPolling());
  }

  @Test
  void shouldShowFailureOnlyOnce() {
    Instant finishTime = Instant.parse("2026-08-24T12:00:00Z");
    when(stateService.getStatus(BEARBEITER_ID)).thenReturn(FAILED);
    when(stateService.getFinishTime(BEARBEITER_ID)).thenReturn(finishTime);
    when(stateService.getErrorMessageKey(BEARBEITER_ID))
        .thenReturn(FILE_EMPTY);

    controller.checkImportStatus();
    controller.checkImportStatus();

    verify(facesContext, times(1)).addMessage(
        org.mockito.ArgumentMatchers.eq("globalmessages"), any(FacesMessage.class));
    assertFalse(controller.isPolling());
  }

  @Test
  void shouldProvideResultCsvWithoutRemovingIt() throws Exception {
    byte[] result = "result".getBytes(StandardCharsets.UTF_8);
    List<ExterneRessourcenImportResult> results = List.of();
    when(stateService.getResults(BEARBEITER_ID)).thenReturn(results);
    when(stateService.getResultFileName(BEARBEITER_ID))
        .thenReturn("externe-ressourcen-import-ergebnis.csv");
    when(resultCSVWriter.write(results, Locale.GERMAN)).thenReturn(result);

    StreamedContent streamedContent = controller.getResultCsv();

    assertNotNull(streamedContent);
    assertEquals("externe-ressourcen-import-ergebnis.csv", streamedContent.getName());
    assertEquals("text/csv;charset=UTF-8", streamedContent.getContentType());
    assertArrayEquals(result, streamedContent.getStream().get().readAllBytes());
    verify(stateService, never()).reset(BEARBEITER_ID);
  }

  @Test
  void shouldGenerateResultCsvUsingCurrentUiLocale() throws Exception {
    List<ExterneRessourcenImportResult> results = List.of();
    byte[] englishCsv = "english".getBytes(StandardCharsets.UTF_8);
    byte[] germanCsv = "deutsch".getBytes(StandardCharsets.UTF_8);
    when(stateService.getResults(BEARBEITER_ID)).thenReturn(results);
    when(stateService.getResultFileName(BEARBEITER_ID)).thenReturn("result.csv");
    when(resultCSVWriter.write(results, Locale.ENGLISH)).thenReturn(englishCsv);
    when(resultCSVWriter.write(results, Locale.GERMAN)).thenReturn(germanCsv);

    when(viewRoot.getLocale()).thenReturn(Locale.ENGLISH);
    assertArrayEquals(englishCsv, controller.getResultCsv().getStream().get().readAllBytes());

    when(viewRoot.getLocale()).thenReturn(Locale.GERMAN);
    assertArrayEquals(germanCsv, controller.getResultCsv().getStream().get().readAllBytes());
  }

  @Test
  void shouldNotProvideDownloadWithoutSuccessfulResult() {
    assertNull(controller.getResultCsv());
  }

  @Test
  void shouldProvideSupportedResourceTypesFromDomainEnum() {
    assertEquals(
        java.util.List.of(
            "HSC", "KDIH", "MANUSCRIPTAAT", "MANUSCRIPTAPL", "FRAGMENTARIUM", "QALAMOS",
            "PINAKES", "WZIS", "EBDB", "HSA", "GESCHICHTSQUELLEN", "JORDANUS", "ECODICES",
            "KOLOPHONE", "SONSTIGES", "BESCHREIBUNG"),
        controller.getSupportedResourceTypes());
  }

  private FileUploadEvent uploadEvent(byte[] content) {
    UploadedFile uploadedFile = mock(UploadedFile.class);
    when(uploadedFile.getContent()).thenReturn(content);
    when(uploadedFile.getFileName()).thenReturn("import.csv");
    FileUploadEvent event = mock(FileUploadEvent.class);
    when(event.getFile()).thenReturn(uploadedFile);
    return event;
  }
}
