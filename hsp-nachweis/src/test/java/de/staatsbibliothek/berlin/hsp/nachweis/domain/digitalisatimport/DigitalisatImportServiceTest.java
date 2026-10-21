package de.staatsbibliothek.berlin.hsp.nachweis.domain.digitalisatimport;

import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportVorgangBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportVorgang;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.network.DistributedSaveKODCommand;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.Ref;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 19.11.2020.
 * @version 1.0
 */

@QuarkusTest
class DigitalisatImportServiceTest {

  private static final String TEI_FILENAME = "kodsurrogateimport.xml";
  private static final Path teiFilePath = Paths
      .get("src", "test", "resources", TEI_FILENAME);
  private static ActivityStreamMessageDTO message;
  private static ActivityStreamObject importObject;

  static {
    try (InputStream is = newInputStream(teiFilePath)) {
      List<TEI> allTEI = TEIObjectFactory.unmarshal(is);
      TEI tei = allTEI.get(0);

      ActivityStreamObject activityStreamObject = ActivityStreamObject.builder()
          .withContent(tei)
          .withName(TEI_FILENAME)
          .withId(TEI_FILENAME)
          .withType(ActivityStreamsDokumentTyp.DIGITALISAT)
          .build();

      importObject = ActivityStreamObject.builder()
          .withCompressed(true)
          .withType(ActivityStreamsDokumentTyp.IMPORT)
          .withName(TEI_FILENAME)
          .withId("1")
          .withContent(
              "{\"id\":\"1c8050b1-616c-4454-b441-9281a57bc99d\",\"creationDate\":\"2020-09-24T15:22:15.926333\",\"benutzerName\":\"Redakteur\",\"importFiles\":[{\"id\":\"bec5a8c1-4e01-4d82-b19f-8189bb6b8a12\",\"path\":\"file:///tmp/hsp-import-/2020.09.24.15.22.15.925/ms_1-wthout-BOM-translated.xml\",\"dateiTyp\":\"ms_1-wthout-BOM.xml\",\"dateiName\":\""
                  + TEI_FILENAME
                  + "\",\"dateiFormat\":\"MXML\",\"error\":false,\"message\":null,\"importEntityData\":[]}],\"name\":\"ms_1.xml\",\"importDir\":\"/tmp/hsp-import-/2020.09.24.15.22.15.925\",\"result\":\"NO_RESULT\",\"errorMessage\":\"\",\"datatype\":\"BESCHREIBUNG\"}")
          .build();
      message = new ActivityStreamMessageDTO(ActivityStream.builder()
          .withId(TEI_FILENAME)
          .withPublished(LocalDateTime.now())
          .withType(ActivityStreamAction.ADD)
          .addObject(activityStreamObject)
          .addObject(importObject)
          .build());
    } catch (ActivityStreamsException | IOException | JAXBException e) {
      e.printStackTrace();
    }
  }

  @Inject
  KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;

  @Inject
  ImportVorgangBoundary importVorgangBoundary;

  @Inject
  DokumentSperreService dokumentSperreService;

  @Inject
  NormdatenReferenzRepository normdatenReferenzRepository;

  @Test
  void testExtractTEIFromMessage() throws JAXBException, ActivityStreamsException {
    NormdatenReferenzBoundary normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);

    DigitalisatImportService service = new DigitalisatImportService(kulturObjektDokumentBoundary,
        kulturObjektDokumentRepository,
        normdatenReferenzBoundaryMock,
        importVorgangBoundary,
        dokumentSperreService,
        "http://localhost:8080/");

    List<TEI> teis = service.extractTEIFromMessage(message);

    assertNotNull(teis);

