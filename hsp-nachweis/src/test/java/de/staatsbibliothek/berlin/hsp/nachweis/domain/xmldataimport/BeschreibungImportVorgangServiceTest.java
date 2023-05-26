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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObjectTag;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamObjectTagId;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.HSPActivityStreamObjectTag;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServicePort;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaImportProducer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import javax.enterprise.event.Event;
import javax.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.05.2020.
 * @version 1.0
 */
public class BeschreibungImportVorgangServiceTest {


  @Test
  void testSendWithIllegalArguments() {
    KafkaImportProducer kafkaImportProducer = mock(KafkaImportProducer.class);
    ImportServicePort importServicePort = mock(ImportServicePort.class);
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    Event<ImportVorgang> importVorgangEvent = Mockito.mock(Event.class);
    ImportVorgangService service = new ImportVorgangService(kafkaImportProducer, importServicePort,
        objectMapper,
        importVorgangEvent);

    ImportUploadDatei datei = new ImportUploadDatei("Test.xml", "Test".getBytes(),
        "application/zip");

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      service.sendDateiUploadMessageToImportService(null, ActivityStreamsDokumentTyp.BESCHREIBUNG,
          new Bearbeiter("1", "Unbekannter Tester"));
    });

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      service.sendDateiUploadMessageToImportService(datei, null,
          new Bearbeiter("1", "Unbekannter Tester"));
    });

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      service.sendDateiUploadMessageToImportService(datei, ActivityStreamsDokumentTyp.BESCHREIBUNG,
          null);
    });

  }

  @Test
  void testSendHappyPathArgumnents()
      throws ImportVorgangException, ActivityStreamsException {
    KafkaImportProducer kafkaImportProducer = mock(KafkaImportProducer.class);
    ImportServicePort importServicePort = mock(ImportServicePort.class);
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    Event<ImportVorgang> importVorgangEvent = Mockito.mock(Event.class);
    ImportVorgangService service = new ImportVorgangService(kafkaImportProducer, importServicePort,
        objectMapper,
        importVorgangEvent);
    ImportUploadDatei datei = new ImportUploadDatei("Test.xml", "Test".getBytes(),
        "application/xml");

    service
        .sendDateiUploadMessageToImportService(datei, ActivityStreamsDokumentTyp.BESCHREIBUNG,
            new Bearbeiter("1", "Unbekannter Tester"));

    Mockito.verify(kafkaImportProducer, Mockito.times(1)).sendImportDateiAsActivityStreamMessage(
        datei, ActivityStreamsDokumentTyp.BESCHREIBUNG, ActivityStreamAction.ADD, null, "Unbekannter Tester");
  }

  @Test
  void testSendBeschreibungWithTag()
      throws ImportVorgangException, ActivityStreamsException {
    KafkaImportProducer kafkaImportProducer = mock(KafkaImportProducer.class);
    ImportServicePort importServicePort = mock(ImportServicePort.class);
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    Event<ImportVorgang> importVorgangEvent = Mockito.mock(Event.class);
    ImportVorgangService service = new ImportVorgangService(kafkaImportProducer, importServicePort,
        objectMapper,
        importVorgangEvent);
    ImportUploadDatei datei = new ImportUploadDatei("Test.xml", "Test".getBytes(),
        "application/xml");

    List<ActivityStreamObjectTag> tags = List
        .of(new HSPActivityStreamObjectTag("type", ActivityStreamObjectTagId.INTERN_EXTERN,
            "name"));

    service
        .sendDateiUploadMessageToImportService(datei, ActivityStreamsDokumentTyp.BESCHREIBUNG,
            new Bearbeiter("1", "Unbekannter Tester"),
            tags);

    Mockito.verify(kafkaImportProducer, Mockito.times(1)).sendImportDateiAsActivityStreamMessage(
        datei, ActivityStreamsDokumentTyp.BESCHREIBUNG, ActivityStreamAction.ADD, tags, "Unbekannter Tester");
  }

  @Test
  void testfindAll() {
    KafkaImportProducer kafkaImportProducer = mock(KafkaImportProducer.class);
    ImportServicePort importServicePort = mock(ImportServicePort.class);
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    Event<ImportVorgang> importVorgangEvent = Mockito.mock(Event.class);
    ImportVorgangService service = new ImportVorgangService(kafkaImportProducer, importServicePort,
        objectMapper,
        importVorgangEvent);

    Set<ImportVorgang> vorgangSet = service.findAll();

    verify(importServicePort, times(1)).findAll();

    assertNotNull(vorgangSet);
  }

  @Test
  void testFindById() {
    KafkaImportProducer kafkaImportProducer = mock(KafkaImportProducer.class);
    ImportServicePort importServicePort = mock(ImportServicePort.class);
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    Event<ImportVorgang> importVorgangEvent = Mockito.mock(Event.class);

    ImportVorgang importVorgang = new ImportVorgang("123", "benutzerName", "name", LocalDateTime.now(),
        IMPORTJOB_RESULT_VALUES.IN_PROGRESS, "", "datentyp");

    when(importServicePort.findById("123")).thenReturn(importVorgang);
    when(importServicePort.findById("456"))
        .thenThrow(new WebApplicationException("Unknown error, status code 404"));

    ImportVorgangService importVorgangService = new ImportVorgangService(kafkaImportProducer, importServicePort,
        objectMapper,
        importVorgangEvent);

    assertTrue(importVorgangService.findById("123").isPresent());
    assertFalse(importVorgangService.findById("456").isPresent());
    assertFalse(importVorgangService.findById("").isPresent());
    assertFalse(importVorgangService.findById(null).isPresent());
  }

  @Test
  void testsendKafkaMessageToImport()
      throws ActivityStreamsException, JsonProcessingException {
    KafkaImportProducer kafkaImportProducer = mock(KafkaImportProducer.class);
    ImportServicePort importServicePort = mock(ImportServicePort.class);
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    Event<ImportVorgang> importVorgangEvent = Mockito.mock(Event.class);
    ImportVorgangService service = new ImportVorgangService(kafkaImportProducer, importServicePort,
        objectMapper,
        importVorgangEvent);

    String jsonJob = "{\"id\":\"03c45a36-a364-4541-ad72-9e640643836b\",\"creationDate\":\"2020-09-24T13:24:22.657368\",\"benutzerName\":\"Redakteur\",\"importFiles\":[{\"id\":\"7ee84c46-78b9-433a-94bc-4ffb3a4a291e\",\"path\":\"file:///tmp/hsp-import-/2020.09.24.13.24.22.656/ms_1_neu-wthout-BOM-translated.xml\",\"dateiTyp\":\"ms_1_neu-wthout-BOM.xml\",\"dateiName\":\"ms_1_neu.xml\",\"dateiFormat\":\"MXML\",\"error\":false,\"message\":null,\"importEntityData\":[]}],\"name\":\"ms_1_neu.xml\",\"importDir\":\"/tmp/hsp-import-/2020.09.24.13.24.22.656\",\"result\":\"FAILED\",\"errorMessage\":null,\"datatype\":\"BESCHREIBUNG\"}";

    ImportVorgang importVorgang = ObjectMapperFactory.getObjectMapper()
        .readValue(jsonJob, ImportVorgang.class);

    service.sendImportJobToKafka(importVorgang);

    verify(kafkaImportProducer, times(1)).sendImportJobAsActivityStreamMessage(importVorgang);
  }
}
