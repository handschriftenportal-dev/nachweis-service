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

import static de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp.BESCHREIBUNG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewModel.ImportViewModelBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImportException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImportService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.KulturObjektDokumentNotFoundException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.primefaces.PrimeFaces;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.09.2020.
 * @version 1.0
 */
public class DatenuebernahmeModelTest {

  static String TEI_FILENAME = "tei-msDesc_Westphal.xml";
  private static TEI tei = null;
  DatenuebernahmeStatusModel datenuebernahmeStatusModel = new DatenuebernahmeStatusModel();

  PrimeFaces current = Mockito.mock(PrimeFaces.class);
  MockedStatic<PrimeFaces> primeFacesCurrent;
  private BeschreibungImportService importBoundary = Mockito.mock(BeschreibungImportService.class);
  private ImportRepository importRepository = Mockito.mock(ImportRepository.class);

  @BeforeEach
  public void setUp() {
    primeFacesCurrent = Mockito.mockStatic(PrimeFaces.class);
    primeFacesCurrent.when(PrimeFaces::current).thenReturn(current);
  }

  @AfterEach
  public void derigisterStaticMock() {
    primeFacesCurrent.close();
  }

  @Test
  void testCreateDatenuebernahmeModel() {
    Mockito.when(importRepository.listAll()).thenReturn(Collections.emptyList());
    importBoundary.setImportRepository(importRepository);
    DatenuebernahmeModel datenuebernahmeModel = new DatenuebernahmeModel(importBoundary, null,
        datenuebernahmeStatusModel, "http://localhost:9296/import");

    assertNotNull(datenuebernahmeModel.getAllImporte());

    assertTrue(datenuebernahmeModel.getAllImporte().isEmpty());

    assertNotNull(datenuebernahmeModel.getImportBoundary());

    assertNotNull(datenuebernahmeModel.getImportServerBaseUrl());

    verify(importBoundary, times(1)).alleImporteAnzeigen();
  }

  @Test
  void testAlleImportanzeigen() throws ActivityStreamsException {

    BeschreibungImport job = new BeschreibungImport(ImportStatus.OFFEN, null);

    Mockito.when(importRepository.listAll()).thenReturn(List.of(job));
    importBoundary.setImportRepository(importRepository);

    DatenuebernahmeModel datenuebernahmeModel = new DatenuebernahmeModel(importBoundary, null,
        datenuebernahmeStatusModel, "http://localhost:9296/import");

    when(importBoundary.alleImporteAnzeigen()).thenReturn(Arrays.asList(job));

    datenuebernahmeModel.alleImporteAnzeigen();

    assertEquals(1, datenuebernahmeModel.getAllImporte().size());
  }

  @Test
  void testMapChoosenImport() throws ActivityStreamsException, KulturObjektDokumentNotFoundException {
    DatenuebernahmeModel datenuebernahmeModel = new DatenuebernahmeModel(importBoundary, null,
        datenuebernahmeStatusModel, "http://localhost:9296/import");

    NormdatenReferenz besitzer = new NormdatenReferenz("", "Staatsbibliothek zu Berlin", "");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .build();

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("1234")
        .withGndIdentifier("gndid1231234x")
        .withGueltigerIdentifikation(identifikation)
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withTitel("Flavius Josephus, de bello Judaico")
        .withIndentifikationen(new LinkedHashSet<>(Arrays.asList(identifikation)))
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("40170459")
        .withKodID(kod.getId())
        .build();

    beschreibung.getBeschreibungsStruktur().add(kopf);

    ActivityStreamObject activityStreamObject = ActivityStreamObject.builder()
        .withContent(tei)
        .withName(TEI_FILENAME)
        .withId(TEI_FILENAME)
        .withType(BESCHREIBUNG)
        .build();

    ActivityStream message = ActivityStream.builder()
        .withId("Berlin_test_4_190405.fmt.xml")
        .withActorName("Konrad Eichstädt")
        .withType(ActivityStreamAction.ADD)
        .withPublished(LocalDateTime.now())
        .withTargetName("Berlin_test_4_190405.fmt.xml")
        .addObject(activityStreamObject)
        .build();

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN, message);

    when(importBoundary.ermittleKOD(any())).thenReturn(Optional.of(kod));

    when(importBoundary.findById("1")).thenReturn(Optional.of(beschreibungImportJob));

    datenuebernahmeModel.mapChoosenImport("1");

