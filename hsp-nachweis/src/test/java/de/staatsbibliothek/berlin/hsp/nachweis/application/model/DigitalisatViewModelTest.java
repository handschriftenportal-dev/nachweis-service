package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.DigitalisatErfassenController.DIGITIZATION_SCHEME_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.time.LocalDate;
import java.time.Month;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 18.10.2022
 */

class DigitalisatViewModelTest {

  @Test
  void testMap() {

    assertNull(DigitalisatViewModel.map(null));

    NormdatenReferenz capture = new NormdatenReferenz("1212", "captureType", "",
        DIGITIZATION_SCHEME_TYPE);

    NormdatenReferenz completeness = new NormdatenReferenz("1212weqwe", "completeness", "",
        DIGITIZATION_SCHEME_TYPE);

    Digitalisat digitalisat = Digitalisat.DigitalisatBuilder()
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
        .addDigitalisatsKonzepte(Set.of(capture, completeness))
        .build();

    DigitalisatViewModel viewModel = DigitalisatViewModel.map(digitalisat);
    assertNotNull(viewModel);
    assertEquals(digitalisat.getId(), viewModel.getId());
    assertEquals(digitalisat.getAlternativeURL().toASCIIString(), viewModel.getAlternativeUrl());
    assertEquals(digitalisat.getDigitalisierungsDatum(), viewModel.getDigitalisierungsdatum());
    assertEquals(digitalisat.getDigitalisierendeEinrichtung(), viewModel.getEinrichtung());
    assertEquals(digitalisat.getEntstehungsOrt(), viewModel.getOrt());
    assertEquals(digitalisat.getManifestURL().toASCIIString(), viewModel.getManifestURL());
    assertEquals(digitalisat.getThumbnailURL().toASCIIString(), viewModel.getThumbnail());
    assertEquals(digitalisat.getErstellungsDatum(), viewModel.getErstellungsdatum());
    assertNotNull(digitalisat.getDigitalisatsKonzepte());
    assertEquals(capture, viewModel.getDigitalisatTypKonzepte().get(0));
    assertEquals(completeness, viewModel.getDigitalisatTypKonzepte().get(1));
  }

  @Test
  void testHasSameUrl() {
    DigitalisatViewModel manifestA = DigitalisatViewModel.builder()
        .id("A").manifestURL("https://hsp.de/manifest.json").build();
    DigitalisatViewModel manifestAOtherId = DigitalisatViewModel.builder()
        .id("B").manifestURL("https://hsp.de/manifest.json").build();
    DigitalisatViewModel manifestB = DigitalisatViewModel.builder()
        .id("C").manifestURL("https://hsp.de/other.json").build();
    DigitalisatViewModel alternativeA = DigitalisatViewModel.builder()
        .id("D").alternativeUrl("https://nbn-resolving.org/urn:1").build();
    DigitalisatViewModel alternativeAOtherId = DigitalisatViewModel.builder()
        .id("E").alternativeUrl("https://nbn-resolving.org/urn:1").build();
    DigitalisatViewModel noUrls = DigitalisatViewModel.builder().id("F").build();

    // matches on manifest URL regardless of id
    assertTrue(manifestA.hasSameUrl(manifestAOtherId));
    // matches on alternative URL
    assertTrue(alternativeA.hasSameUrl(alternativeAOtherId));
    // different manifest URLs do not match
    assertFalse(manifestA.hasSameUrl(manifestB));
    // manifest vs alternative (no overlap) do not match
    assertFalse(manifestA.hasSameUrl(alternativeA));
    // two null-url models never match (avoids null==null false positive)
    assertFalse(noUrls.hasSameUrl(DigitalisatViewModel.builder().id("G").build()));
    // null argument is handled
    assertFalse(manifestA.hasSameUrl((DigitalisatViewModel) null));
  }

  @Test
  void testHasSameUrlAgainstDomainDigitalisat() {
    DigitalisatViewModel vm = DigitalisatViewModel.builder()
        .manifestURL("https://hsp.de/manifest.json").build();

    Digitalisat sameManifest = Digitalisat.DigitalisatBuilder()
        .withID("X").withManifest("https://hsp.de/manifest.json").build();
    Digitalisat otherManifest = Digitalisat.DigitalisatBuilder()
        .withID("Y").withManifest("https://hsp.de/other.json").build();

    assertTrue(vm.hasSameUrl(sameManifest));
    assertFalse(vm.hasSameUrl(otherManifest));
    assertFalse(vm.hasSameUrl((Digitalisat) null));
  }

  @Test
  void testToDigitalisat() {
    NormdatenReferenz konzept = new NormdatenReferenz("1212", "completeness", "",
        DIGITIZATION_SCHEME_TYPE);
    DigitalisatViewModel vm = DigitalisatViewModel.builder()
        .id("D-1")
        .manifestURL("https://hsp.de/manifest.json")
        .alternativeUrl("https://alternative.url")
        .thumbnail("https://hsp.de/thumbnail.jpg")
        .ort(new NormdatenReferenz("HSP-2", "Berlin", "GND-2", NormdatenReferenz.ORT_TYPE_NAME))
        .einrichtung(
            new NormdatenReferenz("HSP-1", "StaBi", "GND-1",
                NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .digitalisierungsdatum(LocalDate.of(2020, Month.MAY, 4))
        .digitalisatTypKonzepte(java.util.List.of(konzept))
        .build();

    Digitalisat digitalisat = vm.toDigitalisat();

    assertNotNull(digitalisat);
    assertEquals("D-1", digitalisat.getId());
    assertEquals(vm.getManifestURL(), digitalisat.getManifestURL().toASCIIString());
    assertEquals(vm.getAlternativeUrl(), digitalisat.getAlternativeURL().toASCIIString());
    assertEquals(vm.getDigitalisierungsdatum(), digitalisat.getDigitalisierungsDatum());
    assertEquals(LocalDate.now(), digitalisat.getErstellungsDatum());
    assertTrue(digitalisat.getDigitalisatsKonzepte().contains(konzept));
  }

}
