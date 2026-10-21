package de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.PublikationTeiSnapshot;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.LizenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.approval.ApprovalBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.approval.ApprovalException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.events.PublikationEvent;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.PublikationTeiSnapshotRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.tei.TeiSnapshotZipper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

/**
 * Service for managing publications and the publication approval workflow.
 */
@Slf4j
@ApplicationScoped
public class PublikationsService {

  private static final String REVIEWED_BY_MUST_NOT_BE_NULL = "ReviewedBy must not be null";
  private static final String BESCHREIBUNG_MUST_NOT_BE_NULL = "Beschreibung must not be null";
  private static final String BESCHREIBUNG_S_PUBLIKATION_MUST_NOT_BE_NULL = "Beschreibung's Publikation must not be null";
  private static final String ASSIGNED_TO_MUST_NOT_BE_NULL = "AssignedTo must not be null";
  private static final String BESCHREIBUNG_NOT_FOUND = "beschreibung_not_found";
  private static final String LICENSE_TYPE_MUST_NOT_BE_NULL = "License type must not be null";
  private static final String ERROR_DURING_AUTOMATIC_PUBLICATION_FOR_BESCHREIBUNG = "publikation_automatic_approval";

  private BeschreibungsBoundary beschreibungsService;
  private PublikationPURLService publikationPURLService;
  private BeschreibungApprovalSperrenService approvalSperrenService;
  private ApprovalBoundary<Beschreibung> approvalService;
  private PublikationTeiSnapshotRepository teiSnapshotRepository;
  private Event<PublikationEvent> publikationEventPublisher;

  protected PublikationsService() {
  }

  @Inject
  public PublikationsService(
      BeschreibungsBoundary beschreibungsService,
      PublikationPURLService publikationPURLService,
      BeschreibungApprovalSperrenService approvalSperrenService,
      PublikationApprovalService approvalService,
      PublikationTeiSnapshotRepository teiSnapshotRepository,
      Event<PublikationEvent> publikationEventPublisher
  ) {
    this.beschreibungsService = beschreibungsService;
    this.publikationPURLService = publikationPURLService;
    this.approvalSperrenService = approvalSperrenService;
    this.approvalService = approvalService;
    this.teiSnapshotRepository = teiSnapshotRepository;
    this.publikationEventPublisher = publikationEventPublisher;
  }

  /**
   * Submits a publication for approval. Creates a new ApprovalRequest. Rejects the call if the
   * Beschreibung is currently locked by any Sperre.
   *
   * @param beschreibung the Beschreibung whose publication should be approved
   * @param license      the license to attach to the Publikation
   * @param requestedBy  the user requesting approval
   * @param comment      optional comment
   * @return the updated Beschreibung
   * @throws PublikationException if submission fails or the Beschreibung is locked
   */
  @Transactional
  public Beschreibung applyForPublication(Beschreibung beschreibung, Lizenz license,
      String requestedBy, String comment) {
    Objects.requireNonNull(beschreibung, BESCHREIBUNG_MUST_NOT_BE_NULL);
    Objects.requireNonNull(beschreibung.getPublikation(), "Publikation must not be null");
    Objects.requireNonNull(license, "License must not be null");
    approvalSperrenService.checkHasSperre(beschreibung.getId());

    log.info("Submitting approval request for Beschreibung {} by {}", beschreibung.getId(),
        requestedBy);

    Beschreibung reloadedBeschreibung = reloadBeschreibung(beschreibung.getId());
    reloadedBeschreibung.getPublikation().setLizenz(license);
    try {
      approvalService.createRequest(reloadedBeschreibung, requestedBy, comment);
      approvalSperrenService.acquireApprovalSperre(reloadedBeschreibung.getId());
      return reloadedBeschreibung;
    } catch (ApprovalException e) {
      log.error("Error creating approval request for Beschreibung {}", reloadedBeschreibung.getId(),
          e);
      throw new PublikationException(
          "publikation_error_approval_creation", reloadedBeschreibung.getId(), e);

    } catch (DokumentSperreException | SperreAlreadyExistException e) {
      log.error("Error acquiring approval lock for Beschreibung {}", reloadedBeschreibung.getId(),
          e);
      throw new PublikationException(
          "publikation_error_acquiring_approval_lock",
          reloadedBeschreibung.getId(), e);
    }
  }

