package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;
import lombok.Setter;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */
@Entity
@Table(name = "beschreibungimport")
@Cacheable
public class BeschreibungImport implements Comparable<BeschreibungImport> {

  @Id
  private String id;

  @Setter
  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 15)
  private ImportStatus status;

  @OneToOne(cascade = CascadeType.ALL)
  private ActivityStreamMessageDTO messageDTO;

  @Column
  private LocalDateTime creationDate;

  @Column
  private LocalDateTime modificationDate;

  @Setter
  @Column(length = 16384)
  private String fehlerBeschreibung;

  @Setter
  @Column
  private boolean isSelectedForImport;

  protected BeschreibungImport() {
  }

  public BeschreibungImport(ImportStatus status, ActivityStream message)
      throws ActivityStreamsException {
    this.id = UUID.randomUUID().toString();
    this.status = status;
    if (message != null) {
      this.messageDTO = new ActivityStreamMessageDTO(message);
    }
    this.creationDate = LocalDateTime.now(ZoneId.systemDefault());
    this.modificationDate = LocalDateTime.now(ZoneId.systemDefault());
  }

  public String getId() {
    return id;
  }

  public ImportStatus getStatus() {
    return status;
  }

  private ActivityStream getMessage() throws ActivityStreamsException {
    if (messageDTO == null) {
      return null;
    }
    return messageDTO.getActivityStream();
  }

  public ActivityStreamMessageDTO getMessageDTO() {
    return messageDTO;
  }

  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  public LocalDateTime getModificationDate() {
    return modificationDate;
  }

  public boolean isSelectedForImport() {
    return isSelectedForImport;
  }

  public String getFehlerBeschreibung() {
    return fehlerBeschreibung;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BeschreibungImport anBeschreibungImport)) {
      return false;
    }
    return Objects.equals(id, anBeschreibungImport.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Import{" +
        "id='" + id + '\'' +
        ", status=" + status +
        ", message=" + messageDTO +
        ", creationDate=" + creationDate +
        ", modificationDate=" + modificationDate +
        ", fehlerBeschreibung='" + fehlerBeschreibung + '\'' +
        '}';
  }

  @Override
  public int compareTo(BeschreibungImport o) {
    return o.getCreationDate().compareTo(this.creationDate);
  }
}
