package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.StreamReader.readStream;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.WerkzeugeRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchAllBeschreibungenValidate;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchAllKatalogeValidate;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchAllKulturobjekteValidate;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchIndexAllBeschreibungen;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchIndexAllKODs;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchIndexPraesentation;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchJob;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchJobStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugOverrideAttributsReferenzen;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 09.03.2023
 */
@QuarkusTest
class WerkzeugeControllerTest {

  private static final Logger logger = LoggerFactory.getLogger(WerkzeugeControllerTest.class);
  @TestHTTPResource("werkzeuge.xhtml")
  URL url;

  @Test
  void testOpenPage() throws IOException {

    try (InputStream in = url.openStream()) {
      String contents = readStream(in);
      Locale locale = Locale.getDefault();
      String titel = I18NController.getMessage(locale, "document_titel_werkzeuge");
      logger.info("Locale: {}, titel: {}", locale, titel);
      assertTrue(contents.contains(titel));
    }
  }

  @Test
  void testSetup() throws DokumentSperreException {
    DokumentSperreBoundary dokumentSperreBoundaryMock = mock(
        DokumentSperreBoundary.class);

    WerkzeugeController werkzeugeController = new WerkzeugeController(
        dokumentSperreBoundaryMock,
        null, null,
        null, null, null, null,
        mock(WerkzeugOverrideAttributsReferenzen.class), null, null, null,
        mock(WerkzeugBatchIndexPraesentation.class), null);

    werkzeugeController.setup();

    assertFalse(werkzeugeController.isSpeereExists());

    verify(dokumentSperreBoundaryMock, times(1)).findAll();
  }

  @Test
  void testSetup_withSperre() throws DokumentSperreException {
    DokumentSperreBoundary dokumentSperreBoundaryMock = mock(
        DokumentSperreBoundary.class);

    when(dokumentSperreBoundaryMock.findAll()).thenReturn(
        List.of(createSperre()));

    WerkzeugeController werkzeugeController = new WerkzeugeController(
        dokumentSperreBoundaryMock,
        null, null, null,
        null, null, null,
        mock(WerkzeugOverrideAttributsReferenzen.class), null, null, null,
        mock(WerkzeugBatchIndexPraesentation.class), null);

    werkzeugeController.setup();

    assertTrue(werkzeugeController.isSpeereExists());

    verify(dokumentSperreBoundaryMock, times(1)).findAll();
  }

  private Sperre createSperre() {
    Bearbeiter bearbeiter = Bearbeiter.newBuilder()
        .withBearbeitername("b-ml123")
        .withVorname("Martin")
        .withNachname("Luther")
        .withEmail("martin@luther.de")
        .withRolle("Redakteur")
        .build();

    return Sperre.newBuilder()
        .withSperreTyp(SperreTyp.IN_TRANSACTION)
        .withSperreGrund("KerndatenBearbeiten")
        .withBearbeiter(bearbeiter)
        .addSperreEintrag(new SperreEintrag("HSP-123", SperreDokumentTyp.KOD))
        .withTxId("T-123").build();
  }

  @Test
  void testIsCsvValidationDownloadAvailableODD() {
    var besch = mock(WerkzeugBatchAllBeschreibungenValidate.class);
    var batchOdd = mock(WerkzeugBatchJob.class);

    when(besch.getBatchOdd()).thenReturn(batchOdd);
    when(batchOdd.getDownload()).thenReturn("DATA".getBytes());

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        null, null, null,
        null, besch, null, null,
        null, null, null, null, null);