  /**
   * {@code String}-ID overload of
   * {@link #applyForPublication(Beschreibung, Lizenz, String, String)}. Looks up the Beschreibung
   * by id and delegates.
   *
   * @throws PublikationException if the Beschreibung is not found, is locked, or submission fails
   */
  @Transactional
  public Beschreibung applyForPublication(String beschreibungID, LizenzTyp licenseType,
      String requestedBy, String comment) {
    Objects.requireNonNull(licenseType, LICENSE_TYPE_MUST_NOT_BE_NULL);
    Beschreibung beschreibung = beschreibungsService.findById(beschreibungID)
        .orElseThrow(() -> new PublikationException(
            BESCHREIBUNG_NOT_FOUND, beschreibungID));
    return applyForPublication(beschreibung, new Lizenz(licenseType), requestedBy, comment);
  }

  /**
   * {@link LizenzTyp} overload of
   * {@link #applyForPublication(Beschreibung, Lizenz, String, String)}. Wraps the type in a fresh
   * {@link Lizenz} and delegates.
   *
   * @throws PublikationException if the Beschreibung is locked or submission fails
   */
  @Transactional
  public Beschreibung applyForPublication(Beschreibung beschreibung, LizenzTyp licenseType,
      String requestedBy, String comment) {
    Objects.requireNonNull(licenseType, LICENSE_TYPE_MUST_NOT_BE_NULL);
    return applyForPublication(beschreibung, new Lizenz(licenseType), requestedBy, comment);
  }

  /**
   * Assigns an approval request to a reviewer.
   *
   * @param beschreibung the Beschreibung whose approval request should be assigned
   * @param assignedTo   the reviewer to assign to
   * @return the updated Beschreibung
   * @throws PublikationException if the assignment fails
   */
  @Transactional
  public Beschreibung assignReviewer(Beschreibung beschreibung, String assignedTo) {
    Objects.requireNonNull(beschreibung, BESCHREIBUNG_MUST_NOT_BE_NULL);
    Objects.requireNonNull(assignedTo, ASSIGNED_TO_MUST_NOT_BE_NULL);
    log.info("Assigning approval request for Beschreibung {} to {}", beschreibung.getId(),
        assignedTo);

    Beschreibung reloadedBeschreibung = reloadBeschreibung(beschreibung.getId());

    try {
      approvalService.assignReviewer(reloadedBeschreibung, assignedTo);
    } catch (ApprovalException e) {
      throw new PublikationException("publikation_error_assigning_reviewer",
          reloadedBeschreibung.getId(), e);
    }
    return reloadedBeschreibung;
  }

  /**
   * Approves a publication request and publishes the Beschreibung.
   *
   * @param beschreibung the Beschreibung to publish
   * @param reviewedBy   the reviewer who approved
   * @param comment      optional comment
   * @return the updated Beschreibung
   * @throws PublikationException if approval fails
   */
  @Transactional
  public Beschreibung approve(Beschreibung beschreibung, String reviewedBy, String comment) {
    Objects.requireNonNull(beschreibung, BESCHREIBUNG_MUST_NOT_BE_NULL);
    Objects.requireNonNull(reviewedBy, REVIEWED_BY_MUST_NOT_BE_NULL);
    Objects.requireNonNull(beschreibung.getPublikation(),
        BESCHREIBUNG_S_PUBLIKATION_MUST_NOT_BE_NULL);

    Beschreibung reloadedBeschreibung = reloadBeschreibung(beschreibung.getId());
    return approveInternal(reloadedBeschreibung, reviewedBy, comment);
  }

