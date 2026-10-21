package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BestandNachweisDTO;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 24.04.23.
 * @version 1.0
 */

@QuarkusTest
class BestandsRepositoryAdapterTest {

  private static final Identifikation IDENTIFIKATION = new IdentifikationBuilder()
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .withIdent("1-1-1")
      .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
      .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
      .build();

  @Inject
  BestandsRepository bestandsRepository;

  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;

  @Inject
  BeschreibungsRepository beschreibungsRepository;

  @Test
  void testCreation() {

    assertNotNull(bestandsRepository);
    assertNotNull(((BestandsRepositoryAdapter) bestandsRepository).getEntityManager());
  }

  @Test
  @TestTransaction
  void testFindAllBestaende() {

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withGndIdentifier("GND-ID-123")
        .withRegistrierungsDatum(LocalDateTime.now())
        .withTEIXml("<TEI/>").build();
    kulturObjektDokumentRepository.save(kod);

    Beschreibung beschreibung = new BeschreibungsBuilder(TEIValues.UUID_PREFIX + UUID.randomUUID(),
        kod.getId(), IDENTIFIKATION)
        .withKatalog("kat-1")
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN).build();
    beschreibungsRepository.save(beschreibung);

    BestandNachweisDTO bestandNachweisDTO = bestandsRepository.findAllBestaende();

    assertNotNull(bestandNachweisDTO);
    assertEquals(1, bestandNachweisDTO.getAnzahlKOD());
    assertEquals(1, bestandNachweisDTO.getAnzahlBeschreibungen());
    assertEquals(0, bestandNachweisDTO.getAnzahlDigitalisate());
  }

  @Test
  @TestTransaction
  void testfindAllKulturObjekteForInstitution() {

    Identifikation gueltige = new Identifikation.IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withId("weeqweqwe")
        .withBesitzer(new NormdatenReferenz("wwewewe", "Staatsbibliothek zu Berlin", "GND2323"))
        .withAufbewahrungsOrt(new NormdatenReferenz("wwfffewewe", "Berlin", "GND23ddd23"))
        .build();

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withGndIdentifier("GND-ID-123")
        .withRegistrierungsDatum(LocalDateTime.now())
        .withTEIXml("<TEI/>")
        .withGueltigerIdentifikation(gueltige)
        .build();

    kulturObjektDokumentRepository.save(kod);

    List<BestandInstitutionElementDTO> elements = bestandsRepository.findAllKulturObjekteForInstitution();

    assertEquals(1, elements.size());

    assertEquals("Berlin", elements.get(0).getAufbewahrungsOrt());
    assertEquals("wwfffewewe", elements.get(0).getAufbewahrungsOrtID());
    assertEquals("Staatsbibliothek zu Berlin", elements.get(0).getBesitzendeEinrichtung());
    assertEquals("wwewewe", elements.get(0).getBesitzendeEinrichtungID());
    assertEquals(1, elements.get(0).getBestandNachweis());
  }

  @Test
  @TestTransaction
  void testfindAllBeschreibungenForInstitution() {

    Identifikation gueltige = new Identifikation.IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withId(UUID.randomUUID().toString())
        .withBesitzer(
            new NormdatenReferenz(UUID.randomUUID().toString(), "Staatsbibliothek zu Berlin",
                "GND2323"))
        .withAufbewahrungsOrt(
            new NormdatenReferenz(UUID.randomUUID().toString(), "Berlin", "GND23ddd23"))
        .build();

    Identifikation alternative = new Identifikation.IdentifikationBuilder()
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .withId(UUID.randomUUID().toString())
        .withBesitzer(
            new NormdatenReferenz(UUID.randomUUID().toString(), "Staatsbibliothek zu Berlin",
                "GND2323"))
        .withAufbewahrungsOrt(
            new NormdatenReferenz(UUID.randomUUID().toString(), "Berlin", "GND23ddd23"))
        .build();

    BeschreibungsKomponenteKopf komponenteKopf = new BeschreibungsKomponenteKopf.
        BeschreibungsKomponenteKopfBuilder().withId("HSP2323234")
        .withAlternativeIdentifikationen(Set.of(alternative))
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder(
        TEIValues.UUID_PREFIX + UUID.randomUUID(), "KOD-123", gueltige)
        .withTEIXml("<TEI/>")
        .build();

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withGndIdentifier("GND-ID-123")
        .withRegistrierungsDatum(LocalDateTime.now())
        .withTEIXml("<TEI/>")
        .withGueltigerIdentifikation(gueltige)
        .build();

    kulturObjektDokumentRepository.save(kod);

    beschreibung.getBeschreibungsStruktur().add(komponenteKopf);

    beschreibungsRepository.save(beschreibung);

    List<BestandInstitutionElementDTO> elements = bestandsRepository.findAllBeschreibungenForInstitution();

    assertEquals(1, elements.size());

    assertEquals("Berlin", elements.getFirst().getAufbewahrungsOrt());
    assertEquals("Staatsbibliothek zu Berlin", elements.getFirst().getBesitzendeEinrichtung());
    assertEquals(1, elements.getFirst().getBestandNachweis());
  }

  @Test
  @TestTransaction
  void testfindAllDigitalisteForKOD() {

    Digitalisat digitalisat = Digitalisat.DigitalisatBuilder()
        .withID(UUID.randomUUID().toString())
        .withErstellungsdatum(LocalDate.now())
        .withTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL)
        .withManifest("http://manifest.json")
        .build();

    Digitalisat digitalisat1 = Digitalisat.DigitalisatBuilder()
        .withID(UUID.randomUUID().toString())
        .withErstellungsdatum(LocalDate.now())
        .withTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL)
        .withManifest("http://manifest.json")
        .build();

    Identifikation gueltige = new Identifikation.IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withId("weeqweqwe")
        .withBesitzer(new NormdatenReferenz("wwewewe", "Staatsbibliothek zu Berlin", "GND2323"))
        .withAufbewahrungsOrt(new NormdatenReferenz("wwfffewewe", "Berlin", "GND23ddd23"))
        .build();

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withGndIdentifier("GND-ID-123")
        .withRegistrierungsDatum(LocalDateTime.now())
        .withTEIXml("<TEI/>")
        .withGueltigerIdentifikation(gueltige)
        .withDigitalisaten(Set.of(digitalisat, digitalisat1))
        .build();

    kulturObjektDokumentRepository.save(kod);

    List<BestandInstitutionElementDTO> elements = bestandsRepository.findAllDigitalisateForKOD();

    assertNotNull(elements);

    assertEquals(1, elements.size());

    assertEquals("Berlin", elements.get(0).getAufbewahrungsOrt());
    assertEquals("Staatsbibliothek zu Berlin", elements.get(0).getBesitzendeEinrichtung());
    assertEquals(2, elements.get(0).getBestandNachweis());
  }

}
