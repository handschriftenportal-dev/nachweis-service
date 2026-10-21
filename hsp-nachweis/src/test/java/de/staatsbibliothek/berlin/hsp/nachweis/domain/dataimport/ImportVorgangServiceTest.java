package de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServicePort;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportVorgang;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaImportProducer;
import jakarta.enterprise.event.Event;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.05.2020.
 * @version 1.0
 */
class ImportVorgangServiceTest {


  @Test
  void testSendWithIllegalArguments() {
    KafkaImportProducer kafkaImportProducer = mock(KafkaImportProducer.class);
    ImportServicePort importServicePort = mock(ImportServicePort.class);
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    Event importVorgangEvent = mock(Event.class);
    ImportVorgangService service = new ImportVorgangService(kafkaImportProducer, importServicePort,
        objectMapper, importVorgangEvent);

    ImportUploadDatei datei = new ImportUploadDatei("Test.xml", "Test".getBytes(),
        "application/zip");

    assertThrows(IllegalArgumentException.class, () -> {
      service.sendDateiUploadMessageToImportService(null, ActivityStreamsDokumentTyp.BESCHREIBUNG,
          new Bearbeiter("1", "Unbekannter Tester"));
    });

    assertThrows(IllegalArgumentException.class, () -> {
      service.sendDateiUploadMessageToImportService(datei, null,
          new Bearbeiter("1", "Unbekannter Tester"));
    });

    assertThrows(IllegalArgumentException.class, () -> {
      service.sendDateiUploadMessageToImportService(datei, ActivityStreamsDokumentTyp.BESCHREIBUNG,
          null);
    });

  }

  @Test
  void testSendHappyPathArgumnents() throws ImportVorgangException, ActivityStreamsException {
    KafkaImportProducer kafkaImportProducer = mock(KafkaImportProducer.class);
    ImportServicePort importServicePort = mock(ImportServicePort.class);
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    Event<ImportVorgang> importVorgangEvent = mock(Event.class);
    ImportVorgangService service = new ImportVorgangService(kafkaImportProducer, importServicePort,
        objectMapper, importVorgangEvent);
    ImportUploadDatei datei = new ImportUploadDatei("Test.xml", "Test".getBytes(),
        "application/xml");

    service.sendDateiUploadMessageToImportService(datei, ActivityStreamsDokumentTyp.BESCHREIBUNG,
        new Bearbeiter("1", "Unbekannter Tester"));

    Mockito.verify(kafkaImportProducer, Mockito.times(1))
        .sendImportDateiAsActivityStreamMessage(datei, ActivityStreamsDokumentTyp.BESCHREIBUNG,
            ActivityStreamAction.ADD, null, "Unbekannter Tester");
  }

  @Test
  void testSendBeschreibungWithTag() throws ImportVorgangException, ActivityStreamsException {
    KafkaImportProducer kafkaImportProducer = mock(KafkaImportProducer.class);
    ImportServicePort importServicePort = mock(ImportServicePort.class);
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    Event<ImportVorgang> importVorgangEvent = mock(Event.class);
    ImportVorgangService service = new ImportVorgangService(kafkaImportProducer, importServicePort,
        objectMapper, importVorgangEvent);
    ImportUploadDatei datei = new ImportUploadDatei("Test.xml", "Test".getBytes(),
        "application/xml");

    List<ActivityStreamObjectTag> tags = List.of(
        new HSPActivityStreamObjectTag("type", ActivityStreamObjectTagId.INTERN_EXTERN, "name"));

    service.sendDateiUploadMessageToImportService(datei, ActivityStreamsDokumentTyp.BESCHREIBUNG,
        new Bearbeiter("1", "Unbekannter Tester"), tags);

    Mockito.verify(kafkaImportProducer, Mockito.times(1))
        .sendImportDateiAsActivityStreamMessage(datei, ActivityStreamsDokumentTyp.BESCHREIBUNG,
            ActivityStreamAction.ADD, tags, "Unbekannter Tester");
  }

  @Test
  void testfindAll() {
    KafkaImportProducer kafkaImportProducer = mock(KafkaImportProducer.class);
    ImportServicePort importServicePort = mock(ImportServicePort.class);
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    Event<ImportVorgang> importVorgangEvent = mock(Event.class);
    ImportVorgangService service = new ImportVorgangService(kafkaImportProducer, importServicePort,
        objectMapper, importVorgangEvent);

    Set<ImportVorgang> vorgangSet = service.findAll();

    verify(importServicePort, times(1)).findAll();

    assertNotNull(vorgangSet);
  }

