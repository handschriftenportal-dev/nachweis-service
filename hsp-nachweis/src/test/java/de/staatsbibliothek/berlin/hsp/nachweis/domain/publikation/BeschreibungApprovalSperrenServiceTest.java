package de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BeschreibungApprovalSperrenServiceTest {

  private BeschreibungsBoundary beschreibungsService;
  private DokumentSperreBoundary dokumentSperreBoundary;
  private BearbeiterBoundary bearbeiterBoundary;
  private BeschreibungApprovalSperrenService service;

  @BeforeEach
  void setUp() {
    beschreibungsService = mock(BeschreibungsBoundary.class);
    dokumentSperreBoundary = mock(DokumentSperreBoundary.class);
    bearbeiterBoundary = mock(BearbeiterBoundary.class);
    service = new BeschreibungApprovalSperrenService(
        beschreibungsService, dokumentSperreBoundary, bearbeiterBoundary);
  }

  @Test
  void checkHasSperre_passesWhenNotLocked() throws Exception {
    when(beschreibungsService.isBeschreibungGesperrt("BSB123")).thenReturn(false);

    service.checkHasSperre("BSB123");

    verify(beschreibungsService).isBeschreibungGesperrt("BSB123");
  }

  @Test
  void checkHasSperre_throwsPublikationExceptionWhenLocked() throws Exception {
    when(beschreibungsService.isBeschreibungGesperrt("BSB123")).thenReturn(true);

    PublikationException ex = assertThrows(PublikationException.class,
        () -> service.checkHasSperre("BSB123"));

    assertEquals("publikation_error_beschreibung_locked", ex.getMessage());
    assertEquals("BSB123", ex.getBeschreibungId());
  }

  @Test
  void checkHasSperre_wrapsDokumentSperreException() throws Exception {
    when(beschreibungsService.isBeschreibungGesperrt("BSB123"))
        .thenThrow(new DokumentSperreException("db error"));

    PublikationException ex = assertThrows(PublikationException.class,
        () -> service.checkHasSperre("BSB123"));

    assertEquals("publikation_error_sperre_check", ex.getMessage());
    assertInstanceOf(DokumentSperreException.class, ex.getCause());
  }

  @Test
  void acquireApprovalSperre_delegatesToBeschreibungsService() throws Exception {
    Bearbeiter bearbeiter = new Bearbeiter("b1", "testuser");
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);

    service.acquireApprovalSperre("BSB123");

    verify(beschreibungsService).beschreibungSperrenForApproval(bearbeiter, "BSB123");
  }

  @Test
  void acquireApprovalSperre_propagatesDokumentSperreException() throws Exception {
    Bearbeiter bearbeiter = new Bearbeiter("b1", "testuser");
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
    when(beschreibungsService.beschreibungSperrenForApproval(any(Bearbeiter.class), anyString()))
        .thenThrow(new DokumentSperreException("lock error"));

    assertThrows(DokumentSperreException.class, () -> service.acquireApprovalSperre("BSB123"));
  }

  @Test
  void acquireApprovalSperre_propagatesSperreAlreadyExistException() throws Exception {
    Bearbeiter bearbeiter = new Bearbeiter("b1", "testuser");
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
    when(beschreibungsService.beschreibungSperrenForApproval(any(Bearbeiter.class), anyString()))
        .thenThrow(new SperreAlreadyExistException("already", Collections.emptyList()));

    assertThrows(SperreAlreadyExistException.class,
        () -> service.acquireApprovalSperre("BSB123"));
  }

  /**
   * Audit-contract pin: {@code BearbeiterService.getLoggedBearbeiter()} falls back to the
   * {@code unbekannterBearbeiter} placeholder when no session/auth is present. This test locks in
   * that {@link BeschreibungApprovalSperrenService#acquireApprovalSperre} accepts that fallback
   * without filtering — background jobs and REST calls without security context will acquire the
   * Sperre under the unknown user rather than failing. If a future change adds a defensive null
   * check or rejects the unknown user, this test will fail and force a conscious revisit of the
   * audit-trail semantics documented on the method.
   */
  @Test
  void acquireApprovalSperre_acceptsUnbekannterBearbeiterFallback() throws Exception {
    Bearbeiter unknown = new Bearbeiter("unbekannterBearbeiter", "unbekannterBearbeiter");
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(unknown);

    service.acquireApprovalSperre("BSB123");

    verify(beschreibungsService).beschreibungSperrenForApproval(unknown, "BSB123");
  }

  @Test
  void releaseApprovalSperre_releasesApprovalTypeSperre() throws Exception {
    Sperre approvalSperre = mock(Sperre.class);
    when(approvalSperre.getSperreTyp()).thenReturn(SperreTyp.APPROVAL);
    when(beschreibungsService.findSperreForBeschreibung("BSB123"))
        .thenReturn(Optional.of(approvalSperre));

    service.releaseApprovalSperre("BSB123");

    verify(dokumentSperreBoundary).releaseSperreInCurrentTransaction(approvalSperre);
  }

  @Test
  void releaseApprovalSperre_doesNothingWhenNoSperrePresent() throws Exception {
    when(beschreibungsService.findSperreForBeschreibung("BSB123"))
        .thenReturn(Optional.empty());

    service.releaseApprovalSperre("BSB123");

    verify(dokumentSperreBoundary, never()).releaseSperreInCurrentTransaction(any());
  }

  @Test
  void releaseApprovalSperre_ignoresNonApprovalSperre() throws Exception {
    Sperre manualSperre = mock(Sperre.class);
    when(manualSperre.getSperreTyp()).thenReturn(SperreTyp.MANUAL);
    when(beschreibungsService.findSperreForBeschreibung("BSB123"))
        .thenReturn(Optional.of(manualSperre));

    service.releaseApprovalSperre("BSB123");

    verify(dokumentSperreBoundary, never()).releaseSperreInCurrentTransaction(any());
  }

  @Test
  void releaseApprovalSperre_propagatesDokumentSperreException() throws Exception {
    Sperre approvalSperre = mock(Sperre.class);
    when(approvalSperre.getSperreTyp()).thenReturn(SperreTyp.APPROVAL);
    when(beschreibungsService.findSperreForBeschreibung("BSB123"))
        .thenReturn(Optional.of(approvalSperre));
    when(dokumentSperreBoundary.releaseSperreInCurrentTransaction(approvalSperre))
        .thenThrow(new DokumentSperreException("release error"));

    assertThrows(DokumentSperreException.class, () -> service.releaseApprovalSperre("BSB123"));
  }
}
