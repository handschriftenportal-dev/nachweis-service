package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.KulturObjektSortkeyUpdateException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.KulturObjektSortkeyUpdateReport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyImportStateService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyPartialImportException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

/**
 * The ManagedExecutor is deliberately passed as null: the model then falls back to synchronous
 * execution (same pattern as DatenuebernahmeModel), so the tests can verify the complete import
 * flow deterministically.
 */
class SortkeyUpdateModelTest {

  private static final String BEARBEITER_ID = "b-1";

  private KulturObjektDokumentBoundary boundary;
  private SortkeyImportStateService stateService;
  private BearbeiterBoundary bearbeiterBoundary;
  private SortkeyUpdateModel model;

  @BeforeEach
  void setUp() {
    boundary = mock(KulturObjektDokumentBoundary.class);
    stateService = mock(SortkeyImportStateService.class);
    bearbeiterBoundary = mock(BearbeiterBoundary.class);

    model = new SortkeyUpdateModel(boundary, stateService, bearbeiterBoundary, null);
  }

  private FileUploadEvent uploadEvent() {
    UploadedFile file = mock(UploadedFile.class);
    when(file.getContent()).thenReturn("data".getBytes());
    when(file.getFileName()).thenReturn("sortkeys.csv");

    FileUploadEvent event = mock(FileUploadEvent.class);
    when(event.getFile()).thenReturn(file);
    return event;
  }

  private void stubLoggedBearbeiter() {
    when(bearbeiterBoundary.getLoggedBearbeiter())
        .thenReturn(new Bearbeiter(BEARBEITER_ID, "Tester"));
  }

  @Test
  void shouldStartImportAndFinishSuccessfully() throws KulturObjektSortkeyUpdateException {

    stubLoggedBearbeiter();
    when(stateService.tryStartImport(BEARBEITER_ID)).thenReturn(true);

    KulturObjektSortkeyUpdateReport expectedReport =
        new KulturObjektSortkeyUpdateReport(2, List.of(), List.of());

    when(boundary.updateSortkeys(any(), any())).thenReturn(expectedReport);

    FacesContext facesContext = mock(FacesContext.class);

    try (MockedStatic<FacesContext> facesContextMock =
        Mockito.mockStatic(FacesContext.class)) {

      facesContextMock.when(FacesContext::getCurrentInstance)
          .thenReturn(facesContext);

      model.handleSortkeysUpload(uploadEvent());

      verify(stateService).tryStartImport(BEARBEITER_ID);
      verify(stateService).finishSuccessfully(expectedReport);
      verify(stateService, never()).finishWithError(anyInt());

      // The report is no longer set directly on the model; it is delivered
      // to the poll via the StateService.
      assertNull(model.getReport());

      verify(facesContext).addMessage(anyString(), any(FacesMessage.class));
    }
  }

  @Test
  void shouldHandleNullEventGracefully() throws KulturObjektSortkeyUpdateException {

    FacesContext facesContext = mock(FacesContext.class);

    try (MockedStatic<FacesContext> facesContextMock =
        Mockito.mockStatic(FacesContext.class)) {

      facesContextMock.when(FacesContext::getCurrentInstance)
          .thenReturn(facesContext);

      model.handleSortkeysUpload(null);

      assertNull(model.getReport());

      verify(boundary, never()).updateSortkeys(any(), any());
      verifyNoInteractions(stateService);
      verifyNoInteractions(bearbeiterBoundary);

      verify(facesContext).addMessage(anyString(), any(FacesMessage.class));
    }
  }

  @Test
  void shouldHandleNullFileGracefully() throws KulturObjektSortkeyUpdateException {

    FileUploadEvent event = mock(FileUploadEvent.class);
    when(event.getFile()).thenReturn(null);

    FacesContext facesContext = mock(FacesContext.class);

    try (MockedStatic<FacesContext> facesContextMock =
        Mockito.mockStatic(FacesContext.class)) {

      facesContextMock.when(FacesContext::getCurrentInstance)
          .thenReturn(facesContext);

      model.handleSortkeysUpload(event);

      assertNull(model.getReport());

      verify(boundary, never()).updateSortkeys(any(), any());
      verifyNoInteractions(stateService);

      verify(facesContext).addMessage(anyString(), any(FacesMessage.class));
    }
  }

