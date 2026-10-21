package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Approvable;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ApprovalEventType;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ApprovalStatus;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ApprovalType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic approval request entity. The target entity relationship is managed externally through
 * join tables (e.g., publikation_approval), allowing the approval_request table to remain generic
 * and reusable for different entity types.
 */
@Entity
@Table(name = "approval_request")
public class ApprovalRequest {

  private static final Logger log = LoggerFactory.getLogger(ApprovalRequest.class);
  private static final String TARGET_MUST_NOT_BE_NULL = "target must not be null";

  @Id
  private String id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApprovalStatus status;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApprovalType type;

  @Column(name = "requested_by", nullable = false)
  private String requestedBy;

  @Column(name = "requested_at", nullable = false)
  private LocalDateTime requestedAt;

  @Column(name = "assigned_to")
  private String assignedTo;

  @Column(name = "assigned_at")
  private LocalDateTime assignedAt;

  @Column(name = "reviewed_by")
  private String reviewedBy;

  @Column(name = "reviewed_at")
  private LocalDateTime reviewedAt;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinTable(
      name = "approval_request_event",
      joinColumns = @JoinColumn(name = "approval_request_id"),
      inverseJoinColumns = @JoinColumn(name = "approval_event_id")
  )
  @OrderBy("createdAt ASC")
  private List<ApprovalEvent> events = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  protected ApprovalRequest() {
  }

  private ApprovalRequest(Approvable approvableObject, String requestedBy) {
    this.id = UUID.randomUUID().toString();
    this.status = ApprovalStatus.REQUESTED;
    this.type = approvableObject.getApprovalType();
    this.requestedBy = Objects.requireNonNull(requestedBy, "requestedBy must not be null");
    this.requestedAt = LocalDateTime.now();
    this.createdAt = LocalDateTime.now();
  }

  /**
   * Submits this request for approval with an optional comment. Calls
   * {@link Approvable#onApprovalCreated(ApprovalRequest)} on the target.
   *
   * @param target      the target entity being approved
   * @param requestedBy the user requesting approval
   * @param comment     optional comment from the requester
   * @return this request for method chaining
   * @throws IllegalStateException if the target cannot be submitted for approval
   */
  public static ApprovalRequest create(Approvable target, String requestedBy, String comment) {
    Objects.requireNonNull(target, TARGET_MUST_NOT_BE_NULL);
    if (!target.canRequestApproval()) {
      throw new IllegalStateException("Target cannot be submitted for approval");
    }
    var request = new ApprovalRequest(target, requestedBy);
    request.status = ApprovalStatus.REQUESTED;
    request.addEvent(ApprovalEventType.REQUESTED, requestedBy, comment);
    target.onApprovalCreated(request);
    log.info("ApprovalRequest {} submitted by {}", request.id, requestedBy);
    return request;
  }

  /**
   * Submits this request for approval. Calls {@link Approvable#onApprovalCreated(ApprovalRequest)}
   * on the target.
   *
   * @param target      the target entity being approved
   * @param requestedBy the user requesting approval
   * @return this request for method chaining
   * @throws IllegalStateException if the target cannot be submitted for approval
   */
  public static ApprovalRequest create(Approvable target, String requestedBy) {
    return create(target, requestedBy, null);
  }

  @PrePersist
  protected void onPersist() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  /**
   * Assigns this request to a reviewer.
   *
   * @param assignedTo the name/ID of the reviewer
   * @return this request for method chaining
   * @throws IllegalStateException if the request is not in REQUESTED status
   */
  public ApprovalRequest assignTo(String assignedTo) {
    if (status != ApprovalStatus.REQUESTED && status != ApprovalStatus.IN_REVIEW) {
      throw new IllegalStateException(
          "Can only assign REQUESTED requests. Current status: " + status);
    }
    this.status = ApprovalStatus.IN_REVIEW;
    this.assignedTo = Objects.requireNonNull(assignedTo, "assignedTo must not be null");
    this.assignedAt = LocalDateTime.now();
    addEvent(ApprovalEventType.ASSIGNED, assignedTo, null);
    log.info("ApprovalRequest {} assigned to {}", id, assignedTo);
    return this;
  }

  /**
   * Approves this request. Calls {@link Approvable#onApproved()} on the target.
   *
   * @param target     the target entity being approved
   * @param reviewedBy the name/ID of the reviewer who approved
   * @param comment    optional comment (can be empty string)
   * @return this request for method chaining
   * @throws IllegalStateException if the request is not in REQUESTED or IN_REVIEW status
   */
  public ApprovalRequest approve(Approvable target, String reviewedBy, String comment) {
    Objects.requireNonNull(target, TARGET_MUST_NOT_BE_NULL);
    if (status != ApprovalStatus.REQUESTED && status != ApprovalStatus.IN_REVIEW) {
      throw new IllegalStateException(
          "Can only approve REQUESTED or IN_REVIEW requests. Current status: " + status);
    }
    if (!target.canBeApproved()) {
      throw new IllegalStateException("Target cannot be approved");
    }
    this.status = ApprovalStatus.APPROVED;
    this.reviewedBy = Objects.requireNonNull(reviewedBy, "reviewedBy must not be null");
    this.reviewedAt = LocalDateTime.now();
    addEvent(ApprovalEventType.APPROVED, reviewedBy, comment);
    target.onApproved();
    log.info("ApprovalRequest {} approved by {}", id, reviewedBy);
    return this;
  }

