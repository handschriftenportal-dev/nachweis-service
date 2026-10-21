package de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.DatensicherungException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.Status;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.DatensicherungDTO;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class DatensicherungTest {

  @Test
  void testMappingFromDTO() {
    DatensicherungDTO dto = DatensicherungDTO.builder()
        .id("ID")
        .dateiName("datei.zip")
        .benutzer("Benutzer 1")
        .status(Status.IN_PROGRESS)
        .startZeit(Timestamp.valueOf(LocalDateTime.of(1984, 1, 20, 13, 37, 14)))
        .dokumentAuswahl(
            new HashSet<>(Arrays.asList(DokumentTyp.KATALOG, DokumentTyp.KOD)))
        .errorMessage("Error Message")
        .build();
    Datensicherung domainDs = Datensicherung.map(dto);

    assertEquals(dto.getId(), domainDs.getId());
    assertEquals(dto.getDateiName(), domainDs.getDateiName());
    assertEquals(dto.getBenutzer(), domainDs.getBenutzer());
    assertEquals(dto.getStatus(), domainDs.getStatus());
    assertEquals(dto.getStartZeit(), Timestamp.valueOf(domainDs.getStartZeit()));
    assertEquals(dto.getDokumentenAuswahl(), domainDs.getDokumentenAuswahl());
    assertEquals(dto.getMessage(), domainDs.getMessage());
    assertNull(domainDs.getEndZeit());
  }

  @Test
  void testMappingFromDTOThrows() {
    assertThrows(IllegalArgumentException.class, () -> Datensicherung.map(null));
  }

  @Test
  void testDatensicherungBuilder() {
    Datensicherung ds = Datensicherung.builder()
        .id("ID")
        .dateiName("datei.zip")
        .benutzer("Benutzer 1")
        .status(Status.IN_PROGRESS)
        .startZeit(LocalDateTime.of(1984, 1, 20, 13, 37, 14))
        .dokumentenAuswahl(
            new HashSet<>(Arrays.asList(DokumentTyp.KATALOG, DokumentTyp.KOD)))
        .build();
    assertEquals("ID", ds.getId());
    assertEquals("datei.zip", ds.getDateiName());
    assertEquals("Benutzer 1", ds.getBenutzer());
    assertEquals(Status.IN_PROGRESS, ds.getStatus());
    assertEquals(LocalDateTime.of(1984, 1, 20, 13, 37, 14), ds.getStartZeit());
    assertTrue(ds.getDokumentenAuswahl().containsAll(
        new HashSet<>(Arrays.asList(DokumentTyp.KATALOG, DokumentTyp.KOD))));
    assertNull(ds.getEndZeit());
  }

  @Test
  void testDatensicherungInitialisieren() {
    Datensicherung ds = Datensicherung.initialisieren(
        new HashSet<>(Arrays.asList(DokumentTyp.KATALOG, DokumentTyp.KOD)), "Benutzer 1");

    assertNotNull(ds.getId());
    assertNotNull(ds.getStartZeit());
    assertNotNull(ds.getDateiName());

    assertEquals(Status.IN_PROGRESS, ds.getStatus());
    assertEquals(new HashSet<>(Arrays.asList(DokumentTyp.KATALOG, DokumentTyp.KOD)),
        ds.getDokumentenAuswahl());
    assertEquals("Benutzer 1", ds.getBenutzer());

    assertNull(ds.getEndZeit());
  }

  @Test
  void testDatensicherungInitialisierenWithNull() {
    assertThrows(DatensicherungException.class, () -> Datensicherung.initialisieren(null, null));
    assertThrows(DatensicherungException.class,
        () -> Datensicherung.initialisieren(new HashSet<>(List.of(DokumentTyp.KATALOG)),
            null));
    assertThrows(DatensicherungException.class,
        () -> Datensicherung.initialisieren(null, "Benutzer 1"));
  }

  @Test
  void testAbschliessen() {
    Datensicherung ds = Datensicherung.initialisieren(
        new HashSet<>(Arrays.asList(DokumentTyp.KATALOG, DokumentTyp.KOD)), "Benutzer 1");

    assertNull(ds.getEndZeit());
    assertEquals(Status.IN_PROGRESS, ds.getStatus());

    ds.abschliessen();

    assertNotNull(ds.getEndZeit());
    assertEquals(Status.FINISHED, ds.getStatus());

  }

  @Test
  void testFehlgeschlagen() {
    Datensicherung datensicherung = spy(
        Datensicherung.initialisieren(new HashSet<>(List.of(DokumentTyp.KATALOG)), "Benutzer"));

    datensicherung.fehlgeschlagen("Error Message");

    LocalDateTime start = datensicherung.getStartZeit();
    LocalDateTime ende = datensicherung.getEndZeit();

    assertEquals(Status.ERROR, datensicherung.getStatus());
    assertEquals("Error Message", datensicherung.getMessage());
    assertTrue(start.isBefore(ende));
  }

}
