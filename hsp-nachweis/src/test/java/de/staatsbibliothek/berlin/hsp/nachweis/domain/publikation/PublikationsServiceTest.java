package de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation.PublikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.LizenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.PublikationTeiSnapshotRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
class PublikationsServiceTest {

  @Inject
  PublikationsService publikationsService;

  @Inject
  BeschreibungsRepository beschreibungRepository;

  @InjectMock
  BeschreibungsBoundary mockBeschreibungsService;

  @InjectMock
  PURLBoundary mockPURLBoundary;

  @InjectMock
  KulturObjektDokumentBoundary mockKodBoundary;

  @Inject
  PublikationApprovalService approvalService;

  @Inject
  PublikationTeiSnapshotRepository teiSnapshotRepository;

  @Test
  @TestTransaction
  void applyForPublication() throws PublikationException {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));

    Beschreibung result = publikationsService.applyForPublication(beschreibung, LizenzTyp.CC_BY,
        "TestUser", null);

    assertNotNull(result);
    assertTrue(result.getPublikation().getActiveApprovalRequest().isPresent());
  }

  @Test
  @TestTransaction
  void applyForPublication_blockedBySperre() throws DokumentSperreException {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));
    when(mockBeschreibungsService.isBeschreibungGesperrt(anyString())).thenReturn(true);

    assertThrows(PublikationException.class,
        () -> publikationsService.applyForPublication(beschreibung, LizenzTyp.CC_BY,
            "TestUser", null));
  }

  @Test
  @TestTransaction
  void applyForPublicationByIdString_blockedBySperre() throws DokumentSperreException {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    when(mockBeschreibungsService.isBeschreibungGesperrt(anyString())).thenReturn(true);
    String beschreibungId = beschreibung.getId();

    assertThrows(PublikationException.class,
        () -> publikationsService.applyForPublication(beschreibungId, LizenzTyp.CC_BY,
            "TestUser", null));
  }

  @Test
  void applyForPublicationWithNullPublikation() {
    Beschreibung beschreibung = new BeschreibungsBuilder("BESCHREIBUNG1", "KOD1",
        createIdentifikation())
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withTEIXml("<xml></xml>")
        .build();
    // Force null publikation
    try {
      var field = Beschreibung.class.getDeclaredField("publikation");
      field.setAccessible(true);
      field.set(beschreibung, null);
    } catch (Exception _) {
      // ignore
    }

    assertThrows(NullPointerException.class,
        () -> publikationsService.applyForPublication(beschreibung, LizenzTyp.CC_BY, "TestUser",
            null));
  }

  @Test
  @TestTransaction
  void assignReviewer() throws Exception {
    Beschreibung beschreibung = createBeschreibung();
    approvalService.createRequest(beschreibung, "TestUser");
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));

    Beschreibung result = publikationsService.assignReviewer(beschreibung, "TestReviewer");

    assertNotNull(result);
    assertTrue(result.getPublikation().getActiveApprovalRequest().isPresent());
    assertEquals("TestReviewer",
        result.getPublikation().getActiveApprovalRequest().get().getAssignedTo());
  }

  @Test
  @TestTransaction
  void assignReviewerNoActiveRequest() {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));

    assertThrows(PublikationException.class,
        () -> publikationsService.assignReviewer(beschreibung, "TestReviewer"));
  }

  @Test
  @TestTransaction
  void approve() throws Exception {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    approvalService.createRequest(beschreibung, "TestUser");
    approvalService.assignReviewer(beschreibung, "TestRedakteur");
    KulturObjektDokument kod = createKulturObjektDokument();
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));
    when(mockKodBoundary.findById(beschreibung.getKodID())).thenReturn(Optional.of(kod));

    Beschreibung result = publikationsService.approve(beschreibung, "TestRedakteur", "Approved");

    assertNotNull(result);
    assertEquals(PublikationStatus.PUBLISHED, result.getPublikation().getStatus());
  }

  @Test
  @TestTransaction
  void approveNoActiveRequest() {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));

    assertThrows(PublikationException.class,
        () -> publikationsService.approve(beschreibung, "TestRedakteur", ""));
  }

  @Test
  @TestTransaction
  void approveCapturesTeiSnapshotAsZip() throws Exception {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    approvalService.createRequest(beschreibung, "TestUser");
    approvalService.assignReviewer(beschreibung, "TestRedakteur");
    KulturObjektDokument kod = createKulturObjektDokument();
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));
    when(mockKodBoundary.findById(beschreibung.getKodID())).thenReturn(Optional.of(kod));

    Beschreibung result = publikationsService.approve(beschreibung, "TestRedakteur", "Approved");

    Optional<byte[]> snapshotOpt = publikationsService.findTeiSnapshotZip(result.getId());
    assertTrue(snapshotOpt.isPresent());
    byte[] snapshot = snapshotOpt.get();
    assertTrue(snapshot.length > 0);

    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(snapshot))) {
      var entry = zis.getNextEntry();
      assertNotNull(entry);
      assertEquals("tei-" + result.getId() + ".xml", entry.getName());
      assertEquals("<xml></xml>", new String(zis.readAllBytes(), StandardCharsets.UTF_8));
    }
  }

  @Test
  @TestTransaction
  void approveWithBlankTeiThrowsPublikationException() throws Exception {
    Beschreibung beschreibung = new BeschreibungsBuilder("BESCHREIBUNG_NO_TEI", "KOD1",
        createIdentifikation())
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withTEIXml("")
        .build();
    var publikation = new PublikationBuilder(beschreibung.getPublikation())
        .withLizenz(new Lizenz(LizenzTyp.CC_BY))
        .build();
    beschreibung.setPublikation(publikation);
    beschreibungRepository.save(beschreibung);
    approvalService.createRequest(beschreibung, "TestUser");
    approvalService.assignReviewer(beschreibung, "TestRedakteur");
    KulturObjektDokument kod = createKulturObjektDokument();
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));
    when(mockKodBoundary.findById(beschreibung.getKodID())).thenReturn(Optional.of(kod));

    assertThrows(PublikationException.class,
        () -> publikationsService.approve(beschreibung, "TestRedakteur", "Approved"));
  }

  @Test
  @TestTransaction
  void approveTriggersPublizieren() throws Exception {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    approvalService.createRequest(beschreibung, "TestUser");
    approvalService.assignReviewer(beschreibung, "TestRedakteur");
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));

    publikationsService.approve(beschreibung, "TestRedakteur", "");

    verify(mockBeschreibungsService).publizieren(beschreibung);
  }

  @Test
  @TestTransaction
  void reject() throws Exception {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    approvalService.createRequest(beschreibung, "TestUser");
    approvalService.assignReviewer(beschreibung, "TestRedakteur");
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));

    publikationsService.reject(beschreibung, "TestRedakteur", "Rejected for reason");

    assertEquals(PublikationStatus.DRAFT, beschreibung.getPublikation().getStatus());
    assertTrue(beschreibung.getPublikation().getActiveApprovalRequest().isEmpty());
  }

  @Test
  @TestTransaction
  void rejectNoActiveRequest() {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));

    assertThrows(PublikationException.class,
        () -> publikationsService.reject(beschreibung, "TestRedakteur", ""));
  }

  @Test
  @TestTransaction
  void withdraw() throws Exception {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    approvalService.createRequest(beschreibung, "TestUser");
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));

    publikationsService.withdraw(beschreibung, "TestUser", "Withdrawing request");

    assertEquals(PublikationStatus.DRAFT, beschreibung.getPublikation().getStatus());
    assertTrue(beschreibung.getPublikation().getActiveApprovalRequest().isEmpty());
  }

  @Test
  @TestTransaction
  void withdrawDoesNotCreateTeiSnapshot() throws Exception {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    approvalService.createRequest(beschreibung, "TestUser");
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));

    publikationsService.withdraw(beschreibung, "TestUser", "Withdrawing request");

    assertTrue(publikationsService.findTeiSnapshotZip(beschreibung.getId()).isEmpty(),
        "No snapshot should exist when the request was withdrawn before approval");
  }

  @Test
  @TestTransaction
  void rejectDoesNotCreateTeiSnapshot() throws Exception {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    approvalService.createRequest(beschreibung, "TestUser");
    approvalService.assignReviewer(beschreibung, "TestRedakteur");
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));

    publikationsService.reject(beschreibung, "TestRedakteur", "Rejected for reason");

    assertTrue(publikationsService.findTeiSnapshotZip(beschreibung.getId()).isEmpty(),
        "No snapshot should exist when the request was rejected before approval");
  }

  @Test
  @TestTransaction
  void withdrawThenChangeTeiThenApproveCapturesUpdatedTei() throws Exception {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    KulturObjektDokument kod = createKulturObjektDokument();
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));
    when(mockKodBoundary.findById(beschreibung.getKodID())).thenReturn(Optional.of(kod));

    publikationsService.applyForPublication(beschreibung, LizenzTyp.CC_BY, "TestUser", null);
    publikationsService.withdraw(beschreibung, "TestUser", "Withdrawing request");

    String updatedTei = "<xml>updated</xml>";
    beschreibung.setTeiXML(updatedTei);

    publikationsService.applyForPublication(beschreibung, LizenzTyp.CC_BY, "TestUser", null);
    approvalService.assignReviewer(beschreibung, "TestRedakteur");
    Beschreibung result = publikationsService.approve(beschreibung, "TestRedakteur", "Approved");

    Optional<byte[]> snapshotOpt = publikationsService.findTeiSnapshotZip(result.getId());
    assertTrue(snapshotOpt.isPresent(),
        "Snapshot should exist after re-applying and approving");

    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(snapshotOpt.get()))) {
      var entry = zis.getNextEntry();
      assertNotNull(entry);
      assertEquals("tei-" + result.getId() + ".xml", entry.getName());
      assertEquals(updatedTei, new String(zis.readAllBytes(), StandardCharsets.UTF_8),
          "Snapshot must reflect the TEI present at approval time, not the original TEI");
    }
  }

  @Test
  @TestTransaction
  void publizierenOhneApproval() throws PublikationException {
    Beschreibung beschreibung = createBeschreibung();

    assertEquals(VerwaltungsTyp.EXTERN, beschreibung.getVerwaltungsTyp());
    assertEquals(PublikationStatus.DRAFT, beschreibung.getPublikation().getStatus());

    publikationsService.publizierenOhneApproval(beschreibung);

    assertEquals(PublikationStatus.PUBLISHED, beschreibung.getPublikation().getStatus());
    assertTrue(beschreibung.getPublikation().getApprovalRequests().stream()
            .anyMatch(r -> "System - Automatic Import".equals(r.getRequestedBy())),
        "Approval request should be created for audit trail");
  }

  /**
   * Reproduces the FK violation observed during BeschreibungImport (regression for commit 234fa0a8d
   * "Changed TEI Snapshot to @OneToOne"):
   * <pre>
   * insert or update on table "publikation_tei" violates foreign key constraint
   * "fk_publikation_tei_publikation" Detail: Key (publikation_id)=(...) is not present in
   * table "publikation".
   * </pre>
   *
   * <p>The import publishes a brand-new EXTERN Beschreibung via
   * {@link PublikationsService#publizierenOhneApproval}, which captures the TEI snapshot
   * ({@code teiSnapshotRepository.save(...)} = {@code merge}) <em>before</em> the parent
   * Publikation row is inserted — that insert only happens later, by cascade, when the import batch
   * persists the Beschreibung ({@code beschreibungsRepository.saveAll}). Because
   * {@code PublikationTeiSnapshot} has no JPA association to {@code Publikation} (only a bare
   * {@code String publikationId} PK that doubles as the FK), Hibernate cannot order the inserts, so
   * on flush the child row is written before its parent exists.
   *
   * <p>The pre-existing {@code publizierenOhneApproval()} test does not surface this because it
   * never persists the Beschreibung and never flushes — the queued snapshot INSERT is rolled back
   * by {@code @TestTransaction} before it ever reaches the database. This test mirrors the import
   * ordering exactly: publish, then persist the aggregate and flush. After the fix the snapshot is
   * no longer written by {@code publizierenOhneApproval} (it is captured later, once the
   * Publikation row exists), so persisting the aggregate must succeed without tripping the foreign
   * key.
   */
  @Test
  @TestTransaction
  void publizierenOhneApproval_thenPersistAggregate_doesNotViolatePublikationTeiForeignKey() {
    // Fresh, unpersisted EXTERN Beschreibung — exactly as the import mapper produces it.
    Beschreibung beschreibung = createBeschreibung();

    // Import step 1: publish without approval. The TEI snapshot must NOT be written here, because
    // the parent Publikation row does not exist yet.
    publikationsService.publizierenOhneApproval(beschreibung);

    // Import step 2: the batch persists the Beschreibung (cascading to its Publikation) and
    // flushes. This must succeed — the snapshot is captured only afterwards (see the import flow),
    // once the parent row exists.
    assertDoesNotThrow(() -> beschreibungRepository.saveAndFlush(beschreibung),
        "Persisting a freshly published EXTERN Beschreibung must not violate "
            + "fk_publikation_tei_publikation");
  }

  /**
   * Mirrors the corrected import ordering: the TEI snapshot is captured via
   * {@link PublikationsService#captureTeiSnapshots} only after the aggregate (and therefore the
   * Publikation row) has been persisted. Guards against a "fix" that simply drops snapshot capture:
   * the snapshot must still end up persisted.
   */
  @Test
  @TestTransaction
  void captureTeiSnapshots_afterAggregatePersisted_persistsSnapshot() {
    Beschreibung beschreibung = createBeschreibung();
    publikationsService.publizierenOhneApproval(beschreibung);
    beschreibungRepository.saveAndFlush(beschreibung);

    publikationsService.captureTeiSnapshots(List.of(beschreibung));
    beschreibungRepository.flush();

    assertTrue(
        teiSnapshotRepository.findByIdOptional(beschreibung.getPublikation().getId()).isPresent(),
        "TEI snapshot must be persisted once the Publikation exists");
  }

  @Test
  @TestTransaction
  void publizierenOhneApprovalSkipForIntern() throws PublikationException {
    Beschreibung internBeschreibung = new BeschreibungsBuilder("BESCHREIBUNG2", "KOD1",
        createIdentifikation())
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withTEIXml("<xml></xml>")
        .build();

    assertEquals(VerwaltungsTyp.INTERN, internBeschreibung.getVerwaltungsTyp());
    assertEquals(PublikationStatus.DRAFT, internBeschreibung.getPublikation().getStatus());

    publikationsService.publizierenOhneApproval(internBeschreibung);

    // Status should remain DRAFT for INTERN
    assertEquals(PublikationStatus.DRAFT, internBeschreibung.getPublikation().getStatus());
  }

  @Test
  void updateExistingBeschreibung() {
    Beschreibung beschreibung = createBeschreibung();
    Publikation publikation = beschreibung.getPublikation();

    assertNull(publikation.getPublikationsDatumHSP());

    publikationsService.updateExistingBeschreibung(beschreibung);

    assertNotNull(publikation.getPublikationsDatumHSP());
  }

  @Test
  @TestTransaction
  void withdrawNoActiveRequest() {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));

    assertThrows(PublikationException.class,
        () -> publikationsService.withdraw(beschreibung, "TestUser", ""));
  }

  @Test
  @TestTransaction
  void withdrawByIdString() throws Exception {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    approvalService.createRequest(beschreibung, "TestUser");
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));

    Beschreibung result = publikationsService.withdraw(beschreibung.getId(), "TestUser",
        "Withdrawing by ID");

    assertNotNull(result);
    assertEquals(PublikationStatus.DRAFT, result.getPublikation().getStatus());
    assertTrue(result.getPublikation().getActiveApprovalRequest().isEmpty());
  }

  @Test
  void withdrawByIdString_notFound() {
    when(mockBeschreibungsService.findById("UNKNOWN_ID")).thenReturn(Optional.empty());

    assertThrows(PublikationException.class,
        () -> publikationsService.withdraw("UNKNOWN_ID", "TestUser", "comment"));
  }

  @Test
  @TestTransaction
  void applyForPublicationByIdString() throws PublikationException {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));

    Beschreibung result = publikationsService.applyForPublication(beschreibung.getId(),
        LizenzTyp.CC_BY, "TestUser", null);

    assertNotNull(result);
    assertTrue(result.getPublikation().getActiveApprovalRequest().isPresent());
  }

  @Test
  @TestTransaction
  void applyForPublication_duplicateRequest() {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));

    publikationsService.applyForPublication(beschreibung, LizenzTyp.CC_BY, "TestUser", null);

    assertThrows(PublikationException.class,
        () -> publikationsService.applyForPublication(beschreibung, LizenzTyp.CC_BY,
            "TestUser", null));
  }

  @Test
  void reloadBeschreibungByString_happyPath() {
    Beschreibung beschreibung = createBeschreibung();
    when(mockBeschreibungsService.findById("BESCHREIBUNG1")).thenReturn(
        Optional.of(beschreibung));

    Beschreibung result = publikationsService.reloadBeschreibung("BESCHREIBUNG1");

    assertNotNull(result);
    assertEquals("BESCHREIBUNG1", result.getId());
  }

  @Test
  void reloadBeschreibungByString_notFound() {
    when(mockBeschreibungsService.findById("UNKNOWN_ID")).thenReturn(Optional.empty());

    assertThrows(PublikationException.class,
        () -> publikationsService.reloadBeschreibung("UNKNOWN_ID"));
  }

  @Test
  void findAllBeschreibungWithActiveApprovalRequest() {
    Beschreibung beschreibung = createBeschreibung();
    when(mockBeschreibungsService.findAllBeschreibungWithActiveApprovalRequest()).thenReturn(
        List.of(beschreibung));

    List<Beschreibung> result = publikationsService.findAllBeschreibungWithActiveApprovalRequest();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(beschreibung.getId(), result.getFirst().getId());
  }

  @Test
  void findAllBeschreibungWithActiveApprovalRequest_empty() {
    when(mockBeschreibungsService.findAllBeschreibungWithActiveApprovalRequest()).thenReturn(
        Collections.emptyList());

    List<Beschreibung> result = publikationsService.findAllBeschreibungWithActiveApprovalRequest();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void publizierenOhneApproval_duplicateApprovalRequest() throws Exception {
    Beschreibung beschreibung = createBeschreibung();

    // First apply creates a valid approval request
    approvalService.createRequest(beschreibung, "TestUser");

    // publizierenOhneApproval tries to create another request which should fail
    assertThrows(PublikationException.class,
        () -> publikationsService.publizierenOhneApproval(beschreibung));
  }

  @Test
  @TestTransaction
  void approveCallsAddNewPURLsToPublication() throws Exception {
    Beschreibung beschreibung = createBeschreibung();
    beschreibungRepository.save(beschreibung);
    approvalService.createRequest(beschreibung, "TestUser");
    approvalService.assignReviewer(beschreibung, "TestRedakteur");
    KulturObjektDokument kod = createKulturObjektDokument();
    when(mockBeschreibungsService.findById(beschreibung.getId())).thenReturn(
        Optional.of(beschreibung));
    when(mockKodBoundary.findById(beschreibung.getKodID())).thenReturn(Optional.of(kod));

    Beschreibung result = publikationsService.approve(beschreibung, "TestRedakteur", "Approved");

    assertNotNull(result);
    assertEquals(PublikationStatus.PUBLISHED, result.getPublikation().getStatus());
    // purl.autogenerate.enabled is true in test config, so createNewPURL is called
    verify(mockPURLBoundary).createNewPURL(eq(beschreibung.getId()), any());
  }

  private Identifikation createIdentifikation() {
    return new IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withIdent("1-1-1")
        .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
        .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
        .build();
  }

  private Beschreibung createBeschreibung() {
    var beschreibung = new BeschreibungsBuilder("BESCHREIBUNG1", "KOD1", createIdentifikation())
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withTEIXml("<xml></xml>")
        .build();
    var publikation = new PublikationBuilder(beschreibung.getPublikation())
        .withLizenz(new Lizenz(LizenzTyp.CC_BY))
        .build();
    beschreibung.setPublikation(publikation);
    return beschreibung;
  }

  private KulturObjektDokument createKulturObjektDokument() {
    return new KulturObjektDokumentBuilder().withId("KOD1").withTEIXml("<xml></xml>").build();
  }
}
