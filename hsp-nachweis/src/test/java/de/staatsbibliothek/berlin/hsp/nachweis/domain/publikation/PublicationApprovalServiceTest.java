package de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.ApprovalRequest;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation.PublikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ApprovalStatus;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.LizenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.approval.ApprovalException;
import org.junit.jupiter.api.Test;

class PublicationApprovalServiceTest {

  private final PublikationApprovalService approvalService = new PublikationApprovalService();

  // ---------------------------------------------------------------------------
  // createRequest – happy path
  // ---------------------------------------------------------------------------

  @Test
  void createRequest_returnsBeschreibungWithActiveApprovalRequest() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibung();

    Beschreibung result = approvalService.createRequest(beschreibung, "TestUser", "Please review");

    assertSame(beschreibung, result);
    assertTrue(beschreibung.hasActiveApprovalRequest());
  }

  @Test
  void createRequest_setsPublikationStatusToInReview() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibung();

    approvalService.createRequest(beschreibung, "TestUser", "comment");

    assertEquals(PublikationStatus.IN_REVIEW, beschreibung.getPublikation().getStatus());
  }

  @Test
  void createRequest_createsApprovalRequestInRequestedStatus() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibung();

    approvalService.createRequest(beschreibung, "TestUser", "comment");

    ApprovalRequest request = beschreibung.getActiveApprovalRequest().orElseThrow();
    assertEquals(ApprovalStatus.REQUESTED, request.getStatus());
  }

  @Test
  void createRequest_setsRequestedByOnApprovalRequest() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibung();

    approvalService.createRequest(beschreibung, "Alice", "comment");

    ApprovalRequest request = beschreibung.getActiveApprovalRequest().orElseThrow();
    assertEquals("Alice", request.getRequestedBy());
  }

  @Test
  void createRequest_withNullComment_succeeds() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibung();

    Beschreibung result = approvalService.createRequest(beschreibung, "TestUser", null);

    assertNotNull(result);
    assertTrue(beschreibung.hasActiveApprovalRequest());
  }

  @Test
  void createRequest_withEmptyComment_succeeds() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibung();

    Beschreibung result = approvalService.createRequest(beschreibung, "TestUser", "");

    assertNotNull(result);
    assertTrue(beschreibung.hasActiveApprovalRequest());
  }

  @Test
  void createRequest_withoutComment_usesDefaultOverload() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibung();

    Beschreibung result = approvalService.createRequest(beschreibung, "TestUser");

    assertNotNull(result);
    assertTrue(beschreibung.hasActiveApprovalRequest());
  }

  // ---------------------------------------------------------------------------
  // createRequest – duplicate / already active
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
  // createRequest – null preconditions
  // ---------------------------------------------------------------------------

  @Test
  void createRequest_throwsNullPointerException_whenBeschreibungIsNull() {
    assertThrows(NullPointerException.class,
        () -> approvalService.createRequest(null, "TestUser", "comment"));
  }

  @Test
  void createRequest_throwsNullPointerException_whenPublikationIsNull() {
    Beschreibung beschreibung = createBeschreibungWithNullPublikation();

    assertThrows(NullPointerException.class,
        () -> approvalService.createRequest(beschreibung, "TestUser", "comment"));
  }

  @Test
  void createRequest_succeeds_whenLizenzIsNull() throws ApprovalException {
    // The approval service deliberately does not enforce a Lizenz: the manual entry point
    // (PublikationsService.applyForPublication) requires one, the automatic-import path does not.
    Beschreibung beschreibung = createBeschreibungWithoutLizenz();

    Beschreibung result = approvalService.createRequest(beschreibung, "TestUser", "comment");

    assertSame(beschreibung, result);
    assertTrue(beschreibung.getActiveApprovalRequest().isPresent());
  }

  // ---------------------------------------------------------------------------
  // assignReviewer – happy path
  // ---------------------------------------------------------------------------

  @Test
  void assignReviewer_returnsBeschreibungWithAssignedReviewer() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();

    Beschreibung result = approvalService.assignReviewer(beschreibung, "Reviewer");

    assertSame(beschreibung, result);
    ApprovalRequest request = beschreibung.getActiveApprovalRequest().orElseThrow();
    assertEquals("Reviewer", request.getAssignedTo());
  }

  @Test
  void assignReviewer_setsApprovalRequestStatusToInReview() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();

    approvalService.assignReviewer(beschreibung, "Reviewer");

    ApprovalRequest request = beschreibung.getActiveApprovalRequest().orElseThrow();
    assertEquals(ApprovalStatus.IN_REVIEW, request.getStatus());
  }

  @Test
  void assignReviewer_canReassignAlreadyAssignedRequest() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();
    approvalService.assignReviewer(beschreibung, "FirstReviewer");

    approvalService.assignReviewer(beschreibung, "SecondReviewer");

    ApprovalRequest request = beschreibung.getActiveApprovalRequest().orElseThrow();
    assertEquals("SecondReviewer", request.getAssignedTo());
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
  // assignReviewer – null preconditions
  // ---------------------------------------------------------------------------

  @Test
  void assignReviewer_throwsNullPointerException_whenBeschreibungIsNull() {
    assertThrows(NullPointerException.class,
        () -> approvalService.assignReviewer(null, "Reviewer"));
  }

  @Test
  void assignReviewer_throwsNullPointerException_whenPublikationIsNull() {
    Beschreibung beschreibung = createBeschreibungWithNullPublikation();

    assertThrows(NullPointerException.class,
        () -> approvalService.assignReviewer(beschreibung, "Reviewer"));
  }

  // ---------------------------------------------------------------------------
  // approve – happy path
  // ---------------------------------------------------------------------------

  @Test
  void approve_setsPublikationStatusToPublished() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();
    approvalService.assignReviewer(beschreibung, "Reviewer");

    Beschreibung result = approvalService.approve(beschreibung, "Reviewer", "Looks good");

    assertSame(beschreibung, result);
    assertEquals(PublikationStatus.PUBLISHED, beschreibung.getPublikation().getStatus());
  }

  @Test
  void approve_setsApprovalRequestStatusToApproved() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();
    approvalService.assignReviewer(beschreibung, "Reviewer");

    approvalService.approve(beschreibung, "Reviewer", "Approved");

    // After approval, the request is no longer active (APPROVED is not active)
    assertFalse(beschreibung.hasActiveApprovalRequest());
    ApprovalRequest request = beschreibung.getPublikation().getApprovalRequests().iterator().next();
    assertEquals(ApprovalStatus.APPROVED, request.getStatus());
  }

  @Test
  void approve_setsPublikationsDatumHSP() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();
    approvalService.assignReviewer(beschreibung, "Reviewer");

    approvalService.approve(beschreibung, "Reviewer", "");

    assertNotNull(beschreibung.getPublikation().getPublikationsDatumHSP());
  }

  @Test
  void approve_setsErstpublikationsDatumOnFirstApproval() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();
    approvalService.assignReviewer(beschreibung, "Reviewer");

    approvalService.approve(beschreibung, "Reviewer", "");

    assertNotNull(beschreibung.getPublikation().getErstpublikationsDatum());
  }

  @Test
  void approve_withNullComment_succeeds() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();
    approvalService.assignReviewer(beschreibung, "Reviewer");

    assertDoesNotThrow(
        () -> approvalService.approve(beschreibung, "Reviewer", null));
  }

  @Test
  void approve_fromRequestedStatus_succeeds() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();
    // Do NOT assign a reviewer -- the request remains in REQUESTED status

    ApprovalRequest request = beschreibung.getActiveApprovalRequest().orElseThrow();
    assertEquals(ApprovalStatus.REQUESTED, request.getStatus());

    approvalService.approve(beschreibung, "Reviewer", "Direct approval");

    assertEquals(PublikationStatus.PUBLISHED, beschreibung.getPublikation().getStatus());
  }

  // ---------------------------------------------------------------------------
  // approve – no active request
  // ---------------------------------------------------------------------------

  @Test
  void approve_throwsApprovalException_whenNoActiveRequest() {
    Beschreibung beschreibung = createBeschreibung();

    ApprovalException exception = assertThrows(ApprovalException.class,
        () -> approvalService.approve(beschreibung, "Reviewer", ""));

    assertTrue(exception.getMessage().contains(beschreibung.getId()));
  }

  // ---------------------------------------------------------------------------
  // approve – null preconditions
  // ---------------------------------------------------------------------------

  @Test
  void approve_throwsNullPointerException_whenBeschreibungIsNull() {
    assertThrows(NullPointerException.class,
        () -> approvalService.approve(null, "Reviewer", "comment"));
  }

  @Test
  void approve_throwsNullPointerException_whenReviewedByIsNull() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();

    assertThrows(NullPointerException.class,
        () -> approvalService.approve(beschreibung, null, "comment"));
  }

  // ---------------------------------------------------------------------------
  // reject – happy path
  // ---------------------------------------------------------------------------

  @Test
  void reject_setsPublikationStatusBackToDraft() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();
    approvalService.assignReviewer(beschreibung, "Reviewer");

    Beschreibung result = approvalService.reject(beschreibung, "Reviewer", "Needs changes");

    assertSame(beschreibung, result);
    assertEquals(PublikationStatus.DRAFT, beschreibung.getPublikation().getStatus());
  }

  @Test
  void reject_setsApprovalRequestStatusToRejected() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();
    approvalService.assignReviewer(beschreibung, "Reviewer");

    approvalService.reject(beschreibung, "Reviewer", "Not acceptable");

    assertFalse(beschreibung.hasActiveApprovalRequest());
    ApprovalRequest request = beschreibung.getPublikation().getApprovalRequests().iterator().next();
    assertEquals(ApprovalStatus.REJECTED, request.getStatus());
  }

  @Test
  void reject_fromRequestedStatus_succeeds() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();
    // No assignReviewer -- request stays REQUESTED

    approvalService.reject(beschreibung, "Reviewer", "Rejected without assignment");

    assertEquals(PublikationStatus.DRAFT, beschreibung.getPublikation().getStatus());
  }

  @Test
  void reject_withNullComment_succeeds() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();

    assertDoesNotThrow(
        () -> approvalService.reject(beschreibung, "Reviewer", null));
  }

  @Test
  void reject_withEmptyComment_succeeds() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();

    assertDoesNotThrow(
        () -> approvalService.reject(beschreibung, "Reviewer", ""));
  }

  // ---------------------------------------------------------------------------
  // reject – no active request
  // ---------------------------------------------------------------------------

  @Test
  void reject_throwsApprovalException_whenNoActiveRequest() {
    Beschreibung beschreibung = createBeschreibung();

    ApprovalException exception = assertThrows(ApprovalException.class,
        () -> approvalService.reject(beschreibung, "Reviewer", "reason"));

    assertTrue(exception.getMessage().contains(beschreibung.getId()));
  }

  // ---------------------------------------------------------------------------
  // reject – null preconditions
  // ---------------------------------------------------------------------------

  @Test
  void reject_throwsNullPointerException_whenBeschreibungIsNull() {
    assertThrows(NullPointerException.class,
        () -> approvalService.reject(null, "Reviewer", "comment"));
  }

  @Test
  void reject_throwsNullPointerException_whenReviewedByIsNull() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();

    assertThrows(NullPointerException.class,
        () -> approvalService.reject(beschreibung, null, "comment"));
  }

  // ---------------------------------------------------------------------------
  // withdraw – happy path
  // ---------------------------------------------------------------------------

  @Test
  void withdraw_setsPublikationStatusBackToDraft() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();

    Beschreibung result = approvalService.withdraw(beschreibung, "TestUser", "Changed my mind");

    assertSame(beschreibung, result);
    assertEquals(PublikationStatus.DRAFT, beschreibung.getPublikation().getStatus());
  }

  @Test
  void withdraw_setsApprovalRequestStatusToWithdrawn() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();

    approvalService.withdraw(beschreibung, "TestUser", "Withdrawn");

    assertFalse(beschreibung.hasActiveApprovalRequest());
    ApprovalRequest request = beschreibung.getPublikation().getApprovalRequests().iterator().next();
    assertEquals(ApprovalStatus.WITHDRAWN, request.getStatus());
  }

  @Test
  void withdraw_afterAssignment_succeeds() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();
    approvalService.assignReviewer(beschreibung, "Reviewer");

    approvalService.withdraw(beschreibung, "TestUser", "Withdrawing after assignment");

    assertEquals(PublikationStatus.DRAFT, beschreibung.getPublikation().getStatus());
    assertFalse(beschreibung.hasActiveApprovalRequest());
  }

  @Test
  void withdraw_withNullComment_succeeds() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();

    assertDoesNotThrow(
        () -> approvalService.withdraw(beschreibung, "TestUser", null));
  }

  @Test
  void withdraw_withEmptyComment_succeeds() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibungWithActiveRequest();

    assertDoesNotThrow(
        () -> approvalService.withdraw(beschreibung, "TestUser", ""));
  }

  // ---------------------------------------------------------------------------
  // withdraw – no active request
  // ---------------------------------------------------------------------------

  @Test
  void withdraw_throwsApprovalException_whenNoActiveRequest() {
    Beschreibung beschreibung = createBeschreibung();

    ApprovalException exception = assertThrows(ApprovalException.class,
        () -> approvalService.withdraw(beschreibung, "TestUser", "comment"));

    assertTrue(exception.getMessage().contains(beschreibung.getId()));
  }

  // ---------------------------------------------------------------------------
  // withdraw – null preconditions
  // ---------------------------------------------------------------------------

  @Test
  void withdraw_throwsNullPointerException_whenBeschreibungIsNull() {
    assertThrows(NullPointerException.class,
        () -> approvalService.withdraw(null, "TestUser", "comment"));
  }

  // ---------------------------------------------------------------------------
  // Workflow scenario: full lifecycle create -> assign -> approve
  // ---------------------------------------------------------------------------

  @Test
  void fullWorkflow_createAssignApprove() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibung();

    approvalService.createRequest(beschreibung, "Author", "Initial submission");
    assertEquals(PublikationStatus.IN_REVIEW, beschreibung.getPublikation().getStatus());

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
  }

  // ---------------------------------------------------------------------------
  // Workflow scenario: create -> withdraw -> create again
  // ---------------------------------------------------------------------------

  @Test
  void fullWorkflow_createWithdrawThenCreateAgain() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibung();

    approvalService.createRequest(beschreibung, "Author", "submission");
    approvalService.withdraw(beschreibung, "Author", "withdraw");

    assertEquals(PublikationStatus.DRAFT, beschreibung.getPublikation().getStatus());
    assertFalse(beschreibung.hasActiveApprovalRequest());

    // Should be able to create a new request after withdrawal
    approvalService.createRequest(beschreibung, "Author", "resubmission");
    assertTrue(beschreibung.hasActiveApprovalRequest());
  }

  // ---------------------------------------------------------------------------
  // Verify approval requests accumulate in the history
  // ---------------------------------------------------------------------------

  @Test
  void multipleRounds_accumulateApprovalRequestsInHistory() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibung();

    // First round: create and reject
    approvalService.createRequest(beschreibung, "Author", "round 1");
    approvalService.reject(beschreibung, "Editor", "no");

    // Second round: create and withdraw
    approvalService.createRequest(beschreibung, "Author", "round 2");
    approvalService.withdraw(beschreibung, "Author", "cancel");

    // Third round: create and approve
    approvalService.createRequest(beschreibung, "Author", "round 3");
    approvalService.approve(beschreibung, "Editor", "yes");

    assertEquals(3, beschreibung.getPublikation().getApprovalRequests().size());
    assertEquals(PublikationStatus.PUBLISHED, beschreibung.getPublikation().getStatus());

    long totalEvents = beschreibung.getPublikation().getApprovalRequests().stream()
        .mapToLong(r -> r.getEvents().size())
        .sum();
    assertEquals(6, totalEvents);
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
    var publikation = new PublikationBuilder(beschreibung.getPublikation())
        .withLizenz(new Lizenz(LizenzTyp.CC_BY))
        .build();
    beschreibung.setPublikation(publikation);
    return beschreibung;
  }

  private Beschreibung createBeschreibungWithActiveRequest() throws ApprovalException {
    Beschreibung beschreibung = createBeschreibung();
    approvalService.createRequest(beschreibung, "TestUser", "setup");
    return beschreibung;
  }

  private Beschreibung createBeschreibungWithNullPublikation() {
    Beschreibung beschreibung = new BeschreibungsBuilder("BESCHREIBUNG1", "KOD1",
        createIdentifikation())
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withTEIXml("<xml></xml>")
        .build();
    try {
      var field = Beschreibung.class.getDeclaredField("publikation");
      field.setAccessible(true);
      field.set(beschreibung, null);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set publikation to null via reflection", e);
    }
    return beschreibung;
  }

  private Beschreibung createBeschreibungWithoutLizenz() {
    // Build a Beschreibung with a Publikation but without setting a Lizenz
    return new BeschreibungsBuilder("BESCHREIBUNG1", "KOD1", createIdentifikation())
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withTEIXml("<xml></xml>")
        .build();
  }
}