  @Test
  void shouldFinishWithErrorOnServiceException() throws KulturObjektSortkeyUpdateException {

    stubLoggedBearbeiter();
    when(stateService.tryStartImport(BEARBEITER_ID)).thenReturn(true);

    when(boundary.updateSortkeys(any(), any()))
        .thenThrow(new KulturObjektSortkeyUpdateException("kod_sortkeys_error"));

    FacesContext facesContext = mock(FacesContext.class);

    try (MockedStatic<FacesContext> facesContextMock =
        Mockito.mockStatic(FacesContext.class)) {

      facesContextMock.when(FacesContext::getCurrentInstance)
          .thenReturn(facesContext);

      model.handleSortkeysUpload(uploadEvent());

      assertNull(model.getReport());

      verify(stateService).tryStartImport(BEARBEITER_ID);
      verify(stateService).finishWithError(0);
      verify(stateService, never()).finishSuccessfully(any());
    }
  }

  @Test
  void shouldFinishWithPartialCountOnPartialImportException()
      throws KulturObjektSortkeyUpdateException {

    stubLoggedBearbeiter();
    when(stateService.tryStartImport(BEARBEITER_ID)).thenReturn(true);

    when(boundary.updateSortkeys(any(), any()))
        .thenThrow(new SortkeyPartialImportException(800, new RuntimeException("DB error")));

    FacesContext facesContext = mock(FacesContext.class);

    try (MockedStatic<FacesContext> facesContextMock =
        Mockito.mockStatic(FacesContext.class)) {

      facesContextMock.when(FacesContext::getCurrentInstance)
          .thenReturn(facesContext);

      model.handleSortkeysUpload(uploadEvent());

      verify(stateService).finishWithError(800);
      verify(stateService, never()).finishSuccessfully(any());
    }
  }

  @Test
  void shouldFinishWithErrorOnUnexpectedException() throws KulturObjektSortkeyUpdateException {

    stubLoggedBearbeiter();
    when(stateService.tryStartImport(BEARBEITER_ID)).thenReturn(true);

    when(boundary.updateSortkeys(any(), any()))
        .thenThrow(new RuntimeException("unexpected"));

    FacesContext facesContext = mock(FacesContext.class);

    try (MockedStatic<FacesContext> facesContextMock =
        Mockito.mockStatic(FacesContext.class)) {

      facesContextMock.when(FacesContext::getCurrentInstance)
          .thenReturn(facesContext);

      model.handleSortkeysUpload(uploadEvent());

      assertNull(model.getReport());

      verify(stateService).tryStartImport(BEARBEITER_ID);
      verify(stateService).finishWithError(0);
      verify(stateService, never()).finishSuccessfully(any());
    }
  }

  @Test
  void shouldNotStartImportWhenAnotherImportIsRunning()
      throws KulturObjektSortkeyUpdateException {

    stubLoggedBearbeiter();
    when(stateService.tryStartImport(BEARBEITER_ID)).thenReturn(false);

    FacesContext facesContext = mock(FacesContext.class);

    try (MockedStatic<FacesContext> facesContextMock =
        Mockito.mockStatic(FacesContext.class)) {

      facesContextMock.when(FacesContext::getCurrentInstance)
          .thenReturn(facesContext);

      model.handleSortkeysUpload(uploadEvent());

      assertNull(model.getReport());

      verify(stateService).tryStartImport(BEARBEITER_ID);

      verify(boundary, never()).updateSortkeys(any(), any());

      verify(stateService, never()).finishSuccessfully(any());
      verify(stateService, never()).finishWithError(anyInt());

      verify(facesContext).addMessage(anyString(), any(FacesMessage.class));
    }
  }
}
