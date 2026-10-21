package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KatalogBeschreibungReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 18.10.23.
 * @version 1.0
 */
class MMBeschreibungURICreationTest {

  private static final Identifikation IDENTIFIKATION = new IdentifikationBuilder()
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .withIdent("1-1-1")
      .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
      .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
      .build();

  @Test
  void testextractObjectNumberFromPath() {

    MMBeschreibungURICreation mmBeschreibungURICreation = new MMBeschreibungURICreation(
        null, null);

    String number = mmBeschreibungURICreation.extractMXMLNumberFromPath("obj31576567.html");

    assertEquals("MXML-31576567", number);

    number = mmBeschreibungURICreation.extractMXMLNumberFromPath("dokumente/obj31576567");

    assertEquals("MXML-31576567", number);

    number = mmBeschreibungURICreation.extractMXMLNumberFromPath("/dokumente/html/obj90506982,T");

    assertEquals("MXML-90506982-T", number);
  }

  @Test
  void testCreateForKatalogOrRegularBeschreibung() {
    BeschreibungsRepository beschreibungsRepository = mock(BeschreibungsRepository.class);
    KatalogRepository katalogRepository = mock(KatalogRepository.class);
    when(beschreibungsRepository.findByAltIdentifierOptional("MXML-31576567")).thenReturn(
        Optional.of(new BeschreibungsBuilder("HSP123", "KOD1", IDENTIFIKATION).build()));
    when(katalogRepository.findKatalogBeschreibungReferenzByBeschreibungsID("HSP123")).thenReturn(
        Optional.of(new KatalogBeschreibungReferenz("HSP123", "", "", "", null)));

    MMBeschreibungURICreation mmBeschreibungURICreation = new MMBeschreibungURICreation(
        katalogRepository, beschreibungsRepository);

    assertEquals("https://handschriftenportal.de/?id=HSP123",
        mmBeschreibungURICreation.create("obj31576567.html").toString());
  }

  @Test
  void testCreateForNotKatalog() {
    BeschreibungsRepository beschreibungsRepository = mock(BeschreibungsRepository.class);
    KatalogRepository katalogRepository = mock(KatalogRepository.class);
    when(beschreibungsRepository.findByAltIdentifierOptional("MXML-31576567")).thenReturn(
        Optional.of(new BeschreibungsBuilder("HSP123", "KOD123", IDENTIFIKATION)
            .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION_RETRO)
            .build()));
    when(katalogRepository.findKatalogBeschreibungReferenzByBeschreibungsID("HSP123")).thenReturn(
        Optional.empty());

    MMBeschreibungURICreation mmBeschreibungURICreation = new MMBeschreibungURICreation(
        katalogRepository, beschreibungsRepository);

    assertEquals("https://handschriftenportal.de/?id=KOD123",
        mmBeschreibungURICreation.create("obj31576567.html").toString());
  }
}
