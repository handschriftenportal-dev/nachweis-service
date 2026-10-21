package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLConfiguration;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLConfiguration.PURLResolverConfiguration;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLResolverRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.context.ManagedExecutor;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 03.08.22
 */
@ApplicationScoped
@Slf4j
public class PURLResolverRepositoryBerkleyAdapter implements PURLResolverRepository, Serializable {

  @Serial
  private static final long serialVersionUID = -3614840522938237645L;
  private static final String SEPARATOR = " ";
  private final Path txtFilePath;
  private final Path dbmFilePath;
  private final String command;
  private final String configuredUrlPrefix;
  private final boolean isResolverEnabled;
  private final ManagedExecutor managedExecutor;

  @Inject
  public PURLResolverRepositoryBerkleyAdapter(
      PURLConfiguration configuration,
      ManagedExecutor managedExecutor) {

    PURLResolverConfiguration resolverConfiguration = configuration.resolver();
    this.txtFilePath =
        Paths.get(resolverConfiguration.directory(), resolverConfiguration.txtFileName());
    this.dbmFilePath =
        Paths.get(resolverConfiguration.directory(), resolverConfiguration.dbmFileName());
    this.command = resolverConfiguration.command();
    this.configuredUrlPrefix = resolverConfiguration.url();
    this.isResolverEnabled = resolverConfiguration.enabled();
    this.managedExecutor = managedExecutor;
  }

  @Transactional(rollbackOn = Exception.class)
  public Set<PURL> createDBMFile(Set<PURL> purls) throws PURLException {
    Objects.requireNonNull(purls, "purls is required.");

    if (!isResolverEnabled) {
      log.info("createDBMFile: PURL resolver file generation is disabled, skipping.");
      return Set.of();
    }

    log.debug("createDBMFile: purls={}", purls.size());

    Set<PURL> internalPURLS =
        purls.stream()
            .filter(purl -> PURLTyp.INTERNAL == purl.getTyp())
            .collect(Collectors.toSet());

    if (!internalPURLS.isEmpty()) {
      writeTextFile(internalPURLS);
      writeDbmFile();
    }

    return internalPURLS;
  }

  void writeTextFile(Set<PURL> purls) throws PURLException {
    log.info("writeTextFile: txtFilePath={}", txtFilePath);
    deleteIfExists(txtFilePath);
    try (PrintWriter printWriter = new PrintWriter(txtFilePath.toFile())) {
      purls.forEach(
          purl -> {
            printWriter.print(purl.getPurl().toASCIIString().replace(configuredUrlPrefix, ""));
            printWriter.print(SEPARATOR);
            printWriter.println(purl.getTarget().toASCIIString());
          });
    } catch (Exception e) {
      throw new PURLException("Error writing text-file " + txtFilePath, e);
    }
  }

  void writeDbmFile() throws PURLException {
    log.info("writeDbmFile: dbmFilePath={}", dbmFilePath);
    deleteIfExists(dbmFilePath);
    String formattedCommand =
        MessageFormat.format(command, txtFilePath.toString(), dbmFilePath.toString());
    log.info("writeDbmFile: command={}", formattedCommand);

    int exitValue = -1;
    try {
      Process process = createProcess(formattedCommand);
      Future<?> future =
          managedExecutor.submit(
              () ->
                  new BufferedReader(new InputStreamReader(process.getInputStream()))
                      .lines()
                      .forEach(log::debug));
      exitValue = process.waitFor();
      future.get(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      log.error("Writing dbm-file was interrupted!", e);
    } catch (Exception e) {
      throw new PURLException("Error creating dbm-file " + dbmFilePath, e);
    }

    if (exitValue != 0) {
      throw new PURLException(
          "Error creating dbm-file " + dbmFilePath + ": exitValue=" + exitValue);
    }
  }

  void deleteIfExists(Path file) throws PURLException {
    try {
      if (Files.exists(file)) {
        Files.delete(file);
      }
    } catch (Exception e) {
      throw new PURLException("Error deleting file " + file, e);
    }
  }

  Process createProcess(String command) throws IOException {
    return Runtime.getRuntime().exec(command);
  }
}