    assertEquals(1, teis.size());
  }

  @Test
  void testFindAllTEIDigitalisate() throws Exception {
    NormdatenReferenzBoundary normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);

    DigitalisatImportService service = new DigitalisatImportService(kulturObjektDokumentBoundary,
        kulturObjektDokumentRepository,
        normdatenReferenzBoundaryMock,
        importVorgangBoundary,
        dokumentSperreService,
        "http://localhost:8080/");

    List<TEI> allTEI = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    Optional<MsDesc> msDescs = TEICommon.findFirst(MsDesc.class, allTEI.get(0));

    List<Bibl> biblList = service.findAllTEIDigitalisate(msDescs.get());

    assertEquals(3, biblList.size());
  }

  @Test
  void testfindKODIDOrSignature() throws Exception {
    NormdatenReferenzBoundary normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);
    KulturObjektDokumentRepository kulturObjektDokumentRepositoryMock = mock(
        KulturObjektDokumentRepository.class);
    List<TEI> allTEI = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));

    List<Bibl> biblList = new ArrayList<>();

    TEICommon.findAll(Bibl.class, allTEI.get(0), biblList);

    DigitalisatImportService service = new DigitalisatImportService(kulturObjektDokumentBoundary,
        kulturObjektDokumentRepositoryMock,
        normdatenReferenzBoundaryMock,
        importVorgangBoundary,
        dokumentSperreService,
        "http://localhost:8080/");

    NormdatenReferenz aufbewahrungsOrt = new NormdatenReferenz(
        "10",
        "Berlin",
        "4005728-8",
        NormdatenReferenz.ORT_TYPE_NAME);

    NormdatenReferenz besitzer = new NormdatenReferenz("20",
        "Staatsbibliothek zu Berlin", "5036103-X", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder("1234")
        .withGueltigerIdentifikation(new IdentifikationBuilder()
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withId("5678")
            .withIdent("Mus.ms. A I(1")
            .withAufbewahrungsOrt(aufbewahrungsOrt)
            .withBesitzer(besitzer)
            .build())
        .build();

    when(kulturObjektDokumentRepositoryMock.findByIdentifikationIdent("Mus.ms. A I(1"))
        .thenReturn(List.of(kod));

    biblList.stream().findFirst().ifPresentOrElse(bibl -> {
      try {
        assertTrue(service.findKulturObjektDokument("Mus.ms. A I(1", aufbewahrungsOrt, besitzer)
            .isPresent());

        assertEquals("1234",
            service.findKulturObjektDokument("Mus.ms. A I(1", aufbewahrungsOrt, besitzer).get()
                .getId());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }, () -> fail("KOD nicht gefunden"));
  }

  @Test
  void testCreateDigitalisatModelFromTEIBibl() throws Exception {
    NormdatenReferenzBoundary normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);
    List<TEI> allTEI = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));

    NormdatenReferenz bsb = createBSB();
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(bsb.getGndID(),
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .thenReturn(Optional.of(bsb));

    NormdatenReferenz muenchen = createMuenchen();
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(muenchen.getGndID(),
        NormdatenReferenz.ORT_TYPE_NAME))
        .thenReturn(Optional.of(muenchen));

    DigitalisatImportService service = new DigitalisatImportService(kulturObjektDokumentBoundary,
        kulturObjektDokumentRepository,
        normdatenReferenzBoundaryMock,
        importVorgangBoundary,
        dokumentSperreService,
        "http://localhost:8080/");

    List<Bibl> biblList = new ArrayList<>();

    TEICommon.findAll(Bibl.class, allTEI.get(0), biblList);

    Optional<DigitalisatViewModel> digitalisatViewModel = service
        .createDigitalisatModelFromTEIBibl(biblList.get(0));

    assertTrue(digitalisatViewModel.isPresent());

    digitalisatViewModel.ifPresent(model -> {
      assertEquals("https://iiif.ub.uni-leipzig.de/0000009089/manifest.json",
          model.getManifestURL());
      assertEquals(
          "https://iiif.ub.uni-leipzig.de/fcgi-bin/iipsrv.fcgi?iiif=/j2k/0000/0090/0000009089/00000057.jpx/full/500,/0/default.jpg",
          model.getThumbnail());
      assertEquals(
          "https://iiif.ub.uni-leipzig.de/fcgi-bin/iipsrv.fcgi?iiif=/j2k/0000/0090/0000009089/00000057.jpx/full/100,/0/default.jpg",
          model.getAlternativeUrl());
      assertNotNull(model.getErstellungsdatum());
      assertNotNull(model.getDigitalisierungsdatum());
      assertEquals(muenchen, model.getOrt());
      assertEquals(bsb, model.getEinrichtung());
    });
  }

  @Test
  void testmapTEIToModel() throws Exception {
    DigitalisatImportService service = new DigitalisatImportService(kulturObjektDokumentBoundary,
        kulturObjektDokumentRepository,
        null,
        importVorgangBoundary,
        dokumentSperreService,
        "http://localhost:8080/");
    List<TEI> allTEI = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    List<Bibl> biblList = new ArrayList<>();
    TEICommon.findAll(Bibl.class, allTEI.get(0), biblList);

    Optional<DigitalisatViewModel> digitalisatViewModel = service.mapTEIToModel(biblList.get(0));

    assertTrue(digitalisatViewModel.isPresent());

    assertNotNull(digitalisatViewModel.get().getId());

    assertNotNull(digitalisatViewModel.get().getManifestURL());

    assertEquals(1, digitalisatViewModel.get().getDigitalisatTypKonzepte().size());
  }

  @Test
  void testSaveToSystem() throws Exception {
    NormdatenReferenzBoundary normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);
    KulturObjektDokumentBoundary kulturObjektDokumentBoundaryMock = mock(
        KulturObjektDokumentBoundary.class);

    ImportVorgangBoundary importVorgangBoundary = mock(ImportVorgangBoundary.class);
    DigitalisatImportService service = new DigitalisatImportService(
        kulturObjektDokumentBoundaryMock,
        kulturObjektDokumentRepository,
        normdatenReferenzBoundaryMock,
        importVorgangBoundary,
        dokumentSperreService,
        "http://localhost:8080/");

    List<TEI> allTEI = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    List<Bibl> biblList = new ArrayList<>();
    TEICommon.findAll(Bibl.class, allTEI.get(0), biblList);
    DigitalisatViewModel digitalisatViewModel = service
        .createDigitalisatModelFromTEIBibl(biblList.get(0))
        .orElse(null);
    assertNotNull(digitalisatViewModel);

    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder("1").build();
    List<KodDigitalisat> success = new LinkedList<>();

    ImportVorgang importVorgang = ObjectMapperFactory.getObjectMapper()
        .readValue(importObject.getContent(), ImportVorgang.class);

    when(importVorgangBoundary.getImportVorgang(message)).thenReturn(importVorgang);

    success.add(new KodDigitalisat(kulturObjektDokument, digitalisatViewModel));

    service.saveToSystem(message, success);

    verify(kulturObjektDokumentBoundaryMock, times(1))
        .digitalisateHinzufuegen(eq(kulturObjektDokument.getId()),
            eq(List.of(digitalisatViewModel)));
    verify(importVorgangBoundary, times(1)).sendImportJobToKafka(importVorgang);

    assertEquals(IMPORTJOB_RESULT_VALUES.SUCCESS, importVorgang.getStatus());
    assertEquals(1, importVorgang.getDataEntities().size());

    assertEquals(digitalisatViewModel.getId(), importVorgang.getDataEntities().get(0).getId());
  }

  @Test
  void testOnImportMessageWithKODNotFound() throws Exception {
    NormdatenReferenzBoundary normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);
    ImportVorgangBoundary importVorgangBoundaryMock = mock(ImportVorgangBoundary.class);

    KulturObjektDokumentBoundary kulturObjektDokumentBoundaryMock = mock(
        KulturObjektDokumentBoundary.class);

    NormdatenReferenz eigentuemer = createBSB();
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(eigentuemer.getGndID(),
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .thenReturn(Optional.of(eigentuemer));

    NormdatenReferenz aufbewahrungsOrt = createMuenchen();
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(aufbewahrungsOrt.getGndID(),
        NormdatenReferenz.ORT_TYPE_NAME))
        .thenReturn(Optional.of(aufbewahrungsOrt));

    DigitalisatImportService service = new DigitalisatImportService(
        kulturObjektDokumentBoundaryMock,
        kulturObjektDokumentRepository,
        normdatenReferenzBoundaryMock,
        importVorgangBoundaryMock,
        dokumentSperreService,
        "http://localhost:8080/");
    DigitalisatImport digitalisatImport = new DigitalisatImport(message,
        new Bearbeiter("1", "Unbekannter Tester"));

    ImportVorgang importVorgang = ObjectMapperFactory.getObjectMapper()
        .readValue(importObject.getContent(), ImportVorgang.class);
    importVorgang.getImportFiles().get(0).getDataEntityList().clear();

    when(importVorgangBoundaryMock.getImportVorgang(message)).thenReturn(importVorgang);

    when(normdatenReferenzBoundaryMock.findConceptWithSchemeTypeAndURI("completeness",
        "http://normdaten.staatsbibliothek-berlin.de/vocabulary/completeness/complete"))
        .thenReturn(Optional.of(new NormdatenReferenz.NormdatenReferenzBuilder().withId("NORM-123")
            .withTypeName("completeness").withName("complete").withUri(
                "http://normdaten.staatsbibliothek-berlin.de/vocabulary/completeness/complete")
            .build()));

    service.onImportMessage(digitalisatImport);
    TimeUnit.MILLISECONDS.sleep(300L);

    verify(importVorgangBoundaryMock, times(1)).sendFailureMessageToImport(eq(message),
        eq("Kein KOD gefunden zu KOD ID oder Signatur Mus.ms. A I(1,"
            + " Aufbewahrungsort München, besitzende Einrichtung Bayerische Staatsbibliothek"));
  }

  @Test
  void testOnImportMessageWithKODCreated() throws Exception {
    NormdatenReferenzBoundary normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);
    ImportVorgangBoundary importVorgangBoundaryMock = mock(ImportVorgangBoundary.class);

    KulturObjektDokumentRepository kulturObjektDokumentRepositoryMock = mock(
        KulturObjektDokumentRepository.class);
    KulturObjektDokumentBoundary kulturObjektDokumentBoundaryMock = mock(
        KulturObjektDokumentBoundary.class);

    DigitalisatImportService service = new DigitalisatImportService(
        kulturObjektDokumentBoundaryMock,
        kulturObjektDokumentRepositoryMock,
        normdatenReferenzBoundaryMock,
        importVorgangBoundaryMock, dokumentSperreService,
        "http://localhost:8080/");
    DigitalisatImport digitalisatImport = new DigitalisatImport(message,
        new Bearbeiter("1", "Unbekannter Tester"));

    ImportVorgang importVorgang = ObjectMapperFactory.getObjectMapper()
        .readValue(importObject.getContent(), ImportVorgang.class);
    importVorgang.getImportFiles().get(0).getDataEntityList().clear();

    NormdatenReferenz eigentuemer = createBSB();
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(eigentuemer.getGndID(),
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .thenReturn(Optional.of(eigentuemer));

    NormdatenReferenz aufbewahrungsOrt = createMuenchen();
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(aufbewahrungsOrt.getGndID(),
        NormdatenReferenz.ORT_TYPE_NAME))
        .thenReturn(Optional.of(aufbewahrungsOrt));

    when(normdatenReferenzBoundaryMock.findConceptWithSchemeTypeAndURI("completeness",
        "http://normdaten.staatsbibliothek-berlin.de/vocabulary/completeness/complete"))
        .thenReturn(Optional.of(new NormdatenReferenz.NormdatenReferenzBuilder().withId("NORM-123")
            .withTypeName("completeness").withName("complete").withUri(
                "http://normdaten.staatsbibliothek-berlin.de/vocabulary/completeness/complete")
            .build()));

    Identifikation identifikation = new IdentifikationBuilder()
        .withIdent("Mus.ms. A I(1")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withAufbewahrungsOrt(new NormdatenReferenz("10", "Berlin", "4005728-8"))
        .withBesitzer(eigentuemer)
        .build();

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument.KulturObjektDokumentBuilder(
        "1")
        .withTEIXml("TEI")
        .withGueltigerIdentifikation(identifikation)
        .build();

    when(importVorgangBoundaryMock.getImportVorgang(message)).thenReturn(importVorgang);
    when(kulturObjektDokumentRepositoryMock.findByIdentifikationIdent("Mus.ms. A I(1"))
        .thenReturn(List.of(kulturObjektDokument));

    service.onImportMessage(digitalisatImport);
    TimeUnit.MILLISECONDS.sleep(300L);

    verify(importVorgangBoundaryMock, times(1)).sendFailureMessageToImport(eq(message),
        eq("Kein KOD gefunden zu KOD ID oder Signatur Mus.ms. A I(1,"
            + " Aufbewahrungsort München, besitzende Einrichtung Bayerische Staatsbibliothek"));
  }

  @Test
  @TestTransaction
  void testImportAllDigitalisate() throws Exception {
    NormdatenReferenzBoundary normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);
    ImportVorgangBoundary importVorgangBoundaryMock = mock(ImportVorgangBoundary.class);

    DigitalisatImportService service = new DigitalisatImportService(kulturObjektDokumentBoundary,
        kulturObjektDokumentRepository,
        normdatenReferenzBoundaryMock,
        importVorgangBoundaryMock,
        mock(DokumentSperreService.class),
        "http://localhost:8080/");

    DigitalisatImport digitalisatImport = new DigitalisatImport(message,
        new Bearbeiter("1", "Unbekannter Tester"));

    ImportVorgang importVorgang = ObjectMapperFactory.getObjectMapper()
        .readValue(importObject.getContent(), ImportVorgang.class);
    importVorgang.getImportFiles().get(0).getDataEntityList().clear();

    NormdatenReferenz eigentuemer = createBSB();
    normdatenReferenzRepository.saveAndFlush(eigentuemer);
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(eigentuemer.getGndID(),
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .thenReturn(Optional.of(eigentuemer));

    NormdatenReferenz aufbewahrungsOrt = createMuenchen();
    normdatenReferenzRepository.saveAndFlush(aufbewahrungsOrt);
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(aufbewahrungsOrt.getGndID(),
        NormdatenReferenz.ORT_TYPE_NAME))
        .thenReturn(Optional.of(aufbewahrungsOrt));

    NormdatenReferenz digitalisierer = new NormdatenReferenz(
        "6790851b-9519-3874-a9fd-0839d494a3c9",
        "Staatsbibliothek zu Berlin",
        "5036103-X",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);
    normdatenReferenzRepository.saveAndFlush(digitalisierer);
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(digitalisierer.getGndID(),
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .thenReturn(Optional.of(digitalisierer));

    NormdatenReferenz digitalisierungsOrt = new NormdatenReferenz(
        "ee1611b6-1f56-38e7-8c12-b40684dbb395",
        "Berlin",
        "4005728-8",
        NormdatenReferenz.ORT_TYPE_NAME);
    normdatenReferenzRepository.saveAndFlush(digitalisierungsOrt);
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(digitalisierungsOrt.getGndID(),
        NormdatenReferenz.ORT_TYPE_NAME))
        .thenReturn(Optional.of(digitalisierungsOrt));

    Identifikation identifikation = new IdentifikationBuilder()
        .withIdent("Mus.ms. A I(1")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .withBesitzer(eigentuemer)
        .build();

    Identifikation altIdentifikation = new IdentifikationBuilder()
        .withIdent("Mus ms A I 1")
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .withBesitzer(eigentuemer)
        .build();

    File kodFile = Paths.get("src", "test", "resources", "kulturobjekteimport.xml").toFile();

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument
        .KulturObjektDokumentBuilder("1")
        .withGueltigerIdentifikation(identifikation)
        .addAlternativeIdentifikation(altIdentifikation)
        .withTEIXml(FileUtils.readFileToString(kodFile, StandardCharsets.UTF_8))
        .build();

    ((KulturObjektDokumentService) kulturObjektDokumentBoundary)
        .setDistributedSaveCommand(mock(DistributedSaveKODCommand.class));
    kulturObjektDokumentRepository.save(kulturObjektDokument);

    when(importVorgangBoundaryMock.getImportVorgang(message)).thenReturn(importVorgang);

    when(normdatenReferenzBoundaryMock.findConceptWithSchemeTypeAndURI("completeness",
        "http://normdaten.staatsbibliothek-berlin.de/vocabulary/completeness/complete"))
        .thenReturn(Optional.of(new NormdatenReferenz.NormdatenReferenzBuilder().withId("NORM-123")
            .withTypeName("completeness").withName("complete").withUri(
                "http://normdaten.staatsbibliothek-berlin.de/vocabulary/completeness/complete")
            .build()));

    service.onImportMessage(digitalisatImport);

    Thread.sleep(500);

    assertEquals(IMPORTJOB_RESULT_VALUES.SUCCESS, importVorgang.getStatus());

    kulturObjektDokument = kulturObjektDokumentRepository.findById("1");

    assertNotNull(kulturObjektDokument);
    assertEquals(3, kulturObjektDokument.getDigitalisate().size());
  }

  @Test
  void testupdateDigitalisatsTypKonzeptWithNormdatenservice() {

    NormdatenReferenzBoundary normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);
    ImportVorgangBoundary importVorgangBoundaryMock = mock(ImportVorgangBoundary.class);

    DigitalisatImportService service = new DigitalisatImportService(kulturObjektDokumentBoundary,
        kulturObjektDokumentRepository,
        normdatenReferenzBoundaryMock,
        importVorgangBoundaryMock,
        mock(DokumentSperreService.class),
        "http://localhost:8080/");

    DigitalisatViewModel digitalisatViewModel = DigitalisatViewModel.builder().id("HSP-1212")
        .digitalisatTypKonzepte(
            new ArrayList<>(Collections.singletonList(
                new NormdatenReferenzBuilder().withTypeName("completeness")
                    .withUri(
                        "http://normdaten.staatsbibliothek-berlin.de/vocabulary/completeness/complete")
                    .build()))).build();

    final NormdatenReferenz normdatenReferenz = new NormdatenReferenzBuilder().withId("NORM-123")
        .withTypeName("completeness").withName("complete").withUri(
            "https://normdaten.staatsbibliothek-berlin.de/vocabulary/completeness/complete")
        .build();
    when(normdatenReferenzBoundaryMock.findConceptWithSchemeTypeAndURI("completeness",
        "http://normdaten.staatsbibliothek-berlin.de/vocabulary/completeness/complete"))
        .thenReturn(Optional.of(normdatenReferenz));

    assertFalse(digitalisatViewModel.getDigitalisatTypKonzepte().contains(normdatenReferenz));

    service.updateDigitalisatsTypKonzeptWithNormdatenservice(digitalisatViewModel);

    assertTrue(digitalisatViewModel.getDigitalisatTypKonzepte().contains(normdatenReferenz));
  }

  @Test
  void testsetDigitalizationTypesAsConcepts() {
    NormdatenReferenzBoundary normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);
    ImportVorgangBoundary importVorgangBoundaryMock = mock(ImportVorgangBoundary.class);

    DigitalisatImportService service = new DigitalisatImportService(kulturObjektDokumentBoundary,
        kulturObjektDokumentRepository,
        normdatenReferenzBoundaryMock,
        importVorgangBoundaryMock,
        mock(DokumentSperreService.class),
        "http://localhost:8080/");
    DigitalisatViewModel digitalisatViewModel = DigitalisatViewModel.builder()
        .id("HSP-1212")
        .build();

    Ref refDigitalisatTyp = new Ref();
    refDigitalisatTyp.setType("completeness");
    refDigitalisatTyp.getTargets()
        .add("http://normdaten.staatsbibliothek-berlin.de/vocabulary/completeness/complete");

    DigitalisatImportService.setDigitalizationTypesAsConcepts(List.of(refDigitalisatTyp),
        digitalisatViewModel);

    assertEquals(1, digitalisatViewModel.getDigitalisatTypKonzepte().size());
  }

  @Test
  void testCheckIfIdentifikationIsEqual() {
    NormdatenReferenzBoundary normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);
    DigitalisatImportService service = new DigitalisatImportService(
        mock(KulturObjektDokumentBoundary.class),
        mock(KulturObjektDokumentRepository.class),
        normdatenReferenzBoundaryMock,
        mock(ImportVorgangBoundary.class),
        mock(DokumentSperreService.class),
        "http://localhost:8080/");

    NormdatenReferenz ort = null;

    NormdatenReferenz koerperschaft = null;

    Set<Identifikation> identifikationen = Set.of(new IdentifikationBuilder()
        .withAufbewahrungsOrt(new NormdatenReferenz("123", "Berlin", "4005728-8"))
        .withBesitzer(new NormdatenReferenz("456", "Staatsbibliothek zu Berlin", "5036103-X"))
        .build());

    assertFalse(service.checkIfIdentifikationIsEqual(identifikationen, ort, koerperschaft));

    ort = new NormdatenReferenz.NormdatenReferenzBuilder().build();
    koerperschaft = new NormdatenReferenz.NormdatenReferenzBuilder().build();

    assertFalse(service.checkIfIdentifikationIsEqual(identifikationen, ort, koerperschaft));

    ort = new NormdatenReferenzBuilder()
        .withId("123")
        .build();
    koerperschaft = new NormdatenReferenzBuilder()
        .withId("456")
        .build();
    assertTrue(service.checkIfIdentifikationIsEqual(identifikationen, ort, koerperschaft));
  }

  @Test
  void testfindDigitalisierendeEinrichtung() throws Exception {
    NormdatenReferenzBoundary normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);
    NormdatenReferenz sbb = createSBB();
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(sbb.getGndID(),
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .thenReturn(Optional.of(sbb));

    NormdatenReferenz bsb = createBSB();
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(bsb.getGndID(),
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .thenReturn(Optional.of(bsb));

    DigitalisatImportService service = new DigitalisatImportService(
        mock(KulturObjektDokumentBoundary.class),
        mock(KulturObjektDokumentRepository.class),
        normdatenReferenzBoundaryMock,
        mock(ImportVorgangBoundary.class),
        mock(DokumentSperreService.class),
        "http://localhost:8080/");

    List<TEI> allTEI = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    List<Bibl> biblList = new ArrayList<>();
    TEICommon.findAll(Bibl.class, allTEI.get(0), biblList);

    assertEquals(3, biblList.size());

    assertTrue(service.findDigitalisierendeEinrichtung(biblList.get(0)).isPresent());
    assertEquals("2031351-2",
        service.findDigitalisierendeEinrichtung(biblList.get(0)).get().getGndID());

    assertTrue(service.findDigitalisierendeEinrichtung(biblList.get(1))
        .isPresent());
    assertEquals("5036103-X",
        service.findDigitalisierendeEinrichtung(biblList.get(1)).get()
            .getGndID());
  }


  @Test
  void testFindNoDigitalisierendeEinrichtung() throws Exception {
    NormdatenReferenzBoundary normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType("5036103-X",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .thenReturn(Optional.empty());

    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType("2031351-2",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .thenReturn(Optional.empty());

    DigitalisatImportService service = new DigitalisatImportService(
        mock(KulturObjektDokumentBoundary.class),
        mock(KulturObjektDokumentRepository.class),
        normdatenReferenzBoundaryMock,
        mock(ImportVorgangBoundary.class),
        mock(DokumentSperreService.class),
        "http://localhost:8080/");

    List<TEI> allTEI = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    List<Bibl> biblList = new ArrayList<>();
    TEICommon.findAll(Bibl.class, allTEI.get(0), biblList);

    assertEquals(3, biblList.size());

    assertFalse(service.findDigitalisierendeEinrichtung(biblList.get(0)).isPresent());
    assertFalse(service.findDigitalisierendeEinrichtung(biblList.get(0)).isPresent());

    assertFalse(service.findDigitalisierendeEinrichtung(biblList.get(1)).isPresent());
    assertFalse(service.findDigitalisierendeEinrichtung(biblList.get(1)).isPresent());

  }

  @Test
  void testFindDigitalisierungsOrt() throws Exception {
    NormdatenReferenzBoundary normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);

    NormdatenReferenz aufbewahrungsOrt = createMuenchen();
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(aufbewahrungsOrt.getGndID(),
        NormdatenReferenz.ORT_TYPE_NAME))
        .thenReturn(Optional.of(aufbewahrungsOrt));

    NormdatenReferenz digitalisierungsOrt = createBerlin();
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(digitalisierungsOrt.getGndID(),
        NormdatenReferenz.ORT_TYPE_NAME))
        .thenReturn(Optional.of(digitalisierungsOrt));

    DigitalisatImportService service = new DigitalisatImportService(
        mock(KulturObjektDokumentBoundary.class),
        mock(KulturObjektDokumentRepository.class),
        normdatenReferenzBoundaryMock,
        mock(ImportVorgangBoundary.class),
        mock(DokumentSperreService.class),
        "http://localhost:8080/");

    List<TEI> allTEI = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    List<Bibl> biblList = new ArrayList<>();
    TEICommon.findAll(Bibl.class, allTEI.get(0), biblList);

    assertEquals(3, biblList.size());

    assertTrue(service.findDigitalisierungsOrt(biblList.get(0)).isPresent());
    assertEquals("4127793-4", service.findDigitalisierungsOrt(biblList.get(0)).get().getGndID());

    assertTrue(service.findDigitalisierungsOrt(biblList.get(1)).isPresent());
    assertEquals("4005728-8",
        service.findDigitalisierungsOrt(biblList.get(1)).get().getGndID());
  }

  @Test
  @TestTransaction
  void testparseMsDescToCreateKODViewModelAndDigitalisatViewModel() throws Exception {
    NormdatenReferenzBoundary normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);
    DigitalisatImportService service = new DigitalisatImportService(
        kulturObjektDokumentBoundary,
        kulturObjektDokumentRepository,
        normdatenReferenzBoundaryMock,
        mock(ImportVorgangBoundary.class),
        mock(DokumentSperreService.class),
        "http://localhost:8080/");
    Path teiFilePath = Paths
        .get("src", "test", "resources", "03_Ansbach_SchlossB.xml");
    List<TEI> allTEI = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    List<MsDesc> msDescs = new ArrayList<>();
    TEICommon.findAll(MsDesc.class, allTEI.get(0), msDescs);
    NormdatenReferenz aufbewahrungsOrt = createAnsbach();
    normdatenReferenzRepository.saveAndFlush(aufbewahrungsOrt);
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(aufbewahrungsOrt.getGndID(),
        NormdatenReferenz.ORT_TYPE_NAME))
        .thenReturn(Optional.of(aufbewahrungsOrt));

    NormdatenReferenz eigentuemer = createAnsbachSB();
    normdatenReferenzRepository.saveAndFlush(eigentuemer);
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(eigentuemer.getGndID(),
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .thenReturn(Optional.of(eigentuemer));

    Identifikation identifikation = new IdentifikationBuilder()
        .withIdent("Ms. lat. 56")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .withBesitzer(eigentuemer)
        .build();

    Identifikation altIdentifikation = new IdentifikationBuilder()
        .withIdent("Ms. lat. 51")
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .withBesitzer(eigentuemer)
        .build();
    File kodFile = Paths.get("src", "test", "resources", "kulturobjekteimport.xml").toFile();
    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument
        .KulturObjektDokumentBuilder("1")
        .withGueltigerIdentifikation(identifikation)
        .addAlternativeIdentifikation(altIdentifikation)
        .withTEIXml(FileUtils.readFileToString(kodFile, StandardCharsets.UTF_8))
        .build();

    NormdatenReferenz digitalisierungsOrt = createMuenchen();
    normdatenReferenzRepository.saveAndFlush(digitalisierungsOrt);
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(digitalisierungsOrt.getGndID(),
        NormdatenReferenz.ORT_TYPE_NAME))
        .thenReturn(Optional.of(digitalisierungsOrt));

    NormdatenReferenz digitalisierer = createBSB();
    normdatenReferenzRepository.saveAndFlush(digitalisierer);
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(digitalisierer.getGndID(),
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME))
        .thenReturn(Optional.of(digitalisierer));

    ((KulturObjektDokumentService) kulturObjektDokumentBoundary)
        .setSuchDokumentService(mock(SuchDokumentService.class));
    kulturObjektDokumentRepository.save(kulturObjektDokument);

    List<KodDigitalisat> result =
        service.parseMsDescToCreateKODViewModelAndDigitalisatViewModel(msDescs);

    assertEquals(2, result.size());
  }

  private NormdatenReferenz createBSB() {
    return new NormdatenReferenz(
        "0050265e-e21d-3652-b781-e384f8341141",
        "Bayerische Staatsbibliothek",
        "2031351-2",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);
  }

  private NormdatenReferenz createAnsbachSB() {
    return new NormdatenReferenz(
        "0050265e-e21d-3652-b781-e384f8341141",
        "Staatliche Bibliothek Ansbach",
        "137652-4",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);
  }

  private NormdatenReferenz createSBB() {
    return new NormdatenReferenz(
        "774909e2-f687-30cb-a5c4-ddc95806d6be",
        "Staatsbibliothek zu Berlin",
        "5036103-X",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);
  }

  private NormdatenReferenz createMuenchen() {
    return new NormdatenReferenz(
        "d0a6343a-081a-3335-baa9-65ce4fd0845c",
        "München",
        "4127793-4",
        NormdatenReferenz.ORT_TYPE_NAME);
  }

  private NormdatenReferenz createAnsbach() {
    return new NormdatenReferenz(
        "d0a6343a-081a-3335-baa9-65ce4fd0845D",
        "Ansbach",
        "4002170-1",
        NormdatenReferenz.ORT_TYPE_NAME);
  }

  private NormdatenReferenz createBerlin() {
    return new NormdatenReferenz(
        "ee1611b6-1f56-38e7-8c12-b40684dbb395",
        "Berlin",
        "4005728-8",
        NormdatenReferenz.ORT_TYPE_NAME);
  }

}