  private Beschreibung approveInternal(Beschreibung beschreibung, String reviewedBy,
      String comment) {
    Objects.requireNonNull(beschreibung, BESCHREIBUNG_MUST_NOT_BE_NULL);
    Objects.requireNonNull(reviewedBy, REVIEWED_BY_MUST_NOT_BE_NULL);
    Objects.requireNonNull(beschreibung.getPublikation(),
        BESCHREIBUNG_S_PUBLIKATION_MUST_NOT_BE_NULL);
    log.info("Approving publication for Beschreibung {} by {}", beschreibung.getId(), reviewedBy);

    try {
      addPurlsAndApprove(beschreibung, reviewedBy, comment);
      beschreibungsService.publizieren(beschreibung);
      captureTeiSnapshot(beschreibung);
      approvalSperrenService.releaseApprovalSperre(beschreibung.getId());
      publikationEventPublisher.fire(new PublikationEvent());
      return beschreibung;
    } catch (PublikationException e) {
      throw e;
    } catch (PURLException e) {
      throw new PublikationException(
          "publikation_error_purl", beschreibung.getId(), e);
    } catch (DokumentSperreException e) {
      throw new PublikationException(
          "publikation_error_release_lock", beschreibung.getId(), e);
    } catch (UncheckedIOException e) {
      throw new PublikationException(
          "publikation_error_tei", beschreibung.getId(), e);
    } catch (Exception e) {
      throw new PublikationException(
          "publikation_error_internal_approval", beschreibung.getId(), e);
    }
  }

  /**
   * Shared step used by both {@link #approveInternal} and {@link #publizierenOhneApproval}: attach
   * newly-created internal PURLs to the Publikation, then mark the approval request as approved.
   */
  private void addPurlsAndApprove(Beschreibung beschreibung, String reviewedBy, String comment)
      throws PURLException, ApprovalException {
    publikationPURLService.addNewPURLsToPublication(beschreibung);
    approvalService.approve(beschreibung, reviewedBy, comment);
  }

  /**
   * Captures TEI snapshots for a batch of already-published Beschreibungen (used by the import).
   *
   * <p><b>Must be called only AFTER the Beschreibungen and their Publikationen have been
   * persisted.</b> A {@link PublikationTeiSnapshot} is keyed solely by {@code publikationId} (a
   * bare string that doubles as the FK to {@code publikation.id}); it has no JPA association, so
   * Hibernate cannot order the insert after the parent. Capturing it while the parent Publikation
   * row does not yet exist (e.g. during import, before the batch save) trips
   * {@code fk_publikation_tei_publikation}. The import therefore calls this only after
   * {@code publizierenBatch} has persisted the batch. Callers must pass published Beschreibungen
   * only — each must have a TEI (enforced by {@link #captureTeiSnapshot}).
   */
  @Transactional(TxType.MANDATORY)
  public void captureTeiSnapshots(List<Beschreibung> beschreibungen) {
    beschreibungen.forEach(this::captureTeiSnapshot);
  }

  /**
   * Freezes the published TEI-XML of the Beschreibung as a ZIP on its Publikation. Called AFTER
   * {@code publizieren} so the captured TEI reflects post-enrichment state (e.g. Entstehungsort
   * resolution). Runs inside the caller's transaction; the snapshot is committed atomically with
   * the status transition. Throws if no TEI is present — a published Beschreibung without TEI is a
   * domain invariant violation, not a benign condition.
   *
   * <p>The caller must ensure the Publikation row already exists (or is scheduled for insert
   * before this snapshot) — see {@link #captureTeiSnapshots(List)}.
   */
  @Transactional(TxType.MANDATORY)
  public void captureTeiSnapshot(Beschreibung beschreibung) {
    String teiXml = beschreibung.getTeiXML();
    if (StringUtils.isBlank(teiXml)) {
      throw new PublikationException("publikation_error_tei", beschreibung.getId());
    }
    byte[] zipBytes = TeiSnapshotZipper.zip(teiXml, beschreibung.getId());
    String publikationId = beschreibung.getPublikation().getId();
    teiSnapshotRepository.save(new PublikationTeiSnapshot(publikationId, zipBytes));
  }


