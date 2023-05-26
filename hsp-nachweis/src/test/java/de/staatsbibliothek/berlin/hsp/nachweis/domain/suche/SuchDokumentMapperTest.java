/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 21.12.22
 */
class SuchDokumentMapperTest {

  @Test
  void testMapBeschreibung() {
    Identifikation signatur = new Identifikation.IdentifikationBuilder()
        .withId("1")
        .withIdent("Signatur XYZ")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(new NormdatenReferenz("N-1", "Staatsbibliothek zu Berlin", "GND-1",
            NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .withAufbewahrungsOrt(new NormdatenReferenz("N-2", "Berlin", "GND-2",
            NormdatenReferenz.ORT_TYPE_NAME))
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withTitel("JUNIT Titel Beschreibung")
        .withIndentifikationen(Set.of(signatur))
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .addBeschreibungsKomponente(kopf)
        .addBeschreibungsBeteiligter(new NormdatenReferenz("N-3", "Boysen", "GND-3",
            NormdatenReferenz.PERSON_TYPE_NAME))
        .withAenderungsDatum(LocalDateTime.of(1968, 12, 29, 0, 0))
        .withId("123")
        .addPublikation(new Publikation("1", LocalDateTime.of(1983, 1, 2, 0, 0), PublikationsTyp.ERSTPUBLIKATION))
        .addPublikation(new Publikation("2", LocalDateTime.of(2022, 3, 4, 0, 0), PublikationsTyp.PUBLIKATION_HSP))
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .addAutor(new NormdatenReferenz("N-4", "autor, name", "GND-4",
            NormdatenReferenz.PERSON_TYPE_NAME))
        .build();

    SuchDokument suchDokument = SuchDokumentMapper.map(beschreibung, "beschreibungAsJson");

    assertNotNull(suchDokument);
    assertEquals("123", suchDokument.getId());
    assertEquals("Signatur XYZ", suchDokument.getSignatur());
    assertEquals("BS", suchDokument.getSuchDokumentTyp());
    assertEquals("JUNIT Titel Beschreibung", suchDokument.getTitel());
    assertEquals(Boolean.TRUE, suchDokument.getPubliziert());
    assertEquals("NICHT_GESPERRT", suchDokument.getSichtbarkeit());
    assertEquals("intern", suchDokument.getVerwaltungsTyp());
    assertNotNull(suchDokument.getBearbeiter());
    assertEquals(1, suchDokument.getBearbeiter().size());
    assertTrue(suchDokument.getBearbeiter().contains("Boysen"));
    assertEquals("Staatsbibliothek zu Berlin", suchDokument.getBestandhaltendeInstitutionName());
    assertEquals("Staatsbibliothek zu Berlin", suchDokument.getBestandhaltendeInstitutionNameFacette());
    assertEquals("Berlin", suchDokument.getBestandhaltendeInstitutionOrt());
    assertEquals(1983, suchDokument.getJahrDerPublikation());
    assertEquals("1968-12-29T00:00:00Z", suchDokument.getLastUpdate());
    assertNull(suchDokument.getContainsDigitalisat());
    assertNull(suchDokument.getContainsBuchschmuck());
    assertNull(suchDokument.getContainsBeschreibung());
    assertEquals("beschreibungAsJson", suchDokument.getSearchableValues());
    assertNotNull(suchDokument.getAutoren());
    assertEquals(1, suchDokument.getAutoren().size());
    assertTrue(suchDokument.getAutoren().contains("autor, name"));
  }

  @Test
  void testMapKOD() {

    final KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("1234")
        .withRegistrierungsDatum(LocalDateTime.of(1968, 12, 29, 0, 0))
        .withGueltigerIdentifikation(new IdentifikationBuilder()
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withIdent("Signatur")
            .withBesitzer(new NormdatenReferenz("N-1", "besitzer", "GND-1",
                NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
            .withAufbewahrungsOrt(new NormdatenReferenz("N-2", "aufbewahrungsOrt", "GND-2",
                NormdatenReferenz.ORT_TYPE_NAME))

            .build())
        .withDigitalisaten(Set.of(Digitalisat.DigitalisatBuilder().withID("DIGI-1").build()))
        .withRegistrierungsDatum(LocalDateTime.of(2022, 12, 21, 0, 0))
        .addBeschreibungsdokumentID("B-1")
        .build();

    SuchDokument suchDokument = SuchDokumentMapper.map(kod, "kodAsJson");

    assertNotNull(suchDokument);
    assertEquals("1234", suchDokument.getId());
    assertEquals("Signatur", suchDokument.getSignatur());
    assertEquals("KOD", suchDokument.getSuchDokumentTyp());
    assertNull(suchDokument.getTitel());
    assertEquals(Boolean.TRUE, suchDokument.getContainsBeschreibung());
    assertNull(suchDokument.getSichtbarkeit());
    assertNull(suchDokument.getVerwaltungsTyp());
    assertNotNull(suchDokument.getBearbeiter());
    assertEquals(0, suchDokument.getBearbeiter().size());
    assertEquals("besitzer", suchDokument.getBestandhaltendeInstitutionName());
    assertEquals("besitzer", suchDokument.getBestandhaltendeInstitutionNameFacette());
    assertEquals("aufbewahrungsOrt", suchDokument.getBestandhaltendeInstitutionOrt());
    assertNull(suchDokument.getJahrDerPublikation());
    assertEquals("2022-12-21T00:00:00Z", suchDokument.getLastUpdate());
    assertTrue(suchDokument.getContainsDigitalisat());
    assertNull(suchDokument.getContainsBuchschmuck());
    assertEquals("kodAsJson", suchDokument.getSearchableValues());
    assertNotNull(suchDokument.getAutoren());
    assertNull(suchDokument.getPubliziert());
    assertEquals(0, suchDokument.getAutoren().size());
  }
}
