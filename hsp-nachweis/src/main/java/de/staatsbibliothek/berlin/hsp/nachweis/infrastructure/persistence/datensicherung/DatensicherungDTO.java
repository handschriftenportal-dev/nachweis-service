package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.entities.Datensicherung;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.Status;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "datensicherung")
@Table(name = "datensicherung")
@Getter
@Setter
public class DatensicherungDTO implements Serializable {

  @Serial
  private static final long serialVersionUID = -2338626292552177485L;

  @Id
  @Column(name = "id", unique = true, nullable = false)
  private String id;

  @Column(name = "start_zeit")
  private Timestamp startZeit;

  @Column(name = "end_zeit")
  private Timestamp endZeit;

  @ElementCollection(targetClass = DokumentTyp.class)
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "datensicherung_dokumentauswahl")
  @Column(name = "dokument_auswahl")
  private Set<DokumentTyp> dokumentenAuswahl;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private Status status;

  @Column(name = "datei_name")
  private String dateiName;

  @Column(name = "benutzer")
  private String benutzer;

  @Column(name = "message")
  private String message;

  public DatensicherungDTO() {
    this.id = UUID.randomUUID().toString();
  }

  public DatensicherungDTO(DatensicherungDTOBuilder builder) {
    this();
    this.id = builder.id;
    this.startZeit = builder.startZeit;
    this.endZeit = builder.endZeit;
    this.dokumentenAuswahl = builder.dokumentenAuswahl;
    this.status = builder.status;
    this.dateiName = builder.dateiName;
    this.benutzer = builder.benutzer;
    this.message = builder.message;
  }

  public static DatensicherungDTOBuilder builder() {
    return new DatensicherungDTOBuilder();
  }

  public static DatensicherungDTO map(Datensicherung datensicherung) {
    DatensicherungDTOBuilder builder = new DatensicherungDTOBuilder();
    return builder
        .id(datensicherung.getId())
        .startZeit(getTimestamp(datensicherung.getStartZeit()))
        .endZeit(getTimestamp(datensicherung.getEndZeit()))
        .dokumentAuswahl(datensicherung.getDokumentenAuswahl())
        .status(datensicherung.getStatus())
        .benutzer(datensicherung.getBenutzer())
        .dateiName(datensicherung.getDateiName())
        .errorMessage(datensicherung.getMessage())
        .build();
  }

  private static Timestamp getTimestamp(LocalDateTime date) {
    if (date == null) {
      return null;
    }

    return Timestamp.valueOf(date);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DatensicherungDTO that = (DatensicherungDTO) o;
    return Objects.equals(id, that.id);
  }


  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Datensicherung{")
        .append("id=").append(id)
        .append(", startZeit=").append(startZeit)
        .append(", endZeit=").append(endZeit)
        .append(", dokumentAuswahl=").append(dokumentenAuswahl)
        .append(", dateiName='").append(dateiName).append('\'')
        .append(", user='").append(benutzer).append('\'')
        .append(", message='").append(message).append('\'')
        .append('}');
    return sb.toString();
  }

  public static final class DatensicherungDTOBuilder {

    private String id;
    private Timestamp startZeit;
    private Timestamp endZeit;
    private Set<DokumentTyp> dokumentenAuswahl;
    private Status status;
    private String dateiName;
    private String benutzer;
    private String message;

    public DatensicherungDTOBuilder id(String id) {
      this.id = id;
      return this;
    }

    public DatensicherungDTOBuilder startZeit(Timestamp ts) {
      this.startZeit = ts;
      return this;
    }

    public DatensicherungDTOBuilder endZeit(Timestamp ts) {
      this.endZeit = ts;
      return this;
    }

    public DatensicherungDTOBuilder dokumentAuswahl(Set<DokumentTyp> auswahl) {
      this.dokumentenAuswahl = auswahl;
      return this;
    }

    public DatensicherungDTOBuilder dateiName(String dateiName) {
      this.dateiName = dateiName;
      return this;
    }

    public DatensicherungDTOBuilder benutzer(String benutzer) {
      this.benutzer = benutzer;
      return this;
    }

    public DatensicherungDTOBuilder status(Status status) {
      this.status = status;
      return this;
    }

    public DatensicherungDTOBuilder errorMessage(String errorMessage) {
      this.message = errorMessage;
      return this;
    }

    public DatensicherungDTO build() {
      return new DatensicherungDTO(this);
    }
  }

}