  /**
   * Rejects this request. Calls {@link Approvable#onRejected()} on the target.
   *
   * @param target     the target entity being approved
   * @param reviewedBy the name/ID of the reviewer who rejected
   * @param comment    optional comment explaining the rejection (can be empty string)
   * @return this request for method chaining
   * @throws IllegalStateException if the request is not in REQUESTED or IN_REVIEW status
   */
  public ApprovalRequest reject(Approvable target, String reviewedBy, String comment) {
    Objects.requireNonNull(target, TARGET_MUST_NOT_BE_NULL);
    if (status != ApprovalStatus.REQUESTED && status != ApprovalStatus.IN_REVIEW) {
      throw new IllegalStateException(
          "Can only reject REQUESTED or IN_REVIEW requests. Current status: " + status);
    }
    this.status = ApprovalStatus.REJECTED;
    this.reviewedBy = Objects.requireNonNull(reviewedBy, "reviewedBy must not be null");
    this.reviewedAt = LocalDateTime.now();
    addEvent(ApprovalEventType.REJECTED, reviewedBy, comment);
    target.onRejected();
    log.info("ApprovalRequest {} rejected by {}", id, reviewedBy);
    return this;
  }

  /**
   * Withdraws this request. Calls {@link Approvable#onApprovalWithdrawn()} on the target.
   *
   * @param target      the target entity whose approval is being withdrawn
   * @param withdrawnBy the user withdrawing the request
   * @param comment     optional comment
   * @return this request for method chaining
   * @throws IllegalStateException if the request is not in REQUESTED or IN_REVIEW status
   */
  public ApprovalRequest withdraw(Approvable target, String withdrawnBy, String comment) {
    Objects.requireNonNull(target, TARGET_MUST_NOT_BE_NULL);
    Objects.requireNonNull(withdrawnBy, "withdrawnBy must not be null");
    if (status != ApprovalStatus.REQUESTED && status != ApprovalStatus.IN_REVIEW) {
      throw new IllegalStateException(
          "Can only withdraw REQUESTED or IN_REVIEW requests. Current status: " + status);
    }
    this.status = ApprovalStatus.WITHDRAWN;
    addEvent(ApprovalEventType.WITHDRAWN, withdrawnBy, comment);
    target.onApprovalWithdrawn();
    log.info("ApprovalRequest {} withdrawn by {}", id, withdrawnBy);
    return this;
  }


  public boolean isRequested() {
    return status == ApprovalStatus.REQUESTED;
  }

  public boolean isInReview() {
    return status == ApprovalStatus.IN_REVIEW;
  }

  public boolean isApproved() {
    return status == ApprovalStatus.APPROVED;
  }

  public boolean isRejected() {
    return status == ApprovalStatus.REJECTED;
  }

  public boolean isWithdrawn() {
    return status == ApprovalStatus.WITHDRAWN;
  }

  public boolean isActive() {
    return status == ApprovalStatus.REQUESTED || status == ApprovalStatus.IN_REVIEW;
  }


  public String getId() {
    return id;
  }

  public ApprovalStatus getStatus() {
    return status;
  }

  public String getRequestedBy() {
    return requestedBy;
  }

  public LocalDateTime getRequestedAt() {
    return requestedAt;
  }

  public String getAssignedTo() {
    return assignedTo;
  }

  public LocalDateTime getAssignedAt() {
    return assignedAt;
  }

  public String getReviewedBy() {
    return reviewedBy;
  }

  public LocalDateTime getReviewedAt() {
    return reviewedAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public List<ApprovalEvent> getEvents() {
    return Collections.unmodifiableList(events);
  }

  private void addEvent(ApprovalEventType type, String actor, String comment) {
    events.add(ApprovalEvent.builder()
        .eventType(type)
        .actor(actor)
        .createdAt(LocalDateTime.now())
        .comment(comment)
        .build());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ApprovalRequest that)) {
      return false;
    }
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{"
        + "id='" + id + '\''
        + ", status=" + status
        + ", requestedBy='" + requestedBy + '\''
        + ", requestedAt=" + requestedAt
        + ", assignedTo='" + assignedTo + '\''
        + ", reviewedBy='" + reviewedBy + '\''
        + '}';
  }
}
