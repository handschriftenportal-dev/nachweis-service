package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodimport;

import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportVorgangBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportFile;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportVorgang;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.tei_c.ns._1.MsIdentifier;
import org.tei_c.ns._1.TEI;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 09.07.21
 */
@QuarkusTest
public class KulturObjektDokumentImportServiceTest {

  private static final String TEI_FILENAME = "kulturobjekteimport.xml";
  private static final Path TEI_FILE_PATH = Paths.get("src", "test", "resources", TEI_FILENAME);

  @Test
  void testExtractTEIFromMessage() throws Exception {
    KulturObjektDokumentImportService service = new KulturObjektDokumentImportService(
        mock(KulturObjektDokumentBoundary.class),
        mock(NormdatenReferenzBoundary.class),
        mock(ImportVorgangBoundary.class), mock(BearbeiterBoundary.class),
        "http://localhost:8080/");

    ActivityStreamObject importObject = buildImportObject();
    ActivityStreamMessageDTO message = buildMessage(importObject);

    List<TEI> teis = service.extractTEIFromMessage(message);

    assertNotNull(teis);
    assertEquals(1, teis.size());
  }

  @Test
  void testFindAllMsIdentifiers() throws Exception {
    KulturObjektDokumentImportService service = new KulturObjektDokumentImportService(
        mock(KulturObjektDokumentBoundary.class),
        mock(NormdatenReferenzBoundary.class),
        mock(ImportVorgangBoundary.class), mock(BearbeiterBoundary.class),
        "http://localhost:8080/");

    List<TEI> allTEI = TEIObjectFactory.unmarshal(newInputStream(TEI_FILE_PATH));
    List<MsIdentifier> identifiers = service.findAllMsIdentifiers(allTEI);

    assertEquals(2, identifiers.size());
  }

  @Test
  void testFindImportFileForActivityStreamObject() throws Exception {
    KulturObjektDokumentImportService service = new KulturObjektDokumentImportService(
        mock(KulturObjektDokumentBoundary.class),
        mock(NormdatenReferenzBoundary.class),
        mock(ImportVorgangBoundary.class), mock(BearbeiterBoundary.class),
        "http://localhost:8080/");

    ActivityStreamObject importObject = buildImportObject();
    ActivityStreamMessageDTO message = buildMessage(importObject);

    ImportVorgang importVorgang = readImportVorgang(importObject);

    ActivityStreamObjectDTO activityStreamObject = message.getObjects().stream().findFirst().get();

    ImportFile importFile = service.findImportFileForActivityStreamObject(importVorgang,
        activityStreamObject);
    assertNotNull(importFile);
  }

  @Test
  @TestTransaction
  void testSendFailureMessageToImport() throws Exception {
    ActivityStreamObject importObject = buildImportObject();
    ActivityStreamMessageDTO message = buildMessage(importObject);

    ImportVorgang importVorgang = readImportVorgang(importObject);

    ImportVorgangBoundary importVorgangBoundaryMock = mock(ImportVorgangBoundary.class);
    when(
        importVorgangBoundaryMock.getImportVorgang(any(ActivityStreamMessageDTO.class))).thenReturn(
        importVorgang);

    KulturObjektDokumentImportService service = new KulturObjektDokumentImportService(
        mock(KulturObjektDokumentBoundary.class),
        mock(NormdatenReferenzBoundary.class),
        importVorgangBoundaryMock, mock(BearbeiterBoundary.class),
        "http://localhost:8080/");

    service.sendFailureMessageToImport(message, "Test-Error");

    assertEquals(IMPORTJOB_RESULT_VALUES.FAILED, importVorgang.getStatus());
    verify(importVorgangBoundaryMock, times(1)).sendImportJobToKafka(any(ImportVorgang.class));
  }

  @Test
  @TestTransaction
  void testSendSuccessMessageToImport() throws Exception {
    ImportVorgangBoundary importVorgangBoundaryMock = mock(ImportVorgangBoundary.class);

    KulturObjektDokumentImportService service = new KulturObjektDokumentImportService(
        mock(KulturObjektDokumentBoundary.class),
        mock(NormdatenReferenzBoundary.class),
        importVorgangBoundaryMock, mock(BearbeiterBoundary.class),
        "http://localhost:8080/");

    ActivityStreamObject importObject = buildImportObject();
    ActivityStreamMessageDTO message = buildMessage(importObject);

    ImportVorgang importVorgang = readImportVorgang(importObject);

    service.sendSuccessMessageToImport(message, Arrays.asList(), importVorgang);

    assertEquals(IMPORTJOB_RESULT_VALUES.SUCCESS, importVorgang.getStatus());
    verify(importVorgangBoundaryMock, times(1)).sendImportJobToKafka(any(ImportVorgang.class));

  }

