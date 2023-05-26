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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodimport;

import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportFile;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgang;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgangBoundary;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
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

  @Test
  @TestTransaction
  void testOnImportMessageWithNormdaten() throws Exception {
    ActivityStreamObject importObject = buildImportObject();
    ActivityStreamMessageDTO message = buildMessage(importObject);

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
      throws ActivityStreamsException, IOException, JAXBException {

    try (InputStream is = newInputStream(TEI_FILE_PATH)) {
      List<TEI> allTEI = TEIObjectFactory.unmarshal(is);
      TEI tei = allTEI.get(0);
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
