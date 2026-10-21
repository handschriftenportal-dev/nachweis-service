package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.ApprovalRequest;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ApprovalStatus;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record ApprovalRequestViewModel(
    ApprovalStatus status,
    String requestedBy,
    LocalDateTime requestedAt,
    String assignedTo,
    LocalDateTime assignedAt,
    boolean active,
    List<ApprovalEventViewModel> events
) implements Serializable {

  public static ApprovalRequestViewModel fromEntity(ApprovalRequest entity) {
    var events = entity.getEvents().stream()
        .map(ApprovalEventViewModel::fromEntity)
        .toList();
    return new ApprovalRequestViewModel(
        entity.getStatus(),
        entity.getRequestedBy(),
        entity.getRequestedAt(),
        entity.getAssignedTo(),
        entity.getAssignedAt(),
        entity.isActive(),
        events
    );
  }
}
