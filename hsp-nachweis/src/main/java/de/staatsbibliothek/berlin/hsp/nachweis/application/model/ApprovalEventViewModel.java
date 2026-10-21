package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.ApprovalEvent;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ApprovalEventType;
import java.io.Serializable;
import java.time.LocalDateTime;

public record ApprovalEventViewModel(
    ApprovalEventType eventType,
    String actor,
    LocalDateTime createdAt,
    String comment
) implements Serializable {

  public static ApprovalEventViewModel fromEntity(ApprovalEvent entity) {
    return new ApprovalEventViewModel(
        entity.getEventType(),
        entity.getActor(),
        entity.getCreatedAt(),
        entity.getComment()
    );
  }
}