  @Test
  void testFindById() {
    KafkaImportProducer kafkaImportProducer = mock(KafkaImportProducer.class);
    ImportServicePort importServicePort = mock(ImportServicePort.class);
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    Event<ImportVorgang> importVorgangEvent = mock(Event.class);

    ImportVorgang importVorgang = new ImportVorgang("123", "benutzerName", "name",
        LocalDateTime.now(), IMPORTJOB_RESULT_VALUES.IN_PROGRESS, "", "datentyp");

    when(importServicePort.findById("123")).thenReturn(importVorgang);
    when(importServicePort.findById("456")).thenThrow(
        new WebApplicationException("Unknown error, status code 404"));

    ImportVorgangService importVorgangService = new ImportVorgangService(kafkaImportProducer,
        importServicePort, objectMapper, importVorgangEvent);

    assertTrue(importVorgangService.findById("123").isPresent());
    assertFalse(importVorgangService.findById("456").isPresent());
    assertFalse(importVorgangService.findById("").isPresent());
    assertFalse(importVorgangService.findById(null).isPresent());
  }

  @Test
  void testsendKafkaMessageToImport() throws ActivityStreamsException, JsonProcessingException {
    KafkaImportProducer kafkaImportProducer = mock(KafkaImportProducer.class);
    ImportServicePort importServicePort = mock(ImportServicePort.class);
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    Event<ImportVorgang> importVorgangEvent = mock(Event.class);
    ImportVorgangService service = new ImportVorgangService(kafkaImportProducer, importServicePort,
        objectMapper, importVorgangEvent);

    String jsonJob = "{\"id\":\"03c45a36-a364-4541-ad72-9e640643836b\",\"creationDate\":\"2020-09-24T13:24:22.657368\",\"benutzerName\":\"Redakteur\",\"importFiles\":[{\"id\":\"7ee84c46-78b9-433a-94bc-4ffb3a4a291e\",\"path\":\"file:///tmp/hsp-import-/2020.09.24.13.24.22.656/ms_1_neu-wthout-BOM-translated.xml\",\"dateiTyp\":\"ms_1_neu-wthout-BOM.xml\",\"dateiName\":\"ms_1_neu.xml\",\"dateiFormat\":\"MXML\",\"error\":false,\"message\":null,\"importEntityData\":[]}],\"name\":\"ms_1_neu.xml\",\"importDir\":\"/tmp/hsp-import-/2020.09.24.13.24.22.656\",\"result\":\"FAILED\",\"errorMessage\":null,\"datatype\":\"BESCHREIBUNG\"}";

    ImportVorgang importVorgang = ObjectMapperFactory.getObjectMapper()
        .readValue(jsonJob, ImportVorgang.class);

    service.sendImportJobToKafka(importVorgang);

    verify(kafkaImportProducer, times(1)).sendImportJobAsActivityStreamMessage(importVorgang);
  }

  @Test
  void testDeleteImportVorgaenge() {
    KafkaImportProducer kafkaImportProducer = mock(KafkaImportProducer.class);
    ImportServicePort importServicePort = mock(ImportServicePort.class);
    ImportVorgangService service = new ImportVorgangService(kafkaImportProducer, importServicePort,
        null, null);

    ImportVorgang importVorgang = new ImportVorgang("123", "b-ke101", "", LocalDateTime.now(),
        IMPORTJOB_RESULT_VALUES.IN_PROGRESS, "", "datentyp");

    assertDoesNotThrow(() -> service.deleteVorgaenge(List.of(importVorgang)));

    verify(importServicePort, times(1)).deleteByIDs(List.of("123"));

  }

  @Test
  void testDeleteImportVorgaengeWithFailure() {
    KafkaImportProducer kafkaImportProducer = mock(KafkaImportProducer.class);
    ImportServicePort importServicePort = mock(ImportServicePort.class);
    ImportVorgangService service = new ImportVorgangService(kafkaImportProducer, importServicePort,
        null, null);

    ImportVorgang importVorgang = new ImportVorgang("1234", "b-ke101", "", LocalDateTime.now(),
        IMPORTJOB_RESULT_VALUES.IN_PROGRESS, "", "datentyp");

    Mockito.doThrow(new RuntimeException(new ImportVorgangException()))
        .when(importServicePort).deleteByIDs(List.of("1234"));

    assertThrows(ImportVorgangException.class,
        () -> service.deleteVorgaenge(List.of(importVorgang)));

  }
}