  /**
   * Rejects a publication request.
   *
   * @param beschreibung the Beschreibung whose publication was rejected
   * @param reviewedBy   the reviewer who rejected
   * @param comment      optional comment explaining the rejection
   * @throws PublikationException if rejection fails
   */
  @Transactional
  public void reject(Beschreibung beschreibung, String reviewedBy, String comment) {
    Objects.requireNonNull(beschreibung, BESCHREIBUNG_MUST_NOT_BE_NULL);
    Objects.requireNonNull(reviewedBy, REVIEWED_BY_MUST_NOT_BE_NULL);
    log.info("Rejecting publication for Beschreibung {} by {}", beschreibung.getId(), reviewedBy);

    Beschreibung reloadedBeschreibung = reloadBeschreibung(beschreibung.getId());

    try {
      approvalService.reject(reloadedBeschreibung, reviewedBy, comment);
      approvalSperrenService.releaseApprovalSperre(reloadedBeschreibung.getId());
    } catch (ApprovalException e) {
      log.error("Error rejecting publication for Beschreibung {}", reloadedBeschreibung.getId(), e);
      throw new PublikationException("publikation_error_rejecting_publication",
          reloadedBeschreibung.getId(), e);
    } catch (DokumentSperreException e) {
      log.error("Error releasing approval lock for Beschreibung {}", reloadedBeschreibung.getId(),
          e);
      throw new PublikationException("publikation_error_release_lock",
          reloadedBeschreibung.getId(), e);
    }
  }

  /**
   * Withdraws a pending approval request.
   *
   * @param beschreibung the Beschreibung whose approval request should be withdrawn
   * @return the updated Beschreibung
   * @throws PublikationException if withdrawal fails
   */
  @Transactional
  public Beschreibung withdraw(Beschreibung beschreibung, String requestedBy, String comment) {
    Objects.requireNonNull(beschreibung, BESCHREIBUNG_MUST_NOT_BE_NULL);
    Objects.requireNonNull(beschreibung.getPublikation(),
        BESCHREIBUNG_S_PUBLIKATION_MUST_NOT_BE_NULL);
    log.info("Withdrawing approval request for Beschreibung {}", beschreibung.getId());

    Beschreibung reloadedBeschreibung = reloadBeschreibung(beschreibung.getId());

    try {
      approvalService.withdraw(reloadedBeschreibung, requestedBy, comment);
      approvalSperrenService.releaseApprovalSperre(reloadedBeschreibung.getId());
      return reloadedBeschreibung;
    } catch (ApprovalException e) {
      log.error("Error withdrawing approval for Beschreibung {}", reloadedBeschreibung.getId(), e);
      throw new PublikationException("publikation_error_withdrawing_approval",
          reloadedBeschreibung.getId(), e);
    } catch (DokumentSperreException e) {
      throw new PublikationException(
          "publikation_error_release_lock", reloadedBeschreibung.getId(), e);
    }
  }

  /**
   * {@code String}-ID overload of {@link #withdraw(Beschreibung, String, String)}.
   *
   * @throws PublikationException if the Beschreibung is not found or withdrawal fails
   */
  @Transactional
  public Beschreibung withdraw(String beschreibungID, String requestedBy, String comment) {
    Beschreibung beschreibung = beschreibungsService.findById(beschreibungID)
        .orElseThrow(() -> new PublikationException(BESCHREIBUNG_NOT_FOUND, beschreibungID));
    return withdraw(beschreibung, requestedBy, comment);
  }

