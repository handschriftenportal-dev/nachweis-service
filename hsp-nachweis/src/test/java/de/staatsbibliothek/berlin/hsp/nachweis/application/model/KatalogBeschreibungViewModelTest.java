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

package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz.LizenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 18.10.2022
 */

class KatalogBeschreibungViewModelTest {

  @Test
  void testMap() {
    Beschreibung beschreibung = createBeschreibung();

    KatalogBeschreibungViewModel viewModel = KatalogBeschreibungViewModel.map(beschreibung);

    assertNotNull(viewModel);
    assertEquals(beschreibung.getId(), viewModel.getId());
    assertEquals(beschreibung.getKatalogID(), viewModel.getKatalogId());
    assertEquals("Stabi", viewModel.getBestandhaltendeInstitutionName());
    assertEquals("Berlin", viewModel.getBestandhaltendeInstitutionOrt());
    assertEquals(2020, viewModel.getJahrDerErstpublikation());
    assertEquals("Autor1, Name1; Autor2, Name2", viewModel.getAutoren());
    assertEquals("SIG. 1", viewModel.getSignatur());
    assertEquals("Titel", viewModel.getTitel());
    assertEquals(beschreibung.getId(), viewModel.getId());
  }

  Beschreibung createBeschreibung() {
    return new Beschreibung.BeschreibungsBuilder()
        .withId("B-1")
        .withKatalog("KAT-1")
        .withBeschreibungsTyp(BeschreibungsTyp.MEDIEVAL)
        .withLizenz(new LizenzBuilder()
            .addUri("http://creativecommons.org/licenses/by-sa/3.0/de/")
            .withBeschreibungsText("Namensnennung - Weitergabe unter gleichen Bedingungen 3.0 Deutschland")
            .build())
        .addBeschreibungsKomponente(new BeschreibungsKomponenteKopfBuilder()
            .withId("BKK-1")
            .withIndentifikationen(Set.of(new IdentifikationBuilder()
                .withId("GID-1")
                .withIdent("SIG. 1")
                .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
                .withAufbewahrungsOrt(
                    new NormdatenReferenz("HSP-2", "Berlin", "GND-2", NormdatenReferenz.ORT_TYPE_NAME))
                .withBesitzer(
                    new NormdatenReferenz("HSP-1", "Stabi", "GND-1", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
                .build()))
            .withTitel("Titel")
            .build())
        .withKodID("KOD-1")
        .addPublikation(new Publikation("PUB-1", LocalDateTime.of(2020, Month.MAY, 4, 12, 0, 0),
            PublikationsTyp.ERSTPUBLIKATION))
        .addAutor(new NormdatenReferenz("HSP-3", "Autor1, Name1", "GND-3", NormdatenReferenz.PERSON_TYPE_NAME))
        .addAutor(new NormdatenReferenz("HSP-4", "Autor2, Name2", "GND-3", NormdatenReferenz.PERSON_TYPE_NAME))
        .build();
  }

}
