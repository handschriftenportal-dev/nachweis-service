package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 16.11.2022
 */
class KulturObjektDokumentListDTOTest {

  @Test
  void testConstruct() {
    LocalDateTime registrierungsdatum = LocalDateTime.of(2022, 11, 16, 15, 14, 13);
    String registrierungsdatumString = registrierungsdatum.format(
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));

    KulturObjektDokumentListDTO model = new KulturObjektDokumentListDTO(
        "hspId",
        "gndId",
        "gueltigeSignatur",
        "mySortKey",
        "aufbewahrungsort",
        "besitzer",
        registrierungsdatum,
        registrierungsdatumString,
        "HSP-B1 HSP-B2",
        "",
        "Manuell",
        "JA",
        "NEIN"
    );

    assertEquals("hspId", model.getHspId());
    assertEquals("gndId", model.getGndId());
    assertEquals("gueltigeSignatur", model.getGueltigeSignatur());
    assertEquals("mySortKey", model.getSortKey());
    assertEquals("aufbewahrungsort", model.getAufbewahrungsort());
    assertEquals("besitzer", model.getBesitzer());
    assertEquals(registrierungsdatum, model.getRegistrierungsdatum());
    assertEquals(registrierungsdatumString, model.getRegistrierungsdatumString());
    assertEquals("HSP-B1 HSP-B2", model.getBeschreibungenIds());
    assertNotNull(model.getBeschreibungenIdsAsList());
    assertEquals(Set.of("HSP-B1", "HSP-B2"), new HashSet<>(model.getBeschreibungenIdsAsList()));
    assertEquals("", model.getPurl());
    assertEquals("Manuell", model.getKerndaten());
    assertEquals("JA", model.getMitBeschreibungen());
    assertEquals("NEIN", model.getMitDigitalisat());
  }
}
