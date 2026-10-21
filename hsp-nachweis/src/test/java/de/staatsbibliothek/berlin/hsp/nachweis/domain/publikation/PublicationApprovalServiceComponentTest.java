package de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.ApprovalRequest;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ApprovalStatus;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.LizenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.approval.ApprovalException;
import io.quarkus.test.component.QuarkusComponentTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusComponentTest
class PublicationApprovalServiceComponentTest {

  @Inject
  PublikationApprovalService approvalService;

  // ---------------------------------------------------------------------------
  // Service injection
  // ---------------------------------------------------------------------------

  @Test
  void serviceIsInjected() {
    assertNotNull(approvalService);
  }

  // ---------------------------------------------------------------------------
  // createRequest – happy path
  // ---------------------------------------------------------------------------

  @Test
  void createRequest_returnsBeschreibungWithActiveApprovalRequest() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibung();

    Beschreibung result = approvalService.createRequest(beschreibung, "TestUser", "Please review");

    assertSame(beschreibung, result);
    assertTrue(beschreibung.hasActiveApprovalRequest());
    assertEquals(PublikationStatus.IN_REVIEW, beschreibung.getPublikation().getStatus());
  }

  // ---------------------------------------------------------------------------
  // createRequest – duplicate request
  // ---------------------------------------------------------------------------

  @Test
  void createRequest_throwsApprovalException_whenActiveRequestAlreadyExists()
      throws ApprovalException {
    Beschreibung beschreibung = createBeschreibung();
    approvalService.createRequest(beschreibung, "TestUser", "First request");

    ApprovalException exception = assertThrows(ApprovalException.class,
        () -> approvalService.createRequest(beschreibung, "TestUser", "Duplicate request"));

    assertTrue(exception.getMessage().contains(beschreibung.getId()));
  }

  // ---------------------------------------------------------------------------
  // assignReviewer – happy path
  // ---------------------------------------------------------------------------

  @Test
  void assignReviewer_setsReviewerAndStatusToInReview() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();

    Beschreibung result = approvalService.assignReviewer(beschreibung, "Reviewer");

    assertSame(beschreibung, result);
    ApprovalRequest request = beschreibung.getActiveApprovalRequest().orElseThrow();
    assertEquals("Reviewer", request.getAssignedTo());
    assertEquals(ApprovalStatus.IN_REVIEW, request.getStatus());
  }

  // ---------------------------------------------------------------------------
  // assignReviewer – no active request
  // ---------------------------------------------------------------------------

  @Test
  void assignReviewer_throwsApprovalException_whenNoActiveRequest() {
    Beschreibung beschreibung = createBeschreibung();

    ApprovalException exception = assertThrows(ApprovalException.class,
        () -> approvalService.assignReviewer(beschreibung, "Reviewer"));

    assertTrue(exception.getMessage().contains(beschreibung.getId()));
  }

  // ---------------------------------------------------------------------------
  // approve – happy path with status transitions
  // ---------------------------------------------------------------------------

  @Test
  void approve_setsPublikationStatusToPublishedAndRequestToApproved() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();
    approvalService.assignReviewer(beschreibung, "Reviewer");

    Beschreibung result = approvalService.approve(beschreibung, "Reviewer", "Looks good");

    assertSame(beschreibung, result);
    assertEquals(PublikationStatus.PUBLISHED, beschreibung.getPublikation().getStatus());
    assertFalse(beschreibung.hasActiveApprovalRequest());
    ApprovalRequest request = beschreibung.getPublikation().getApprovalRequests().iterator().next();
    assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    assertNotNull(beschreibung.getPublikation().getPublikationsDatumHSP());
    assertNotNull(beschreibung.getPublikation().getErstpublikationsDatum());
  }

  // ---------------------------------------------------------------------------
  // reject – happy path with status transitions
  // ---------------------------------------------------------------------------

  @Test
  void reject_setsPublikationStatusToDraftAndRequestToRejected() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();
    approvalService.assignReviewer(beschreibung, "Reviewer");

    Beschreibung result = approvalService.reject(beschreibung, "Reviewer", "Needs changes");

    assertSame(beschreibung, result);
    assertEquals(PublikationStatus.DRAFT, beschreibung.getPublikation().getStatus());
    assertFalse(beschreibung.hasActiveApprovalRequest());
    ApprovalRequest request = beschreibung.getPublikation().getApprovalRequests().iterator().next();
    assertEquals(ApprovalStatus.REJECTED, request.getStatus());
  }

  // ---------------------------------------------------------------------------
  // withdraw – happy path
  // ---------------------------------------------------------------------------

  @Test
  void withdraw_setsPublikationStatusToDraftAndRequestToWithdrawn() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();

    Beschreibung result = approvalService.withdraw(beschreibung, "TestUser", "Changed my mind");

    assertSame(beschreibung, result);
    assertEquals(PublikationStatus.DRAFT, beschreibung.getPublikation().getStatus());
    assertFalse(beschreibung.hasActiveApprovalRequest());
    ApprovalRequest request = beschreibung.getPublikation().getApprovalRequests().iterator().next();
    assertEquals(ApprovalStatus.WITHDRAWN, request.getStatus());
  }

  // ---------------------------------------------------------------------------
  // Workflow scenario: full lifecycle create -> assign -> approve
  // ---------------------------------------------------------------------------

  @Test
  void fullWorkflow_createAssignApprove() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibung();

    approvalService.createRequest(beschreibung, "Author", "Initial submission");
    assertEquals(PublikationStatus.IN_REVIEW, beschreibung.getPublikation().getStatus());
    assertTrue(beschreibung.hasActiveApprovalRequest());

    approvalService.assignReviewer(beschreibung, "Editor");
    ApprovalRequest request = beschreibung.getActiveApprovalRequest().orElseThrow();
    assertEquals(ApprovalStatus.IN_REVIEW, request.getStatus());
    assertEquals("Editor", request.getAssignedTo());

    approvalService.approve(beschreibung, "Editor", "All good");
    assertEquals(PublikationStatus.PUBLISHED, beschreibung.getPublikation().getStatus());
    assertFalse(beschreibung.hasActiveApprovalRequest());
  }

  // ---------------------------------------------------------------------------
  // Workflow scenario: create -> assign -> reject -> create again
  // ---------------------------------------------------------------------------

  @Test
  void fullWorkflow_createAssignRejectThenCreateAgain() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibung();

    approvalService.createRequest(beschreibung, "Author", "First try");
    approvalService.assignReviewer(beschreibung, "Editor");
    approvalService.reject(beschreibung, "Editor", "Needs work");

    assertEquals(PublikationStatus.DRAFT, beschreibung.getPublikation().getStatus());
    assertFalse(beschreibung.hasActiveApprovalRequest());

    // Should be able to create a new request after rejection
    approvalService.createRequest(beschreibung, "Author", "Second try");
    assertTrue(beschreibung.hasActiveApprovalRequest());
    assertEquals(PublikationStatus.IN_REVIEW, beschreibung.getPublikation().getStatus());
    assertEquals(2, beschreibung.getPublikation().getApprovalRequests().size());
  }

  // ---------------------------------------------------------------------------
  // Helper methods
  // ---------------------------------------------------------------------------

  private Identifikation createIdentifikation() {
    return new IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withIdent("1-1-1")
        .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
        .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
        .build();
  }

  private Beschreibung createBeschreibung() {
    var beschreibung = new BeschreibungsBuilder("BESCHREIBUNG1", "KOD1", createIdentifikation())
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withTEIXml("<xml></xml>")
        .build();
    beschreibung.getPublikation().setLizenz(LizenzTyp.CC_BY);
    return beschreibung;
  }

  private Beschreibung createBeschreibungWithActiveRequest() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibung();
    approvalService.createRequest(beschreibung, "TestUser", "setup");
    return beschreibung;
  }
}
