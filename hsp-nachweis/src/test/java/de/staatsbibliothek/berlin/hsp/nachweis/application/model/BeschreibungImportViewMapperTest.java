package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp.BESCHREIBUNG;
import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel.BeschreibungsViewBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungsImportService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentNotFoundException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tei_c.ns._1.TEI;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 07.06.2019.
 */

class BeschreibungImportViewMapperTest {

  private static TEI tei = null;
  final String TEI_FILENAME = "tei-msDesc_Westphal_head.xml";

  BeschreibungsImportService importBoundary = Mockito.mock(BeschreibungsImportService.class);
  KulturObjektDokumentBoundary kulturObjektDokumentBoundary = Mockito.mock(
      KulturObjektDokumentBoundary.class);
  ImportViewMapper importViewMapper = new ImportViewMapper(importBoundary,
      kulturObjektDokumentBoundary);

  @BeforeEach
  void init() throws IOException, JAXBException {
    if (Objects.isNull(tei)) {
      Path teiFilePath = Paths
          .get("../domainmodel-tei-mapper/src", "test", "resources", "tei", TEI_FILENAME);

      try (InputStream is = newInputStream(teiFilePath)) {
        List<TEI> allTEI = TEIObjectFactory.unmarshal(is);
        tei = allTEI.getFirst();
      }
    }
  }

  @Test
  void test_GivenImport_MapToView() throws Exception {

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
        .withContent(tei)
        .withName(TEI_FILENAME)
        .withId(TEI_FILENAME)
        .withType(BESCHREIBUNG)
        .withUrl("http://importservice:9296/import/job/123")
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

    when(kulturObjektDokumentBoundary.findKodByBeschreibung(any())).thenReturn(Optional.of(kod));

    when(importBoundary.convertTEI2Beschreibung(
        new ActivityStreamObjectDTO(activityStreamObject))).thenCallRealMethod();

    ImportViewModel importViewModel = importViewMapper.map(beschreibungImportJob, true);

    assertNotNull(importViewModel);

    assertNotNull(importViewModel.getId());

    assertEquals("Berlin_test_4_190405.fmt.xml", importViewModel.getDateiName());

    assertEquals("Konrad Eichstädt", importViewModel.getBenutzer());

    assertEquals("http://importservice:9296/import/job/123", importViewModel.getImportUrl());

    assertNotNull(importViewModel.getBeschreibung());

    assertEquals("mss_36-23-aug-2f_tei-msDesc_Westphal",
        importViewModel.getBeschreibung().getFirst().getId());

    assertEquals(beschreibungImportJob.getId(),
        importViewModel.getBeschreibung().getFirst().getImportJobID());

    assertEquals("Index term title",
        importViewModel.getBeschreibung().getFirst().getTitel());

    assertEquals("Cod. Guelf. 36.23 Aug. 2°",
        importViewModel.getBeschreibung().getFirst().getSignatur());

    assertNotNull(importViewModel.getBeschreibung().getFirst().getTeiXML());

    assertEquals("Kulturobjektdokument 1234<br/>Mscr.Dresd.A.111<br/>Staatsbibliothek zu Berlin",
        importViewModel.getBeschreibung().getFirst().getPreconditionResult());
  }

  @Test
  void testcheckBeschreibungsImportPreCondition() throws KulturObjektDokumentNotFoundException {

    NormdatenReferenz besitzer = new NormdatenReferenz("", "Staatsbibliothek zu Berlin", "");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withTitel("Flavius Josephus, de bello Judaico")
        .withAlternativeIdentifikationen(new LinkedHashSet<>(
            Collections.singletonList(identifikation)))
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("40170459", "1",
        identifikation)
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .build();

    beschreibung.getBeschreibungsStruktur().add(kopf);

    BeschreibungsViewModel view = new BeschreibungsViewBuilder()
        .withID((beschreibung).getId()).build();

    when(importBoundary.findExistingBeschreibung(any(Beschreibung.class)))
        .thenReturn(Optional.empty());
    when(kulturObjektDokumentBoundary.findKodByBeschreibung(any(Beschreibung.class))).thenThrow(
        new KulturObjektDokumentNotFoundException("KOD nicht gefunden"));

    importViewMapper.checkBeschreibungsImportPreCondition(beschreibung, view);

    assertFalse(view.isPreconditionState());

    assertEquals("KOD nicht gefunden", view.getPreconditionResult());

    when(importBoundary.findExistingBeschreibung(any(Beschreibung.class))).thenReturn(
        Optional.of(beschreibung));

    importViewMapper.checkBeschreibungsImportPreCondition(beschreibung, view);

    assertFalse(view.isPreconditionState());

    assertEquals("Beschreibung (intern) existiert bereits 40170459", view.getPreconditionResult());
  }

