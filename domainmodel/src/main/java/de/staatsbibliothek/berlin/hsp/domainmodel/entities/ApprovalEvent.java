package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ApprovalEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single event in the approval workflow history. Each action (request, assignment,
 * approval, rejection, withdrawal) creates an event with actor, timestamp, and optional comment.
 */
@Entity
@Table(name = "approval_event")
public class ApprovalEvent {

  @Id
  private String id;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false)
  private ApprovalEventType eventType;

  @Column(nullable = false)
  private String actor;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(length = 4096)
  private String comment;

  protected ApprovalEvent() {
  }

  private ApprovalEvent(Builder builder) {
    this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
    this.eventType = Objects.requireNonNull(builder.eventType, "eventType must not be null");
    this.actor = Objects.requireNonNull(builder.actor, "actor must not be null");
    this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
    this.comment = builder.comment;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getId() {
    return id;
  }

  public ApprovalEventType getEventType() {
    return eventType;
  }

  public String getActor() {
    return actor;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public String getComment() {
    return comment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ApprovalEvent that)) {
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
    return "ApprovalEvent{"
        + "id='" + id + '\''
        + ", eventType=" + eventType
        + ", actor='" + actor + '\''
        + ", createdAt=" + createdAt
        + ", comment='" + (comment != null ? comment.substring(0, Math.min(comment.length(), 50))
        : null) + '\''
        + '}';
  }

  public static class Builder {

    private String id;
    private ApprovalEventType eventType;
    private String actor;
    private LocalDateTime createdAt;
    private String comment;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder eventType(ApprovalEventType eventType) {
      this.eventType = eventType;
      return this;
    }

    public Builder actor(String actor) {
      this.actor = actor;
      return this;
    }

    public Builder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder comment(String comment) {
      this.comment = comment;
      return this;
    }

    public ApprovalEvent build() {
      return new ApprovalEvent(this);
    }
  }
}