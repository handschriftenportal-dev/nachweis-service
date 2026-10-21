package de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DokumentTyp.BESCHREIBUNG;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DokumentTyp.KATALOG;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DokumentTyp.KOD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.entities.Datensicherung;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DatensicherungDownload;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.Status;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.KatalogRepositoryAdapter;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.DatensicherungDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.DatensicherungRepositoryAdapter;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class DatensicherungServiceTest {

  static String pathOnServer =
      ConfigProvider.getConfig().getValue("datensicherung.path", String.class);

  @Inject
  DatensicherungService service;

  @Inject
  KatalogRepositoryAdapter katalogRepository;

  @Inject
  DatensicherungRepositoryAdapter datensicherungRepository;

  private static void cleanUpFiles() {
    File directory = new File(pathOnServer);
    if (directory.listFiles() != null) {
      for (File file : directory.listFiles()) {
        file.delete();
      }
    }
  }

  @BeforeEach
  void setUp() {
    cleanUpFiles();
  }

  @AfterEach
  @TestTransaction
  void tearDown() {
    cleanUpFiles();
    katalogRepository.deleteAll();
  }

  @Test
  @TestTransaction
  void testInitialeDatensicherung() {
    Set<DokumentTyp> auswahl = new HashSet<>(List.of(KATALOG, KOD, BESCHREIBUNG));
    String benutzer = "Benutzer 1";

    String datensicherungId = service.createInitialDatensicherung(auswahl, benutzer);

    assertNotNull(datensicherungId);

    Datensicherung datensicheurng =
        Datensicherung.map(datensicherungRepository.findById(datensicherungId));

    assertEquals(auswahl, datensicheurng.getDokumentenAuswahl());
    assertEquals(benutzer, datensicheurng.getBenutzer());
    datensicherungRepository.deleteById(datensicherungId);
  }

  @Test
  @TestTransaction
  void testInitialeDatensicherungThrow() {
    Set<DokumentTyp> auswahl = new HashSet<>(List.of(KATALOG));
    String benutzer = "Benutzer 1";

    assertThrows(
        DatensicherungException.class, () -> service.createInitialDatensicherung(null, null));
    assertThrows(
        DatensicherungException.class, () -> service.createInitialDatensicherung(auswahl, null));
    assertThrows(
        DatensicherungException.class, () -> service.createInitialDatensicherung(null, benutzer));
  }

  @Test
  @Transactional
  void testStarteDatensicherung() {
    Set<DokumentTyp> auswahl = new HashSet<>(List.of(KATALOG));

    String id = service.createInitialDatensicherung(auswahl, "Benutzer 1");
    service.starteDatensicherung(id);
    Datensicherung ds = Datensicherung.map(datensicherungRepository.findById(id));
    assertNotNull(ds);
    assertNotNull(ds.getEndZeit());
    assertEquals(Status.FINISHED, ds.getStatus());
  }

  @Test
  @TestTransaction
  void testStarteDatensicheurngNoDocumentsInDatabase() {
    Set<DokumentTyp> auswahl = new HashSet<>(List.of(KATALOG));
    String benutzer = "Benutzer 1";
    String id = service.createInitialDatensicherung(auswahl, benutzer);
    service.starteDatensicherung(id);
    Datensicherung ds = Datensicherung.map(datensicherungRepository.findById(id));
    assertNotNull(ds);
    assertNotNull(ds.getEndZeit());

    File file = new File(pathOnServer + "/" + ds.getDateiName());
    assertFalse(file.exists());
  }

  @Test
  @TestTransaction
  void testloadDataAsStreamAndSaveToFilesystemMultipleSelectionAsCopiedInWebEx() throws Exception {
    DatensicherungService service = new DatensicherungService(null, null, null, null, null);
    DatensicherungService spy = Mockito.spy(service);

    doReturn(Datensicherung.initialisieren(Set.of(KATALOG, KOD, BESCHREIBUNG), "Benutzer 1"))
        .when(spy)
        .loadDatensicherung(any());

    doNothing().when(spy).saveDataStream(any(), any());

    spy.loadDataAsStreamAndSaveToFilesystem("testId");
    verify(spy, times(3)).saveDataStream(any(), any());
  }

  @Test
  @TestTransaction
  void testloadDataAsStreamAndSaveToFilesystemEmptySelection() throws Exception {
    KulturObjektDokumentRepository kodRepositoryMock = mock(KulturObjektDokumentRepository.class);
    BeschreibungsRepository beschreibungRepositoryMock = mock(BeschreibungsRepository.class);
    KatalogRepository katalogRepositoryMock = mock(KatalogRepository.class);

    String benutzer = "Benutzer 1";
    DatensicherungDTO datensicherung =
        DatensicherungDTO.builder()
            .id("1")
            .dokumentAuswahl(new HashSet<>())
            .startZeit(Timestamp.valueOf(LocalDateTime.now()))
            .status(Status.IN_PROGRESS)
            .benutzer(benutzer)
            .build();

    QuarkusTransaction.requiringNew().run(() -> {
      service.getDatensicherungRepository().save(datensicherung);
    });

    service.loadDataAsStreamAndSaveToFilesystem("1");
    verify(katalogRepositoryMock, times(0)).selectAllTeiXml();
    verify(kodRepositoryMock, times(0)).selectAllTeiXml();
    verify(beschreibungRepositoryMock, times(0)).selectAllTeiXml();
  }

  @Test
  @TestTransaction
  void testStarteDatensicherungThrowPersistenceException() {
    DatensicherungRepository datensicherungRepositoryMock = mock(DatensicherungRepository.class);
    KulturObjektDokumentRepository kodRepositoryMock = mock(KulturObjektDokumentRepository.class);
    BeschreibungsRepository beschreibungRepositoryMock = mock(BeschreibungsRepository.class);
    KatalogRepository katalogRepositoryMock = mock(KatalogRepository.class);
    ZipDateisystemRepository fileRepositoryMock = mock(ZipDateisystemRepository.class);

    DatensicherungService service =
        new DatensicherungService(
            datensicherungRepositoryMock,
            kodRepositoryMock,
            beschreibungRepositoryMock,
            katalogRepositoryMock,
            fileRepositoryMock);

    Set<DokumentTyp> auswahl = new HashSet<>(List.of(KATALOG, KOD, BESCHREIBUNG));
    String benutzer = "Benutzer 1";
    Datensicherung.initialisieren(auswahl, benutzer);

    when(kodRepositoryMock.selectAllTeiXml()).thenThrow(PersistenceException.class);
    assertThrows(DatensicherungException.class, () -> service.starteDatensicherung("1"));

    when(katalogRepositoryMock.selectAllTeiXml()).thenThrow(PersistenceException.class);
    assertThrows(DatensicherungException.class, () -> service.starteDatensicherung("1"));

    when(beschreibungRepositoryMock.selectAllTeiXml()).thenThrow(PersistenceException.class);
    assertThrows(DatensicherungException.class, () -> service.starteDatensicherung("1"));
  }

  @Test
  @TestTransaction
  void testStarteDatensicherungThrowIOException() throws IOException {
    DatensicherungRepository datensicherungRepositoryMock = mock(DatensicherungRepository.class);
    KulturObjektDokumentRepository kodRepositoryMock = mock(KulturObjektDokumentRepository.class);
    BeschreibungsRepository beschreibungRepositoryMock = mock(BeschreibungsRepository.class);
    KatalogRepository katalogRepositoryMock = mock(KatalogRepository.class);
    ZipDateisystemRepository fileRepositoryMock = mock(ZipDateisystemRepository.class);

    DatensicherungService service =
        new DatensicherungService(
            datensicherungRepositoryMock,
            kodRepositoryMock,
            beschreibungRepositoryMock,
            katalogRepositoryMock,
            fileRepositoryMock);

    Set<DokumentTyp> auswahl = new HashSet<>(List.of(KATALOG, KOD, BESCHREIBUNG));
    String benutzer = "Benutzer 1";

    Datensicherung.initialisieren(auswahl, benutzer);

    doThrow(IOException.class).when(fileRepositoryMock).saveToFile(anyString(), any());
    assertThrows(DatensicherungException.class, () -> service.starteDatensicherung("1"));
  }

  @Test
  @TestTransaction
  void testHandleSuccess() {
    String id = service.createInitialDatensicherung(new HashSet<>(), "Benutzer 1");

    service.handleSuccess(id);
    Datensicherung ds = Datensicherung.map(service.getDatensicherungRepository().findById(id));
    assertNotNull(ds.getEndZeit());
    assertEquals(Status.FINISHED, ds.getStatus());
  }

  @Test
  @TestTransaction
  void testHandleSuccessFailure() {
    String id = UUID.randomUUID().toString();
    LocalDateTime endTime = LocalDateTime.of(2020, 1, 1, 1, 1, 1);
    Datensicherung ds =
        Datensicherung.builder()
            .id(id)
            .startZeit(LocalDateTime.now())
            .endZeit(endTime)
            .dokumentenAuswahl(new HashSet<>())
            .status(Status.FINISHED)
            .dateiName("datei")
            .benutzer("Benutzer 1")
            .build();
    service.getDatensicherungRepository().save(DatensicherungDTO.map(ds));
    service.handleSuccess(id);

    Datensicherung ds2 = Datensicherung.map(service.getDatensicherungRepository().findById(id));

    assertEquals(endTime, ds2.getEndZeit());
    assertEquals(Status.FINISHED, ds2.getStatus());
  }

  @Test
  @TestTransaction
  void testHandleError() {
    String id = UUID.randomUUID().toString();
    Datensicherung ds =
        Datensicherung.builder()
            .id(id)
            .startZeit(LocalDateTime.now())
            .endZeit(null)
            .dokumentenAuswahl(new HashSet<>())
            .status(Status.IN_PROGRESS)
            .dateiName("datei")
            .benutzer("Benutzer 1")
            .build();
    service.getDatensicherungRepository().save(DatensicherungDTO.map(ds));
    service.handleError(id, "Error");
    Datensicherung ds2 = Datensicherung.map(datensicherungRepository.findById(id));

    assertNotNull(ds2.getEndZeit());
    assertEquals(Status.ERROR, ds2.getStatus());
    assertEquals("Error", ds2.getMessage());
  }

  @Test
  @TestTransaction
  void testGetFileByDatensicherungId() throws IOException {
    String katalogId = UUID.randomUUID().toString();
    String katalogId2 = UUID.randomUUID().toString();

    String fileContentA = "<xml>A</xml>";
    String fileContentB = "<xml>B</xml>";
    Katalog katalogA =
        new Katalog.KatalogBuilder().withId(katalogId).withTEIXML(fileContentA).build();

    Katalog katalogB =
        new Katalog.KatalogBuilder().withId(katalogId2).withTEIXML(fileContentB).build();
    saveKataloge(katalogA, katalogB);

    assertEquals(fileContentA, katalogRepository.findById(katalogId).getTeiXML());
    assertEquals(fileContentB, katalogRepository.findById(katalogId2).getTeiXML());

    String datensicherungId = service.createInitialDatensicherung(Set.of(KATALOG), "Benutzer 1");

    service.starteDatensicherung(datensicherungId);

    DatensicherungDownload dsd = service.getFileByDatensicherungId(datensicherungId);
    assertNotNull(dsd);
    assertNotNull(dsd.dateiName());
    assertNotNull(dsd.data());
    assertTrue(dsd.data().length > 0);

    File file = new File(pathOnServer + "/" + dsd.dateiName());
    Path path = Files.write(file.toPath(), dsd.data());
    try (ZipFile zipFile = new ZipFile(path.toAbsolutePath().toString())) {
      var entries = zipFile.entries();
      ZipEntry entry1 = entries.nextElement();
      assertEquals("KATALOG/", entry1.getName());
      assertTrue(entry1.isDirectory());

      ZipEntry entry2 = entries.nextElement();
      assertEquals("KATALOG/" + katalogId + ".xml", entry2.getName());
      InputStream inputStream2 = zipFile.getInputStream(entry2);
      assertEquals(fileContentA, new String(inputStream2.readAllBytes()));

      ZipEntry entry3 = entries.nextElement();
      assertEquals("KATALOG/" + katalogId2 + ".xml", entry3.getName());
      InputStream inputStream3 = zipFile.getInputStream(entry3);
      assertEquals(fileContentB, new String(inputStream3.readAllBytes()));
    }
    deleteKataloge(List.of(katalogA, katalogB));
  }

  @Transactional(TxType.REQUIRES_NEW)
  void saveKataloge(Katalog katalogA, Katalog katalogB) {
    katalogRepository.saveAndFlush(katalogA);
    katalogRepository.saveAndFlush(katalogB);
  }

  @Transactional(TxType.REQUIRES_NEW)
  void deleteKataloge(List<Katalog> kataloge) {
    kataloge.forEach(katalog -> katalogRepository.delete(katalog));
  }

  @Test
  @TestTransaction
  void testGetFileByDatensicherungIdDatensicherungNull() {
    DatensicherungRepository datensicherungRepositoryMock = mock(DatensicherungRepository.class);
    ZipDateisystemRepository fileRepositoryMock = mock(ZipDateisystemRepository.class);

    DatensicherungService service =
        new DatensicherungService(
            datensicherungRepositoryMock, null, null, null, fileRepositoryMock);

    String id = "1";
    when(datensicherungRepositoryMock.findById(any())).thenReturn(null);
    assertThrows(DatensicherungException.class, () -> service.getFileByDatensicherungId(id));
  }

  @Test
  @TestTransaction
  void testGetFileByDatensicherungIdThrowIOException() throws IOException {
    DatensicherungRepository datensicherungRepositoryMock = mock(DatensicherungRepository.class);
    ZipDateisystemRepository fileRepositoryMock = mock(ZipDateisystemRepository.class);

    DatensicherungService service =
        new DatensicherungService(
            datensicherungRepositoryMock, null, null, null, fileRepositoryMock);

    String id = "1";
    when(datensicherungRepositoryMock.findById(any()))
        .thenReturn(DatensicherungDTO.builder().id(id).dateiName("Datei1").build());
    doThrow(IOException.class).when(fileRepositoryMock).getFileDownload(any());
    assertThrows(DatensicherungException.class, () -> service.getFileByDatensicherungId(id));
  }

  @Test
  @TestTransaction
  void testLoadDatensicherung() {
    String id = service.createInitialDatensicherung(Set.of(KOD), "Benutzer1");
    Datensicherung ds = service.loadDatensicherung(id);
    assertNotNull(ds);
    assertEquals(id, ds.getId());
    assertEquals(Status.IN_PROGRESS, ds.getStatus());
    assertEquals("Benutzer1", ds.getBenutzer());
  }

  @Test
  @TestTransaction
  void testLoadDatensicherungThrow() {
    assertThrows(DatensicherungException.class, () -> service.loadDatensicherung("10"));
  }
}
