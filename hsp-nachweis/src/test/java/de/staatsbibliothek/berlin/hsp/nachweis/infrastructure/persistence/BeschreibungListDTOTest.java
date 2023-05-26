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
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 28.11.2022
 */

class BeschreibungListDTOTest {

  @Test
  void testConstruct() {
    LocalDateTime erstellungsdatum = LocalDateTime.of(2022, 11, 16, 15, 14, 13);
    LocalDateTime aenderungsdatum = LocalDateTime.of(2022, 11, 17, 16, 15, 14);

    BeschreibungListDTO beschreibungListDTO = new BeschreibungListDTO(
        "hspId",
        "kodId",
        "katalogId",
        "gueltigeSignatur",
        "titel",
        "verwaltungsTyp",
        "dokumentobjekttyp",
        "Piotr Czarnecki",
        null,
        "aufbewahrungsort",
        "besitzer",
        erstellungsdatum,
        erstellungsdatum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
        null,
        aenderungsdatum,
        aenderungsdatum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy 00:00:00")),
        null
    );

    assertNotNull(beschreibungListDTO);
    assertEquals("hspId", beschreibungListDTO.getHspId());
    assertEquals("kodId", beschreibungListDTO.getKodId());
    assertEquals("katalogId", beschreibungListDTO.getKatalogId());
    assertEquals("gueltigeSignatur", beschreibungListDTO.getGueltigeSignatur());
    assertEquals("titel", beschreibungListDTO.getTitel());
    assertEquals("verwaltungsTyp", beschreibungListDTO.getVerwaltungsTyp());
    assertEquals("dokumentobjekttyp", beschreibungListDTO.getDokumentObjektTyp());
    assertEquals("aufbewahrungsort", beschreibungListDTO.getAufbewahrungsort());
    assertEquals("besitzer", beschreibungListDTO.getBesitzer());
    assertEquals(erstellungsdatum, beschreibungListDTO.getErstellungsdatum());
    assertEquals("16.11.2022 15:14:13", beschreibungListDTO.getErstellungsdatumString());
    assertEquals(aenderungsdatum, beschreibungListDTO.getAenderungsdatum());
    assertEquals("17.11.2022", beschreibungListDTO.getAenderungsdatumString());
  }

}
