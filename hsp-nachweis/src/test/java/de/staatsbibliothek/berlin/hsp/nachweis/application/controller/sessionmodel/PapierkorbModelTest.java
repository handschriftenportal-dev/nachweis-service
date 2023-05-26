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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GeloeschtesDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.papierkorb.PapierkorbBoundary;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 30.11.22
 */

class PapierkorbModelTest {

  GeloeschtesDokument kod;
  GeloeschtesDokument beschreibung;

  PapierkorbBoundary papierkorbBoundaryMock;

  PapierkorbModel papierkorbModel;

  @BeforeEach
  void beforeEach() {
    kod = createGeloeschtesDokument("HSP-k1", DokumentObjektTyp.HSP_OBJECT);
    beschreibung = createGeloeschtesDokument("HSP-b1", DokumentObjektTyp.HSP_DESCRIPTION);

    papierkorbBoundaryMock = mock(PapierkorbBoundary.class);
    when(papierkorbBoundaryMock.findAllGeloeschteDokumente())
        .thenReturn(List.of(kod, beschreibung));

    papierkorbModel = new PapierkorbModel(papierkorbBoundaryMock);
  }

  @Test
  void testSetup() {
    papierkorbModel.setup();
    assertNotNull(papierkorbModel.getAllDokumente());
    assertEquals(2, papierkorbModel.getAllDokumente().size());
    assertNull(papierkorbModel.getAllFilteredDokumente());

    Mockito.verify(papierkorbBoundaryMock, Mockito.times(1)).findAllGeloeschteDokumente();
  }

  GeloeschtesDokument createGeloeschtesDokument(String dokumentId, DokumentObjektTyp dokumentObjektTyp) {
    return GeloeschtesDokument.builder()
        .withDokumentId(dokumentId)
        .withDokumentObjektTyp(dokumentObjektTyp)
        .withGueltigeSignatur("gueltigeSignatur")
        .withAlternativeSignaturen("alternativeSignaturen")
        .withInternePurls("internePurls")
        .withBesitzerId("besitzerId")
        .withBesitzerName("besitzerName")
        .withAufbewahrungsortId("aufbewahrungsortId")
        .withAufbewahrungsortName("aufbewahrungsortName")
        .withTeiXML("<teiXML/>")
        .withLoeschdatum(LocalDateTime.now())
        .withBearbeiterId("bearbeiterId")
        .withBearbeiterName("bearbeiterName")
        .build();
  }


}
