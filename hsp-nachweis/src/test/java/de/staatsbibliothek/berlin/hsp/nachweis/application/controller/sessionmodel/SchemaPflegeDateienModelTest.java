package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterService.UNBEKANNTER_BEARBEITER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDateiBoundary;
import jakarta.ws.rs.core.MediaType;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.primefaces.model.StreamedContent;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.03.2022
 */

public class SchemaPflegeDateienModelTest {

  SchemaPflegeDateiBoundary schemaPflegeDateiBoundary;

  SchemaPflegeDatei schemaPflegeDatei;
  String dateiName = "MXML-to-TEI-P5.xsl";
  String datei = "<Test/>";
  String id = UUID.nameUUIDFromBytes(dateiName.getBytes(StandardCharsets.UTF_8)).toString();
  String version = "1.2.3";
  LocalDateTime datum = LocalDateTime.of(2022, Month.MARCH, 1, 12, 0);

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

    schemaPflegeDateiBoundary = mock(SchemaPflegeDateiBoundary.class);
    when(schemaPflegeDateiBoundary.findAll()).thenReturn(Set.of(schemaPflegeDatei));
    when(schemaPflegeDateiBoundary.findById(anyString())).thenReturn(
        Optional.of(schemaPflegeDatei));
  }

  @Test
  void testSetup() {
    SchemaPflegeDateienModel schemaPflegeDateienModel = new SchemaPflegeDateienModel(
        schemaPflegeDateiBoundary);
    schemaPflegeDateienModel.setup();

    assertNotNull(schemaPflegeDateienModel.getSchemaPflegeDateien());
    assertEquals(1, schemaPflegeDateienModel.getSchemaPflegeDateien().size());

    assertNull(schemaPflegeDateienModel.getFilteredSchemaPflegeDateien());
  }

  @Test
  void testGetAenderungsDatum() {
    SchemaPflegeDateienModel schemaPflegeDateienModel = new SchemaPflegeDateienModel(
        schemaPflegeDateiBoundary);
    schemaPflegeDateienModel.setup();

    assertNotNull(schemaPflegeDateienModel.getSchemaPflegeDateien());
    assertFalse(schemaPflegeDateienModel.getSchemaPflegeDateien().isEmpty());

    String aenderungsDatum = schemaPflegeDateienModel.getAenderungsDatum(
        schemaPflegeDateienModel.getSchemaPflegeDateien().get(0));
    assertNotNull(aenderungsDatum);
    assertEquals("01.03.2022 12:00:00", aenderungsDatum);
  }

  @Test
  void testLoadDatei() {
    SchemaPflegeDateienModel schemaPflegeDateienModel = new SchemaPflegeDateienModel(
        schemaPflegeDateiBoundary);
    schemaPflegeDateienModel.setup();

    assertNotNull(schemaPflegeDateienModel.getSchemaPflegeDateien());
    assertFalse(schemaPflegeDateienModel.getSchemaPflegeDateien().isEmpty());

    StreamedContent streamedContent = schemaPflegeDateienModel.loadDatei(id);
    assertNotNull(streamedContent);
    assertNotNull(streamedContent.getStream());
    assertEquals(dateiName, streamedContent.getName());
    assertEquals(MediaType.APPLICATION_OCTET_STREAM, streamedContent.getContentType());
    assertEquals(7, streamedContent.getContentLength());
  }


}
