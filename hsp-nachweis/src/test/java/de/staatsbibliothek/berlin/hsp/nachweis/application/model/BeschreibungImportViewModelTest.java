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

import static org.hamcrest.core.Is.is;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 13.06.2019.
 */
public class BeschreibungImportViewModelTest {


  @Test
  public void testGivenValuesCreate() {

    ImportViewModel importViewModel = new ImportViewModel.ImportViewModelBuilder().withId("1")
        .withBenutzer("Konrad").withInstitution("SBB").withDateiName("Test.xml")
        .withImportUrl("http://localhost.de").withImportDatum(LocalDateTime.of(2019,7,20,0,0,0))
        .withImportStatus(ImportStatus.OFFEN).withBeschreibung(Arrays.asList())
        .withFehler("Fehler im System")
        .build();

    MatcherAssert.assertThat(importViewModel.getId(), is("1"));

    MatcherAssert.assertThat(importViewModel.getBenutzer(), is("Konrad"));

    MatcherAssert.assertThat(importViewModel.getInstitution(), is("SBB"));

    MatcherAssert.assertThat(importViewModel.getDateiName(), is("Test.xml"));

    MatcherAssert.assertThat(importViewModel.getImportUrl(), is("http://localhost.de"));

    MatcherAssert.assertThat(importViewModel.getImportDatum(), is(LocalDateTime.of(2019,7,20,0,0,0)));

    Assertions.assertEquals("Fehler im System", importViewModel.getFehler());
  }

  @Test
  public void test_GivenTwoViews_Equals() {

    ImportViewModel importViewModel1 = new ImportViewModel.ImportViewModelBuilder().withId("1")
        .withBenutzer("Konrad").withInstitution("SBB").withDateiName("Test.xml")
        .withImportUrl("http://localhost.de").withImportDatum(LocalDateTime.of(2019,7,20,0,0,0))
        .withImportStatus(ImportStatus.OFFEN).withBeschreibung(Arrays.asList())
        .build();

    ImportViewModel importViewModel2 = new ImportViewModel.ImportViewModelBuilder().withId("2")
        .withBenutzer("Konrad").withInstitution("SBB").withDateiName("Test.xml")
        .withImportUrl("http://localhost.de").withImportDatum(LocalDateTime.of(2019,7,20,0,0,0))
        .withImportStatus(ImportStatus.OFFEN).withBeschreibung(Arrays.asList())
        .build();

    Assertions.assertNotEquals(importViewModel1, importViewModel2);
  }
}
