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

package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.11.22
 */

class GeloeschtesDokumentTest {

  @Test
  void testBuilder() {
    LocalDateTime loeschDatum = LocalDateTime.of(2022, 11, 12, 13, 14, 15);

    GeloeschtesDokument geloeschtesDokument = GeloeschtesDokument.builder()
        .withDokumentId("dokumentId")
        .withDokumentObjektTyp(DokumentObjektTyp.HSP_OBJECT)
        .withGueltigeSignatur("gueltigeSignatur")
        .withAlternativeSignaturen("alternativeSignaturen")
        .withInternePurls("internePurls")
        .withBesitzerId("besitzerId")
        .withBesitzerName("besitzerName")
        .withAufbewahrungsortId("aufbewahrungsortId")
        .withAufbewahrungsortName("aufbewahrungsortName")
        .withTeiXML("<teiXML/>")
        .withLoeschdatum(loeschDatum)
        .withBearbeiterId("bearbeiterId")
        .withBearbeiterName("bearbeiterName")
        .build();

    assertNotNull(geloeschtesDokument);
    assertNotNull(geloeschtesDokument.getId());
    assertEquals("dokumentId", geloeschtesDokument.getDokumentId());
    assertEquals(DokumentObjektTyp.HSP_OBJECT, geloeschtesDokument.getDokumentObjektTyp());
    assertEquals("gueltigeSignatur", geloeschtesDokument.getGueltigeSignatur());
    assertEquals("alternativeSignaturen", geloeschtesDokument.getAlternativeSignaturen());
    assertEquals("internePurls", geloeschtesDokument.getInternePurls());
    assertEquals("besitzerId", geloeschtesDokument.getBesitzerId());
    assertEquals("besitzerName", geloeschtesDokument.getBesitzerName());
    assertEquals("aufbewahrungsortId", geloeschtesDokument.getAufbewahrungsortId());
    assertEquals("aufbewahrungsortName", geloeschtesDokument.getAufbewahrungsortName());
    assertEquals("<teiXML/>", geloeschtesDokument.getTeiXML());
    assertEquals(loeschDatum, geloeschtesDokument.getLoeschdatum());
    assertEquals("bearbeiterId", geloeschtesDokument.getBearbeiterId());
    assertEquals("bearbeiterName", geloeschtesDokument.getBearbeiterName());

    assertEquals("GeloeschtesDokument[id='" + geloeschtesDokument.getId() + "', "
            + "dokumentId='dokumentId', dokumentObjektTyp='hsp:object', gueltigeSignatur='gueltigeSignatur', "
            + "alternativeSignaturen='alternativeSignaturen', internePurls='internePurls', besitzerId='besitzerId', "
            + "besitzerName='besitzerName', aufbewahrungsortId='aufbewahrungsortId', "
            + "aufbewahrungsortName='aufbewahrungsortName', loeschdatum=2022-11-12T13:14:15, "
            + "bearbeiterId='bearbeiterId', bearbeiterName='bearbeiterName', teiXML='9']",
        geloeschtesDokument.toString());
  }

}
