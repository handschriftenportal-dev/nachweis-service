package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

/**
 * Status of a publication.
 */
public enum PublikationStatus {

  /**
   * Draft - not publicly visible, still being edited.
   */
  DRAFT,

  /**
   * In Review - approval request has been created, awaiting review.
   */
  IN_REVIEW,

  /**
   * Published - live and publicly accessible.
   */
  PUBLISHED
}
