package de.staatsbibliothek.berlin.hsp.nachweis.domain.approval;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Approvable;

public interface ApprovalBoundary<T extends Approvable> {

  default T createRequest(T approvable, String requestedBy) throws ApprovalException {
    return createRequest(approvable, requestedBy, "");
  }

  T createRequest(T approvable, String requestedBy, String comment) throws ApprovalException;

  T assignReviewer(T approvable, String assignTo) throws ApprovalException;

  T approve(T approvable, String reviewedBy, String comment) throws ApprovalException;

  T reject(T approvable, String reviewedBy, String comment) throws ApprovalException;

  T withdraw(T publikation, String requestedBy, String comment) throws ApprovalException;

}
