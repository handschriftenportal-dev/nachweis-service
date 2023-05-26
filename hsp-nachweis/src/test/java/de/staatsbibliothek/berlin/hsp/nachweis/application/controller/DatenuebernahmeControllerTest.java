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

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.DatenuebernahmeModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.DatenuebernahmeStatusModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImportException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 07.06.2019.
 */
class DatenuebernahmeControllerTest {

  private ImportBoundary importBoundary = Mockito.mock(ImportBoundary.class);

  private DatenuebernahmeStatusModel datenuebernahmeStatusModel = new DatenuebernahmeStatusModel();


  private DatenuebernahmeModel datenuebernahmeModel = new DatenuebernahmeModel(importBoundary, null,
      datenuebernahmeStatusModel, "http://localhost:9090");

  private ActivityStream message;

  @BeforeEach
  public void setUp() throws ActivityStreamsException {

    NormdatenReferenz besitzer = new NormdatenReferenz("", "Staatsbibliothek zu Berlin", "");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withTitel("Flavius Josephus, de bello Judaico")
        .withIndentifikationen(new LinkedHashSet<>(Arrays.asList(identifikation)))
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("40170459")
        .build();

    beschreibung.getBeschreibungsStruktur().add(kopf);

    message = ActivityStream.builder()
        .withId("Berlin_test_4_190405.fmt.xml")
        .withTargetName("Berlin_test_4_190405.fmt.xml")
        .withActorName("Konrad Eichstädt")
        .withPublished(LocalDateTime.now())
        .build();

    //TODO: Add load to AS Arrays.asList(beschreibung)
  }

  @Test
  public void testGivenImportAblehnen() throws BeschreibungImportException {

    DatenuebernahmeController controller = new DatenuebernahmeController(datenuebernahmeModel);

    BeschreibungsViewModel beschreibungsViewModel = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withImportJobID("1")
        .withID("1")
        .withTitel("Title")
        .withSignatur("HDBMS")
        .withExterneID("")
        .withKODView("kods123")
        .withTEIXML("").build();

    ImportViewModel importViewModel = new ImportViewModel.ImportViewModelBuilder().withId("23")
        .withBenutzer("Konrad").withInstitution("Staatsbibliothek zu Berlin")
        .withDateiName("Test.xml").withImportUrl("").withImportDatum(LocalDateTime.now())
        .withImportStatus(ImportStatus.OFFEN)
        .withBeschreibung(Arrays.asList(beschreibungsViewModel))
        .build();
    importViewModel.setSelectedForImport(true);

    BeschreibungImport mockBeschreibungImport = Mockito.mock(BeschreibungImport.class);

    when(mockBeschreibungImport.getId()).thenReturn("1");

    controller.setBeschreibungImportJobToAnnehmenOrAblehnen(mockBeschreibungImport);
    controller.setBeschreibungImportJobToAnnehmenOrAblehnenImportViewId(importViewModel.getId());

    controller.importAblehnen();

    verify(importBoundary, times(1)).importeAblehnen(Arrays.asList("1"));
  }

  @Test
  public void testGivenImportUebernehmen() throws BeschreibungImportException {

    DatenuebernahmeController controller = new DatenuebernahmeController(datenuebernahmeModel);

    BeschreibungsViewModel beschreibungsViewModel = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withImportJobID("1")
        .withID("1")
        .withTitel("Title")
        .withSignatur("HDBMS")
        .withExterneID("")
        .withKODView("kods123")
        .withTEIXML("").build();

    ImportViewModel importViewModel = new ImportViewModel.ImportViewModelBuilder().withId("23")
        .withBenutzer("Konrad").withInstitution("Staatsbibliothek zu Berlin")
        .withDateiName("Test.xml").withImportUrl("").withImportDatum(LocalDateTime.now())
        .withImportStatus(ImportStatus.OFFEN)
        .withBeschreibung(Arrays.asList(beschreibungsViewModel))
        .build();
    importViewModel.setSelectedForImport(true);

    BeschreibungImport mockBeschreibungImport = Mockito.mock(BeschreibungImport.class);

    when(mockBeschreibungImport.getId()).thenReturn("1");

    controller.setBeschreibungImportJobToAnnehmenOrAblehnen(mockBeschreibungImport);
    controller.setBeschreibungImportJobToAnnehmenOrAblehnenImportViewId(importViewModel.getId());

    controller.setBeschreibungsVerwaltungsType("extern");
    controller.importUebernehmen();

    verify(importBoundary, times(1)).importeUebernehmen("1", "extern");
  }


  @Test
  public void testGivenImportAlleImporteAnzeigen() throws ActivityStreamsException {

    List<BeschreibungImport> importe = new ArrayList<>();

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN, message);
    importe.add(beschreibungImportJob);

    Mockito.when(importBoundary.alleImporteAnzeigen()).thenReturn(importe);

    DatenuebernahmeController controller = new DatenuebernahmeController(datenuebernahmeModel);

    controller.alleImporteAnzeigen();

    Assertions.assertNotNull(controller.getAllImporte());

    MatcherAssert.assertThat(controller.getAllImporte().size(), is(1));

    Mockito.verify(importBoundary, times(2)).alleImporteAnzeigen();
  }

}
