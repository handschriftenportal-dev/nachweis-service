package de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJob;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJobBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.hsp_frontend.DiscoveryServicePort;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.hsp_frontend.DiscoveryServiceStubSetup;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.04.23.
 * @version 1.0
 */

@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
class BestandsBoundaryServiceTest extends DiscoveryServiceStubSetup {

  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;
  @Inject
  BeschreibungsRepository beschreibungsRepository;
  @Inject
  BestandsBoundary bestandsBoundary;
  @Inject
  @RestClient
  DiscoveryServicePort discoveryServicePort;
  @Inject
  BestandsRepository bestandsRepository;

  @Test
  void testCreation() {

    assertNotNull(bestandsBoundary);
    assertNotNull(((BestandsBoundaryService) bestandsBoundary).getBestandsRepository());
    assertNotNull(((BestandsBoundaryService) bestandsBoundary).getDiscoveryServicePort());
  }

  @Test
  void testcreateBestandsView() {

    BestandsView view = bestandsBoundary.createBestandsView();

    assertNotNull(view);

    assertEquals(133232, view.getKulturobjekteBestandsElement().get().getBestandPraesentation());
    assertEquals(0, view.getKulturobjekteBestandsElement().get().getBestandNachweis());
  }

  @Test
  @TestTransaction
  void testloadBestandsInstitutionDataForKulturobjektdokument() {

    Identifikation gueltige = new Identifikation.IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withId("weeqweqwe")
        .withBesitzer(new NormdatenReferenz("NORM-83c5a34d-300e-3b8d-af1e-2d5582778963",
            "Bayerische Staatsbibliothek", "GND2323"))
        .withAufbewahrungsOrt(
            new NormdatenReferenz("NORM-d0a6343a-081a-3335-baa9-65ce4fd0845c", "München",
                "GND23ddd23"))
        .build();

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withGndIdentifier("GND-ID-123")
        .withRegistrierungsDatum(LocalDateTime.now())
        .withTEIXml("<TEI/>")
        .withGueltigerIdentifikation(gueltige)
        .build();

    kulturObjektDokumentRepository.save(kod);

    IndexJobBoundary indexJobStatusBoundary = mock(IndexJobBoundary.class);
    Event<IndexJob> indexJobEvent = mock(Event.class);

    BestandsBoundaryService bestandsBoundaryService = new BestandsBoundaryService(
        discoveryServicePort, bestandsRepository, indexJobEvent);

    List<BestandInstitutionElement> bestandInstitutionElements = bestandsBoundaryService.loadBestandsInstitutionDataForKulturobjektdokument();

    assertNotNull(bestandInstitutionElements);

    assertEquals(240, bestandInstitutionElements.size());

    bestandInstitutionElements.stream()
        .filter(
            b -> b.getBesitzendeEinrichtung().equals("Bayerische Staatsbibliothek"))
        .findAny().ifPresent(sum -> assertEquals(49394, sum.getBestandPraesentation()));
  }

  @Test
  @TestTransaction
  void testloadBestandsInstitutionDataForBeschreibungen() {

    Identifikation gueltigeIdentifikation = new Identifikation.IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withId("weeqweqwe")
        .withBesitzer(new NormdatenReferenz("NORM-774909e2-f687-30cb-a5c4-ddc95806d6be",
            "Staatsbibliothek zu Berlin", "GND2323"))
        .withAufbewahrungsOrt(
            new NormdatenReferenz("NORM-ee1611b6-1f56-38e7-8c12-b40684dbb395", "Berlin",
                "GND23ddd23"))
        .build();

    BeschreibungsKomponenteKopf komponenteKopf = new BeschreibungsKomponenteKopf
        .BeschreibungsKomponenteKopfBuilder().withId("HSP2323234")
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder(
        TEIValues.UUID_PREFIX + UUID.randomUUID(), "kodid", gueltigeIdentifikation)
        .withTEIXml("<TEI/>")
        .build();

    beschreibung.getBeschreibungsStruktur().add(komponenteKopf);
    beschreibungsRepository.save(beschreibung);

    IndexJobBoundary indexJobStatusBoundary = mock(IndexJobBoundary.class);
    Event<IndexJob> indexJobEvent = mock(Event.class);

    BestandsBoundaryService bestandsBoundaryService = new BestandsBoundaryService(
        discoveryServicePort, bestandsRepository, indexJobEvent);

    List<BestandInstitutionElement> bestandInstitutionElements = bestandsBoundaryService.loadBestandsInstitutionDataForBeschreibungen();

    assertNotNull(bestandInstitutionElements);

    assertEquals(220, bestandInstitutionElements.size());

    bestandInstitutionElements.stream()
        .filter(
            b -> b.getBesitzendeEinrichtung().equals("Staatsbibliothek zu Berlin"))
        .findAny().ifPresent(sum -> assertEquals(8276, sum.getBestandPraesentation()));

  }

  @Test
  @TestTransaction
  void testloadBestandsInstitutionDataForDigitalisate() {

    Digitalisat digitalisat = Digitalisat.DigitalisatBuilder()
        .withID(UUID.randomUUID().toString())
        .withErstellungsdatum(LocalDate.now())
        .withTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL)
        .withManifest("http://manifest.json")
        .build();

    Identifikation gueltige = new Identifikation.IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withId("weeqweqwe")
        .withBesitzer(new NormdatenReferenz("NORM-83c5a34d-300e-3b8d-af1e-2d5582778963",
            "Bayerische Staatsbibliothek", "GND2323"))
        .withAufbewahrungsOrt(
            new NormdatenReferenz("NORM-d0a6343a-081a-3335-baa9-65ce4fd0845c", "München",
                "GND23ddd23"))
        .build();

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withGndIdentifier("GND-ID-123")
        .withRegistrierungsDatum(LocalDateTime.now())
        .withTEIXml("<TEI/>")
        .withGueltigerIdentifikation(gueltige)
        .withDigitalisaten(Set.of(digitalisat))
        .build();

    kulturObjektDokumentRepository.save(kod);

    IndexJobBoundary indexJobBoundary = mock(IndexJobBoundary.class);
    Event<IndexJob> indexJobEvent = mock(Event.class);

    BestandsBoundaryService bestandsBoundaryService = new BestandsBoundaryService(
        discoveryServicePort, bestandsRepository, indexJobEvent);

    List<BestandInstitutionElement> bestandInstitutionElements = bestandsBoundaryService.loadBestandsInstitutionDataForDigitalisate();

    assertNotNull(bestandInstitutionElements);

    assertEquals(71, bestandInstitutionElements.size());

    bestandInstitutionElements.stream()
        .filter(b -> b.getBesitzendeEinrichtung().equals("Bayerische Staatsbibliothek"))
        .findAny().ifPresent(sum -> {
          assertEquals(10247, sum.getBestandPraesentation());
          assertEquals(1, sum.getBestandNachweis());
          assertEquals(-10246, sum.getBestandDifferenz());
        });

  }
}
