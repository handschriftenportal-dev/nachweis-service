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

package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KatalogBeschreibungReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz.LizenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 18.10.2022
 */

class KatalogViewModelTest {

  @Test
  void testMap() {
    Beschreibung beschreibung = createBeschreibung();
    Katalog katalog = createKatalog();

    KatalogViewModel viewModel = KatalogViewModel.map(katalog, List.of(beschreibung));

    assertNotNull(viewModel);
    assertEquals(katalog.getId(), viewModel.getId());
    assertEquals(katalog.getTitle(), viewModel.getTitel());
    assertEquals("Autor1, Name1; Autor2, Name2", viewModel.getAutoren());
    assertEquals("1968", viewModel.getPublikationsJahr());
    assertEquals("Harrassowitz", viewModel.getVerlag());
    assertEquals(katalog.getLizenzUri(), viewModel.getLizenz());
    assertEquals(katalog.getTeiXML(), viewModel.getTeiXML());
    assertEquals(1, viewModel.getAnzahlBeschreibungen());
    assertEquals(2, viewModel.getAnzahlReferenzen());
    assertNotNull(viewModel.getDigitalisat());
    assertEquals(katalog.getDigitalisat().getId(), viewModel.getDigitalisat().getId());
    assertNotNull(viewModel.getBeschreibungen());
    assertEquals(1, viewModel.getBeschreibungen().size());

    assertEquals(katalog.getErstellDatum(), viewModel.getErstellDatum());
    assertEquals(katalog.getAenderungsDatum(), viewModel.getAenderungsDatum());

  }

  Beschreibung createBeschreibung() {
    return new Beschreibung.BeschreibungsBuilder()
        .withId("HSP-2")
        .withKatalog("HSP-1")
        .withBeschreibungsTyp(BeschreibungsTyp.MEDIEVAL)
        .withLizenz(new LizenzBuilder()
            .addUri("http://creativecommons.org/licenses/by-sa/3.0/de/")
            .withBeschreibungsText("Namensnennung - Weitergabe unter gleichen Bedingungen 3.0 Deutschland")
            .build())
        .addBeschreibungsKomponente(new BeschreibungsKomponenteKopfBuilder()
            .withId("BKK-1")
            .withIndentifikationen(Set.of(new IdentifikationBuilder()
                .withId("GID-1")
                .withIdent("SIG. 1")
                .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
                .withAufbewahrungsOrt(
                    new NormdatenReferenz("HSP-2", "Berlin", "GND-2", NormdatenReferenz.ORT_TYPE_NAME))
                .withBesitzer(
                    new NormdatenReferenz("HSP-1", "Stabi", "GND-1", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
                .build()))
            .withTitel("Titel")
            .build())
        .withKodID("KOD-1")
        .addPublikation(new Publikation("PUB-1", LocalDateTime.of(2020, Month.MAY, 4, 12, 0, 0),
            PublikationsTyp.ERSTPUBLIKATION))
        .addAutor(new NormdatenReferenz("HSP-3", "Autor1, Name1", "GND-3", NormdatenReferenz.PERSON_TYPE_NAME))
        .addAutor(new NormdatenReferenz("HSP-4", "Autor2, Name2", "GND-3", NormdatenReferenz.PERSON_TYPE_NAME))
        .build();
  }

  Katalog createKatalog() {
    return new KatalogBuilder()
        .withId("HSP-1")
        .withHSKID("987654321")
        .withTitle("Katalog-Titel")
        .addAutor(new NormdatenReferenz("HSP-3", "Autor1, Name1", "GND-3", NormdatenReferenz.PERSON_TYPE_NAME))
        .addAutor(new NormdatenReferenz("HSP-4", "Autor2, Name2", "GND-4", NormdatenReferenz.PERSON_TYPE_NAME))
        .withErstelldatum(LocalDateTime.of(2020, Month.MAY, 4, 12, 0, 0))
        .withAenderungsdatum(LocalDateTime.of(2020, Month.JUNE, 5, 13, 59, 0))
        .withPublikationsJahr("1968")
        .withLizenzURI("http://creativecommons.org/licenses/by-sa/3.0/de/")
        .withTEIXML("<TEI/>")
        .withVerlag(new NormdatenReferenz("HSP-5", "Harrassowitz", "GND-5", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .addKatalogBeschreibungReferenz(new KatalogBeschreibungReferenz("B-1", "1",
            "2", "text 1", URI.create("https://iiif.org/manifest_1.json")))
        .addKatalogBeschreibungReferenz(new KatalogBeschreibungReferenz("B-2", "3",
            "4", "text 2", URI.create("https://iiif.org/manifest_2.json")))
        .withDigitalisat(Digitalisat.DigitalisatBuilder()
            .withAlternativeUrl("https://alternative.url")
            .withDigitalisierendeEinrichtung(
                new NormdatenReferenz("HSP-1", "StaBi", "GND-1", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
            .withDigitalisierungsdatum(LocalDate.of(2020, Month.MAY, 4))
            .withEntstehungsort(
                new NormdatenReferenz("HSP-2", "Berlin", "GND-2", NormdatenReferenz.ORT_TYPE_NAME))
            .withErstellungsdatum(LocalDate.of(2020, Month.MARCH, 14))
            .withID("D-1")
            .withManifest("https://hsp.de/manifest.json")
            .withThumbnail("https://hsp.de/thumbnail.jpg")
            .withTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL)
            .build())
        .build();
  }
}
