package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects.ManuscriptaMediaevaliaController.HTTP_BILDER_MANUSCRIPTA_MEDIAEVALIA_DE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 18.10.23.
 * @version 1.0
 */
class MMKatalogURICreationTest {

  @Test
  void testextractObjectNumberFromPath() {

    MMKatalogURICreation mmKatalogURICreation = new MMKatalogURICreation(null);

    String number = mmKatalogURICreation.extractObjectNumberFromPath("HSK0496.htm");

    assertEquals("496", number);

    number = mmKatalogURICreation.extractObjectNumberFromPath("hs//kataloge/HSK0496");

    assertEquals("496", number);
  }

  @Test
  void testCreate() {
    KatalogRepository katalogRepository = Mockito.mock(KatalogRepository.class);
    Mockito.when(katalogRepository.findByHskId("77")).thenReturn(
        Optional.of(new KatalogBuilder().withId("77")
            .withDigitalisat(
                Digitalisat.DigitalisatBuilder().withManifest("https://manifest.url").build())
            .build()));

    MMKatalogURICreation mmKatalogURICreation = new MMKatalogURICreation(
        katalogRepository);

    assertEquals("https://handschriftenportal.de/?id=https://manifest.url",
        mmKatalogURICreation.create("HSK077.htm").toString());


  }

  @Test
  void testInfoRedirectURL() {
    KatalogRepository katalogRepository = Mockito.mock(KatalogRepository.class);
    Mockito.when(katalogRepository.findByHskId("77")).thenReturn(
        Optional.empty());

    MMKatalogURICreation mmKatalogURICreation = new MMKatalogURICreation(
        katalogRepository);

    assertEquals(HTTP_BILDER_MANUSCRIPTA_MEDIAEVALIA_DE + "HSK077.htm",
        mmKatalogURICreation.create("HSK077.htm").toString());


  }

}
