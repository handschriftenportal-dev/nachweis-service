package de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.entities;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.DatensicherungException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.Status;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.DatensicherungDTO;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@ToString
@EqualsAndHashCode
public class Datensicherung {

  private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd-HH-mm-ss");
  private String id;
  private LocalDateTime startZeit;
  private LocalDateTime endZeit;
  private Set<DokumentTyp> dokumentenAuswahl;
  private Status status;
  private String dateiName;
  private String benutzer;
  private String message;

  public static Datensicherung map(
      DatensicherungDTO dto) {
    log.debug("Map Datensicherung {}", dto);
    if (Objects.isNull(dto)) {
      throw new IllegalArgumentException("The argument must not be null");
    }

    return Datensicherung.builder()
        .id(dto.getId())
        .startZeit(fromTimestamp(dto.getStartZeit()))
        .endZeit(fromTimestamp(dto.getEndZeit()))
        .dokumentenAuswahl(dto.getDokumentenAuswahl())
        .status(dto.getStatus())
        .dateiName(dto.getDateiName())
        .benutzer(dto.getBenutzer())
        .message(dto.getMessage())
        .build();
  }

  private static LocalDateTime fromTimestamp(Timestamp ts) {
    if (ts == null) {
      return null;
    }
    return ts.toLocalDateTime();
  }

  public static Datensicherung initialisieren(Set<DokumentTyp> dokumentenAuswahl, String benutzer) {
    if (Objects.isNull(dokumentenAuswahl) || Objects.isNull(benutzer)) {
      throw new DatensicherungException(
          "To initialize a new data backup a document selection and a user name need to be provided");
    }

    LocalDateTime now = LocalDateTime.now();

    return builder()
        .id(UUID.randomUUID().toString())
        .startZeit(now)
        .status(Status.IN_PROGRESS)
        .dokumentenAuswahl(dokumentenAuswahl)
        .benutzer(benutzer)
        .dateiName(now.format(dateFormatter))
        .build();
  }

  public boolean abschliessen() {
    return this.abschliessen("");
  }

  public boolean abschliessen(String message) {
    if (this.getStatus() != Status.IN_PROGRESS) {
      log.error("Datensicherung " + this + " konnte nicht abgeschlossen werden");
      return false;
    }
    this.endZeit = LocalDateTime.now();
    this.status = Status.FINISHED;
    this.message = message;
    return true;
  }

  public void fehlgeschlagen(String errorMessage) {
    this.endZeit = LocalDateTime.now();
    this.status = Status.ERROR;
    this.message = errorMessage;
  }
}
