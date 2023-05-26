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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.BeschreibungListDTO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 11.04.2022
 */

class DashboardControllerTest {

  static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

  BeschreibungsBoundary beschreibungsBoundary;

  @BeforeEach
  void beforeEach() {
    LocalDateTime erstellungsdatum = LocalDateTime.of(2022, 11, 10, 9, 8, 7);
    LocalDateTime aenderungsdatum = LocalDateTime.of(2022, 12, 11, 10, 9, 8);

    BeschreibungListDTO model = new BeschreibungListDTO(
        "HSP-1",
        "KOD-1",
        "KAT-1",
        "Sig. 1",
        "Titel",
        VerwaltungsTyp.EXTERN.name(),
        DokumentObjektTyp.HSP_DESCRIPTION.name(),
        "Piotr Czarnecki",
        null,
        "Berlin",
        "Staatsbibliothek zu Berlin",
        erstellungsdatum,
        erstellungsdatum.format(DATE_TIME_FORMATTER),
        null,
        aenderungsdatum,
        aenderungsdatum.format(DATE_TIME_FORMATTER),
        null);

    beschreibungsBoundary = mock(BeschreibungsBoundary.class);
    when(beschreibungsBoundary.findLatestModified()).thenReturn(List.of(model));
  }

  @Test
  void testSetup() {
    DashboardController dashboardController = new DashboardController(beschreibungsBoundary);
    dashboardController.setup();

    assertNotNull(dashboardController.getBeschreibungen());
    assertEquals(1, dashboardController.getBeschreibungen().size());
    assertNotNull(dashboardController.getBeschreibungen().get(0));
    assertEquals("HSP-1", dashboardController.getBeschreibungen().get(0).getHspId());
  }

}
