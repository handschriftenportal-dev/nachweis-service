package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

/**
 * Types of events that can occur during an approval workflow.
 */
public enum ApprovalEventType {

  /**
   * Approval was requested.
   */
  REQUESTED,

  /**
   * Reviewer was assigned to the request.
   */
  ASSIGNED,

  /**
   * Request was approved by the reviewer.
   */
  APPROVED,

  /**
   * Request was rejected by the reviewer.
   */
  REJECTED,

  /**
   * Request was withdrawn by the requester.
   */
  WITHDRAWN
}