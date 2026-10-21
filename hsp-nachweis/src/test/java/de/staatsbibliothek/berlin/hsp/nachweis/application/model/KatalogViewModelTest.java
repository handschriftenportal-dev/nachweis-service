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
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KatalogBeschreibungReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 18.10.2022
 */

class KatalogViewModelTest {

  private static final Identifikation IDENTIFIKATION = new IdentifikationBuilder()
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .withIdent("1-1-1")
      .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
      .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
      .build();

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
    return new Beschreibung.BeschreibungsBuilder("HSP-2", "KOD-1", IDENTIFIKATION)
        .withKatalog("HSP-1")
        .withBeschreibungsTyp(BeschreibungsTyp.MEDIEVAL)
        .addBeschreibungsKomponente(new BeschreibungsKomponenteKopfBuilder()
            .withId("BKK-1")
            .withTitel("Titel")
            .build())
        .withPublikation(new Publikation.PublikationBuilder()
            .withErstpublikationsDatum(LocalDateTime.of(2020, Month.MAY, 4, 12, 0, 0))
            .build())
        .addAutor(new NormdatenReferenz("HSP-3", "Autor1, Name1", "GND-3",
            NormdatenReferenz.PERSON_TYPE_NAME))
        .addAutor(new NormdatenReferenz("HSP-4", "Autor2, Name2", "GND-3",
            NormdatenReferenz.PERSON_TYPE_NAME))
        .build();
  }

  Katalog createKatalog() {
    return new KatalogBuilder()
        .withId("HSP-1")
        .withHSKID("987654321")
        .withTitle("Katalog-Titel")
        .addAutor(new NormdatenReferenz("HSP-3", "Autor1, Name1", "GND-3",
            NormdatenReferenz.PERSON_TYPE_NAME))
        .addAutor(new NormdatenReferenz("HSP-4", "Autor2, Name2", "GND-4",
            NormdatenReferenz.PERSON_TYPE_NAME))
        .withErstelldatum(LocalDateTime.of(2020, Month.MAY, 4, 12, 0, 0))
        .withAenderungsdatum(LocalDateTime.of(2020, Month.JUNE, 5, 13, 59, 0))
        .withPublikationsJahr("1968")
        .withLizenzURI("http://creativecommons.org/licenses/by-sa/3.0/de/")
        .withTEIXML("<TEI/>")
        .withVerlag(new NormdatenReferenz("HSP-5", "Harrassowitz", "GND-5",
            NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .addKatalogBeschreibungReferenz(new KatalogBeschreibungReferenz("B-1", "1",
            "2", "text 1", URI.create("https://iiif.org/manifest_1.json")))
        .addKatalogBeschreibungReferenz(new KatalogBeschreibungReferenz("B-2", "3",
            "4", "text 2", URI.create("https://iiif.org/manifest_2.json")))
        .withDigitalisat(Digitalisat.DigitalisatBuilder()
            .withAlternativeUrl("https://alternative.url")
            .withDigitalisierendeEinrichtung(
                new NormdatenReferenz("HSP-1", "StaBi", "GND-1",
                    NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
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
