package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 16.09.2020.
 * @version 1.0
 */
public class DatenDokumentTypTest {

  @Test
  void testValues() {
    Assertions.assertEquals(12, DatenDokumentTyp.values().length);
  }

  @Test
  void testToString() {
    Assertions.assertEquals("Kulturobjektdokument", DatenDokumentTyp.KOD.toString());
    Assertions.assertEquals("Beschreibung", DatenDokumentTyp.BESCHREIBUNG.toString());
    Assertions.assertEquals("Katalog", DatenDokumentTyp.KATALOG.toString());
    Assertions.assertEquals("Ort", DatenDokumentTyp.ORT.toString());
    Assertions.assertEquals("Körperschaft", DatenDokumentTyp.KOERPERSCHAFT.toString());
    Assertions.assertEquals("Beziehung", DatenDokumentTyp.BEZIEHUNG.toString());
    Assertions.assertEquals("Digitalisat", DatenDokumentTyp.DIGITALISAT.toString());
    Assertions.assertEquals("Person", DatenDokumentTyp.PERSON.toString());
    Assertions.assertEquals("Sprache", DatenDokumentTyp.SPRACHE.toString());
    Assertions.assertEquals("Konzept", DatenDokumentTyp.KONZEPT.toString());
    Assertions.assertEquals("Ontologie", DatenDokumentTyp.ONTOLOGIE.toString());
    Assertions.assertEquals("Initien", DatenDokumentTyp.INITIEN.toString());
  }
}