  /**
   * Publishes an EXTERN Beschreibung during import without redaktionelle Approval. EXTERN
   * Beschreibungen are mirrored from external systems and are already published there; they do not
   * go through our editorial review. A dummy ApprovalRequest ("System - Automatic Import") is still
   * created for audit trail consistency.
   *
   * <p>Works entirely in-memory without reloading from DB or acquiring sperren, since the entity
   * may not yet be persisted. For that reason it does <b>not</b> capture the TEI snapshot here: the
   * Publikation row does not exist yet, so writing the snapshot would violate
   * {@code fk_publikation_tei_publikation}. The import captures snapshots via
   * {@link #captureTeiSnapshots(List)} after the batch has been persisted.
   *
   * <p><b>Restricted use</b>: only the import pipeline ({@code BeschreibungsImportService}) may
   * call this. Calling it from a redaktioneller workflow would bypass the Prüfungsworkflow. The
   * restriction is enforced by code review, not by compile-time or test-time checks.
   *
   * @param beschreibung the Beschreibung to publish
   * @throws PublikationException if publication fails
   */
  @Transactional(TxType.MANDATORY)
  public void publizierenOhneApproval(Beschreibung beschreibung) {
    Objects.requireNonNull(beschreibung, BESCHREIBUNG_MUST_NOT_BE_NULL);
    Objects.requireNonNull(beschreibung.getPublikation(),
        BESCHREIBUNG_S_PUBLIKATION_MUST_NOT_BE_NULL);
    VerwaltungsTyp verwaltungstyp = beschreibung.getVerwaltungsTyp();
    log.info("Automatic publication for Beschreibung {} (type: {})",
        beschreibung.getId(), verwaltungstyp);

    if (VerwaltungsTyp.EXTERN != verwaltungstyp) {
      log.debug("Skipping automatic publication for INTERN Beschreibung {}",
          beschreibung.getId());
      return;
    }

    String signatur = beschreibung.getGueltigeIdentifikation()
        .map(Identifikation::getIdent)
        .orElse(beschreibung.getId());

    try {
      approvalService.createRequest(beschreibung, "System - Automatic Import", null);
      addPurlsAndApprove(beschreibung, "System - Automatic Import", "");
      publikationEventPublisher.fire(new PublikationEvent());

      log.info("Automatic publication successful for Beschreibung {}", beschreibung.getId());
    } catch (ApprovalException e) {
      log.error("Error during automatic approval for Beschreibung {} ({})",
          signatur, beschreibung.getId(), e);
      throw new PublikationException(
          ERROR_DURING_AUTOMATIC_PUBLICATION_FOR_BESCHREIBUNG,
          beschreibung.getId(), e, signatur, String.valueOf(e.getMessage()));
    } catch (PURLException e) {
      log.error("Error creating PURL during automatic publication for Beschreibung {} ({})",
          signatur, beschreibung.getId(), e);
      throw new PublikationException(
          ERROR_DURING_AUTOMATIC_PUBLICATION_FOR_BESCHREIBUNG,
          beschreibung.getId(), e, signatur, String.valueOf(e.getMessage()));
    } catch (RuntimeException e) {
      log.error("Unexpected error during automatic publication for Beschreibung {} ({})",
          signatur, beschreibung.getId(), e);
      throw new PublikationException(
          ERROR_DURING_AUTOMATIC_PUBLICATION_FOR_BESCHREIBUNG,
          beschreibung.getId(), e, signatur, String.valueOf(e.getMessage()));
    }
  }

  /**
   * Updates the HSP publication date for an existing published Beschreibung.
   *
   * @param beschreibung the Beschreibung to update
   */
  @Transactional
  public void updateExistingBeschreibung(Beschreibung beschreibung) {
    Objects.requireNonNull(beschreibung, BESCHREIBUNG_MUST_NOT_BE_NULL);
    Objects.requireNonNull(beschreibung.getPublikation(),
        BESCHREIBUNG_S_PUBLIKATION_MUST_NOT_BE_NULL);
    Publikation publikation = beschreibung.getPublikation();

    publikation.setPublikationsDatumHSP(LocalDateTime.now(ZoneId.systemDefault()));
    log.info("HSP publication date updated for Publikation {}", publikation.getId());
  }

  /**
   * Reloads a Beschreibung from the repository by id.
   *
   * @throws PublikationException if no Beschreibung is found for the given id
   */
  public Beschreibung reloadBeschreibung(String beschreibungId) {
    return beschreibungsService.findById(beschreibungId)
        .orElseThrow(() -> new PublikationException(
            BESCHREIBUNG_NOT_FOUND, beschreibungId));
  }

  public List<Beschreibung> findAllBeschreibungWithActiveApprovalRequest() {
    return beschreibungsService.findAllBeschreibungWithActiveApprovalRequest();
  }

  /**
   * Loads the captured TEI ZIP for a Beschreibung's Publikation. Empty when the Beschreibung is not
   * found, has no Publikation, or has no snapshot row yet.
   */
  public Optional<byte[]> findTeiSnapshotZip(String beschreibungId) {
    return beschreibungsService.findById(beschreibungId)
        .map(Beschreibung::getPublikation)
        .map(Publikation::getId)
        .flatMap(teiSnapshotRepository::findByIdOptional)
        .map(PublikationTeiSnapshot::getTeiZip);
  }
}
