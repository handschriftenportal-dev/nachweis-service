package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.ApprovalRequest;
import java.util.Optional;

/**
 * Interface for entities that can go through an approval workflow. Implementing classes define
 * callbacks that are invoked during approval state transitions.
 */
public interface Approvable {

  String getId();

  /**
   * Called when an approval request is submitted for this entity.
   */
  void onApprovalCreated(ApprovalRequest request);

  /**
   * Called when the approval request is approved.
   */
  void onApproved();

  /**
   * Called when the approval request is rejected.
   */
  void onRejected();

  /**
   * called when the approval is withdrawn.
   */
  void onApprovalWithdrawn();

  /**
   * Gets the type of approval for this entity.
   */
  ApprovalType getApprovalType();

  /**
   * Checks if this entity can be submitted for approval.
   *
   * @return true if approval can be requested, false otherwise
   */
  default boolean canRequestApproval() {
    return true;
  }

  /**
   * Checks if this entity can be approved.
   *
   * @return true if the entity can be approved, false otherwise
   */
  default boolean canBeApproved() {
    return true;
  }

  Optional<ApprovalRequest> getActiveApprovalRequest();

  boolean hasActiveApprovalRequest();
}
