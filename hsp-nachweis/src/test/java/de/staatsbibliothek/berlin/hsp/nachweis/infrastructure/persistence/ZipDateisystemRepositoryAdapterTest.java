package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DatensicherungDownload;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.BeschreibungXmlDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.XMLDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.KatalogXmlDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.KodXmlDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipFile;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ZipDateisystemRepositoryAdapterTest {

  public static final String STAGE =
      ConfigProvider.getConfig().getValue("quarkus.profile", String.class);

  String PATH_ON_SERVER = ConfigProvider.getConfig().getValue("datensicherung.path", String.class);

  @Inject ZipDateisystemRepositoryAdapter repo;

  private static void cleanUpFiles() {
    String pathOnServer = ConfigProvider.getConfig().getValue("datensicherung.path", String.class);
    File directory = new File(pathOnServer);
    if (directory.listFiles() != null) {
      for (File file : directory.listFiles()) {
        file.delete();
      }
    }
  }

  @BeforeAll
  static void cleanUpBefore() {
    cleanUpFiles();
  }

  @AfterAll
  static void cleanUpAfter() {
    cleanUpFiles();
  }

  @Test
  @Order(1)
  void testGetFilePath() {
    Path path = repo.getFilePath("Datei1");
    assertEquals(PATH_ON_SERVER + "/" + "Datei1-" + STAGE + ".zip", path.toString());
  }

  @Test
  @Order(2)
  void testGetPathInZipFile() throws IOException {
    XMLDokument dokument = new KodXmlDTO("Xml-1", "<xml></xml>");
    try (FileSystem fileSystem = repo.createNewFileSystem("DateiPathInZipFile")) {
      Path path = repo.getPathInZipFile(dokument, fileSystem);
      assertEquals(Path.of("KOD", "Xml-1.xml").toString(), path.toString());
    }
  }

  @Test
  @Order(3)
  void testCreateNewFileSystem() throws IOException {
    try (FileSystem fileSystem = repo.createNewFileSystem("Datei1")) {
      assertNotNull(fileSystem);
    }
  }

  @Test
  @Order(4)
  void testZipPropertiesFileExists() {
    Map<String, String> zipProperties = repo.getZipProperties(true);
    assertEquals("false", zipProperties.get("create"));
    assertEquals("UTF-8", zipProperties.get("encoding"));
  }

  @Test
  @Order(5)
  void testZipPropertiesFileNotExists() {
    Map<String, String> zipProperties = repo.getZipProperties(false);
    assertEquals("true", zipProperties.get("create"));
    assertEquals("UTF-8", zipProperties.get("encoding"));
  }

  @Test
  @Order(6)
  void testFileDownload() throws IOException {
    try (FileSystem fs = repo.createNewFileSystem("DateiDownload")) {
      DatensicherungDownload download = repo.getFileDownload("DateiDownload");
      assertEquals("DateiDownload.zip", download.dateiName());
      assertNotNull(download.data());
    }
  }

  @Test
  @Order(7)
  void testFileDownloadThrow() {
    assertThrows(IOException.class, () -> repo.getFileDownload("DateiThrow"));
  }

  @Test
  @Order(8)
  void testDeleteFile() throws IOException {
    try (FileSystem fs = repo.createNewFileSystem("DateiDelete")) {
      assertNotNull(fs);
    }
    boolean deleted = repo.deleteFile("DateiDelete");
    assertTrue(deleted);
  }

  @Test
  @Order(9)
  void testFileNotDeleted() throws IOException {
    assertFalse(repo.deleteFile("DateiNotDeleted"));
  }

  @Test
  @Order(11)
  void testSaveToFileSystem() throws IOException {
    String dateiName = "DateiSaveToFileSystem";
    XMLDokument dokument = new KodXmlDTO("Xml-1", "<xml>1</xml>");
    XMLDokument dokument2 = new KodXmlDTO("Xml-2", "<xml>2</xml>");
    XMLDokument dokument3 = new KodXmlDTO("Xml-3", "<xml>3</xml>");
    Stream<XMLDokument> stream = Stream.of(dokument, dokument2, dokument3);

    repo.saveToFile(dateiName, stream);
    File file = repo.getFilePath(dateiName).toFile();
    assertTrue(file.exists());
    try (ZipFile zipFile = new ZipFile(file)) {
      // 3 Files + 1 Folder = 4
      assertEquals(4, zipFile.size());
    }
  }

  @Test
  @Order(12)
  void testSaveToFileSystemMultipleFolders() throws IOException {
    String dateiName = "DateiSaveToFileSystem2";
    XMLDokument dokument = new KodXmlDTO("Xml-1", "<xml>1</xml>");
    XMLDokument dokument2 = new KodXmlDTO("Xml-2", "<xml>2</xml>");
    XMLDokument dokument3 = new BeschreibungXmlDTO("Xml-3", "<xml>3</xml>");
    XMLDokument dokument4 = new KatalogXmlDTO("Xml-4", "<xml>4</xml>");
    Stream<XMLDokument> stream = Stream.of(dokument, dokument2, dokument3, dokument4);

    repo.saveToFile(dateiName, stream);
    File file = repo.getFilePath(dateiName).toFile();
    assertTrue(file.exists());
    try (ZipFile zipFile = new ZipFile(file)) {
      // 4 Files + 3 Folder = 7
      assertEquals(7, zipFile.size());
    }
  }
}
