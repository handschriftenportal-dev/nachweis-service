package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.ApprovalRequest;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungInReviewDbView;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungListDbView;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungRejectedDbView;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 11.04.2022
 */

@QuarkusTest
class DashboardControllerTest {

  static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
      "dd.MM.yyyy HH:mm:ss");

  @Inject
  BeschreibungsBoundary beschreibungsBoundary;

  @Inject
  BearbeiterRepository bearbeiterRepository;
  @Inject
  BeschreibungsRepository beschreibungsRepository;

  @Inject
  BeschreibungsService beschreibungsService;
  LocalDateTime erstellungsdatum = LocalDateTime.of(2022, 11, 10, 9, 8, 7);
  LocalDateTime aenderungsdatum = LocalDateTime.of(2022, 12, 11, 10, 9, 8);
  BeschreibungListDbView model = new BeschreibungListDbView(
      "HSP-1",
      "KOD-1",
      "KAT-1",
      "Sig. 1",
      "Titel",
      VerwaltungsTyp.EXTERN.name(),
      DokumentObjektTyp.HSP_DESCRIPTION.name(),
      DokumentObjektTyp.HSP_DESCRIPTION.name(),
      "Piotr Czarnecki",
      "Berlin",
      "Staatsbibliothek zu Berlin",
      erstellungsdatum,
      erstellungsdatum.format(DATE_TIME_FORMATTER),
      null,
      aenderungsdatum,
      aenderungsdatum.format(DATE_TIME_FORMATTER),
      null,
      "Khallouf, Masaoud",
      null,
      null,
      null);


  @Test
  void testSetup() {
    BeschreibungsBoundary beschreibungsBoundaryMock = mock(BeschreibungsBoundary.class);
    when(beschreibungsBoundaryMock.findLatestModifiedTopTen()).thenReturn(List.of(model));
    BearbeiterService bearbeiterServiceMock = mock(BearbeiterService.class, RETURNS_DEEP_STUBS);
    when(bearbeiterServiceMock.getLoggedBearbeiter().getFullName()).thenReturn("Khallouf, Masaoud");
    DashboardController dashboardController = new DashboardController(beschreibungsBoundaryMock,
        bearbeiterServiceMock);
    dashboardController.setup();

    assertNotNull(dashboardController.getBeschreibungen());
    assertEquals(1, dashboardController.getBeschreibungen().size());
    assertTrue(dashboardController.getBeschreibungen().stream()
        .anyMatch(b -> "HSP-1".equals(b.getHspId())));
    assertTrue(dashboardController.getBeschreibungen().stream()
        .anyMatch(b -> "Khallouf, Masaoud".equals(b.getBearbeiter())));
    assertNotNull(dashboardController.getMeineBeschreibungen());
  }

  @Test
  void testAnmelden() {
    BearbeiterService bearbeiterServiceMock = mock(BearbeiterService.class);
    DashboardController dashboardController = new DashboardController(null,
        bearbeiterServiceMock);

    dashboardController.anmelden();

    verify(bearbeiterServiceMock, times(1)).startLogin();
  }

  @Test
  @TestTransaction
  void testMeineBeschreibungen() throws BeschreibungsException {
    Bearbeiter bearbeiter = new Bearbeiter.BearbeiterBuilder()
        .withBearbeitername("max.mustermann")
        .withNachname("Mustermann")
        .withVorname("Max")
        .build();
    setupTestData("HSP-1", Set.of(bearbeiter));

    BearbeiterService bearbeiterServiceMock = mock(BearbeiterService.class, RETURNS_DEEP_STUBS);
    when(bearbeiterServiceMock.getLoggedBearbeiter().getFullName()).thenReturn("Mustermann, Max");
    DashboardController dashboardController = new DashboardController(beschreibungsBoundary,
        bearbeiterServiceMock);
    dashboardController.setup();
    dashboardController.getMeineBeschreibungen();

    assertNotNull(dashboardController.getMeineBeschreibungen());
    assertEquals(1, dashboardController.getMeineBeschreibungen().size());
    assertTrue(dashboardController.getMeineBeschreibungen().stream()
        .anyMatch(b -> "HSP-1".equals(b.getHspId())));
    assertTrue(dashboardController.getMeineBeschreibungen().stream()
        .anyMatch(b -> "Mustermann, Max".equals(b.getBearbeiter())));
  }

  @Test
  @TestTransaction
  void testMeineBeschreibungenMultipleBearbeiter() throws BeschreibungsException {
    Bearbeiter bearbeiter = new Bearbeiter.BearbeiterBuilder()
        .withBearbeitername("max.mustermann")
        .withNachname("Mustermann")
        .withVorname("Max")
        .build();
    setupTestData("HSP-1", Set.of(bearbeiter));
    Bearbeiter bearbeiter2 = new Bearbeiter.BearbeiterBuilder()
        .withBearbeitername("hansGlueck")
        .withNachname("Glueck")
        .withVorname("Hans")
        .build();
    setupTestData("HSP-2", Set.of(bearbeiter, bearbeiter2));

    BearbeiterService bearbeiterServiceMock = mock(BearbeiterService.class, RETURNS_DEEP_STUBS);
    when(bearbeiterServiceMock.getLoggedBearbeiter().getFullName()).thenReturn("Mustermann, Max");
    DashboardController dashboardController = new DashboardController(beschreibungsBoundary,
        bearbeiterServiceMock);
    dashboardController.setup();
    dashboardController.getMeineBeschreibungen();
    assertNotNull(dashboardController.getMeineBeschreibungen());
    assertEquals(2, dashboardController.getMeineBeschreibungen().size());
    assertFalse(dashboardController.getMeineBeschreibungen().isEmpty());
    assertTrue(dashboardController.getMeineBeschreibungen().stream()
        .anyMatch(b -> "HSP-1".equals(b.getHspId())));
    assertTrue(dashboardController.getMeineBeschreibungen().stream()
        .anyMatch(b -> "Mustermann, Max".equals(b.getBearbeiter())));
  }

  @TestTransaction
  void setupTestData(String bId, Set<Bearbeiter> bearbeiter) throws BeschreibungsException {
    LocalDateTime erstellungsdatum = LocalDateTime.of(2022, 11, 10, 9, 8, 7);
    LocalDateTime aenderungsdatum = LocalDateTime.of(2022, 12, 11, 10, 9, 8);
    bearbeiter.forEach(b -> bearbeiterRepository.save(b));
    Beschreibung beschreibung = createBeschreibung(bId, "KOD-1", "Titel", "Sig. 1", "Sig. 2",
        "Alt-1", erstellungsdatum, aenderungsdatum);

    beschreibungsRepository.save(beschreibung);
    beschreibungsService.addBearbeiterToBeschreibung(beschreibung.getId(),
        bearbeiter.stream().map(Bearbeiter::getId).collect(Collectors.toSet()));
  }

  private Beschreibung createBeschreibung(String id, String kodId, String titel, String signatur,
      String altSignatur,
      String altIdentifier, LocalDateTime erstellungsDatum, LocalDateTime aenderungsDatum) {
    NormdatenReferenz besitzer =
        new NormdatenReferenz(UUID.randomUUID().toString(), "Staatsbibliothek zu Berlin",
            "5036103-X",
            "CorporateBody");

    NormdatenReferenz aufbewahrungsOrt
        = new NormdatenReferenz(UUID.randomUUID().toString(), "Berlin", "4005728-8", "Place");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent(signatur)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .build();

    Identifikation identifikationAlt = new Identifikation.IdentifikationBuilder()
        .withIdent(altSignatur)
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .build();

    BeschreibungsKomponenteKopf bkKopf = new BeschreibungsKomponenteKopfBuilder()
        .withId(UUID.randomUUID().toString())
        .withKulturObjektTyp(KulturObjektTyp.CODEX)
        .withTitel(titel)
        .withAlternativeIdentifikationen(Set.of(identifikationAlt))
        .build();

    return new BeschreibungsBuilder(id, kodId, identifikation)
        .withKatalog("kat-1")
        .addBeschreibungsKomponente(bkKopf)
        .addAltIdentifier(altIdentifier)
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION)
        .withBeschreibungsTyp(BeschreibungsTyp.MEDIEVAL)
        .withErstellungsDatum(erstellungsDatum)
        .withAenderungsDatum(aenderungsDatum)
        .build();
  }

  @Test
  @TestTransaction
  void testMeinePublizierten() throws BeschreibungsException {
    Bearbeiter bearbeiter = new Bearbeiter.BearbeiterBuilder()
        .withBearbeitername("pub.bearbeiter")
        .withNachname("Publiziert").withVorname("Anna").build();
    setupTestData("HSP-PUB-1", Set.of(bearbeiter));

    Beschreibung beschreibung = beschreibungsRepository.findById("HSP-PUB-1");
    ApprovalRequest request = ApprovalRequest.create(beschreibung, "requester");
    request.approve(beschreibung, "reviewer", "ok");
    beschreibungsRepository.saveAndFlush(beschreibung);

    BearbeiterService bearbeiterServiceMock = mock(BearbeiterService.class, RETURNS_DEEP_STUBS);
    when(bearbeiterServiceMock.getLoggedBearbeiter().getFullName()).thenReturn("Publiziert, Anna");
    DashboardController controller = new DashboardController(beschreibungsBoundary,
        bearbeiterServiceMock);
    controller.setup();

    List<BeschreibungListDbView> result =
        controller.getMeinePublizierten();
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("HSP-PUB-1", result.getFirst().getHspId());
    assertEquals(PublikationStatus.PUBLISHED, result.getFirst().getPublikationStatus());
    assertNotNull(result.getFirst().getPublikationsDatumHsp());
  }

  @Test
  @TestTransaction
  void testMeineImReview() throws BeschreibungsException {
    Bearbeiter bearbeiter = new Bearbeiter.BearbeiterBuilder()
        .withBearbeitername("review.bearbeiter")
        .withNachname("Review").withVorname("Bob").build();
    setupTestData("HSP-REV-1", Set.of(bearbeiter));

    Beschreibung beschreibung = beschreibungsRepository.findById("HSP-REV-1");
    ApprovalRequest.create(beschreibung, "requester");
    beschreibungsRepository.saveAndFlush(beschreibung);

    BearbeiterService bearbeiterServiceMock = mock(BearbeiterService.class, RETURNS_DEEP_STUBS);
    when(bearbeiterServiceMock.getLoggedBearbeiter().getFullName()).thenReturn("Review, Bob");
    DashboardController controller = new DashboardController(beschreibungsBoundary,
        bearbeiterServiceMock);
    controller.setup();

    List<BeschreibungInReviewDbView> result =
        controller.getMeineImReview();
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("HSP-REV-1", result.getFirst().getHspId());
    assertNotNull(result.getFirst().getRequestedAt());
  }

  @Test
  @TestTransaction
  void testMeineZurUeberarbeitung() throws BeschreibungsException {
    Bearbeiter bearbeiter = new Bearbeiter.BearbeiterBuilder()
        .withBearbeitername("reject.bearbeiter")
        .withNachname("Abgelehnt").withVorname("Clara").build();
    setupTestData("HSP-REJ-1", Set.of(bearbeiter));

    Beschreibung beschreibung = beschreibungsRepository.findById("HSP-REJ-1");
    ApprovalRequest request = ApprovalRequest.create(beschreibung, "requester");
    request.reject(beschreibung, "reviewer", "needs work");
    beschreibungsRepository.saveAndFlush(beschreibung);

    BearbeiterService bearbeiterServiceMock = mock(BearbeiterService.class, RETURNS_DEEP_STUBS);
    when(bearbeiterServiceMock.getLoggedBearbeiter().getFullName()).thenReturn("Abgelehnt, Clara");
    DashboardController controller = new DashboardController(beschreibungsBoundary,
        bearbeiterServiceMock);
    controller.setup();

    List<BeschreibungRejectedDbView> result =
        controller.getMeineZurUeberarbeitung();
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("HSP-REJ-1", result.getFirst().getHspId());
    assertEquals("DRAFT", result.getFirst().getPublikationStatus());
    assertEquals("reviewer", result.getFirst().getReviewedBy());
    assertNotNull(result.getFirst().getReviewedAt());
  }

  @Test
  @TestTransaction
  void testLatestPublikationen() throws BeschreibungsException {
    Bearbeiter bearbeiter = new Bearbeiter.BearbeiterBuilder()
        .withBearbeitername("latest.bearbeiter")
        .withNachname("Latest").withVorname("Diana").build();
    setupTestData("HSP-LAT-1", Set.of(bearbeiter));
    setupTestData("HSP-LAT-2", Set.of(bearbeiter));

    Beschreibung b1 = beschreibungsRepository.findById("HSP-LAT-1");
    ApprovalRequest r1 = ApprovalRequest.create(b1, "requester");
    r1.approve(b1, "reviewer", "ok");
    beschreibungsRepository.saveAndFlush(b1);

    Beschreibung b2 = beschreibungsRepository.findById("HSP-LAT-2");
    ApprovalRequest r2 = ApprovalRequest.create(b2, "requester");
    r2.approve(b2, "reviewer", "ok");
    beschreibungsRepository.saveAndFlush(b2);

    BearbeiterService bearbeiterServiceMock = mock(BearbeiterService.class, RETURNS_DEEP_STUBS);
    when(bearbeiterServiceMock.getLoggedBearbeiter().getFullName()).thenReturn("Latest, Diana");
    DashboardController controller = new DashboardController(beschreibungsBoundary,
        bearbeiterServiceMock);
    controller.setup();

    List<BeschreibungListDbView> result =
        controller.getLatestPublikationen();
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.stream().allMatch(
        dto -> PublikationStatus.PUBLISHED == dto.getPublikationStatus()));
  }

  @Test
  @TestTransaction
  void testLazyLoadingCachesResult() throws BeschreibungsException {
    Bearbeiter bearbeiter = new Bearbeiter.BearbeiterBuilder()
        .withBearbeitername("cache.bearbeiter")
        .withNachname("Cache").withVorname("Emil").build();
    setupTestData("HSP-CACHE-1", Set.of(bearbeiter));

    Beschreibung beschreibung = beschreibungsRepository.findById("HSP-CACHE-1");
    ApprovalRequest request = ApprovalRequest.create(beschreibung, "requester");
    request.approve(beschreibung, "reviewer", "ok");
    beschreibungsRepository.saveAndFlush(beschreibung);

    BearbeiterService bearbeiterServiceMock = mock(BearbeiterService.class, RETURNS_DEEP_STUBS);
    when(bearbeiterServiceMock.getLoggedBearbeiter().getFullName()).thenReturn("Cache, Emil");
    DashboardController controller = new DashboardController(beschreibungsBoundary,
        bearbeiterServiceMock);
    controller.setup();

    var first = controller.getMeinePublizierten();
    var second = controller.getMeinePublizierten();
    assertSame(first, second, "Second call should return cached list");
  }
}
