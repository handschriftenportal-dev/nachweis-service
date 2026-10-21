package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 18.10.2022
 */

class KatalogBeschreibungViewModelTest {

  @Test
  void testMap() {
    Beschreibung beschreibung = createBeschreibung();

    KatalogBeschreibungViewModel viewModel = KatalogBeschreibungViewModel.map(beschreibung);

    assertNotNull(viewModel);
    assertEquals(beschreibung.getId(), viewModel.getId());
    assertEquals(beschreibung.getKatalogID(), viewModel.getKatalogId());
    assertEquals("Stabi", viewModel.getBestandhaltendeInstitutionName());
    assertEquals("Berlin", viewModel.getBestandhaltendeInstitutionOrt());
    assertEquals(2020, viewModel.getJahrDerErstpublikation());
    assertEquals("Autor1, Name1; Autor2, Name2", viewModel.getAutoren());
    assertEquals("SIG. 1", viewModel.getSignatur());
    assertEquals("Titel", viewModel.getTitel());
    assertEquals(beschreibung.getId(), viewModel.getId());
  }

  Beschreibung createBeschreibung() {
    Identifikation identifikation = new IdentifikationBuilder()
        .withId("GID-1")
        .withIdent("SIG. 1")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withAufbewahrungsOrt(
            new NormdatenReferenz("HSP-2", "Berlin", "GND-2", NormdatenReferenz.ORT_TYPE_NAME))
        .withBesitzer(
            new NormdatenReferenz("HSP-1", "Stabi", "GND-1",
                NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .build();
    return new Beschreibung.BeschreibungsBuilder("B-1", "KOD-1", identifikation)
        .withKatalog("KAT-1")
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

}
