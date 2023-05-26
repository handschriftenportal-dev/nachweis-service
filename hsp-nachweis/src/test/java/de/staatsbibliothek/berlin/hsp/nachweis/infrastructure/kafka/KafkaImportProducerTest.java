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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgang;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.MockedTransactionManager;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import javax.transaction.SystemException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.05.2020.
 * @version 1.0
 */
class KafkaImportProducerTest extends MockedTransactionManager {

  @Test
  void testCreation() {
    KafkaImportProducer kafkaImportProducer = new KafkaImportProducer(transactionManagerMOCK, "tei-import",
        "localhost:9092", "import", 900);

    assertNotNull(kafkaImportProducer);

    assertEquals("tei-import", kafkaImportProducer.getTopic());

    assertEquals("localhost:9092", kafkaImportProducer.getBootstrapserver());

    assertEquals("import", kafkaImportProducer.getGroupid());
  }

  @Test
  void testSetup() {
    KafkaImportProducer kafkaImportProducer = new KafkaImportProducer(transactionManagerMOCK, "tei-import",
        "localhost:9092", "import", 900);


    assertNotNull(kafkaImportProducer);

  }

  @Test
  void testSendingMessage() throws ActivityStreamsException, SystemException {

    when(xidImpleMOCK.getTransactionUid()).thenReturn(uid);
    when(transactionManagerMOCK.getTransaction()).thenReturn(transactionMOCK);
    when(transactionMOCK.getTxId()).thenReturn(xidImpleMOCK);

    KafkaImportProducer kafkaImportProducer = new KafkaImportProducer(transactionManagerMOCK, "tei-import",
        "localhost:9092", "import", 900);

    KafkaProducer<String, ActivityStream> producer = (KafkaProducer<String, ActivityStream>) Mockito
        .mock(KafkaProducer.class);

    kafkaImportProducer.addProducer(txId, producer);

    ActivityStreamObject uploadObject = ActivityStreamObject.builder()
        .withCompressed(true)
        .withType(ActivityStreamsDokumentTyp.BESCHREIBUNG)
        .withContent("Beschreibung")
        .build();

    ActivityStream message = ActivityStream
        .builder()
        .withId(ActivityStreamsDokumentTyp.BESCHREIBUNG.name())
        .withType(ActivityStreamAction.ADD)
        .withPublished(LocalDateTime.now())
        .withActorName("Konrad Eichstädt")
        .addObject(uploadObject)
        .build();

    kafkaImportProducer.send(message);

    verify(producer).send(any(), any());
  }

  @Test
  void testcreateImportJobMessage() throws JsonProcessingException, ActivityStreamsException {
    String jsonJob = "{\"id\":\"1c8050b1-616c-4454-b441-9281a57bc99d\",\"creationDate\":\"2020-09-24T15:22:15.926333\",\"benutzerName\":\"Redakteur\",\"importFiles\":[{\"id\":\"bec5a8c1-4e01-4d82-b19f-8189bb6b8a12\",\"path\":\"file:///tmp/hsp-import-/2020.09.24.15.22.15.925/ms_1-wthout-BOM-translated.xml\",\"dateiTyp\":\"ms_1-wthout-BOM.xml\",\"dateiName\":\"ms_1.xml\",\"dateiFormat\":\"MXML\",\"error\":false,\"message\":null,\"importEntityData\":[{\"id\":\"HSP-c137570e-95b8-4af8-a92e-eae03d0363b2\",\"label\":\"Ms 1\",\"url\":null}]}],\"name\":\"ms_1.xml\",\"importDir\":\"/tmp/hsp-import-/2020.09.24.15.22.15.925\",\"result\":\"SUCCESS\",\"errorMessage\":\"\",\"datatype\":\"BESCHREIBUNG\"}";
    ActivityStreamMessageFactory messageFactory = new ActivityStreamMessageFactory();

    ImportVorgang importVorgang = ObjectMapperFactory.getObjectMapper()
        .readValue(jsonJob, ImportVorgang.class);

    ActivityStream message = messageFactory.createMessageForImportJob(importVorgang);

    assertNotNull(message);

    assertNotNull(message.getId());
    assertEquals(1, message.getObjects().size());
    assertEquals(ActivityStreamAction.UPDATE, message.getAction());

    message.getObjects().stream().findFirst().ifPresentOrElse((o) -> {
      assertEquals(ActivityStreamsDokumentTyp.IMPORT, o.getType());
      assertEquals("application/json", o.getMediaType());
      try {
        assertEquals(ObjectMapperFactory.getObjectMapper().writeValueAsString(importVorgang)
            .getBytes(StandardCharsets.UTF_8).length, o.getContent().length);
      } catch (ActivityStreamsException e) {
        e.printStackTrace();
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    }, () -> assertTrue(false));
  }
}
