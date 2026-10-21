package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static de.staatsbibliothek.berlin.hsp.nachweis.TestUtils.BeschreibungTestUtils.assertBeschreibungListDTOEqualsBeschreibung;
import static de.staatsbibliothek.berlin.hsp.nachweis.TestUtils.BeschreibungTestUtils.createBeschreibung;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.ApprovalRequest;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.TestUtils.BeschreibungTestUtils;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungInReviewDbView;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungListDbView;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungRejectedDbView;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 12.04.22
 */

@QuarkusTest
class BeschreibungsRepositoryAdapterTest {

  static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
      "dd.MM.yyyy HH:mm:ss");

  @Inject
  BeschreibungsRepository beschreibungsRepository;

  @Inject
  BearbeiterRepository bearbeiterRepository;

  void setupBearbeiter() {
    Bearbeiter bearbeiter = new Bearbeiter("1", "Tester");
    bearbeiterRepository.saveAndFlush(bearbeiter);
  }

  @Test
  @TestTransaction
  void testFindByAltIdentifier() {
    setupBearbeiter();
    String altIdentifier = "alt_Id";
    Beschreibung beschreibung1 = createBeschreibung("b-1", "kod-1",
        "Titel-1", "SIG. I",
        "SIG. I alt",
        altIdentifier, true);

    beschreibungsRepository.save(beschreibung1);

    Beschreibung result = beschreibungsRepository.findByAltIdentifier(altIdentifier);
    assertNotNull(result);
    assertEquals(beschreibung1, result);
  }

  @Test
  @TestTransaction
  void testFindByKatalogId() {
    setupBearbeiter();
    Beschreibung beschreibung1 = createBeschreibung("b-1", "kod-1",
        "Titel-1", "SIG. I",
        "SIG. I alt",
        "alt_1", true);

    beschreibungsRepository.save(beschreibung1);
    List<Beschreibung> beschreibungen = beschreibungsRepository.findByKatalogId("kat-1");
    assertNotNull(beschreibungen);
    assertEquals(1, beschreibungen.size());
    Assertions.assertEquals(beschreibung1, beschreibungen.get(0));
  }

  @Test
  @TestTransaction
  void testFindLatestModifiedTopTen() {
    setupBearbeiter();
    Beschreibung beschreibung1 = createBeschreibung("b-1", "kod-1",
        "Titel-1", "SIG. I",
        "SIG. I alt",
        "alt_1", true);
    beschreibung1.setErstellungsDatum(LocalDateTime.of(2021, 3, 3, 12, 0));
    beschreibung1.setAenderungsDatum(LocalDateTime.of(2022, 4, 4, 12, 0));
    Beschreibung beschreibung2 = createBeschreibung("b-2", "kod-2",
        "Titel-2", "SIG. II",
        "SIG. II alt",
        "alt_2", false);
    beschreibung2.setErstellungsDatum(LocalDateTime.of(2021, 1, 1, 12, 0));
    beschreibung2.setAenderungsDatum(LocalDateTime.of(2022, 2, 2, 12, 0));

    beschreibungsRepository.saveAndFlush(beschreibung1);
    beschreibungsRepository.saveAndFlush(beschreibung2);

    List<BeschreibungListDbView> viewModels = beschreibungsRepository.findLatestModified();
    assertNotNull(viewModels);
    assertEquals(2, viewModels.size());

    assertBeschreibungListDTOEqualsBeschreibung(beschreibung1, viewModels.get(0));
    assertBeschreibungListDTOEqualsBeschreibung(beschreibung2, viewModels.get(1));
  }

  @Test
  @TestTransaction
  void testFindLatestModifiedTopTenByBearbeiter() {
    Bearbeiter bearbeiter = new Bearbeiter.BearbeiterBuilder().withBearbeitername("bearbeiter1")
        .withNachname("Nachname").withVorname("Vorname").build();
    Bearbeiter bearbeiter2 = new Bearbeiter.BearbeiterBuilder().withBearbeitername("bearbeiter2")
        .withNachname("Mustermann").withVorname("Max").build();
    bearbeiterRepository.saveAndFlush(bearbeiter);
    bearbeiterRepository.saveAndFlush(bearbeiter2);
    Beschreibung beschreibung1 = BeschreibungTestUtils.createBeschreibung("b-1", "kod-1",
        "Titel-1", "SIG. I",
        "SIG. I alt",
        "alt_1", true);
    beschreibung1.setErstellungsDatum(LocalDateTime.of(2021, 3, 3, 12, 0));
    beschreibung1.setAenderungsDatum(LocalDateTime.of(2022, 4, 4, 12, 0));
    beschreibung1.setBearbeiterIDs(Set.of(bearbeiter.getId(), bearbeiter2.getId()));

    Beschreibung beschreibung2 = BeschreibungTestUtils.createBeschreibung("b-2", "kod-2",
        "Titel-2", "SIG. II",
        "SIG. II alt",
        "alt_2", false);
    beschreibung2.setErstellungsDatum(LocalDateTime.of(2021, 1, 1, 12, 0));
    beschreibung2.setAenderungsDatum(LocalDateTime.of(2022, 2, 2, 12, 0));
    beschreibung2.setBearbeiterIDs(Set.of(bearbeiter.getId()));

    beschreibungsRepository.saveAndFlush(beschreibung1);
    beschreibungsRepository.saveAndFlush(beschreibung2);

    List<BeschreibungListDbView> viewModels = beschreibungsRepository.findLatestModifiedByBearbeiter(
        10,
        bearbeiter.getFullName());
    assertNotNull(viewModels);
    assertEquals(2, viewModels.size());

    List<BeschreibungListDbView> viewModels2 = beschreibungsRepository.findLatestModifiedByBearbeiter(
        10,
        bearbeiter2.getFullName());
    assertNotNull(viewModels2);
    assertEquals(1, viewModels2.size());

    assertBeschreibungListDTOEqualsBeschreibung(beschreibung1, viewModels.get(0));
    assertBeschreibungListDTOEqualsBeschreibung(beschreibung2, viewModels.get(1));
    assertBeschreibungListDTOEqualsBeschreibung(beschreibung1, viewModels2.get(0));
  }

  @Test
  @TestTransaction
  void testFindFilteredBeschreibungListDTOs() {
    setupBearbeiter();
    Beschreibung beschreibung = createBeschreibung("b-1", "kod-1", "Titel-1",
        "SIG. I",
        "SIG. I alt",
        "alt_1", true);
    beschreibung.setErstellungsDatum(LocalDateTime.of(2021, 3, 3, 12, 0));
    beschreibung.setAenderungsDatum(LocalDateTime.of(2022, 4, 4, 12, 0));

    beschreibungsRepository.saveAndFlush(beschreibung);

    Map<String, Boolean> sortByAsc = new HashMap<>();
    sortByAsc.put("hspId", Boolean.TRUE);

    Map<String, String> filterBy_1 = new HashMap<>();
    filterBy_1.put("kodId", "HSP-3B");
    List<BeschreibungListDbView> result_1 =
        beschreibungsRepository.findFilteredBeschreibungListDTOs(0, 10, filterBy_1, sortByAsc);
    assertNotNull(result_1);
    assertEquals(0, result_1.size());

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    Identifikation identifikation = beschreibung.getGueltigeIdentifikation().orElseThrow();

    Map<String, String> filterBy_2 = new HashMap<>();
    filterBy_2.put("hspId", beschreibung.getId());
    filterBy_2.put("kodId", beschreibung.getKodID());
    filterBy_2.put("katalogId", beschreibung.getKatalogID());
    filterBy_2.put("gueltigeSignatur", identifikation.getIdent());
    filterBy_2.put("titel", "TITEL-1");
    filterBy_2.put("verwaltungsTyp", beschreibung.getVerwaltungsTyp().name());
    filterBy_2.put("dokumentObjektTyp", beschreibung.getDokumentObjektTyp().name());
    filterBy_2.put("aufbewahrungsort", identifikation.getAufbewahrungsOrt().getName());
    filterBy_2.put("besitzer", identifikation.getBesitzer().getName());
    filterBy_2.put("erstellungsdatumString", beschreibung.getErstellungsDatum().format(dtf));
    filterBy_2.put("aenderungsdatumString", beschreibung.getAenderungsDatum().format(dtf));

    List<BeschreibungListDbView> result_2 = beschreibungsRepository.findFilteredBeschreibungListDTOs(
        0,
        10, filterBy_2, sortByAsc);
    assertNotNull(result_2);
    assertEquals(1, result_2.size());

    assertBeschreibungListDTOEqualsBeschreibung(beschreibung, result_2.get(0));
  }

  @Test
  @TestTransaction
  void testCountFilteredBeschreibungListDTOs() {
    setupBearbeiter();
    Beschreibung beschreibung = BeschreibungTestUtils.createBeschreibung("b-1", "kod-1", "Titel-1",
        "SIG. I",
        "SIG. I alt",
        "alt_1", true);
    beschreibung.setErstellungsDatum(LocalDateTime.of(2021, 3, 3, 12, 0));
    beschreibung.setAenderungsDatum(LocalDateTime.of(2022, 4, 4, 12, 0));
    beschreibungsRepository.saveAndFlush(beschreibung);

    Map<String, String> filterBy_1 = new HashMap<>();
    filterBy_1.put("kodId", "HSP-3B");

    assertEquals(0, beschreibungsRepository.countFilteredBeschreibungListDTOs(filterBy_1));

    Identifikation identifikation = beschreibung.getGueltigeIdentifikation().orElseThrow();

    Map<String, String> filterBy_2 = new HashMap<>();
    filterBy_2.put("hspId", beschreibung.getId());
    filterBy_2.put("kodId", beschreibung.getKodID());
    filterBy_2.put("katalogId", beschreibung.getKatalogID());
    filterBy_2.put("gueltigeSignatur", identifikation.getIdent());
    filterBy_2.put("titel", "TITEL-1");
    filterBy_2.put("verwaltungsTyp", beschreibung.getVerwaltungsTyp().name());
    filterBy_2.put("dokumentObjektTyp", beschreibung.getDokumentObjektTyp().name());
    filterBy_2.put("aufbewahrungsort", identifikation.getAufbewahrungsOrt().getName());
    filterBy_2.put("besitzer", identifikation.getBesitzer().getName());
    filterBy_2.put("erstellungsdatumString",
        beschreibung.getErstellungsDatum().format(DATE_TIME_FORMATTER));
    filterBy_2.put("aenderungsdatumString",
        beschreibung.getAenderungsDatum().format(DATE_TIME_FORMATTER));

    assertEquals(1, beschreibungsRepository.countFilteredBeschreibungListDTOs(filterBy_2));
  }

  @Test
  @TestTransaction
  void testFindBeschreibungListDTOById() {
    setupBearbeiter();
    Beschreibung beschreibung = createBeschreibung("b-1", "kod-1", "Titel-1",
        "SIG. I",
        "SIG. I alt",
        "alt_1", true);
    beschreibung.setErstellungsDatum(LocalDateTime.of(2021, 3, 3, 12, 0));
    beschreibung.setAenderungsDatum(LocalDateTime.of(2022, 4, 4, 12, 0));
    beschreibungsRepository.saveAndFlush(beschreibung);

    BeschreibungListDbView viewModel = beschreibungsRepository.findBeschreibungListDTOById(
        beschreibung.getId());

    assertNotNull(viewModel);

    assertBeschreibungListDTOEqualsBeschreibung(beschreibung, viewModel);

    assertNull(beschreibungsRepository.findBeschreibungListDTOById("does.not.exist"));
  }


  @Test
  @TestTransaction
  void testSelectAllTeiXml() {
    setupBearbeiter();
    Beschreibung beschreibung = createBeschreibung("b-1", "kod-1", "Titel-1",
        "SIG. I",
        "SIG. I alt",
        "alt_1", true);
    beschreibung.setTeiXML("<TEI></TEI>");
    beschreibung.setErstellungsDatum(LocalDateTime.of(2021, 3, 3, 12, 0));
    beschreibung.setAenderungsDatum(LocalDateTime.of(2022, 4, 4, 12, 0));
    beschreibungsRepository.saveAndFlush(beschreibung);

    assertEquals(1, beschreibungsRepository.selectAllTeiXml().count());

    beschreibungsRepository.selectAllTeiXml().findFirst().ifPresentOrElse(
        d -> {
          assertEquals(beschreibung.getId(), d.getId());
          assertEquals(beschreibung.getTeiXML(), d.getTeiXml());
        },
        () -> fail("No Beschreibung found"));
  }

  @Test
  @TestTransaction
  void testSelectAllTeiXmlNotFound() {
    beschreibungsRepository.selectAllTeiXml().findFirst().ifPresentOrElse(
        d -> fail("No Beschreibung should be found"),
        () -> {
        });
  }

  @Test
  @TestTransaction
  void testFindAllById() {
    setupBearbeiter();
    Beschreibung beschreibung1 = createBeschreibung("b-1", "kod-1", "Titel-1", "SIG. I",
        "SIG. I alt", "alt_1", true);
    Beschreibung beschreibung2 = createBeschreibung("b-2", "kod-2", "Titel-2", "SIG. II",
        "SIG. II alt", "alt_2", false);
    Beschreibung beschreibung_3 = createBeschreibung("b-3", "kod-3", "Titel-3", "SIG. III",
        "SIG. III alt", "alt_3", true);

    beschreibungsRepository.saveAndFlush(beschreibung1);
    beschreibungsRepository.saveAndFlush(beschreibung2);
    beschreibungsRepository.saveAndFlush(beschreibung_3);

    // Test finding all existing IDs
    List<Beschreibung> result1 = beschreibungsRepository.findAllById(List.of("b-1", "b-2", "b-3"));
    assertNotNull(result1);
    assertEquals(3, result1.size());

    // Test finding subset of existing IDs
    List<Beschreibung> result2 = beschreibungsRepository.findAllById(List.of("b-1", "b-3"));
    assertNotNull(result2);
    assertEquals(2, result2.size());

    // Test finding with some non-existing IDs
    List<Beschreibung> result3 = beschreibungsRepository.findAllById(
        List.of("b-1", "non-existing-id", "b-2"));
    assertNotNull(result3);
    assertEquals(2, result3.size());

    // Test with empty collection
    List<Beschreibung> result4 = beschreibungsRepository.findAllById(List.of());
    assertNotNull(result4);
    assertEquals(0, result4.size());

    // Test with only non-existing IDs
    List<Beschreibung> result5 = beschreibungsRepository.findAllById(
        List.of("non-existing-1", "non-existing-2"));
    assertNotNull(result5);
    assertEquals(0, result5.size());
  }

  @Test
  @TestTransaction
  void testFindAllBeschreibungWithActiveApprovalRequest() {
    setupBearbeiter();
    // Create beschreibungen with different approval request statuses
    Beschreibung beschreibungRequested = createBeschreibung("b-pending", "kod-1",
        "Titel Pending", "SIG. I", "SIG. I alt", "alt_pending", true);
    Beschreibung beschreibungInReview = createBeschreibung("b-in-review", "kod-2",
        "Titel In Review", "SIG. II", "SIG. II alt", "alt_in_review", true);
    Beschreibung beschreibungApproved = createBeschreibung("b-approved", "kod-3",
        "Titel Approved", "SIG. III", "SIG. III alt", "alt_approved", true);
    Beschreibung beschreibungNoRequest = createBeschreibung("b-no-request", "kod-4",
        "Titel No Request", "SIG. IV", "SIG. IV alt", "alt_no_request", true);

    // Create approval requests with different statuses
    ApprovalRequest requestedRequest = ApprovalRequest.create(beschreibungRequested, "requester1");
    ApprovalRequest inReviewRequest = ApprovalRequest.create(beschreibungInReview, "requester2");
    inReviewRequest.assignTo("reviewer1");
    ApprovalRequest approvedRequest = ApprovalRequest.create(beschreibungApproved, "requester3");
    approvedRequest.approve(beschreibungApproved, "reviewer2", "Approved");

    beschreibungsRepository.saveAndFlush(beschreibungRequested);
    beschreibungsRepository.saveAndFlush(beschreibungInReview);
    beschreibungsRepository.saveAndFlush(beschreibungApproved);
    beschreibungsRepository.saveAndFlush(beschreibungNoRequest);

    // Execute the query
    List<Beschreibung> result = beschreibungsRepository.findAllBeschreibungWithActiveApprovalRequest();
    assertEquals(4, beschreibungsRepository.count());
    assertEquals(3, beschreibungsRepository.count("""
        SELECT b
        FROM Beschreibung b
        JOIN Publikation p ON b.publikation = p
        JOIN p.approvalRequests ar
        """));

    // Verify results
    assertNotNull(result);
    assertEquals(2, result.size());

    List<String> resultIds = result.stream().map(Beschreibung::getId).toList();
    assertTrue(resultIds.contains("b-pending"),
        "Should contain beschreibung with REQUESTED approval");
    assertTrue(resultIds.contains("b-in-review"),
        "Should contain beschreibung with IN_REVIEW approval");
  }

  @Test
  @TestTransaction
  void testFindPublikationenByBearbeiter() {
    Bearbeiter bearbeiter1 = new Bearbeiter.BearbeiterBuilder()
        .withBearbeitername("pub-bearbeiter1")
        .withNachname("Mueller").withVorname("Anna").build();
    Bearbeiter bearbeiter2 = new Bearbeiter.BearbeiterBuilder()
        .withBearbeitername("pub-bearbeiter2")
        .withNachname("Schmidt").withVorname("Max").build();
    bearbeiterRepository.saveAndFlush(bearbeiter1);
    bearbeiterRepository.saveAndFlush(bearbeiter2);

    Beschreibung beschreibung1 = createBeschreibung("b-pub-1", "kod-pub-1",
        "Titel-Pub-1", "SIG. I", "SIG. I alt", "alt_pub_1", true);
    beschreibung1.setBearbeiterIDs(Set.of(bearbeiter1.getId()));
    ApprovalRequest request1 = ApprovalRequest.create(beschreibung1, "requester1");
    request1.approve(beschreibung1, "reviewer1", "ok");

    Beschreibung beschreibung2 = createBeschreibung("b-pub-2", "kod-pub-2",
        "Titel-Pub-2", "SIG. II", "SIG. II alt", "alt_pub_2", true);
    beschreibung2.setBearbeiterIDs(Set.of(bearbeiter2.getId()));
    ApprovalRequest request2 = ApprovalRequest.create(beschreibung2, "requester2");
    request2.approve(beschreibung2, "reviewer2", "ok");

    beschreibungsRepository.saveAndFlush(beschreibung1);
    beschreibungsRepository.saveAndFlush(beschreibung2);

    List<BeschreibungListDbView> result1 =
        beschreibungsRepository.findPublikationenByBearbeiter(bearbeiter1.getFullName());
    assertNotNull(result1);
    assertEquals(1, result1.size());
    assertEquals("b-pub-1", result1.getFirst().getHspId());
    assertEquals(PublikationStatus.PUBLISHED, result1.getFirst().getPublikationStatus());
    assertNotNull(result1.getFirst().getPublikationsDatumHsp());

    List<BeschreibungListDbView> result2 =
        beschreibungsRepository.findPublikationenByBearbeiter(bearbeiter2.getFullName());
    assertNotNull(result2);
    assertEquals(1, result2.size());
    assertEquals("b-pub-2", result2.getFirst().getHspId());

    List<BeschreibungListDbView> resultNone =
        beschreibungsRepository.findPublikationenByBearbeiter("Unknown Person");
    assertNotNull(resultNone);
    assertTrue(resultNone.isEmpty());
  }

  @Test
  @TestTransaction
  void testFindLatestPublikationen() {
    setupBearbeiter();
    Beschreibung beschreibung1 = createBeschreibung("b-latest-1", "kod-latest-1",
        "Titel-1", "SIG. I", "SIG. I alt", "alt_latest_1", true);
    ApprovalRequest request1 = ApprovalRequest.create(beschreibung1, "requester1");
    request1.approve(beschreibung1, "reviewer1", "ok");

    Beschreibung beschreibung2 = createBeschreibung("b-latest-2", "kod-latest-2",
        "Titel-2", "SIG. II", "SIG. II alt", "alt_latest_2", true);
    ApprovalRequest request2 = ApprovalRequest.create(beschreibung2, "requester2");
    request2.approve(beschreibung2, "reviewer2", "ok");

    Beschreibung beschreibung3 = createBeschreibung("b-latest-3", "kod-latest-3",
        "Titel-3", "SIG. III", "SIG. III alt", "alt_latest_3", true);
    // Not approved - stays DRAFT, should not appear

    Beschreibung beschreibungExtern = createBeschreibung("b-latest-extern", "kod-latest-extern",
        "Titel-Extern", "SIG. EXT", "SIG. EXT alt", "alt_latest_extern", false);
    ApprovalRequest requestExtern = ApprovalRequest.create(beschreibungExtern, "requesterE");
    requestExtern.approve(beschreibungExtern, "reviewerE", "ok");

    beschreibungsRepository.saveAndFlush(beschreibung1);
    beschreibungsRepository.saveAndFlush(beschreibung2);
    beschreibungsRepository.saveAndFlush(beschreibung3);
    beschreibungsRepository.saveAndFlush(beschreibungExtern);

    List<BeschreibungListDbView> allPublished =
        beschreibungsRepository.findLatestPublikationen(15);
    assertNotNull(allPublished);
    assertEquals(2, allPublished.size());
    assertTrue(allPublished.stream()
        .noneMatch(view -> "b-latest-extern".equals(view.getHspId())));

    List<BeschreibungListDbView> limited =
        beschreibungsRepository.findLatestPublikationen(1);
    assertNotNull(limited);
    assertEquals(1, limited.size());
  }

  @Test
  @TestTransaction
  void testFindInReviewByBearbeiter() {
    Bearbeiter bearbeiter1 = new Bearbeiter.BearbeiterBuilder()
        .withBearbeitername("review-bearbeiter1")
        .withNachname("Weber").withVorname("Lisa").build();
    Bearbeiter bearbeiter2 = new Bearbeiter.BearbeiterBuilder()
        .withBearbeitername("review-bearbeiter2")
        .withNachname("Fischer").withVorname("Tom").build();
    bearbeiterRepository.saveAndFlush(bearbeiter1);
    bearbeiterRepository.saveAndFlush(bearbeiter2);

    Beschreibung beschreibungInReview = createBeschreibung("b-review-1", "kod-review-1",
        "Titel-Review", "SIG. I", "SIG. I alt", "alt_review_1", true);
    beschreibungInReview.setBearbeiterIDs(Set.of(bearbeiter1.getId()));
    ApprovalRequest reviewRequest = ApprovalRequest.create(beschreibungInReview, "requester1");

    Beschreibung beschreibungOther = createBeschreibung("b-review-2", "kod-review-2",
        "Titel-Other", "SIG. II", "SIG. II alt", "alt_review_2", true);
    beschreibungOther.setBearbeiterIDs(Set.of(bearbeiter2.getId()));
    ApprovalRequest otherRequest = ApprovalRequest.create(beschreibungOther, "requester2");

    beschreibungsRepository.saveAndFlush(beschreibungInReview);
    beschreibungsRepository.saveAndFlush(beschreibungOther);

    List<BeschreibungInReviewDbView> result1 =
        beschreibungsRepository.findInReviewByBearbeiter(bearbeiter1.getFullName());
    assertNotNull(result1);
    assertEquals(1, result1.size());
    assertEquals("b-review-1", result1.getFirst().getHspId());
    assertNotNull(result1.getFirst().getRequestedAt());

    List<BeschreibungInReviewDbView> result2 =
        beschreibungsRepository.findInReviewByBearbeiter(bearbeiter2.getFullName());
    assertNotNull(result2);
    assertEquals(1, result2.size());
    assertEquals("b-review-2", result2.getFirst().getHspId());

    List<BeschreibungInReviewDbView> resultNone =
        beschreibungsRepository.findInReviewByBearbeiter("Unknown Person");
    assertNotNull(resultNone);
    assertTrue(resultNone.isEmpty());
  }

  @Test
  @TestTransaction
  void testFindRejectedByBearbeiter() {
    Bearbeiter bearbeiter1 = new Bearbeiter.BearbeiterBuilder()
        .withBearbeitername("reject-bearbeiter1")
        .withNachname("Braun").withVorname("Karl").build();
    Bearbeiter bearbeiter2 = new Bearbeiter.BearbeiterBuilder()
        .withBearbeitername("reject-bearbeiter2")
        .withNachname("Schwarz").withVorname("Eva").build();
    bearbeiterRepository.saveAndFlush(bearbeiter1);
    bearbeiterRepository.saveAndFlush(bearbeiter2);

    Beschreibung beschreibungRejected = createBeschreibung("b-reject-1", "kod-reject-1",
        "Titel-Rejected", "SIG. I", "SIG. I alt", "alt_reject_1", true);
    beschreibungRejected.setBearbeiterIDs(Set.of(bearbeiter1.getId()));
    ApprovalRequest rejectedRequest = ApprovalRequest.create(beschreibungRejected, "requester1");
    rejectedRequest.reject(beschreibungRejected, "reviewer1", "needs revision");

    Beschreibung beschreibungApproved = createBeschreibung("b-reject-2", "kod-reject-2",
        "Titel-Approved", "SIG. II", "SIG. II alt", "alt_reject_2", true);
    beschreibungApproved.setBearbeiterIDs(Set.of(bearbeiter2.getId()));
    ApprovalRequest approvedRequest = ApprovalRequest.create(beschreibungApproved, "requester2");
    approvedRequest.approve(beschreibungApproved, "reviewer2", "ok");

    beschreibungsRepository.saveAndFlush(beschreibungRejected);
    beschreibungsRepository.saveAndFlush(beschreibungApproved);

    List<BeschreibungRejectedDbView> result1 =
        beschreibungsRepository.findRejectedByBearbeiter(bearbeiter1.getFullName());
    assertNotNull(result1);
    assertEquals(1, result1.size());
    assertEquals("b-reject-1", result1.getFirst().getHspId());
    assertEquals("DRAFT", result1.getFirst().getPublikationStatus());
    assertEquals("reviewer1", result1.getFirst().getReviewedBy());
    assertNotNull(result1.getFirst().getReviewedAt());

    List<BeschreibungRejectedDbView> result2 =
        beschreibungsRepository.findRejectedByBearbeiter(bearbeiter2.getFullName());
    assertNotNull(result2);
    assertTrue(result2.isEmpty(), "Approved beschreibung should not appear in rejected view");

    List<BeschreibungRejectedDbView> resultNone =
        beschreibungsRepository.findRejectedByBearbeiter("Unknown Person");
    assertNotNull(resultNone);
    assertTrue(resultNone.isEmpty());
  }
}
