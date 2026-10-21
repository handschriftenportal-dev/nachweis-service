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
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewMapper;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungsImportException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

  private final DatenuebernahmeStatusModel datenuebernahmeStatusModel = new DatenuebernahmeStatusModel();
  ImportBoundary importBoundary = Mockito.mock(ImportBoundary.class);
  ImportViewMapper importViewMapper = Mockito.mock(ImportViewMapper.class);
  ;
  KulturObjektDokumentBoundary kulturObjektDokumentBoundary = Mockito.mock(
      KulturObjektDokumentBoundary.class);
  ;

  DatenuebernahmeModel datenuebernahmeModel;

  ActivityStream message;

  @BeforeEach
  void setUp() throws ActivityStreamsException {
    datenuebernahmeModel = new DatenuebernahmeModel(
        importBoundary,
        importViewMapper,
        null,
        datenuebernahmeStatusModel, "http://localhost:9090");

    NormdatenReferenz besitzer = new NormdatenReferenz("", "Staatsbibliothek zu Berlin", "");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withTitel("Flavius Josephus, de bello Judaico")
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("40170459", "kod-1",
        identifikation)
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
  void testGivenImportAblehnen() throws BeschreibungsImportException {

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
        .withBeschreibung(Collections.singletonList(beschreibungsViewModel))
        .build();
    importViewModel.setSelectedForImport(true);

    BeschreibungImport mockBeschreibungImport = Mockito.mock(BeschreibungImport.class);

    when(mockBeschreibungImport.getId()).thenReturn("1");

    controller.setBeschreibungImportJobToAnnehmenOrAblehnen(mockBeschreibungImport);
    controller.setBeschreibungImportJobToAnnehmenOrAblehnenImportViewId(importViewModel.getId());

    controller.importAblehnen();

    verify(importBoundary, times(1)).importeAblehnen(List.of("1"));
  }

  @Test
  void testGivenImportUebernehmen() throws BeschreibungsImportException {

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
        .withBeschreibung(Collections.singletonList(beschreibungsViewModel))
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
  void testGivenImportAlleImporteAnzeigen() throws ActivityStreamsException {

    List<BeschreibungImport> importe = new ArrayList<>();

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN, message);
    importe.add(beschreibungImportJob);

    Mockito.when(importBoundary.alleImporteAnzeigen()).thenReturn(importe);
    Mockito.when(importViewMapper.map(beschreibungImportJob, false))
        .thenReturn(new ImportViewModel.ImportViewModelBuilder().withId("23").build());

    DatenuebernahmeController controller = new DatenuebernahmeController(datenuebernahmeModel);

    controller.alleImporteAnzeigen();

    Assertions.assertNotNull(controller.getAllImporte());

    MatcherAssert.assertThat(controller.getAllImporte().size(), is(1));

    Mockito.verify(importBoundary, times(2)).alleImporteAnzeigen();
  }

}