  @ParameterizedTest
  @MethodSource("sortKeys")
  @TestTransaction
  void testOnImportMessageWithNormdaten(String sortKey, String expectedSortKey) throws Exception {
    ActivityStreamObject importObject = buildImportObject();
    ActivityStreamMessageDTO message = buildMessage(importObject, sortKey);

    ImportVorgang importVorgang = readImportVorgang(importObject);

    ImportVorgangBoundary importVorgangBoundaryMock = mock(ImportVorgangBoundary.class);
    when(
        importVorgangBoundaryMock.getImportVorgang(any(ActivityStreamMessageDTO.class))).thenReturn(
        importVorgang);

    KulturObjektDokumentBoundary kulturObjektDokumentBoundaryMock = mock(
        KulturObjektDokumentBoundary.class);
    ArgumentCaptor<List<KulturObjektDokument>> captureRegistrieren = ArgumentCaptor.forClass(
        List.class);

    NormdatenReferenzBoundary normdatenReferenzBoundary = mock(NormdatenReferenzBoundary.class);

    when(normdatenReferenzBoundary.findOneByIdOrNameAndType("4004968-1",
        NormdatenReferenz.ORT_TYPE_NAME))
        .thenReturn(Optional.of(new NormdatenReferenz("4004968-1", "Bautzen", "4004968-1",
            NormdatenReferenz.ORT_TYPE_NAME)));

    when(normdatenReferenzBoundary.findOneByIdOrNameAndType("1027608000",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME)).thenReturn(
        Optional.of(
            new NormdatenReferenz("1027608000", "Domstiftsbibliothek St. Petri", "1027608000",
                NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME)));

    BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);

    KulturObjektDokumentImportService service = new KulturObjektDokumentImportService(
        kulturObjektDokumentBoundaryMock,
        normdatenReferenzBoundary,
        importVorgangBoundaryMock, bearbeiterBoundary,
        "http://localhost:8080/");

    when(bearbeiterBoundary.findBearbeiterWithName(any())).thenReturn(
        new Bearbeiter("1", "Unbekannter Tester"));

    KulturObjektDokumentImport kodImport = new KulturObjektDokumentImport(message);

    service.onImportMessage(kodImport);

    verify(importVorgangBoundaryMock, times(1)).sendImportJobToKafka(any(ImportVorgang.class));

    assertEquals(IMPORTJOB_RESULT_VALUES.SUCCESS, importVorgang.getStatus());

    verify(kulturObjektDokumentBoundaryMock).registrieren(any(Bearbeiter.class),
        captureRegistrieren.capture());
    List<KulturObjektDokument> registrierteKODs = captureRegistrieren.getValue();
    assertNotNull(registrierteKODs);
    assertEquals(2, registrierteKODs.size());

    KulturObjektDokument ersterKod = registrierteKODs.getFirst();
    assertEquals("M II 104", ersterKod.getGueltigeSignatur());
    assertEquals(expectedSortKey, ersterKod.getSortKey());
    assertEquals(List.of("M II 104 alt1", "M II 104 alt2"),
        ersterKod.getAlternativeIdentifikationen().stream()
            .map(Identifikation::getIdent).toList());

