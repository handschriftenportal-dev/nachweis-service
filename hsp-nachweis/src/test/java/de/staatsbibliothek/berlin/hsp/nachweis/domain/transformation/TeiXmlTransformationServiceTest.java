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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.transformation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.TeiXmlTransformationServicePort;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 31.03.2022
 */

public class TeiXmlTransformationServiceTest {

  @Test
  void testIsTeiToHspEnabled() throws Exception {
    TeiXmlTransformationServicePort teiXmlTransformationServicePort = createTeiXmlTransformationServicePort();
    TeiXmlTransformationService service = new TeiXmlTransformationService(teiXmlTransformationServicePort);

    boolean result = service.isTeiToHspEnabled();

    assertTrue(result);
    verify(teiXmlTransformationServicePort, times(1)).isTeiToHspEnabled();
  }

  @Test
  void testTransformTei2Hsp() throws Exception {
    TeiXmlTransformationServicePort teiXmlTransformationServicePort = createTeiXmlTransformationServicePort();
    TeiXmlTransformationService service = new TeiXmlTransformationService(teiXmlTransformationServicePort);

    String result = service.transformTei2Hsp("original");

    assertNotNull(result);
    assertEquals("transformed", result);
    verify(teiXmlTransformationServicePort, times(1))
        .transformTeiXml(TeiXmlTransformationServicePort.MODE_TEI2HSP, "original");
  }

  @Test
  void testTransformTei2HspForBeschreibung() throws Exception {
    TeiXmlTransformationServicePort teiXmlTransformationServicePort = createTeiXmlTransformationServicePort();
    TeiXmlTransformationService service = new TeiXmlTransformationService(teiXmlTransformationServicePort);

    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withTEIXml("original")
        .build();

    service.transformTei2Hsp(beschreibung);
    assertEquals("transformed", beschreibung.getTeiXML());
  }

  @Test
  void testTransformTei2HspForKod() throws Exception {
    TeiXmlTransformationServicePort teiXmlTransformationServicePort = createTeiXmlTransformationServicePort();
    TeiXmlTransformationService service = new TeiXmlTransformationService(teiXmlTransformationServicePort);

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withTEIXml("original")
        .build();

    service.transformTei2Hsp(kod);
    assertEquals("transformed", kod.getTeiXML());
  }

  TeiXmlTransformationServicePort createTeiXmlTransformationServicePort() {
    TeiXmlTransformationServicePort teiXmlTransformationServicePort = mock(TeiXmlTransformationServicePort.class);
    when(teiXmlTransformationServicePort.transformTeiXml(
        TeiXmlTransformationServicePort.MODE_TEI2HSP, "original")).thenReturn("transformed");

    Map<String, Boolean> response = new HashMap<>();
    response.put(TeiXmlTransformationServicePort.KEY_ENABLED, Boolean.TRUE);

    when(teiXmlTransformationServicePort.isTeiToHspEnabled()).thenReturn(response);

    return teiXmlTransformationServicePort;
  }

}
