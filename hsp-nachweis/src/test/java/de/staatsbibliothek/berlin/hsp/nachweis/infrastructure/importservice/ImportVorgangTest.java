package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 15.07.24
 */
class ImportVorgangTest {

  static String TEI_BIG_FILENAME = "02_Wolfenbuettel_HAB_helmst-5_EXT_TEI.xml";

  @Test
  void testSetFehler() throws IOException {
    Path teiFilePath = Paths.get("src", "test", "resources", TEI_BIG_FILENAME);
    String content = Files.readString(teiFilePath);
    ImportVorgang importVorgang = new ImportVorgang();
    importVorgang.setFehler(content);
    assertFalse(importVorgang.getFehler().isEmpty());
    StringBuilder sb = new StringBuilder(content.length());
    for (int i = 0; i < ImportVorgang.MAX_FEHLER_TEILEN; i++) {
      assertFalse(importVorgang.getFehlerTeil(i).isEmpty());
      sb.append(importVorgang.getFehlerTeil(i));
    }
    assertTrue(content.startsWith(sb.toString()));
  }

  @Test
  void testGetDatumFormatted() {
    ImportVorgang importVorgang = new ImportVorgang("id", "PIOTR", "name", LocalDateTime.of(1971, 5, 19, 13, 15, 0),
        IMPORTJOB_RESULT_VALUES.SUCCESS, null, "BESCHREIBUNG");
    String datumFormatted = importVorgang.getDatumFormatted();
    assertNotNull(datumFormatted);
    assertEquals("19.05.1971 13:15:00", datumFormatted);
  }
}
