package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.entities.Datensicherung;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.Status;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class DatensicherungDTOTest {

  @Test
  void testDatensicherungDTODefaultCtor() {
    DatensicherungDTO datensicherungDTO = new DatensicherungDTO();
    datensicherungDTO.setStartZeit(null);
    datensicherungDTO.setEndZeit(null);
    datensicherungDTO.setDokumentenAuswahl(null);
    datensicherungDTO.setStatus(null);
    datensicherungDTO.setDateiName("dateiName");
    datensicherungDTO.setBenutzer("benutzer");
    datensicherungDTO.setMessage("Error Message");

    assertNotNull(datensicherungDTO.getId());
    assertNull(datensicherungDTO.getStartZeit());
    assertNull(datensicherungDTO.getEndZeit());
    assertNull(datensicherungDTO.getDokumentenAuswahl());
    assertNull(datensicherungDTO.getStatus());
    assertEquals("dateiName", datensicherungDTO.getDateiName());
    assertEquals("benutzer", datensicherungDTO.getBenutzer());
    assertEquals("Error Message", datensicherungDTO.getMessage());
  }

  @Test
  void testDatensicherungDTOBuilder() {
    LocalDateTime startZeit = LocalDateTime.of(1984, 1, 20, 13, 37, 14);
    DatensicherungDTO dto = DatensicherungDTO.builder()
        .id("ID")
        .dateiName("datei.zip")
        .benutzer("Benutzer 1")
        .status(Status.IN_PROGRESS)
        .startZeit(Timestamp.valueOf(startZeit))
        .dokumentAuswahl(
            new HashSet<>(Arrays.asList(DokumentTyp.KATALOG, DokumentTyp.KOD)))
        .build();

    assertEquals("ID", dto.getId());
    assertEquals("datei.zip", dto.getDateiName());
    assertEquals("Benutzer 1", dto.getBenutzer());
    assertEquals(Status.IN_PROGRESS, dto.getStatus());
    assertEquals(Timestamp.valueOf(startZeit), dto.getStartZeit());
    assertEquals(Set.of(DokumentTyp.KATALOG, DokumentTyp.KOD), dto.getDokumentenAuswahl());
    assertNull(dto.getEndZeit());
  }

  @Test
  void testDatensicherungDTOBuilderWithNullValues() {
    DatensicherungDTO dto = DatensicherungDTO.builder()
        .id("ID")
        .dateiName("datei.zip")
        .benutzer("Benutzer 1")
        .status(Status.IN_PROGRESS)
        .startZeit(null)
        .dokumentAuswahl(null)
        .build();

    assertEquals("ID", dto.getId());
    assertEquals("datei.zip", dto.getDateiName());
    assertEquals("Benutzer 1", dto.getBenutzer());
    assertEquals(Status.IN_PROGRESS, dto.getStatus());
    assertNull(dto.getStartZeit());
    assertNull(dto.getDokumentenAuswahl());
    assertNull(dto.getEndZeit());
  }

  @Test
  void testDatensicherungDTOBuilderWithEmptyValues() {
    DatensicherungDTO dto = DatensicherungDTO.builder()
        .id("")
        .dateiName("")
        .benutzer("")
        .status(null)
        .startZeit(null)
        .dokumentAuswahl(null)
        .build();

    assertNotNull(dto.getId());
    assertEquals("", dto.getDateiName());
    assertEquals("", dto.getBenutzer());
    assertNull(dto.getStatus());
    assertNull(dto.getStartZeit());
    assertNull(dto.getDokumentenAuswahl());
    assertNull(dto.getEndZeit());
  }

  @Test
  void testDatensicherungDTOmap() {
    Datensicherung ds = Datensicherung.builder()
        .id("ID")
        .dateiName("datei.zip")
        .benutzer("Benutzer 1")
        .status(Status.IN_PROGRESS)
        .startZeit(LocalDateTime.of(1984, 1, 20, 13, 37, 14))
        .dokumentenAuswahl(
            new HashSet<>(Arrays.asList(DokumentTyp.KATALOG, DokumentTyp.KOD)))
        .message("Error Message")
        .build();

    DatensicherungDTO dto = DatensicherungDTO.map(ds);

    assertEquals("ID", dto.getId());
    assertEquals("datei.zip", dto.getDateiName());
    assertEquals("Benutzer 1", dto.getBenutzer());
    assertEquals(Status.IN_PROGRESS, dto.getStatus());
    assertEquals(Timestamp.valueOf(LocalDateTime.of(1984, 1, 20, 13, 37, 14)), dto.getStartZeit());
    assertEquals(Set.of(DokumentTyp.KATALOG, DokumentTyp.KOD), dto.getDokumentenAuswahl());
    assertEquals("Error Message", dto.getMessage());
    assertNull(dto.getEndZeit());

  }


}
