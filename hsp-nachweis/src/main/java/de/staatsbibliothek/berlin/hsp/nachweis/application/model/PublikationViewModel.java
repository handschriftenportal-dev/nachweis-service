package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record PublikationViewModel(
    PublikationStatus status,
    LocalDateTime erstpublikationsDatum,
    LocalDateTime publikationsDatumHSP,
    boolean hasActiveApprovalRequest,
    boolean hasTeiSnapshot,
    List<ApprovalRequestViewModel> approvalRequests
) implements Serializable {

  public static PublikationViewModel fromEntity(Publikation entity) {
    if (entity == null) {
      return null;
    }
    var approvalRequests = entity.getApprovalRequests().stream()
        .map(ApprovalRequestViewModel::fromEntity)
        .toList();
    return new PublikationViewModel(
        entity.getStatus(),
        entity.getErstpublikationsDatum(),
        entity.getPublikationsDatumHSP(),
        entity.hasActiveApprovalRequest(),
        entity.hasTeiSnapshot(),
        approvalRequests
    );
  }

  public boolean canRequestApproval() {
    return status == PublikationStatus.DRAFT && !hasActiveApprovalRequest;
  }

  public Optional<ApprovalRequestViewModel> getActiveApprovalRequest() {
    return approvalRequests.stream()
        .filter(ApprovalRequestViewModel::active)
        .findFirst();
  }
}
