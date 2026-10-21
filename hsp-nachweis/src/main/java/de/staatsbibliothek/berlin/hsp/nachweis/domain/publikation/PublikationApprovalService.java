package de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.ApprovalRequest;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.approval.ApprovalBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.approval.ApprovalException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing publication approval requests. Encapsulates all approval workflow operations
 * and repository access. This service should only be used by {@link PublikationsService}.
 */
@Slf4j
@ApplicationScoped
public class PublikationApprovalService implements ApprovalBoundary<Beschreibung>, Serializable {

  @Serial
  private static final long serialVersionUID = 1L;
  private static final String BESCHREIBUNG_MUST_NOT_BE_NULL = "Beschreibung must not be null";
  private static final String PUBLIKATION_MUST_NOT_BE_NULL = "Publikation must not be null";
  private static final String REVIEWED_BY_MUST_NOT_BE_NULL = "ReviewedBy must not be null";

  /**
   * Creates and submits a new approval request for a Beschreibung.
   *
   * <p>Whether a {@link de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz} must be
   * present is decided by the caller, not this service: the manual publication entry point
   * ({@code PublikationsService.applyForPublication}) requires one, the automatic-import path
   * ({@code PublikationsService.publizierenOhneApproval}) does not.
   *
   * @param beschreibung the Beschreibung to create approval request for
   * @param requestedBy  the user requesting approval
   * @param comment      optional comment from the requester
   * @return the Beschreibung with the created approval request
   * @throws ApprovalException if an active request already exists
   */
  @Override
  @Transactional
  public Beschreibung createRequest(Beschreibung beschreibung, String requestedBy, String comment)
      throws ApprovalException {
    Objects.requireNonNull(beschreibung, BESCHREIBUNG_MUST_NOT_BE_NULL);
    Objects.requireNonNull(beschreibung.getPublikation(), PUBLIKATION_MUST_NOT_BE_NULL);

    if (beschreibung.hasActiveApprovalRequest()) {
      throw new ApprovalException(
          "An active approval request already exists for Beschreibung "
              + beschreibung.getId());
    }

    ApprovalRequest request = ApprovalRequest.create(beschreibung, requestedBy, comment);

    log.info("Created publication approval request {} for Beschreibung {} by {}",
        request.getId(), beschreibung.getId(), requestedBy);

    return beschreibung;
  }

  /**
   * Assigns an approval request to a reviewer.
   *
   * @param beschreibung the Beschreibung to assign a reviewer to
   * @param assignTo     the reviewer to assign to
   * @return the updated Beschreibung
   * @throws ApprovalException if no active approval request exists
   */
  @Transactional
  public Beschreibung assignReviewer(Beschreibung beschreibung, String assignTo)
      throws ApprovalException {
    Objects.requireNonNull(beschreibung, BESCHREIBUNG_MUST_NOT_BE_NULL);
    Objects.requireNonNull(beschreibung.getPublikation(), PUBLIKATION_MUST_NOT_BE_NULL);

    ApprovalRequest request = getExistingApprovalRequestOrThrow(beschreibung);

    request.assignTo(assignTo);
    log.info("Assigned approval request {} to {}", request.getId(), assignTo);
    return beschreibung;
  }

  /**
   * Approves a request for publishing.
   *
   * @param beschreibung the Beschreibung being approved
   * @param reviewedBy   the reviewer who approved
   * @param comment      optional comment
   * @return the approved Beschreibung
   * @throws ApprovalException if no active approval request exists
   */
  @Transactional
  public Beschreibung approve(Beschreibung beschreibung, String reviewedBy, String comment)
      throws ApprovalException {
    Objects.requireNonNull(beschreibung, BESCHREIBUNG_MUST_NOT_BE_NULL);
    Objects.requireNonNull(reviewedBy, REVIEWED_BY_MUST_NOT_BE_NULL);

    ApprovalRequest request = getExistingApprovalRequestOrThrow(
        beschreibung);

    request.approve(beschreibung, reviewedBy, comment);

    log.info("Approved request {} for Beschreibung {} by {}",
        request.getId(), beschreibung.getId(), reviewedBy);
    return beschreibung;
  }

  /**
   * Rejects an approval request.
   *
   * @param beschreibung the Beschreibung being rejected
   * @param reviewedBy   the reviewer who rejected
   * @param comment      optional comment explaining the rejection
   * @return the rejected Beschreibung
   * @throws ApprovalException if no active approval request exists
   */
  @Transactional
  public Beschreibung reject(Beschreibung beschreibung, String reviewedBy, String comment)
      throws ApprovalException {
    Objects.requireNonNull(beschreibung, BESCHREIBUNG_MUST_NOT_BE_NULL);
    Objects.requireNonNull(reviewedBy, REVIEWED_BY_MUST_NOT_BE_NULL);

    ApprovalRequest request = getExistingApprovalRequestOrThrow(
        beschreibung);

    request.reject(beschreibung, reviewedBy, comment);
    log.info("Rejected request {} for Beschreibung {} by {}",
        request.getId(), beschreibung.getId(), reviewedBy);
    return beschreibung;
  }


  /**
   * Withdraws an approval request for a Beschreibung.
   *
   * @param beschreibung the Beschreibung whose approval request should be withdrawn
   * @param comment      optional comment
   * @return the Beschreibung with withdrawn request
   * @throws ApprovalException if no active approval request exists
   */
  @Transactional
  public Beschreibung withdraw(Beschreibung beschreibung, String requestedBy, String comment)
      throws ApprovalException {
    Objects.requireNonNull(beschreibung, BESCHREIBUNG_MUST_NOT_BE_NULL);

    ApprovalRequest request = getExistingApprovalRequestOrThrow(
        beschreibung);

    request.withdraw(beschreibung, requestedBy, comment);

    log.info("Withdrawn request {} for Beschreibung {}", request.getId(), beschreibung.getId());
    return beschreibung;
  }

  private ApprovalRequest getExistingApprovalRequestOrThrow(Beschreibung beschreibung)
      throws ApprovalException {
    return beschreibung.getActiveApprovalRequest()
        .orElseThrow(() -> new ApprovalException(
            "No active approval request exists for Beschreibung " + beschreibung.getId()));
  }
}
