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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GeloeschtesDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.PapierkorbModel;
import javax.ws.rs.core.MediaType;
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
