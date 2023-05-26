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
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 8.11.2021
 */
public class SperreTest {

  @Test
  void testBuilder() {
    Bearbeiter bearbeiter = new Bearbeiter("B-1", "Bearbeiter");
    SperreEintrag sperreEintragKOD = new SperreEintrag("HSP-5f355105-ab83-3c3d-a03e-4cb0ae49f244",
        SperreDokumentTyp.KOD);
    SperreEintrag sperreEintragBeschreibung = new SperreEintrag("HSP-c90eeff6-b8ca-4766-9362-f3006e7817e5",
        SperreDokumentTyp.BESCHREIBUNG);

    LocalDateTime startDatum = LocalDateTime.now();

    Sperre sperre = Sperre.newBuilder()
        .withId("S-1")
        .withStartDatum(startDatum)
        .withBearbeiter(bearbeiter)
        .withSperreGrund("Test")
        .withSperreTyp(SperreTyp.IN_TRANSACTION)
        .withTxId("TXID_1")
        .withSperreEintraege(Set.of(sperreEintragKOD))
        .addSperreEintrag(sperreEintragBeschreibung)
        .build();

    assertNotNull(sperre);
    assertEquals("S-1", sperre.getId());
    assertEquals(startDatum, sperre.getStartDatum());
    assertEquals(bearbeiter, sperre.getBearbeiter());
    assertEquals("Test", sperre.getSperreGrund());
    assertEquals(SperreTyp.IN_TRANSACTION, sperre.getSperreTyp());
    assertEquals("TXID_1", sperre.getTxId());
    assertNotNull(sperre.getSperreEintraege());
    assertTrue(sperre.getSperreEintraege().contains(sperreEintragKOD));
    assertTrue(sperre.getSperreEintraege().contains(sperreEintragBeschreibung));
  }
}
