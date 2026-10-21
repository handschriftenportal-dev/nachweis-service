package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GeloeschtesDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.PapierkorbModel;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.primefaces.model.StreamedContent;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 01.12.2022
 */
class PapierkorbControllerTest {

  @Test
  void testLoadTeiXML() {
    PapierkorbModel papierkorbModelMock = mock(PapierkorbModel.class);
    PapierkorbController papierkorbController = new PapierkorbController(papierkorbModelMock);

    StreamedContent streamedContent = papierkorbController.loadTeiXML(null);
    assertNull(streamedContent);

    GeloeschtesDokument geloeschtesDokument = GeloeschtesDokument.builder()
        .withDokumentId("HSP-123")
        .withTeiXML("<TEI/>")
        .build();
    streamedContent = papierkorbController.loadTeiXML(geloeschtesDokument);

    assertNotNull(streamedContent);
    assertEquals("HSP-123_tei.xml", streamedContent.getName());
    assertEquals(MediaType.TEXT_XML, streamedContent.getContentType());
    assertNotNull(streamedContent.getStream());
    assertEquals(6, streamedContent.getContentLength());
  }

}
