package de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation.PublikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.LizenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.approval.ApprovalException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.events.PublikationEvent;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.PublikationTeiSnapshotRepository;
import jakarta.enterprise.event.Event;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PublikationsServiceUnitTest {

  private BeschreibungsBoundary beschreibungsService;
  private PublikationPURLService publikationPURLService;
  private BeschreibungApprovalSperrenService approvalSperrenService;
  private PublikationApprovalService approvalService;
  private Event<PublikationEvent> publikationEventPublisher;

  private PublikationsService service;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    beschreibungsService = mock(BeschreibungsBoundary.class);
    publikationPURLService = mock(PublikationPURLService.class);
    approvalSperrenService = mock(BeschreibungApprovalSperrenService.class);
    approvalService = mock(PublikationApprovalService.class);
    publikationEventPublisher = mock(Event.class);

    service = new PublikationsService(
        beschreibungsService,
        publikationPURLService,
        approvalSperrenService,
        approvalService,
        mock(PublikationTeiSnapshotRepository.class),
        publikationEventPublisher
    );
  }

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

  private Beschreibung createBeschreibungWithType() {
    var beschreibung = new BeschreibungsBuilder("BESCHREIBUNG_INTERN", "KOD1",
        createIdentifikation())
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withTEIXml("<xml></xml>")
        .build();
    var publikation = new PublikationBuilder(beschreibung.getPublikation())
        .withLizenz(new Lizenz(LizenzTyp.CC_BY))
        .build();
    beschreibung.setPublikation(publikation);
    return beschreibung;
  }

  private Beschreibung createBeschreibungWithNullPublikation() {
    Beschreibung beschreibung = new BeschreibungsBuilder("BESCHREIBUNG1", "KOD1",
        createIdentifikation())
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withTEIXml("<xml></xml>")
        .build();
    try {
      Field field = Beschreibung.class.getDeclaredField("publikation");
      field.setAccessible(true);
      field.set(beschreibung, null);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set publikation to null via reflection", e);
    }
    return beschreibung;
  }

  @Nested
  @DisplayName("applyForPublication(Beschreibung, Lizenz, String, String)")
  class ApplyForPublicationWithLizenz {

    @Test
    @DisplayName("happy path: reloads beschreibung, sets lizenz, creates request, acquires sperre")
    void testHappyPath() throws Exception {
      Beschreibung beschreibung = createBeschreibung();
      Beschreibung reloaded = createBeschreibung();
      Lizenz lizenz = new Lizenz(LizenzTyp.CC_BY_SA);

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(Optional.of(reloaded));
      when(approvalService.createRequest(reloaded, "TestUser", "comment"))
          .thenReturn(reloaded);

      Beschreibung result = service.applyForPublication(beschreibung, lizenz, "TestUser",
          "comment");

      assertSame(reloaded, result);
      assertEquals(lizenz, reloaded.getPublikation().getLizenz());
      verify(approvalSperrenService).checkHasSperre(beschreibung.getId());
      verify(beschreibungsService).findById(beschreibung.getId());
      verify(approvalService).createRequest(reloaded, "TestUser", "comment");
      verify(approvalSperrenService).acquireApprovalSperre(reloaded.getId());
    }

    @Test
    @DisplayName("throws PublikationException when sperre check rejects the request")
    void testBlockedBySperre() throws Exception {
      Beschreibung beschreibung = createBeschreibung();
      doThrow(
          new PublikationException("publikation_error_beschreibung_locked", beschreibung.getId()))
          .when(approvalSperrenService).checkHasSperre(beschreibung.getId());

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.applyForPublication(beschreibung, new Lizenz(LizenzTyp.CC_BY),
              "user", "comment"));

      assertEquals("publikation_error_beschreibung_locked", ex.getMessage());
      verify(approvalService, never()).createRequest(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("throws NullPointerException when beschreibung is null")
    void testNullBeschreibung() {
      Lizenz license = new Lizenz(LizenzTyp.CC_BY);
      assertThrows(NullPointerException.class,
          () -> service.applyForPublication(null, license,
              "user", "comment"));
    }

    @Test
    @DisplayName("throws NullPointerException when publikation is null")
    void testNullPublikation() {
      Beschreibung beschreibung = createBeschreibungWithNullPublikation();
      Lizenz license = new Lizenz(LizenzTyp.CC_BY);

      assertThrows(NullPointerException.class,
          () -> service.applyForPublication(beschreibung, license,
              "user", "comment"));
    }

    @Test
    @DisplayName("throws NullPointerException when license is null")
    void testNullLicense() {
      Beschreibung beschreibung = createBeschreibung();

      assertThrows(NullPointerException.class,
          () -> service.applyForPublication(beschreibung, (Lizenz) null, "user", "comment"));
    }

    @Test
    @DisplayName("wraps ApprovalException in PublikationException")
    void testApprovalExceptionWrapped() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(
          Optional.of(beschreibung));
      when(approvalService.createRequest(any(), anyString(), anyString()))
          .thenThrow(new ApprovalException("already exists"));

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.applyForPublication(beschreibung, new Lizenz(LizenzTyp.CC_BY),
              "user", "comment"));

      assertInstanceOf(ApprovalException.class, ex.getCause());
      assertEquals("publikation_error_approval_creation", ex.getMessage());
    }

    @Test
    @DisplayName("wraps DokumentSperreException in PublikationException")
    void testDokumentSperreExceptionWrapped() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(
          Optional.of(beschreibung));
      when(approvalService.createRequest(any(), anyString(), anyString())).thenReturn(beschreibung);
      doThrow(new DokumentSperreException("lock error"))
          .when(approvalSperrenService).acquireApprovalSperre(anyString());

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.applyForPublication(beschreibung, new Lizenz(LizenzTyp.CC_BY),
              "user", "comment"));

      assertInstanceOf(DokumentSperreException.class, ex.getCause());
      assertEquals("publikation_error_acquiring_approval_lock", ex.getMessage());
    }

    @Test
    @DisplayName("wraps SperreAlreadyExistException in PublikationException")
    void testSperreAlreadyExistExceptionWrapped() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(
          Optional.of(beschreibung));
      when(approvalService.createRequest(any(), anyString(), anyString())).thenReturn(beschreibung);
      doThrow(new SperreAlreadyExistException("already locked", Collections.emptyList()))
          .when(approvalSperrenService).acquireApprovalSperre(anyString());

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.applyForPublication(beschreibung, new Lizenz(LizenzTyp.CC_BY),
              "user", "comment"));

      assertInstanceOf(SperreAlreadyExistException.class, ex.getCause());
    }

    @Test
    @DisplayName("acquires approval sperre via approvalSperrenService")
    void testAcquiresApprovalSperre() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(
          Optional.of(beschreibung));
      when(approvalService.createRequest(any(), anyString(), anyString())).thenReturn(beschreibung);

      service.applyForPublication(beschreibung, new Lizenz(LizenzTyp.CC_BY), "user", "comment");

      verify(approvalSperrenService).acquireApprovalSperre(beschreibung.getId());
    }
  }

  @Nested
  @DisplayName("applyForPublication(String, LizenzTyp, String, String)")
  class ApplyForPublicationByIdString {

    @Test
    @DisplayName("happy path: checks sperre, finds beschreibung, delegates")
    void testHappyPath() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId()))
          .thenReturn(Optional.of(beschreibung));
      when(approvalService.createRequest(any(), anyString(), any()))
          .thenReturn(beschreibung);

      Beschreibung result = service.applyForPublication(
          beschreibung.getId(), LizenzTyp.CC_BY, "TestUser", "comment");

      assertNotNull(result);

      verify(approvalSperrenService).checkHasSperre(beschreibung.getId());
      verify(beschreibungsService, times(2)).findById(beschreibung.getId());
    }

    @Test
    @DisplayName("throws PublikationException when sperre check rejects the request")
    void testBlockedBySperre() throws Exception {
      Beschreibung beschreibung = createBeschreibung();
      when(beschreibungsService.findById(beschreibung.getId()))
          .thenReturn(Optional.of(beschreibung));
      doThrow(
          new PublikationException("publikation_error_beschreibung_locked", beschreibung.getId()))
          .when(approvalSperrenService).checkHasSperre(beschreibung.getId());

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.applyForPublication(beschreibung.getId(), LizenzTyp.CC_BY, "user",
              "comment"));

      assertEquals("publikation_error_beschreibung_locked", ex.getMessage());
      assertEquals(beschreibung.getId(), ex.getBeschreibungId());
    }

    @Test
    @DisplayName("throws PublikationException when beschreibung not found")
    void testBeschreibungNotFound() {
      when(beschreibungsService.findById("NONEXISTENT")).thenReturn(Optional.empty());

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.applyForPublication("NONEXISTENT", LizenzTyp.CC_BY, "user", "comment"));

      assertEquals("beschreibung_not_found", ex.getMessage());
      assertEquals("NONEXISTENT", ex.getBeschreibungId());
    }

    @Test
    @DisplayName("throws NullPointerException when licenseType is null")
    void testNullLicenseType() {
      assertThrows(NullPointerException.class,
          () -> service.applyForPublication("BSB123", null, "user", "comment"));
    }
  }

  @Nested
  @DisplayName("applyForPublication(Beschreibung, LizenzTyp, String, String)")
  class ApplyForPublicationWithLizenzTyp {

    @Test
    @DisplayName("happy path: checks sperre and delegates with new Lizenz")
    void testHappyPath() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId()))
          .thenReturn(Optional.of(beschreibung));
      when(approvalService.createRequest(any(), anyString(), anyString()))
          .thenReturn(beschreibung);

      Beschreibung result = service.applyForPublication(
          beschreibung, LizenzTyp.CC_BY_SA, "TestUser", "comment");

      assertNotNull(result);
      verify(approvalSperrenService).checkHasSperre(beschreibung.getId());
    }

    @Test
    @DisplayName("throws PublikationException when beschreibung has sperre")
    void testBlockedBySperre() throws Exception {
      Beschreibung beschreibung = createBeschreibung();
      doThrow(
          new PublikationException("publikation_error_beschreibung_locked", beschreibung.getId()))
          .when(approvalSperrenService).checkHasSperre(beschreibung.getId());

      assertThrows(PublikationException.class,
          () -> service.applyForPublication(beschreibung, LizenzTyp.CC_BY, "user", "comment"));
    }

    @Test
    @DisplayName("throws NullPointerException when licenseType is null")
    void testNullLicenseType() {
      Beschreibung beschreibung = createBeschreibung();
      assertThrows(NullPointerException.class,
          () -> service.applyForPublication(beschreibung, (LizenzTyp) null, "user", "comment"));
    }
  }

  @Nested
  @DisplayName("assignReviewer")
  class AssignReviewer {

    @Test
    @DisplayName("happy path: reloads beschreibung and delegates to approvalService")
    void testHappyPath() throws Exception {
      Beschreibung beschreibung = createBeschreibung();
      Beschreibung reloaded = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(Optional.of(reloaded));
      when(approvalService.assignReviewer(reloaded, "Reviewer")).thenReturn(reloaded);

      Beschreibung result = service.assignReviewer(beschreibung, "Reviewer");

      assertSame(reloaded, result);
      verify(beschreibungsService).findById(beschreibung.getId());
      verify(approvalService).assignReviewer(reloaded, "Reviewer");
    }

    @Test
    @DisplayName("throws NullPointerException when beschreibung is null")
    void testNullBeschreibung() {
      assertThrows(NullPointerException.class,
          () -> service.assignReviewer(null, "Reviewer"));
    }

    @Test
    @DisplayName("throws NullPointerException when assignedTo is null")
    void testNullAssignedTo() {
      Beschreibung beschreibung = createBeschreibung();

      assertThrows(NullPointerException.class,
          () -> service.assignReviewer(beschreibung, null));
    }

    @Test
    @DisplayName("wraps ApprovalException in PublikationException")
    void testApprovalExceptionWrapped() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(
          Optional.of(beschreibung));
      when(approvalService.assignReviewer(any(), anyString()))
          .thenThrow(new ApprovalException("no active request"));

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.assignReviewer(beschreibung, "Reviewer"));

      assertInstanceOf(ApprovalException.class, ex.getCause());
      assertEquals("publikation_error_assigning_reviewer", ex.getMessage());
    }

    @Test
    @DisplayName("throws PublikationException when reloaded beschreibung not found")
    void testReloadFails() {
      Beschreibung beschreibung = createBeschreibung();
      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(Optional.empty());

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.assignReviewer(beschreibung, "Reviewer"));

      assertEquals("beschreibung_not_found", ex.getMessage());
    }
  }

  @Nested
  @DisplayName("approve")
  class Approve {

    @Test
    @DisplayName("happy path: reloads, adds PURLs, approves, updates solr/indexer, releases sperre")
    void testHappyPath() throws Exception {
      Beschreibung beschreibung = createBeschreibung();
      Beschreibung reloaded = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(Optional.of(reloaded));
      when(approvalService.approve(reloaded, "Reviewer", "OK")).thenReturn(reloaded);

      Beschreibung result = service.approve(beschreibung, "Reviewer", "OK");

      assertSame(reloaded, result);
      verify(approvalService).approve(reloaded, "Reviewer", "OK");
      verify(beschreibungsService).publizieren(reloaded);
      verify(publikationEventPublisher).fire(any(PublikationEvent.class));
    }

    @Test
    @DisplayName("throws NullPointerException when beschreibung is null")
    void testNullBeschreibung() {
      assertThrows(NullPointerException.class,
          () -> service.approve(null, "Reviewer", "OK"));
    }

    @Test
    @DisplayName("throws NullPointerException when reviewedBy is null")
    void testNullReviewedBy() {
      Beschreibung beschreibung = createBeschreibung();

      assertThrows(NullPointerException.class,
          () -> service.approve(beschreibung, null, "OK"));
    }

    @Test
    @DisplayName("throws NullPointerException when publikation is null")
    void testNullPublikation() {
      Beschreibung beschreibung = createBeschreibungWithNullPublikation();

      assertThrows(NullPointerException.class,
          () -> service.approve(beschreibung, "Reviewer", "OK"));
    }

    @Test
    @DisplayName("wraps ApprovalException in PublikationException")
    void testApprovalExceptionWrapped() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(
          Optional.of(beschreibung));
      when(approvalService.approve(any(), anyString(), anyString()))
          .thenThrow(new ApprovalException("no active request"));

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.approve(beschreibung, "Reviewer", "OK"));

      assertInstanceOf(ApprovalException.class, ex.getCause());
    }

    /**
     * P0 regression: the deleted {@code PublikationSucheIndexerService} wrapped Kafka/Solr failures
     * to {@code "publikation_error_suche_indexer"}. {@code publizieren} now wraps Kafka/Solr
     * failures internally to that key; {@code approveInternal} must pass the typed
     * {@link PublikationException} through untouched, not re-wrap into
     * {@code "publikation_error_internal_approval"}.
     */
    @Test
    @DisplayName("passes PublikationException from publizieren through untouched")
    void testPublizierenSucheIndexerFailureWrapped() throws Exception {
      Beschreibung beschreibung = createBeschreibung();
      PublikationException sucheIndexerFailure = new PublikationException(
          "publikation_error_suche_indexer", beschreibung.getId(),
          new ActivityStreamsException("kafka broker unreachable"));

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(
          Optional.of(beschreibung));
      when(approvalService.approve(any(), anyString(), anyString())).thenReturn(beschreibung);
      doThrow(sucheIndexerFailure).when(beschreibungsService).publizieren(any());

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.approve(beschreibung, "Reviewer", "OK"));

      assertEquals("publikation_error_suche_indexer", ex.getMessage());
      assertInstanceOf(ActivityStreamsException.class, ex.getCause());
    }

    @Test
    @DisplayName("wraps PURLException in PublikationException when PURL creation fails")
    void testPURLExceptionWrapped() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(
          Optional.of(beschreibung));
      doThrow(new PURLException("purl error"))
          .when(publikationPURLService).addNewPURLsToPublication(any());

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.approve(beschreibung, "Reviewer", "OK"));

      assertInstanceOf(PURLException.class, ex.getCause());
    }

    @Test
    @DisplayName("wraps DokumentSperreException from releaseSperre in PublikationException")
    void testDokumentSperreExceptionWrapped() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(
          Optional.of(beschreibung));
      when(approvalService.approve(any(), anyString(), anyString())).thenReturn(beschreibung);
      doThrow(new DokumentSperreException("release error"))
          .when(approvalSperrenService).releaseApprovalSperre(beschreibung.getId());

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.approve(beschreibung, "Reviewer", "OK"));

      assertInstanceOf(DokumentSperreException.class, ex.getCause());
      assertEquals("publikation_error_release_lock", ex.getMessage());
    }

    @Test
    @DisplayName("delegates sperre release to approvalSperrenService after successful approval")
    void testReleasesApprovalSperre() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(
          Optional.of(beschreibung));
      when(approvalService.approve(any(), anyString(), anyString())).thenReturn(beschreibung);

      service.approve(beschreibung, "Reviewer", "OK");

      verify(approvalSperrenService).releaseApprovalSperre(beschreibung.getId());
    }
  }

  @Nested
  @DisplayName("reject")
  class Reject {

    @Test
    @DisplayName("happy path: reloads, rejects, releases sperre")
    void testHappyPath() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId()))
          .thenReturn(Optional.of(beschreibung));
      when(approvalService.reject(beschreibung, "Reviewer", "Needs changes"))
          .thenReturn(beschreibung);

      service.reject(beschreibung, "Reviewer", "Needs changes");

      verify(approvalService).reject(beschreibung, "Reviewer", "Needs changes");
      verify(approvalSperrenService).releaseApprovalSperre(beschreibung.getId());
    }

    @Test
    @DisplayName("throws NullPointerException when beschreibung is null")
    void testNullBeschreibung() {
      assertThrows(NullPointerException.class,
          () -> service.reject(null, "Reviewer", "comment"));
    }

    @Test
    @DisplayName("throws NullPointerException when reviewedBy is null")
    void testNullReviewedBy() {
      Beschreibung beschreibung = createBeschreibung();

      assertThrows(NullPointerException.class,
          () -> service.reject(beschreibung, null, "comment"));
    }

    @Test
    @DisplayName("wraps ApprovalException in PublikationException")
    void testApprovalExceptionWrapped() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(
          Optional.of(beschreibung));
      when(approvalService.reject(any(), anyString(), anyString()))
          .thenThrow(new ApprovalException("no active request"));

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.reject(beschreibung, "Reviewer", "comment"));

      assertInstanceOf(ApprovalException.class, ex.getCause());
    }

    @Test
    @DisplayName("wraps DokumentSperreException from releaseSperre in PublikationException")
    void testDokumentSperreExceptionWrapped() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(
          Optional.of(beschreibung));
      when(approvalService.reject(any(), anyString(), anyString())).thenReturn(beschreibung);
      doThrow(new DokumentSperreException("release error"))
          .when(approvalSperrenService).releaseApprovalSperre(beschreibung.getId());

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.reject(beschreibung, "Reviewer", "comment"));

      assertInstanceOf(DokumentSperreException.class, ex.getCause());
    }
  }

  @Nested
  @DisplayName("withdraw(Beschreibung, String, String)")
  class WithdrawBeschreibung {

    @Test
    @DisplayName("happy path: reloads, withdraws, releases sperre, returns reloaded beschreibung")
    void testHappyPath() throws Exception {
      Beschreibung beschreibung = createBeschreibung();
      Beschreibung reloaded = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(Optional.of(reloaded));
      when(approvalService.withdraw(reloaded, "TestUser", "Withdrawn")).thenReturn(reloaded);

      Beschreibung result = service.withdraw(beschreibung, "TestUser", "Withdrawn");

      assertSame(reloaded, result);
      verify(approvalService).withdraw(reloaded, "TestUser", "Withdrawn");
      verify(approvalSperrenService).releaseApprovalSperre(reloaded.getId());
    }

    @Test
    @DisplayName("throws NullPointerException when beschreibung is null")
    void testNullBeschreibung() {
      assertThrows(NullPointerException.class,
          () -> service.withdraw((Beschreibung) null, "user", "comment"));
    }

    @Test
    @DisplayName("throws NullPointerException when publikation is null")
    void testNullPublikation() {
      Beschreibung beschreibung = createBeschreibungWithNullPublikation();

      assertThrows(NullPointerException.class,
          () -> service.withdraw(beschreibung, "user", "comment"));
    }

    @Test
    @DisplayName("wraps ApprovalException in PublikationException")
    void testApprovalExceptionWrapped() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(
          Optional.of(beschreibung));
      when(approvalService.withdraw(any(), anyString(), anyString()))
          .thenThrow(new ApprovalException("no active request"));

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.withdraw(beschreibung, "user", "comment"));

      assertInstanceOf(ApprovalException.class, ex.getCause());
    }

    @Test
    @DisplayName("wraps DokumentSperreException from releaseSperre in PublikationException")
    void testDokumentSperreExceptionWrapped() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(
          Optional.of(beschreibung));
      when(approvalService.withdraw(any(), anyString(), anyString())).thenReturn(beschreibung);
      doThrow(new DokumentSperreException("release error"))
          .when(approvalSperrenService).releaseApprovalSperre(beschreibung.getId());

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.withdraw(beschreibung, "user", "comment"));

      assertInstanceOf(DokumentSperreException.class, ex.getCause());
    }
  }

  @Nested
  @DisplayName("withdraw(String, String, String)")
  class WithdrawByIdString {

    @Test
    @DisplayName("happy path: finds beschreibung by ID and delegates")
    void testHappyPath() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId()))
          .thenReturn(Optional.of(beschreibung));
      when(approvalService.withdraw(any(), anyString(), anyString()))
          .thenReturn(beschreibung);

      Beschreibung result = service.withdraw(beschreibung.getId(), "TestUser", "Withdrawn");

      assertNotNull(result);
      // findById called twice: once by withdraw(String,...) to find the beschreibung,
      // once by reloadBeschreibung in withdraw(Beschreibung,...)
      verify(beschreibungsService, atLeastOnce()).findById(beschreibung.getId());
    }

    @Test
    @DisplayName("throws PublikationException when beschreibung not found")
    void testBeschreibungNotFound() {
      when(beschreibungsService.findById("NONEXISTENT")).thenReturn(Optional.empty());

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.withdraw("NONEXISTENT", "user", "comment"));

      assertEquals("beschreibung_not_found", ex.getMessage());
      assertEquals("NONEXISTENT", ex.getBeschreibungId());
    }
  }

  @Nested
  @DisplayName("publizierenOhneApproval")
  class PublizierenOhneApproval {

    @Test
    @DisplayName("skips INTERN beschreibungen")
    void testSkipsIntern() throws Exception {
      Beschreibung internBeschreibung = createBeschreibungWithType();

      service.publizierenOhneApproval(internBeschreibung);

      verifyNoInteractions(approvalService);
      verifyNoInteractions(publikationEventPublisher);
      assertEquals(PublikationStatus.DRAFT, internBeschreibung.getPublikation().getStatus());
    }

    @Test
    @DisplayName("processes EXTERN beschreibungen: creates request and approves")
    void testProcessesExtern() throws Exception {
      Beschreibung beschreibung = createBeschreibung(); // EXTERN by default

      when(approvalService.createRequest(any(), anyString(), any()))
          .thenReturn(beschreibung);
      when(approvalService.approve(any(), anyString(), anyString()))
          .thenReturn(beschreibung);

      service.publizierenOhneApproval(beschreibung);

      verify(approvalService).createRequest(any(), eq("System - Automatic Import"), eq(null));
      verify(approvalService).approve(any(), eq("System - Automatic Import"), eq(""));
      verify(publikationEventPublisher).fire(any(PublikationEvent.class));
    }

    @Test
    @DisplayName("does NOT reload from DB or acquire sperren")
    void testDoesNotAccessDatabase() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(approvalService.createRequest(any(), anyString(), any()))
          .thenReturn(beschreibung);
      when(approvalService.approve(any(), anyString(), anyString()))
          .thenReturn(beschreibung);

      service.publizierenOhneApproval(beschreibung);

      verifyNoInteractions(beschreibungsService);
      verifyNoInteractions(approvalSperrenService);
    }

    @Test
    @DisplayName("throws NullPointerException when beschreibung is null")
    void testNullBeschreibung() {
      assertThrows(NullPointerException.class,
          () -> service.publizierenOhneApproval(null));
    }

    @Test
    @DisplayName("throws NullPointerException when publikation is null")
    void testNullPublikation() {
      Beschreibung beschreibung = createBeschreibungWithNullPublikation();

      assertThrows(NullPointerException.class,
          () -> service.publizierenOhneApproval(beschreibung));
    }

    @Test
    @DisplayName("wraps ApprovalException from createRequest with key, cause and signatur+causeMessage args")
    void testExceptionWrapped() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(approvalService.createRequest(any(), any(), any()))
          .thenThrow(new ApprovalException("approval error"));

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.publizierenOhneApproval(beschreibung));

      assertEquals("publikation_automatic_approval", ex.getMessage());
      assertInstanceOf(ApprovalException.class, ex.getCause());
      assertEquals(2, ex.getMessageArgs().length);
      assertEquals("1-1-1", ex.getMessageArgs()[0]);
      assertEquals("approval error", ex.getMessageArgs()[1]);
    }

    @Test
    @DisplayName("wraps ApprovalException from approve (second call inside addPurlsAndApprove)")
    void testApprovalExceptionFromApproveWrapped() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(approvalService.createRequest(any(), any(), any()))
          .thenReturn(beschreibung);
      when(approvalService.approve(any(), any(), any()))
          .thenThrow(new ApprovalException("approve failed"));

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.publizierenOhneApproval(beschreibung));

      assertEquals("publikation_automatic_approval", ex.getMessage());
      assertInstanceOf(ApprovalException.class, ex.getCause());
      assertEquals("1-1-1", ex.getMessageArgs()[0]);
      assertEquals("approve failed", ex.getMessageArgs()[1]);
      verify(approvalService).createRequest(any(), anyString(), any());
      verify(publikationPURLService).addNewPURLsToPublication(beschreibung);
    }

    @Test
    @DisplayName("succeeds when Lizenz is null — import path does not require a licence")
    void testSucceedsWhenLizenzIsNull() throws Exception {
      Beschreibung beschreibung = createBeschreibung();
      beschreibung.getPublikation().setLizenz((Lizenz) null);

      when(approvalService.createRequest(any(), any(), any()))
          .thenReturn(beschreibung);
      when(approvalService.approve(any(), any(), any()))
          .thenReturn(beschreibung);

      service.publizierenOhneApproval(beschreibung);

      verify(approvalService).createRequest(any(), eq("System - Automatic Import"), eq(null));
      verify(approvalService).approve(any(), eq("System - Automatic Import"), eq(""));
    }

    @Test
    @DisplayName("falls back to beschreibung id when gueltigeIdentifikation is missing")
    void testFallsBackToBeschreibungIdWhenSignaturMissing() throws Exception {
      Beschreibung beschreibung = createBeschreibung();
      Field field = Beschreibung.class.getDeclaredField("gueltigeIdentifikation");
      field.setAccessible(true);
      field.set(beschreibung, null);

      when(approvalService.createRequest(any(), any(), any()))
          .thenThrow(new ApprovalException("approval error"));

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.publizierenOhneApproval(beschreibung));

      assertEquals("publikation_automatic_approval", ex.getMessage());
      assertEquals(beschreibung.getId(), ex.getMessageArgs()[0]);
    }

    @Test
    @DisplayName("wraps PURLException with key, cause and args")
    void testWrapsPurlException() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(approvalService.createRequest(any(), any(), any()))
          .thenReturn(beschreibung);
      doThrow(new PURLException("purl backend down"))
          .when(publikationPURLService).addNewPURLsToPublication(any());

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.publizierenOhneApproval(beschreibung));

      assertEquals("publikation_automatic_approval", ex.getMessage());
      assertInstanceOf(PURLException.class, ex.getCause());
      assertEquals("1-1-1", ex.getMessageArgs()[0]);
      assertNotNull(ex.getMessageArgs()[1]);
    }

    @Test
    @DisplayName("wraps unexpected RuntimeException with key, cause and args")
    void testWrapsRuntimeException() throws Exception {
      Beschreibung beschreibung = createBeschreibung();

      when(approvalService.createRequest(any(), any(), any()))
          .thenReturn(beschreibung);
      when(approvalService.approve(any(), any(), any()))
          .thenThrow(new RuntimeException("boom"));

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.publizierenOhneApproval(beschreibung));

      assertEquals("publikation_automatic_approval", ex.getMessage());
      assertInstanceOf(RuntimeException.class, ex.getCause());
      assertEquals("1-1-1", ex.getMessageArgs()[0]);
      assertEquals("boom", ex.getMessageArgs()[1]);
    }
  }

  @Nested
  @DisplayName("updateExistingBeschreibung")
  class UpdateExistingBeschreibung {

    @Test
    @DisplayName("sets publikationsDatumHSP to now")
    void testSetsPublikationsDatumHSP() {
      Beschreibung beschreibung = createBeschreibung();
      Publikation publikation = beschreibung.getPublikation();

      service.updateExistingBeschreibung(beschreibung);

      assertNotNull(publikation.getPublikationsDatumHSP());
    }

    @Test
    @DisplayName("throws NullPointerException when beschreibung is null")
    void testNullBeschreibung() {
      assertThrows(NullPointerException.class,
          () -> service.updateExistingBeschreibung(null));
    }

    @Test
    @DisplayName("throws NullPointerException when publikation is null")
    void testNullPublikation() {
      Beschreibung beschreibung = createBeschreibungWithNullPublikation();

      assertThrows(NullPointerException.class,
          () -> service.updateExistingBeschreibung(beschreibung));
    }
  }

  @Nested
  @DisplayName("reloadBeschreibung")
  class ReloadBeschreibung {

    @Test
    @DisplayName("returns reloaded instance when found")
    void testReloadByIdString() throws PublikationException {
      Beschreibung reloaded = createBeschreibung();

      when(beschreibungsService.findById("BSB123")).thenReturn(Optional.of(reloaded));

      Beschreibung result = service.reloadBeschreibung("BSB123");

      assertSame(reloaded, result);
    }

    @Test
    @DisplayName("throws PublikationException when not found")
    void testReloadByIdStringNotFound() {
      when(beschreibungsService.findById("NONEXISTENT")).thenReturn(Optional.empty());

      PublikationException ex = assertThrows(PublikationException.class,
          () -> service.reloadBeschreibung("NONEXISTENT"));

      assertEquals("beschreibung_not_found", ex.getMessage());
      assertEquals("NONEXISTENT", ex.getBeschreibungId());
    }
  }

  @Nested
  @DisplayName("findAllBeschreibungWithActiveApprovalRequest")
  class FindAllWithActiveApprovalRequest {

    @Test
    @DisplayName("delegates to beschreibungsService")
    void testDelegates() {
      Beschreibung b1 = createBeschreibung();
      List<Beschreibung> expected = List.of(b1);
      when(beschreibungsService.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(expected);

      List<Beschreibung> result = service.findAllBeschreibungWithActiveApprovalRequest();

      assertSame(expected, result);
      verify(beschreibungsService).findAllBeschreibungWithActiveApprovalRequest();
    }

    @Test
    @DisplayName("returns empty list when no active approval requests")
    void testReturnsEmptyList() {
      when(beschreibungsService.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      List<Beschreibung> result = service.findAllBeschreibungWithActiveApprovalRequest();

      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("Workflow scenarios")
  class WorkflowScenarios {

    @Test
    @DisplayName("approve delegates PURL creation to publikationPURLService before approving")
    void testApproveDelegatesPURLCreation() throws Exception {
      Beschreibung beschreibung = createBeschreibung();
      Beschreibung reloaded = createBeschreibung();

      when(beschreibungsService.findById(beschreibung.getId())).thenReturn(
          Optional.of(reloaded));
      when(approvalService.approve(any(), anyString(), anyString())).thenReturn(reloaded);
      doNothing().when(publikationPURLService).addNewPURLsToPublication(any());

      service.approve(beschreibung, "Reviewer", "OK");

      verify(publikationPURLService).addNewPURLsToPublication(reloaded);
    }
  }

  /**
   * Regression pin for the transaction-rollback contract on {@link PublikationsService#approve}.
   * {@link PublikationException} extends {@link RuntimeException} specifically so default JTA
   * {@code @Transactional} rolls back on it automatically (no {@code rollbackOn} needed). If
   * someone ever flips it back to a checked exception, a Solr/indexer failure propagated from
   * {@link PublikationSucheIndexerService} would silently leak past {@code approve()} with the
   * approval state committed to the database while Solr stayed stale. This test locks in the
   * inheritance so the downgrade is caught immediately.
   */
  @Nested
  @DisplayName("Transaction-rollback contract")
  class TransactionRollbackContract {

    @Test
    @DisplayName("PublikationException extends RuntimeException so @Transactional rolls back automatically")
    void publikationException_isRuntimeException() {
      assertTrue(RuntimeException.class.isAssignableFrom(PublikationException.class),
          "PublikationException must extend RuntimeException so the default JTA "
              + "@Transactional(rollbackOn = {RuntimeException.class, Error.class}) contract "
              + "rolls back on Solr/indexer failures propagated from PublikationSucheIndexerService. "
              + "Flipping this to a checked exception would silently re-introduce the "
              + "'DB says published, Solr stale' bug.");
    }
  }

  @Test
  @DisplayName("does not duplicate hsp-ID in TEI XML")
  void testDoesNotDuplicateHspId() throws Exception {

    Path teiFilePath =
        Paths.get("src", "test", "resources", "tei-msDesc_Westphal.xml");

    String teiXml = Files.readString(teiFilePath);

    Beschreibung beschreibung =
        new BeschreibungsBuilder(
            "BESCHREIBUNG1",
            "KOD1",
            createIdentifikation())
            .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
            .withTEIXml(teiXml)
            .build();

    var publikation = new PublikationBuilder(beschreibung.getPublikation())
        .withLizenz(new Lizenz(LizenzTyp.CC_BY))
        .build();
    beschreibung.setPublikation(publikation);

    when(approvalService.createRequest(any(), any(), any()))
        .thenReturn(beschreibung);

    when(approvalService.approve(any(), any(), any()))
        .thenReturn(beschreibung);

    String hspKodId = "HSP-a8b3c8c3-0085-3af5-9886-73fc0609d390";

    assertEquals(
        1,
        StringUtils.countMatches(
            beschreibung.getTeiXML(),
            "<altIdentifier type=\"hsp-ID\">"));

    assertEquals(
        1,
        StringUtils.countMatches(
            beschreibung.getTeiXML(),
            hspKodId));

    service.publizierenOhneApproval(beschreibung);

    assertEquals(
        1,
        StringUtils.countMatches(
            beschreibung.getTeiXML(),
            "<altIdentifier type=\"hsp-ID\">"));

    assertEquals(
        1,
        StringUtils.countMatches(
            beschreibung.getTeiXML(),
            hspKodId));
  }
}
