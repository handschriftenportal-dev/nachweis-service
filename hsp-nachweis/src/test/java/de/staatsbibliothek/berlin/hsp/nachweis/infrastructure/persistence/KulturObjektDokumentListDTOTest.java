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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 16.11.2022
 */
class KulturObjektDokumentListDTOTest {

  @Test
  void testConstruct() {
    LocalDateTime registrierungsdatum = LocalDateTime.of(2022, 11, 16, 15, 14, 13);
    String registrierungsdatumString = registrierungsdatum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss"));
    KulturObjektDokumentListDTO model = new KulturObjektDokumentListDTO("hspId",
        "gndId",
        "gueltigeSignatur",
        "aufbewahrungsort",
        "besitzer",
        registrierungsdatum,
        registrierungsdatumString,
        "HSP-B1 HSP-B2");

    assertEquals("hspId", model.getHspId());
    assertEquals("gndId", model.getGndId());
    assertEquals("gueltigeSignatur", model.getGueltigeSignatur());
    assertEquals("aufbewahrungsort", model.getAufbewahrungsort());
    assertEquals("besitzer", model.getBesitzer());
    assertEquals(registrierungsdatum, model.getRegistrierungsdatum());
    assertEquals(registrierungsdatumString, model.getRegistrierungsdatumString());
    assertEquals("HSP-B1 HSP-B2", model.getBeschreibungenIds());
    assertNotNull(model.getBeschreibungenIdsAsList());
    assertEquals(Set.of("HSP-B1", "HSP-B2"), new HashSet<>(model.getBeschreibungenIdsAsList()));
  }


}
