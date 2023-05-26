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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterService.UNBEKANNTER_BEARBEITER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDatei.SchemaResourceTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDatei.XMLFormate;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.SchemaPflegeDateiMultipartBody;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.SchemaPflegeDateiServicePort;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 30.03.2022
 */

public class SchemaPflegeDateiServiceTest {

  String dateiName = "MXML-to-TEI-P5.xsl";
  String datei = "<Test/>";
  String id = UUID.nameUUIDFromBytes(dateiName.getBytes(StandardCharsets.UTF_8)).toString();
  String version = "1.2.3";
  LocalDateTime datum = LocalDateTime.of(2022, Month.MARCH, 1, 12, 0);
  SchemaPflegeDatei schemaPflegeDatei;

  @BeforeEach
  void init() {
    schemaPflegeDatei = SchemaPflegeDatei.builder().
        id(id)
        .xmlFormat(XMLFormate.MXML)
        .schemaResourceTyp(SchemaResourceTyp.XSLT)
        .dateiName(dateiName)
        .bearbeitername(UNBEKANNTER_BEARBEITER_ID)
        .version(version)
        .erstellungsDatum(datum)
        .aenderungsDatum(datum)
        .datei(datei)
        .build();
  }

  @Test
  void testFindById() {
    SchemaPflegeDateiServicePort schemaPflegeDateiServicePort = mock(SchemaPflegeDateiServicePort.class);
    when(schemaPflegeDateiServicePort.findById(anyString())).thenReturn(schemaPflegeDatei);

    SchemaPflegeDateiService service = new SchemaPflegeDateiService(schemaPflegeDateiServicePort);

    Optional<SchemaPflegeDatei> result = service.findById(id);

    verify(schemaPflegeDateiServicePort, times(1)).findById(anyString());
    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(schemaPflegeDatei, result.get());
  }

  @Test
  void testFindAll() {
    SchemaPflegeDateiServicePort schemaPflegeDateiServicePort = mock(SchemaPflegeDateiServicePort.class);
    when(schemaPflegeDateiServicePort.findAll()).thenReturn(Set.of(schemaPflegeDatei));

    SchemaPflegeDateiService service = new SchemaPflegeDateiService(schemaPflegeDateiServicePort);

    Collection<SchemaPflegeDatei> result = service.findAll();

    verify(schemaPflegeDateiServicePort, times(1)).findAll();
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(schemaPflegeDatei, result.stream().findFirst().orElseGet(null));
  }

  @Test
  void testUpload() {
    SchemaPflegeDateiServicePort schemaPflegeDateiServicePort = mock(SchemaPflegeDateiServicePort.class);

    when(schemaPflegeDateiServicePort.upload(anyString(), any(SchemaPflegeDateiMultipartBody.class)))
        .thenReturn(Response.noContent().status(Status.OK).build());

    SchemaPflegeDateiService service = new SchemaPflegeDateiService(schemaPflegeDateiServicePort);

    boolean success = service.upload(id, version, UNBEKANNTER_BEARBEITER_ID,
        new ByteArrayInputStream(datei.getBytes(StandardCharsets.UTF_8)));

    assertTrue(success);

    ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<SchemaPflegeDateiMultipartBody> multipartBodyCaptor =
        ArgumentCaptor.forClass(SchemaPflegeDateiMultipartBody.class);

    verify(schemaPflegeDateiServicePort, times(1))
        .upload(idCaptor.capture(), multipartBodyCaptor.capture());

    assertNotNull(idCaptor.getValue());
    assertEquals(id, idCaptor.getValue());

    assertNotNull(multipartBodyCaptor.getValue());
    assertNotNull(multipartBodyCaptor.getValue().getBearbeiterName());
    assertEquals(UNBEKANNTER_BEARBEITER_ID, multipartBodyCaptor.getValue().getBearbeiterName());

    assertNotNull(multipartBodyCaptor.getValue().getVersion());
    assertEquals(version, multipartBodyCaptor.getValue().getVersion());

    assertNotNull(multipartBodyCaptor.getValue().getDatei());


  }
}
