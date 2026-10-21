package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.DatensicherungException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.ZipDateisystemRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DatensicherungDownload;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.XMLDokument;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class ZipDateisystemRepositoryAdapter implements ZipDateisystemRepository {

  private static final String ZIP_EXTENSION = ".zip";
  private static final String XML_EXTENSION = ".xml";

  @ConfigProperty(name = "quarkus.profile")
  String STAGE;

  @ConfigProperty(name = "datensicherung.path")
  String PATH_ON_SERVER;

  @Override
  public void saveToFile(
      final String dateiName, final Stream<? extends XMLDokument> dokumente)
      throws DatensicherungException {
    Set<DokumentTyp> dokumentTyp = new HashSet<>();
    try (FileSystem zipfs = createNewFileSystem(dateiName)) {
      dokumente.forEach(
          document -> {
            try (InputStream inputStream =
                new ByteArrayInputStream(document.getTeiXml().getBytes(StandardCharsets.UTF_8))) {
              Path pathInZipFile = getPathInZipFile(document, zipfs);
              createDirectoryInZipFile(pathInZipFile);
              Files.copy(inputStream, pathInZipFile);
              dokumentTyp.add(document.getDokumentTyp());
            } catch (Throwable error) {
              log.error("Error during handling document {} ", document, error);
              throw new DatensicherungException(error);
            }
          });
    } catch (IOException error) {
      log.error("Error during creating new file system", error);
      throw new DatensicherungException(error);
    }
    log.info("Datensicherung completed: {}", dokumentTyp);
  }

  void createDirectoryInZipFile(Path pathInZipFile) throws IOException {
    if (pathInZipFile.getParent() != null) {
      Files.createDirectories(pathInZipFile.getParent());
    }
  }

  Path getPathInZipFile(XMLDokument document, FileSystem zipfs) {
    return zipfs.getPath(
        document.getDokumentTyp().toString(), document.getId().concat(XML_EXTENSION));
  }

  Map<String, String> getZipProperties(boolean zipExists) {
    Map<String, String> zipProperties = new HashMap<>();
    zipProperties.put("create", zipExists ? "false" : "true");
    zipProperties.put("encoding", "UTF-8");
    return zipProperties;
  }

  Path getFilePath(final String dateiName) {
    return Paths.get(PATH_ON_SERVER, getFileName(dateiName));
  }

  public boolean deleteFile(final String dateiName) throws IOException {
    Path path = getFilePath(dateiName);
    return Files.deleteIfExists(path);
  }

  @Override
  public DatensicherungDownload getFileDownload(String dateiName) throws IOException {
    Path path = getFilePath(dateiName);
    if (Files.notExists(path)) {
      throw new IOException("File ".concat(dateiName).concat(" does not exist."));
    }
    return new DatensicherungDownload(dateiName.concat(ZIP_EXTENSION), Files.readAllBytes(path));
  }

  String getFileName(final String dateiName) {
    return dateiName.concat("-").concat(STAGE).concat(ZIP_EXTENSION);
  }

  FileSystem createNewFileSystem(String dateiName) throws IOException {
    Path path = getFilePath(dateiName);
    boolean zipExists = Files.exists(path);
    Map<String, String> zipProperties = getZipProperties(zipExists);
    URI zipDisk = URI.create("jar:file:" + path.toAbsolutePath());
    return FileSystems.newFileSystem(zipDisk, zipProperties);
  }
}
