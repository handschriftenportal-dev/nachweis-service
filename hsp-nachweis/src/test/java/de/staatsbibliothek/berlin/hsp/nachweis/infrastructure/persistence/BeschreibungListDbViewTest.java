package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungListDbView;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 28.11.2022
 */

class BeschreibungListDbViewTest {

  @Test
  void testConstruct() {
    LocalDateTime erstellungsdatum = LocalDateTime.of(2022, 11, 16, 15, 14, 13);
    LocalDateTime aenderungsdatum = LocalDateTime.of(2022, 11, 17, 16, 15, 14);

    LocalDateTime publikationsDatumHsp = LocalDateTime.of(2022, 12, 1, 10, 0, 0);

    BeschreibungListDbView beschreibungListDbView = new BeschreibungListDbView(
        "hspId",
        "kodId",
        "katalogId",
        "gueltigeSignatur",
        "titel",
        "verwaltungsTyp",
        "dokumentobjekttyp",
        "dokumentobjekttyp",
        "Piotr Czarnecki",
        "aufbewahrungsort",
        "besitzer",
        erstellungsdatum,
        erstellungsdatum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
        null,
        aenderungsdatum,
        aenderungsdatum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy 00:00:00")),
        null,
        "Khallouf, Masaoud",
        publikationsDatumHsp,
        PublikationStatus.PUBLISHED,
        "https://hsp.example/purl"
    );

    assertNotNull(beschreibungListDbView);
    assertEquals("hspId", beschreibungListDbView.getHspId());
    assertEquals("kodId", beschreibungListDbView.getKodId());
    assertEquals("katalogId", beschreibungListDbView.getKatalogId());
    assertEquals("gueltigeSignatur", beschreibungListDbView.getGueltigeSignatur());
    assertEquals("titel", beschreibungListDbView.getTitel());
    assertEquals("verwaltungsTyp", beschreibungListDbView.getVerwaltungsTyp());
    assertEquals("dokumentobjekttyp", beschreibungListDbView.getDokumentObjektTyp());
    assertEquals("aufbewahrungsort", beschreibungListDbView.getAufbewahrungsort());
    assertEquals("besitzer", beschreibungListDbView.getBesitzer());
    assertEquals(erstellungsdatum, beschreibungListDbView.getErstellungsdatum());
    assertEquals("16.11.2022 15:14:13", beschreibungListDbView.getErstellungsdatumString());
    assertEquals(aenderungsdatum, beschreibungListDbView.getAenderungsdatum());
    assertEquals("17.11.2022", beschreibungListDbView.getAenderungsdatumString());
    assertEquals("Khallouf, Masaoud", beschreibungListDbView.getBearbeiter());
    assertEquals(publikationsDatumHsp, beschreibungListDbView.getPublikationsDatumHsp());
    assertEquals(PublikationStatus.PUBLISHED, beschreibungListDbView.getPublikationStatus());
    assertEquals("https://hsp.example/purl", beschreibungListDbView.getHspPurl());
  }

}