  @Test
  void testCheckBeschreibungsImportPreCondition_WhenBeschreibungExistsAndIsPublished() {
    NormdatenReferenz besitzer = new NormdatenReferenz("", "Staatsbibliothek zu Berlin", "");
    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .build();

    Beschreibung existingBeschreibung = new Beschreibung.BeschreibungsBuilder("40170459", "1",
        identifikation)
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withPublikation(createPublishedPublikation())
        .build();

    Beschreibung newBeschreibung = new Beschreibung.BeschreibungsBuilder("40170460", "1",
        identifikation)
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder()
        .withID(newBeschreibung.getId()).build();

    when(importBoundary.findExistingBeschreibung(any(Beschreibung.class)))
        .thenReturn(Optional.of(existingBeschreibung));

    importViewMapper.checkBeschreibungsImportPreCondition(newBeschreibung, view);

    assertTrue(view.isPreconditionState());
    assertEquals("Beschreibung (extern) existiert bereits 40170459", view.getPreconditionResult());
  }

  @Test
  void testCheckBeschreibungsImportPreCondition_WhenBeschreibungDoesNotExistButKODFound()
      throws KulturObjektDokumentNotFoundException {
    // Given
    NormdatenReferenz besitzer = new NormdatenReferenz("", "Staatsbibliothek zu Berlin", "");
    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .build();

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("kod-123")
        .withGndIdentifier("gnd-123")
        .withGueltigerIdentifikation(identifikation)
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("40170459", "1",
        identifikation)
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder()
        .withID(beschreibung.getId()).build();

    when(importBoundary.findExistingBeschreibung(any(Beschreibung.class)))
        .thenReturn(Optional.empty());
    when(kulturObjektDokumentBoundary.findKodByBeschreibung(any(Beschreibung.class)))
        .thenReturn(Optional.of(kod));

    // When
    importViewMapper.checkBeschreibungsImportPreCondition(beschreibung, view);

    // Then
    assertTrue(view.isPreconditionState());
    assertEquals("Kulturobjektdokument kod-123<br/>Mscr.Dresd.A.111<br/>Staatsbibliothek zu Berlin",
        view.getPreconditionResult());
  }

  @Test
  void testCheckBeschreibungsImportPreCondition_WhenBeschreibungDoesNotExistAndKODNotFound()
      throws KulturObjektDokumentNotFoundException {
    // Given
    NormdatenReferenz besitzer = new NormdatenReferenz("", "Staatsbibliothek zu Berlin", "");
    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("40170459", "1",
        identifikation)
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder()
        .withID(beschreibung.getId()).build();

    when(importBoundary.findExistingBeschreibung(any(Beschreibung.class)))
        .thenReturn(Optional.empty());
    when(kulturObjektDokumentBoundary.findKodByBeschreibung(any(Beschreibung.class)))
        .thenThrow(new KulturObjektDokumentNotFoundException("KOD nicht gefunden"));

    // When
    importViewMapper.checkBeschreibungsImportPreCondition(beschreibung, view);

    // Then
    assertFalse(view.isPreconditionState());
    assertEquals("KOD nicht gefunden", view.getPreconditionResult());
  }

  @Test
  void testCheckBeschreibungsImportPreCondition_WhenFindKodReturnsEmpty()
      throws KulturObjektDokumentNotFoundException {
    NormdatenReferenz besitzer = new NormdatenReferenz("", "Staatsbibliothek zu Berlin", "");
    NormdatenReferenz aufbewahrungsOrt = new NormdatenReferenz("", "Berlin", "");
    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("40170459", "1",
        identifikation)
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder()
        .withID(beschreibung.getId()).build();

    when(importBoundary.findExistingBeschreibung(any(Beschreibung.class)))
        .thenReturn(Optional.empty());
    when(kulturObjektDokumentBoundary.findKodByBeschreibung(any(Beschreibung.class)))
        .thenReturn(Optional.empty());

    importViewMapper.checkBeschreibungsImportPreCondition(beschreibung, view);

    assertFalse(view.isPreconditionState());
    assertEquals(
        "Kulturobjekt konnte mit Minimaldaten von Beschreibung "
            + "Mscr.Dresd.A.111;Berlin;Staatsbibliothek zu Berlin nicht gefunden werden!",
        view.getPreconditionResult());
  }

  @Test
  void testCheckBeschreibungsImportPreCondition_WhenFindKodReturnsEmptyAndNoIdentifikation()
      throws KulturObjektDokumentNotFoundException {
    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("40170459", null, null)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder()
        .withID(beschreibung.getId()).build();

    when(importBoundary.findExistingBeschreibung(any(Beschreibung.class)))
        .thenReturn(Optional.empty());
    when(kulturObjektDokumentBoundary.findKodByBeschreibung(any(Beschreibung.class)))
        .thenReturn(Optional.empty());

    importViewMapper.checkBeschreibungsImportPreCondition(beschreibung, view);

    assertFalse(view.isPreconditionState());
    assertEquals(
        "Kulturobjekt konnte mit Minimaldaten von Beschreibung ohne Signatur nicht gefunden werden!",
        view.getPreconditionResult());
  }

  private Publikation createPublishedPublikation() {
    Publikation publikation = new Publikation.PublikationBuilder()
        .withErstpublikationsDatum(LocalDateTime.of(2020, 1, 1, 0, 0))
        .build();
    publikation.onApproved();
    return publikation;
  }
}