    KulturObjektDokument zweiterKod = registrierteKODs.get(1);
    assertEquals("M I 4", zweiterKod.getGueltigeSignatur());
    assertEquals(expectedSortKey, zweiterKod.getSortKey());
    assertTrue(zweiterKod.getAlternativeIdentifikationen().isEmpty());
  }

  @Test
  @TestTransaction
  void testOnImportMessageWithNoNormdatenOrtFound() throws Exception {
    ActivityStreamObject importObject = buildImportObject();
    ActivityStreamMessageDTO message = buildMessage(importObject);

    ImportVorgang importVorgang = readImportVorgang(importObject);

    ImportVorgangBoundary importVorgangBoundaryMock = mock(ImportVorgangBoundary.class);
    when(
        importVorgangBoundaryMock.getImportVorgang(any(ActivityStreamMessageDTO.class))).thenReturn(
        importVorgang);

    KulturObjektDokumentBoundary kulturObjektDokumentBoundaryMock = mock(
        KulturObjektDokumentBoundary.class);

    NormdatenReferenzBoundary normdatenReferenzBoundary = mock(NormdatenReferenzBoundary.class);

    KulturObjektDokumentImportService service = new KulturObjektDokumentImportService(
        kulturObjektDokumentBoundaryMock,
        normdatenReferenzBoundary,
        importVorgangBoundaryMock, mock(BearbeiterBoundary.class),
        "http://localhost:8080/");

    KulturObjektDokumentImport kodImport = new KulturObjektDokumentImport(message);

    service.onImportMessage(kodImport);

    verify(importVorgangBoundaryMock, times(1)).sendImportJobToKafka(any(ImportVorgang.class));

    assertEquals(IMPORTJOB_RESULT_VALUES.FAILED, importVorgang.getStatus());

    assertEquals("Keine NormdatenReferenz Ort gefunden!", importVorgang.getFehler());

    verify(kulturObjektDokumentBoundaryMock, never()).registrieren(any(), any());
  }

  static Stream<Arguments> sortKeys() {
    return Stream.of(
        Arguments.of("test_00104", "test_00104"),
        Arguments.of(null, null),
        Arguments.of("", null),
        Arguments.of("   ", null));
  }

  ActivityStreamObject buildImportObject() throws ActivityStreamsException {
    return ActivityStreamObject.builder()
        .withCompressed(true)
        .withType(ActivityStreamsDokumentTyp.IMPORT)
        .withName(TEI_FILENAME)
        .withId("1")
        .withContent(
            "{\"id\":\"8a706deb-c70f-4c78-a16a-d20e96289399\",\"creationDate\":\"2021-07-09T11:51:23.902777400\",\"benutzerName\":\"Redakteur\",\"importFiles\":[{\"id\":\"c1603c89-0a24-4ffb-924f-2958ec3796ee\",\"path\":\"file:///tmp/hsp-import-/2021.07.09.11.51.23.900/kulturobjekteimport.xml\",\"dateiTyp\":\"kulturobjekteimport.xml\",\"dateiName\":\""
                + TEI_FILENAME
                + "\",\"dateiFormat\":\"TEI\",\"error\":false,\"message\":null,\"importEntityData\":[]}],\"name\":\"kulturobjekteimport.xml\",\"importDir\":\"/tmp/hsp-import-/2021.07.09.11.51.23.900\",\"result\":\"NO_RESULT\",\"errorMessage\":\"\",\"datatype\":\"KOD\"}")
        .build();
  }

  ActivityStreamMessageDTO buildMessage(ActivityStreamObject importObject)
      throws Exception {
    return buildMessage(importObject, null);
  }

  ActivityStreamMessageDTO buildMessage(ActivityStreamObject importObject, String sortKey)
      throws Exception {

    try (InputStream is = newInputStream(TEI_FILE_PATH)) {
      List<TEI> allTEI = TEIObjectFactory.unmarshal(is);
      TEI tei = allTEI.getFirst();
      List<MsIdentifier> identifiers = new ArrayList<>();
      TEICommon.findAll(MsIdentifier.class, tei, identifiers);
      identifiers.forEach(identifier -> identifier.getIdnos().getFirst().setSortKey(sortKey));

      ActivityStreamObject activityStreamObject = ActivityStreamObject.builder()
          .withContent(tei)
          .withName(TEI_FILENAME)
          .withId(TEI_FILENAME)
          .withType(ActivityStreamsDokumentTyp.KOD)
          .build();

      return new ActivityStreamMessageDTO(ActivityStream.builder()
          .withId(TEI_FILENAME)
          .withPublished(LocalDateTime.now())
          .withType(ActivityStreamAction.ADD)
          .addObject(activityStreamObject)
          .addObject(importObject)
          .build());
    }
  }

  ImportVorgang readImportVorgang(ActivityStreamObject importObject)
      throws ActivityStreamsException, IOException {
    ImportVorgang importVorgang = ObjectMapperFactory.getObjectMapper()
        .readValue(importObject.getContent(),
            ImportVorgang.class);
    assertNotNull(importVorgang);
    return importVorgang;
  }

}