    Mockito.verify(current, Mockito.times(1)).executeScript("PF('idleDialog').hide();");
    Mockito.verify(current, Mockito.times(1)).executeScript("PF('datenPruefungsDialog').show();");
  }

  @Test
  void testMapChoosenImportWithEmptyJob() throws ActivityStreamsException {
    DatenuebernahmeModel datenuebernahmeModel = new DatenuebernahmeModel(importBoundary, null,
        datenuebernahmeStatusModel, "http://localhost:9296/import");
    datenuebernahmeModel.mapChoosenImport("1");
    Mockito.verify(current, Mockito.times(1)).executeScript("PF('idleDialog').hide();");
    Mockito.verify(current, Mockito.times(0)).executeScript("PF('datenPruefungsDialog').show();");
  }

  @Test
  void testimporteUebernehmen() throws ActivityStreamsException, BeschreibungImportException {

    ActivityStreamObject activityStreamObject = ActivityStreamObject.builder()
        .withName("TEI_FILENAME")
        .withId("TEI_FILENAME")
        .withType(ActivityStreamsDokumentTyp.BESCHREIBUNG)
        .build();

    ActivityStream message = ActivityStream.builder()
        .withId("Berlin_test_4_190405.fmt.xml")
        .withPublished(LocalDateTime.now())
        .withType(ActivityStreamAction.ADD)
        .addObject(activityStreamObject)
        .build();

    BeschreibungImport job = new BeschreibungImport(ImportStatus.OFFEN, message);

    Mockito.when(importRepository.listAll()).thenReturn(List.of(job));
    importBoundary.setImportRepository(importRepository);

    DatenuebernahmeModel datenuebernahmeModel = new DatenuebernahmeModel(importBoundary, null,
        datenuebernahmeStatusModel, "http://localhost:9296/import");

    ImportViewModel model = new ImportViewModel.ImportViewModelBuilder()
        .withImportStatus(ImportStatus.OFFEN)
        .withId(job.getId()).build();

    datenuebernahmeModel.getAllImporte().add(model);

    when(importBoundary.findById(job.getId())).thenReturn(Optional.of(job));

    job.setStatus(ImportStatus.UEBERNOMMEN);

    assertEquals(ImportStatus.OFFEN, datenuebernahmeModel.getAllImporte().get(0).getImportStatus());

    BeschreibungImport mockBeschreibungImport = Mockito.mock(BeschreibungImport.class);

    when(mockBeschreibungImport.getId()).thenReturn(job.getId());

    datenuebernahmeModel.setBeschreibungImportJobToAnnehmenOrAblehnen(mockBeschreibungImport);
    datenuebernahmeModel.setBeschreibungsVerwaltungsType("intern");

    datenuebernahmeModel.importUebernehmen();

    verify(importBoundary, times(1)).importeUebernehmen(job.getId(), "intern");

    assertEquals(1, datenuebernahmeModel.getAllImporte().size());

    assertEquals(model.getId(), datenuebernahmeModel.getAllImporte().get(0).getId());

    assertEquals(ImportStatus.UEBERNOMMEN, datenuebernahmeModel.getAllImporte().get(0).getImportStatus());
  }

  @Test
  void testImporteAblehnen() throws BeschreibungImportException {
    DatenuebernahmeModel datenuebernahmeModel = new DatenuebernahmeModel(importBoundary, null,
        datenuebernahmeStatusModel, "http://localhost:9296/import");

    ImportViewModel model = new ImportViewModel.ImportViewModelBuilder()
        .withImportStatus(ImportStatus.OFFEN)
        .withSelection(true)
        .withId("1").build();

    BeschreibungImport mockBeschreibungImport = Mockito.mock(BeschreibungImport.class);

    when(mockBeschreibungImport.getId()).thenReturn("1");

    datenuebernahmeModel.setBeschreibungImportJobToAnnehmenOrAblehnen(mockBeschreibungImport);

    datenuebernahmeModel.importAblehnen();

    verify(importBoundary, times(1)).importeAblehnen(Arrays.asList("1"));
  }

  @Test
  void testUpdateImportJobView() throws ActivityStreamsException {

    ActivityStreamObject activityStreamObject = ActivityStreamObject.builder()
        .withName("TEI_FILENAME")
        .withId("TEI_FILENAME")
        .withType(ActivityStreamsDokumentTyp.BESCHREIBUNG)
        .build();

    ActivityStream message = ActivityStream.builder()
        .withId("Berlin_test_4_190405.fmt.xml")
        .withPublished(LocalDateTime.now())
        .withType(ActivityStreamAction.ADD)
        .addObject(activityStreamObject)
        .build();

    BeschreibungImport job = new BeschreibungImport(ImportStatus.UEBERNOMMEN, message);

    Mockito.when(importRepository.listAll()).thenReturn(List.of(job));
    importBoundary.setImportRepository(importRepository);

    DatenuebernahmeModel datenuebernahmeModel = new DatenuebernahmeModel(importBoundary, null,
        datenuebernahmeStatusModel, "http://localhost:9296/import");

    ImportViewModel model = new ImportViewModelBuilder()
        .withImportStatus(ImportStatus.OFFEN)
        .withId(job.getId()).build();

    datenuebernahmeModel.getAllImporte().add(model);

    when(importBoundary.findById(job.getId())).thenReturn(Optional.of(job));

    datenuebernahmeModel.updateImportJobView();

    assertEquals(ImportStatus.UEBERNOMMEN, model.getImportStatus());
  }
}
