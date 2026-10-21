package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
@Entity
@Table(name = "normdaten_erkennung_auftrag")
public class NormdatenErkennungAuftrag {
  @Id
  @Column(length = 64)
  private String id;

  @Column(name = "beschreibungsdokument_id", nullable = false)
  private String beschreibungsdokumentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  private NormdatenErkennungAuftragStatus status;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "request_id", nullable = false)
  private List<NormdatenErkennungEintrag> ergebnisse = new ArrayList<>();

  @ManyToOne(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH})
  @JoinColumn(name = "bearbeiter_id")
  private Bearbeiter bearbeiter;

  @ManyToOne(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH})
  @JoinColumn(name = "applied_by_id")
  private Bearbeiter appliedBy;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  protected NormdatenErkennungAuftrag() {}

  public NormdatenErkennungAuftrag(String beschreibungsdokumentId, Bearbeiter bearbeiter) {
    this.id = UUID.randomUUID().toString();
    this.beschreibungsdokumentId = beschreibungsdokumentId;
    this.bearbeiter = bearbeiter;
    this.status = NormdatenErkennungAuftragStatus.PENDING;
    this.createdAt = LocalDateTime.now();
  }

  public void resetToPending() {
    this.status = NormdatenErkennungAuftragStatus.PENDING;
    this.ergebnisse.clear();
  }

  public void startProcessing() {
    this.status = NormdatenErkennungAuftragStatus.PROCESSING;
    this.updatedAt = LocalDateTime.now();
  }

  public void finishProcessing() {
    this.status = NormdatenErkennungAuftragStatus.AWAITING_REVIEW;
    this.updatedAt = LocalDateTime.now();
  }

  public void startApplying(Bearbeiter appliedBy) {
    this.status = NormdatenErkennungAuftragStatus.APPLYING;
    this.appliedBy = appliedBy;
    this.updatedAt = LocalDateTime.now();
  }

  public void completeApplying() {
    this.status = NormdatenErkennungAuftragStatus.APPLIED;
    this.updatedAt = LocalDateTime.now();
  }

  public void cancel() {
    this.status = NormdatenErkennungAuftragStatus.CANCELLED;
    this.updatedAt = LocalDateTime.now();
  }

  public void markError(String errorMessage) {
    this.status = NormdatenErkennungAuftragStatus.ERROR;
    this.errorMessage = errorMessage;
    this.updatedAt = LocalDateTime.now();
  }

  public boolean isInProgress() {
    return this.status == NormdatenErkennungAuftragStatus.PENDING
        || this.status == NormdatenErkennungAuftragStatus.PROCESSING
        || this.status == NormdatenErkennungAuftragStatus.APPLYING ;
  }

  public boolean isFinished() {
    return status == NormdatenErkennungAuftragStatus.APPLIED
        || status == NormdatenErkennungAuftragStatus.CANCELLED
        || status == NormdatenErkennungAuftragStatus.ERROR;
  }

  public boolean isAwaitingReview() {
    return status == NormdatenErkennungAuftragStatus.AWAITING_REVIEW;
  }

  public void addErgebnis(NormdatenErkennungEintrag ergebnis) {
    this.ergebnisse.add(ergebnis);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NormdatenErkennungAuftrag)) {
      return false;
    }
    return Objects.equals(id, ((NormdatenErkennungAuftrag) o).id);
  }

  @Override
  public int hashCode() { return Objects.hash(id); }
}
