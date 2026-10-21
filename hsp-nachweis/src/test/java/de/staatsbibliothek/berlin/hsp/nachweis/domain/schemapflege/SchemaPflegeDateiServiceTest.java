package de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterService.UNBEKANNTER_BEARBEITER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.SchemaPflegeDateiMultipartBody;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.SchemaPflegeDateiServicePort;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        .xmlFormat("MXML")
        .schemaResourceTyp("XSLT")
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
    SchemaPflegeDateiServicePort schemaPflegeDateiServicePort = mock(
        SchemaPflegeDateiServicePort.class);
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
    SchemaPflegeDateiServicePort schemaPflegeDateiServicePort = mock(
        SchemaPflegeDateiServicePort.class);
    when(schemaPflegeDateiServicePort.findAll()).thenReturn(Set.of(schemaPflegeDatei));

    SchemaPflegeDateiService service = new SchemaPflegeDateiService(schemaPflegeDateiServicePort);

    Collection<SchemaPflegeDatei> result = service.findAll();

    verify(schemaPflegeDateiServicePort, times(1)).findAll();
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(schemaPflegeDatei, result.stream().findFirst().orElseGet(null));
  }

  @Test
  void testUpload() throws Exception {
    SchemaPflegeDateiServicePort schemaPflegeDateiServicePort =
        mock(SchemaPflegeDateiServicePort.class);

    when(
        schemaPflegeDateiServicePort.upload(anyString(), any(SchemaPflegeDateiMultipartBody.class)))
        .thenAnswer(invocation -> {
          SchemaPflegeDateiMultipartBody body =
              invocation.getArgument(1, SchemaPflegeDateiMultipartBody.class);

          assertNotNull(body);
          assertEquals(schemaPflegeDatei.getVersion(), body.getVersion());
          assertEquals(schemaPflegeDatei.getBearbeitername(), body.getBearbeiterName());
          assertNotNull(body.getDatei());

          String content = new String(body.getDatei(), StandardCharsets.UTF_8);
          assertEquals(schemaPflegeDatei.getDatei(), content);

          return Response.status(Status.OK).build();
        });

    SchemaPflegeDateiService service = new SchemaPflegeDateiService(schemaPflegeDateiServicePort);

    boolean success = service.upload(
        schemaPflegeDatei.getId(),
        schemaPflegeDatei.getVersion(),
        schemaPflegeDatei.getBearbeitername(),
        new ByteArrayInputStream(schemaPflegeDatei.getDatei().getBytes(StandardCharsets.UTF_8))
    );

    assertTrue(success);

    verify(schemaPflegeDateiServicePort, times(1))
        .upload(eq(schemaPflegeDatei.getId()), any(SchemaPflegeDateiMultipartBody.class));
  }

  @Test
  void testUpload_whenRestClientThrowsException_thenFalse() throws Exception {
    SchemaPflegeDateiServicePort schemaPflegeDateiServicePort =
        mock(SchemaPflegeDateiServicePort.class);

    when(
        schemaPflegeDateiServicePort.upload(anyString(), any(SchemaPflegeDateiMultipartBody.class)))
        .thenThrow(new RuntimeException("Fehler"));

    SchemaPflegeDateiService service = new SchemaPflegeDateiService(schemaPflegeDateiServicePort);

    boolean success = service.upload(
        schemaPflegeDatei.getId(),
        schemaPflegeDatei.getVersion(),
        schemaPflegeDatei.getBearbeitername(),
        new ByteArrayInputStream(schemaPflegeDatei.getDatei().getBytes(StandardCharsets.UTF_8))
    );

    assertFalse(success);

    verify(schemaPflegeDateiServicePort, times(1))
        .upload(eq(schemaPflegeDatei.getId()), any(SchemaPflegeDateiMultipartBody.class));
  }

  @Test
  void testUpload_whenServerReturnsErrorStatus_thenFalse() throws Exception {
    SchemaPflegeDateiServicePort schemaPflegeDateiServicePort =
        mock(SchemaPflegeDateiServicePort.class);

    when(
        schemaPflegeDateiServicePort.upload(anyString(), any(SchemaPflegeDateiMultipartBody.class)))
        .thenAnswer(invocation -> {
          SchemaPflegeDateiMultipartBody body =
              invocation.getArgument(1, SchemaPflegeDateiMultipartBody.class);

          assertNotNull(body);
          assertEquals(schemaPflegeDatei.getVersion(), body.getVersion());
          assertEquals(schemaPflegeDatei.getBearbeitername(), body.getBearbeiterName());
          assertNotNull(body.getDatei());

          String content = new String(body.getDatei(), StandardCharsets.UTF_8);
          assertEquals(schemaPflegeDatei.getDatei(), content);

          return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        });

    SchemaPflegeDateiService service = new SchemaPflegeDateiService(schemaPflegeDateiServicePort);

    boolean success = service.upload(
        schemaPflegeDatei.getId(),
        schemaPflegeDatei.getVersion(),
        schemaPflegeDatei.getBearbeitername(),
        new ByteArrayInputStream(schemaPflegeDatei.getDatei().getBytes(StandardCharsets.UTF_8))
    );

    assertFalse(success);

    verify(schemaPflegeDateiServicePort, times(1))
        .upload(eq(schemaPflegeDatei.getId()), any(SchemaPflegeDateiMultipartBody.class));
  }
}