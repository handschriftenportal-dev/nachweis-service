/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLResolverRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 03.08.22
 */
@ApplicationScoped
@Slf4j
public class PURLResolverRepositoryBerkleyAdapter implements PURLResolverRepository, Serializable {

  static final String TXT_FILE = "hsp_purl.txt";
  static final String DBM_FILE = "hsp_purl.dbm";
  private static final String SEPARATOR = " ";
  private static final long serialVersionUID = -3614840522938237645L;
  private final Path txtFilePath;
  private final Path dbmFilePath;
  private final String httxt2dbm;
  private final String configuredUrlPrefix;
  private final ManagedExecutor managedExecutor;


  @Inject
  public PURLResolverRepositoryBerkleyAdapter(
      @ConfigProperty(name = "purlgen.resolver.directory") String directory,
      @ConfigProperty(name = "purlgen.resolver.httxt2dbm") String httxt2dbm,
      @ConfigProperty(name = "purlgen.resolver.url") String configuredUrlPrefix,
      ManagedExecutor managedExecutor) {

    this.txtFilePath = Paths.get(directory, TXT_FILE);
    this.dbmFilePath = Paths.get(directory, DBM_FILE);
    this.httxt2dbm = httxt2dbm;
    this.configuredUrlPrefix = configuredUrlPrefix;
    this.managedExecutor = managedExecutor;
  }

  @Transactional(rollbackOn = Exception.class)
  public Set<PURL> createDBMFile(Set<PURL> purls) throws PURLException {
    Objects.requireNonNull(purls, "purls is required.");

    log.debug("createDBMFile: purls={}", purls.size());

    Set<PURL> internalPURLS = purls.stream()
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
      purls.forEach(purl -> {
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
    String command = MessageFormat.format(httxt2dbm, txtFilePath.toString(), dbmFilePath.toString());
    log.info("writeDbmFile: command={}", command);

    int exitValue = -1;
    try {
      Process process = createProcess(command);
      Future<?> future = managedExecutor.submit(() ->
          new BufferedReader(new InputStreamReader(process.getInputStream()))
              .lines()
              .forEach(log::debug));
      exitValue = process.waitFor();
      future.get(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      log.error("Writing dbm-file was interrupted!", e);
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      throw new PURLException("Error creating dbm-file " + dbmFilePath, e);
    }

    if (exitValue != 0) {
      throw new PURLException("Error creating dbm-file " + dbmFilePath
          + ": exitValue=" + exitValue);
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