    assertTrue(ctrl.isCsvValidationDownloadAvailableODD());
  }

  @Test
  void testIsCsvValidationDownloadAvailableBeschALL() {
    var besch = mock(WerkzeugBatchAllBeschreibungenValidate.class);
    var batchAll = mock(WerkzeugBatchJob.class);

    when(besch.getBatchAll()).thenReturn(batchAll);
    when(batchAll.getDownload()).thenReturn("DATA".getBytes());

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        null, null, null,
        null, besch, null, null,
        null, null, null, null, null);

    assertTrue(ctrl.isCsvValidationDownloadAvailableBeschALL());
  }

  @Test
  void testIsCsvValidationDownloadAvailableKOD() {
    var kod = mock(WerkzeugBatchAllKulturobjekteValidate.class);
    var batch = mock(WerkzeugBatchJob.class);

    when(kod.getBatch()).thenReturn(batch);
    when(batch.getDownload()).thenReturn("OK".getBytes());

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        null, null, null,
        null, null, null, null,
        kod, null, null, null, null);

    assertTrue(ctrl.isCsvValidationDownloadAvailableKOD());
  }

  @Test
  void testIsCsvValidationDownloadAvailableKatalog() {
    var katalog = mock(WerkzeugBatchAllKatalogeValidate.class);
    var batch = mock(WerkzeugBatchJob.class);

    when(katalog.getBatch()).thenReturn(batch);
    when(batch.getDownload()).thenReturn("OK".getBytes());

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        null, null, null,
        null, null, null, null,
        null, null, katalog, null, null);

    assertTrue(ctrl.isCsvValidationDownloadAvailableKatalog());
  }

  @Test
  void testCsvValidationDownloadBeschODD() throws Exception {
    var besch = mock(WerkzeugBatchAllBeschreibungenValidate.class);
    var batchOdd = mock(WerkzeugBatchJob.class);

    byte[] data = "TESTDATA".getBytes();
    when(besch.getBatchOdd()).thenReturn(batchOdd);
    when(batchOdd.getDownload()).thenReturn(data);

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        null, null, null,
        null, besch, null, null,
        null, null, null, null, null);

    var sc = ctrl.csvValidationDownloadBeschODD();
    assertNotNull(sc);
    assertEquals("CSV-BeschreibungValidationResult-TEI-ODD.csv", sc.getName());

    byte[] result = sc.getStream().get().readAllBytes();
    assertEquals("TESTDATA", new String(result));
  }

  @Test
  void testCsvValidationDownloadKOD() throws Exception {
    var kod = mock(WerkzeugBatchAllKulturobjekteValidate.class);
    var batch = mock(WerkzeugBatchJob.class);

    byte[] data = "KODDATA".getBytes();
    when(kod.getBatch()).thenReturn(batch);
    when(batch.getDownload()).thenReturn(data);

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        null, null, null,
        null, null, null, null,
        kod, null, null, null, null);

    var sc = ctrl.csvValidationDownloadKOD();
    assertNotNull(sc);
    assertEquals("CSV-KulturObjektValidationResult.csv", sc.getName());
    assertEquals("KODDATA", new String(sc.getStream().get().readAllBytes()));
  }

  @Test
  void testIsIndexAktualisierenBeschreibungenDisabled() {
    var indexBeschreibungen = mock(WerkzeugBatchIndexAllBeschreibungen.class);
    var batch = mock(WerkzeugBatchJob.class);
    var rechte = mock(WerkzeugeRechte.class);

    when(indexBeschreibungen.getBatch()).thenReturn(batch);
    when(batch.getStatus()).thenReturn(WerkzeugBatchJobStatus.NOT_RUNNING);
    when(rechte.kannBeschreibungenIndexAktualisieren()).thenReturn(false);

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        null, indexBeschreibungen, null,
        null, null, null, null,
        null, null, null, null, rechte);

    assertTrue(ctrl.isIndexAktualisierenBeschreibungenDisabled());

    when(rechte.kannBeschreibungenIndexAktualisieren()).thenReturn(true);
    assertFalse(ctrl.isIndexAktualisierenBeschreibungenDisabled());

    when(batch.getStatus()).thenReturn(WerkzeugBatchJobStatus.RUNNING);
    assertTrue(ctrl.isIndexAktualisierenBeschreibungenDisabled());
  }

  @Test
  void testIsIndexAktualisierenKodsDisabled() {
    var indexKods = mock(WerkzeugBatchIndexAllKODs.class);
    var batch = mock(WerkzeugBatchJob.class);
    var rechte = mock(WerkzeugeRechte.class);

    when(indexKods.getBatch()).thenReturn(batch);
    when(batch.getStatus()).thenReturn(WerkzeugBatchJobStatus.NOT_RUNNING);
    when(rechte.kannKodsIndexAktualisieren()).thenReturn(false);

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        indexKods, null, null,
        null, null, null, null,
        null, null, null, null, rechte);

    assertTrue(ctrl.isIndexAktualisierenKodsDisabled());

    when(rechte.kannKodsIndexAktualisieren()).thenReturn(true);
    assertFalse(ctrl.isIndexAktualisierenKodsDisabled());

    when(batch.getStatus()).thenReturn(WerkzeugBatchJobStatus.RUNNING);
    assertTrue(ctrl.isIndexAktualisierenKodsDisabled());
  }

  @Test
  void testIsIndexPraesentationDisabled() {
    var indexPraesentation = mock(WerkzeugBatchIndexPraesentation.class);
    var batch = mock(WerkzeugBatchJob.class);
    var rechte = mock(WerkzeugeRechte.class);

    when(indexPraesentation.getBatch()).thenReturn(batch);
    when(indexPraesentation.isReplicationEnabled()).thenReturn(false);
    when(batch.getStatus()).thenReturn(WerkzeugBatchJobStatus.NOT_RUNNING);
    when(rechte.kannPraesentationIndexAktualisieren()).thenReturn(false);

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        null, null, null,
        null, null, null, null,
        null, null, null, indexPraesentation, rechte);

    assertTrue(ctrl.isIndexPraesentationDisabled());

    when(rechte.kannPraesentationIndexAktualisieren()).thenReturn(true);
    assertFalse(ctrl.isIndexPraesentationDisabled());

    when(batch.getStatus()).thenReturn(WerkzeugBatchJobStatus.RUNNING);
    assertTrue(ctrl.isIndexPraesentationDisabled());

    // replication not confirmed disabled blocks the execute button
    when(batch.getStatus()).thenReturn(WerkzeugBatchJobStatus.NOT_RUNNING);
    when(indexPraesentation.isReplicationEnabled()).thenReturn(true);
    assertTrue(ctrl.isIndexPraesentationDisabled());
  }

  @Test
  void testIsReplicationToggleDisabled() {
    var indexPraesentation = mock(WerkzeugBatchIndexPraesentation.class);
    var batch = mock(WerkzeugBatchJob.class);
    var rechte = mock(WerkzeugeRechte.class);

    when(indexPraesentation.getBatch()).thenReturn(batch);
    when(batch.getStatus()).thenReturn(WerkzeugBatchJobStatus.NOT_RUNNING);
    when(rechte.kannPraesentationIndexAktualisieren()).thenReturn(false);

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        null, null, null,
        null, null, null, null,
        null, null, null, indexPraesentation, rechte);

    assertTrue(ctrl.isReplicationToggleDisabled());

    when(rechte.kannPraesentationIndexAktualisieren()).thenReturn(true);
    assertFalse(ctrl.isReplicationToggleDisabled());

    // replication must not be switched back on while the reindex batch is running
    when(batch.getStatus()).thenReturn(WerkzeugBatchJobStatus.RUNNING);
    assertTrue(ctrl.isReplicationToggleDisabled());
  }

  @Test
  void testGetReplicationStatusKey() {
    var indexPraesentation = mock(WerkzeugBatchIndexPraesentation.class);

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        null, null, null,
        null, null, null, null,
        null, null, null, indexPraesentation, mock(WerkzeugeRechte.class));

    when(indexPraesentation.getReplicationEnabled()).thenReturn(null);
    assertEquals(WerkzeugeController.REPLICATION_STATUS_UNKNOWN_KEY,
        ctrl.getReplicationStatusKey());
    assertTrue(ctrl.isReplicationStatusUnknown());

    when(indexPraesentation.getReplicationEnabled()).thenReturn(Boolean.TRUE);
    assertEquals(WerkzeugeController.REPLICATION_STATUS_ENABLED_KEY,
        ctrl.getReplicationStatusKey());
    assertFalse(ctrl.isReplicationStatusUnknown());

    when(indexPraesentation.getReplicationEnabled()).thenReturn(Boolean.FALSE);
    assertEquals(WerkzeugeController.REPLICATION_STATUS_DISABLED_KEY,
        ctrl.getReplicationStatusKey());
  }

  @Test
  void testRefreshReplicationStatus_syncsToggleViewState() {
    var indexPraesentation = mock(WerkzeugBatchIndexPraesentation.class);

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        null, null, null,
        null, null, null, null,
        null, null, null, indexPraesentation, mock(WerkzeugeRechte.class));

    when(indexPraesentation.isReplicationEnabled()).thenReturn(false);
    ctrl.refreshReplicationStatus();
    verify(indexPraesentation, times(1)).refreshReplicationStatus();
    assertFalse(ctrl.isReplicationToggle());

    // unknown or enabled state renders the toggle as "enabled"
    when(indexPraesentation.isReplicationEnabled()).thenReturn(true);
    ctrl.refreshReplicationStatus();
    assertTrue(ctrl.isReplicationToggle());
  }

  @Test
  void testOnReplicationToggle_delegatesAndSyncsViewState() throws Exception {
    var indexPraesentation = mock(WerkzeugBatchIndexPraesentation.class);

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        null, null, null,
        null, null, null, null,
        null, null, null, indexPraesentation, mock(WerkzeugeRechte.class));

    when(indexPraesentation.isReplicationEnabled()).thenReturn(false);
    ctrl.setReplicationToggle(false);

    ctrl.onReplicationToggle();

    verify(indexPraesentation, times(1)).toggleReplication(false);
    assertFalse(ctrl.isReplicationToggle());
  }

  @Test
  void testOnAttributsreferenzenOverrideToggle_delegatesAndSyncsViewState() {
    var overrideAttributsReferenzen = mock(WerkzeugOverrideAttributsReferenzen.class);

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        null, null, null,
        null, null, null, overrideAttributsReferenzen,
        null, null, null, mock(WerkzeugBatchIndexPraesentation.class),
        mock(WerkzeugeRechte.class));

    when(overrideAttributsReferenzen.isAttributsreferenzenOverride()).thenReturn(true);
    ctrl.setAttributsreferenzenOverride(true);

    ctrl.onAttributsreferenzenOverrideToggle();

    verify(overrideAttributsReferenzen, times(1)).setAttributsreferenzenOverride(true);
    assertTrue(ctrl.isAttributsreferenzenOverride());

    // a failed switch must not throw, and the toggle falls back to the actual konfiguration
    doThrow(new IllegalStateException("boom"))
        .when(overrideAttributsReferenzen).setAttributsreferenzenOverride(false);
    when(overrideAttributsReferenzen.isAttributsreferenzenOverride()).thenReturn(true);
    ctrl.setAttributsreferenzenOverride(false);

    assertDoesNotThrow(ctrl::onAttributsreferenzenOverrideToggle);
    assertTrue(ctrl.isAttributsreferenzenOverride());
  }

  @Test
  void testIndexPraesentationStarten_exceptionBecomesFacesMessage() {
    var indexPraesentation = mock(WerkzeugBatchIndexPraesentation.class);
    doThrow(new IllegalStateException("Solr replication is still enabled"))
        .when(indexPraesentation).execute();

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        null, null, null,
        null, null, null, null,
        null, null, null, indexPraesentation, mock(WerkzeugeRechte.class));

    assertDoesNotThrow(ctrl::indexPraesentationStarten);
    verify(indexPraesentation, times(1)).execute();
  }

  @Test
  void testOnReplicationToggle_exceptionBecomesFacesMessage() throws Exception {
    var indexPraesentation = mock(WerkzeugBatchIndexPraesentation.class);
    when(indexPraesentation.isReplicationEnabled()).thenReturn(true);
    doThrow(new SolrServiceException("boom"))
        .when(indexPraesentation).toggleReplication(false);

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        null, null, null,
        null, null, null, null,
        null, null, null, indexPraesentation, mock(WerkzeugeRechte.class));

    ctrl.setReplicationToggle(false);

    assertDoesNotThrow(ctrl::onReplicationToggle);
    verify(indexPraesentation, times(1)).toggleReplication(false);
    // toggle is re-synced with the actual solr state (still enabled after the failure)
    assertTrue(ctrl.isReplicationToggle());
  }

  @Test
  void testIsAnyRunning() {
    var besch = mock(WerkzeugBatchAllBeschreibungenValidate.class);
    var batchOdd = mock(WerkzeugBatchJob.class);

    when(besch.getBatchOdd()).thenReturn(batchOdd);
    when(batchOdd.getStatus()).thenReturn(WerkzeugBatchJobStatus.RUNNING);

    WerkzeugeController ctrl = new WerkzeugeController(
        mock(DokumentSperreBoundary.class),
        null, null, null,
        null, besch, null, null,
        null, null, null, null, null);

    assertTrue(ctrl.isAnyRunning());
  }
}
