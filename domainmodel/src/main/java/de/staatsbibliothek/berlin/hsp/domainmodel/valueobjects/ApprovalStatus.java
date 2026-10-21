package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

/**
 * Status of an approval request in the workflow.
 */
public enum ApprovalStatus {

  /**
   * Submitted, awaiting assignment to a reviewer.
   */
  REQUESTED,

  /**
   * Assigned to a reviewer, being processed.
   */
  IN_REVIEW,

  /**
   * Approved by the reviewer.
   */
  APPROVED,

  /**
   * Rejected by the reviewer.
   */
  REJECTED,

  /**
   * Withdrawn by the requester.
   */
  WITHDRAWN
}
