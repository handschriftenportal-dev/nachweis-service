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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.katalogimport;

import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportFile;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgang;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgangBoundary;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.tei_c.ns._1.TEI;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 01.03.22
 */
@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
public class KatalogImportServiceTest {

  private static final String TEI_FILENAME = "tei-katalog_Biberach.xml";
  private static final Path teiFilePath = Paths.get("src", "test", "resources", TEI_FILENAME);

  TEI tei;
  ActivityStreamMessageDTO message;
  ActivityStreamObject importObject;
  Bearbeiter bearbeiter;

  KatalogBoundary katalogBoundaryMock;
  BearbeiterBoundary bearbeiterBoundaryMock;
  ImportVorgangBoundary importVorgangBoundaryMock;
  KatalogImportService katalogImportService;

  @BeforeEach
  void init() throws Exception {
    try (InputStream is = newInputStream(teiFilePath)) {
      tei = TEIObjectFactory.unmarshalOne(is).orElseThrow();
    }

    ActivityStreamObject activityStreamObject = ActivityStreamObject.builder()
        .withContent(tei)
        .withName(TEI_FILENAME)
        .withId(TEI_FILENAME)
        .withType(ActivityStreamsDokumentTyp.KATALOG)
        .build();

    importObject = ActivityStreamObject.builder()
        .withCompressed(false)
        .withType(ActivityStreamsDokumentTyp.IMPORT)
        .withName(TEI_FILENAME)
        .withId("1")
        .withContent(
            "{\"id\":\"1c8050b1-616c-4454-b441-9281a57bc99d\",\"creationDate\":\"2020-09-24T15:22:15.926333\",\"benutzerName\":\"Redakteur\",\"importFiles\":[{\"id\":\"bec5a8c1-4e01-4d82-b19f-8189bb6b8a12\",\"path\":\"file:///tmp/hsp-import-/2020.09.24.15.22.15.925/tei-katalog_Aurich-wthout-BOM-translated.xml\",\"dateiTyp\":\"ms_1-wthout-BOM.xml\",\"dateiName\":\""
                + TEI_FILENAME
                + "\",\"dateiFormat\":\"TEI\",\"error\":false,\"message\":null,\"importEntityData\":[]}],\"name\":\"ms_1.xml\",\"importDir\":\"/tmp/hsp-import-/2020.09.24.15.22.15.925\",\"result\":\"NO_RESULT\",\"errorMessage\":\"\",\"datatype\":\"KATALOG\"}")
        .build();
    message = new ActivityStreamMessageDTO(ActivityStream.builder()
        .withId(TEI_FILENAME)
        .withPublished(LocalDateTime.now())
        .withType(ActivityStreamAction.ADD)
        .addObject(activityStreamObject)
        .addObject(importObject)
        .build());

    bearbeiter = new Bearbeiter("1", "Unbekannter Tester");

    katalogBoundaryMock = Mockito.mock(KatalogBoundary.class);

    bearbeiterBoundaryMock = Mockito.mock(BearbeiterBoundary.class);
    when(bearbeiterBoundaryMock.findBearbeiterWithName(any())).thenReturn(bearbeiter);

    importVorgangBoundaryMock = Mockito.mock(ImportVorgangBoundary.class);

    katalogImportService = new KatalogImportService(katalogBoundaryMock,
        bearbeiterBoundaryMock, importVorgangBoundaryMock, "https://local.host");
  }

  @Test
  void testOnImportMessage() throws Exception {
    ImportVorgang importVorgang = readImportVorgang(importObject);

    when(importVorgangBoundaryMock.getImportVorgang(any(ActivityStreamMessageDTO.class)))
        .thenReturn(importVorgang);

    when(katalogBoundaryMock.importieren(any(Bearbeiter.class), any(Katalog.class)))
        .thenAnswer(i -> i.getArguments()[1]);

    ArgumentCaptor<Katalog> captureImportieren = ArgumentCaptor.forClass(Katalog.class);

    KatalogImport katalogImport = new KatalogImport(message, bearbeiter);

    katalogImportService.onImportMessage(katalogImport);

    verify(importVorgangBoundaryMock, times(1)).sendImportJobToKafka(any(ImportVorgang.class));

    assertEquals(IMPORTJOB_RESULT_VALUES.SUCCESS, importVorgang.getStatus());

    verify(katalogBoundaryMock).importieren(any(Bearbeiter.class), captureImportieren.capture());
    Katalog importierterKatalog = captureImportieren.getValue();
    assertNotNull(importierterKatalog);
  }

  @Test
  void testConvertTEI2Kataloge() throws Exception {
    Optional<Katalog> katalog = katalogImportService.convertTEI2Katalog(message);
    assertNotNull(katalog);
    assertTrue(katalog.isPresent());
  }

  @Test
  void testSendFailureMessageToImport() throws Exception {
    ImportVorgang importVorgang = readImportVorgang(importObject);

    when(importVorgangBoundaryMock.getImportVorgang(any(ActivityStreamMessageDTO.class)))
        .thenReturn(importVorgang);

    katalogImportService.sendFailureMessageToImport(message, "Test-Error");

    assertEquals(IMPORTJOB_RESULT_VALUES.FAILED, importVorgang.getStatus());
    verify(importVorgangBoundaryMock, times(1)).sendImportJobToKafka(any(ImportVorgang.class));
  }

  @Test
  void testSendSuccessMessageToImport() throws Exception {
    ImportVorgang importVorgang = readImportVorgang(importObject);

    katalogImportService.sendSuccessMessageToImport(message, new KatalogBuilder().build(), importVorgang);

    assertEquals(IMPORTJOB_RESULT_VALUES.SUCCESS, importVorgang.getStatus());
    verify(importVorgangBoundaryMock, times(1)).sendImportJobToKafka(any(ImportVorgang.class));
  }

  @Test
  void testFindImportFileForActivityStreamObject() throws Exception {

    ImportVorgang importVorgang = readImportVorgang(importObject);

    Optional<ActivityStreamObjectDTO> activityStreamObject = message.getObjects().stream().findFirst();

    assertTrue(activityStreamObject.isPresent());

    ImportFile importFile = katalogImportService.findImportFileForActivityStreamObject(importVorgang,
        activityStreamObject.get());
    assertNotNull(importFile);
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
