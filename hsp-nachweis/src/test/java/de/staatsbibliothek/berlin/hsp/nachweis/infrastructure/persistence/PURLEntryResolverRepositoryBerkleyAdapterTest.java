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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 03.08.22
 */
@Slf4j
public class PURLEntryResolverRepositoryBerkleyAdapterTest {

  final String urlPrefix = "https://resolver.staatsbibliothek-berlin.de/";
  final String httxt2dbm = "httxt2dbm -f DB -i {0} -o {1}";
  Path directoryPath;

  URI source = URI.create("https://resolver.staatsbibliothek-berlin.de/HSP0000002300000000");
  URI target = URI.create("https://localhost:8080/123");
  PURL purl = new PURL(source, target, PURLTyp.INTERNAL);

  Process processMock;
  PURLResolverRepositoryBerkleyAdapter adapter;
  ManagedExecutor managedExecutorMock;

  @BeforeEach
  void beforeEach() throws Exception {
    directoryPath = Files.createTempDirectory("hsp_purl");

    processMock = mock(Process.class);
    when(processMock.waitFor(anyLong(), any())).thenReturn(Boolean.TRUE);
    when(processMock.exitValue()).thenReturn(0);
    when(processMock.getInputStream())
        .thenReturn(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)));

    Future future = mock(Future.class);
    managedExecutorMock = mock(ManagedExecutor.class);
    when(managedExecutorMock.submit(any(Runnable.class))).thenReturn(future);

    adapter = spy(
        new PURLResolverRepositoryBerkleyAdapter(directoryPath.toString(), httxt2dbm, urlPrefix, managedExecutorMock));

    doReturn(processMock).when(adapter).createProcess(anyString());
  }

  @AfterEach
  void shutdown() {
    if (Objects.nonNull(directoryPath)) {
      try {
        FileUtils.deleteDirectory(directoryPath.toFile());
      } catch (Exception e) {
        log.error("Error deleting directoryPath " + directoryPath, e);
      }
    }
  }

  @Test
  void testCreateDBMFile() throws Exception {
    NullPointerException npe = assertThrows(NullPointerException.class,
        () -> adapter.createDBMFile(null));
    assertEquals("purls is required.", npe.getMessage());

    Set<PURL> noPurls = adapter.createDBMFile(Collections.emptySet());
    verify(adapter, times(0)).writeTextFile(any());
    verify(adapter, times(0)).writeDbmFile();
    assertNotNull(noPurls);
    assertEquals(0, noPurls.size());

    Set<PURL> saved = adapter.createDBMFile(Set.of(purl));
    verify(adapter, times(1)).writeTextFile(any());
    verify(adapter, times(1)).writeDbmFile();
    assertNotNull(saved);
    assertEquals(1, saved.size());
  }

  @Test
  void testWriteTextFile() throws Exception {
    Set<PURL> saved = adapter.createDBMFile(Set.of(purl));

    adapter.writeTextFile(saved);

    Path txtFilePath = Paths.get(directoryPath.toString(), PURLResolverRepositoryBerkleyAdapter.TXT_FILE);
    assertTrue(Files.exists(txtFilePath));
    assertTrue(Files.readString(txtFilePath, StandardCharsets.UTF_8)
        .startsWith("HSP0000002300000000 https://localhost:8080/123"));
  }

  @Test
  void testWriteDbmFile() throws Exception {
    adapter.writeDbmFile();
    verify(processMock, times(1)).waitFor();
  }
}
