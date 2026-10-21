package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp.BESCHREIBUNG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewMapper;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewModel.ImportViewModelBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungsImportException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungsImportService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import java.time.LocalDateTime;
import java.util.Collections;
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
class DatenuebernahmeModelTest {

  static String TEI_FILENAME = "tei-msDesc_Westphal.xml";

  DatenuebernahmeStatusModel datenuebernahmeStatusModel = new DatenuebernahmeStatusModel();

  PrimeFaces current = Mockito.mock(PrimeFaces.class);
  MockedStatic<PrimeFaces> primeFacesCurrent;

  BeschreibungsImportService importBoundary = Mockito.mock(BeschreibungsImportService.class);
  ImportRepository importRepository = Mockito.mock(ImportRepository.class);
  ImportViewMapper importViewMapper = Mockito.mock(ImportViewMapper.class);

  DatenuebernahmeModel datenuebernahmeModel;

  @BeforeEach
  void setUp() {
    primeFacesCurrent = Mockito.mockStatic(PrimeFaces.class);
    primeFacesCurrent.when(PrimeFaces::current).thenReturn(current);
    datenuebernahmeModel = new DatenuebernahmeModel(importBoundary,
        importViewMapper,
        null,
        datenuebernahmeStatusModel,
        "http://localhost:9296/import");
  }

  @AfterEach
  void derigisterStaticMock() {
    primeFacesCurrent.close();
  }

  @Test
  void testCreateDatenuebernahmeModel() {
    Mockito.when(importRepository.listAll()).thenReturn(Collections.emptyList());
    importBoundary.setImportRepository(importRepository);

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

    when(importBoundary.alleImporteAnzeigen()).thenReturn(List.of(job));
    ImportViewModel model = new ImportViewModel.ImportViewModelBuilder()
        .withId(job.getId())
        .withImportStatus(ImportStatus.OFFEN)
        .build();
    when(importViewMapper.map(job, false)).thenReturn(model);

    datenuebernahmeModel.alleImporteAnzeigen();

    assertEquals(1, datenuebernahmeModel.getAllImporte().size());
  }

  @Test
  void testMapChoosenImport()
      throws ActivityStreamsException {

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
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("40170459", kod.getId(),
        identifikation)
        .build();

    beschreibung.getBeschreibungsStruktur().add(kopf);
    ActivityStreamObject activityStreamObject = ActivityStreamObject.builder()
        .withContent((TEI) null)
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

    when(importBoundary.findById("1")).thenReturn(Optional.of(beschreibungImportJob));

    datenuebernahmeModel.mapChoosenImport("1");

    Mockito.verify(current, Mockito.times(1)).executeScript("PF('idleDialog').hide();");
    Mockito.verify(current, Mockito.times(1)).executeScript("PF('datenPruefungsDialog').show();");
  }

  @Test
  void testMapChoosenImportWithEmptyJob() {
    datenuebernahmeModel.mapChoosenImport("1");
    Mockito.verify(current, Mockito.times(1)).executeScript("PF('idleDialog').hide();");
    Mockito.verify(current, Mockito.times(0)).executeScript("PF('datenPruefungsDialog').show();");
  }

  @Test
  void testimporteUebernehmen() throws ActivityStreamsException, BeschreibungsImportException {

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

    ImportViewModel model = new ImportViewModel.ImportViewModelBuilder()
        .withImportStatus(ImportStatus.OFFEN)
        .withId(job.getId()).build();

    datenuebernahmeModel.getAllImporte().add(model);

    when(importBoundary.findById(job.getId())).thenReturn(Optional.of(job));

    job.setStatus(ImportStatus.UEBERNOMMEN);
    ImportViewModel uebernommenModel = new ImportViewModel.ImportViewModelBuilder()
        .withImportStatus(ImportStatus.UEBERNOMMEN)
        .withId(job.getId()).build();
    when(importViewMapper.map(job, false)).thenReturn(uebernommenModel);

    assertEquals(ImportStatus.OFFEN,
        datenuebernahmeModel.getAllImporte().getFirst().getImportStatus());

    BeschreibungImport mockBeschreibungImport = Mockito.mock(BeschreibungImport.class);

    when(mockBeschreibungImport.getId()).thenReturn(job.getId());

    datenuebernahmeModel.setBeschreibungImportJobToAnnehmenOrAblehnen(mockBeschreibungImport);
    datenuebernahmeModel.setBeschreibungsVerwaltungsType("intern");
    datenuebernahmeModel.importUebernehmen();

    verify(importBoundary, times(1)).importeUebernehmen(job.getId(), "intern");
    assertEquals(1, datenuebernahmeModel.getAllImporte().size());
    assertEquals(model.getId(), datenuebernahmeModel.getAllImporte().getFirst().getId());
    assertEquals(ImportStatus.UEBERNOMMEN,
        datenuebernahmeModel.getAllImporte().getFirst().getImportStatus());
  }

  @Test
  void testImporteAblehnen() throws BeschreibungsImportException {
    BeschreibungImport mockBeschreibungImport = Mockito.mock(BeschreibungImport.class);

    when(mockBeschreibungImport.getId()).thenReturn("1");

    datenuebernahmeModel.setBeschreibungImportJobToAnnehmenOrAblehnen(mockBeschreibungImport);
    datenuebernahmeModel.importAblehnen();

    verify(importBoundary, times(1)).importeAblehnen(List.of("1"));
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

    ImportViewModel model = new ImportViewModelBuilder()
        .withImportStatus(ImportStatus.OFFEN)
        .withId(job.getId()).build();

    datenuebernahmeModel.getAllImporte().add(model);

    when(importBoundary.findById(job.getId())).thenReturn(Optional.of(job));
    ImportViewModel uebernommenModel = new ImportViewModelBuilder()
        .withImportStatus(ImportStatus.UEBERNOMMEN)
        .withId(job.getId()).build();
    when(importViewMapper.map(job, false)).thenReturn(uebernommenModel);

    datenuebernahmeModel.updateImportJobView();

    assertEquals(1, datenuebernahmeModel.getAllImporte().size());
    assertEquals(ImportStatus.UEBERNOMMEN,
        datenuebernahmeModel.getAllImporte().getFirst().getImportStatus());
  }
}
