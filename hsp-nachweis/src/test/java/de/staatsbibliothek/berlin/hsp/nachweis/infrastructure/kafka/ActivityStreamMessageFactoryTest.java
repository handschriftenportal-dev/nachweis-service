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

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForKatalog;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForKulturobjektDokument;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportUploadDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgang;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 29.03.23.
 * @version 1.0
 */
public class ActivityStreamMessageFactoryTest {

  @Test
  void testCreation() {

    ActivityStreamMessageFactory streamMessageFactory = new ActivityStreamMessageFactory();
    assertNotNull(streamMessageFactory);
  }

  @Test
  void testCreateMessageForUpload() throws ActivityStreamsException {
    ActivityStreamMessageFactory streamMessageFactory = new ActivityStreamMessageFactory();
    byte[] content = "Test".getBytes(StandardCharsets.UTF_8);
    ImportUploadDatei datei = new ImportUploadDatei("test.txt",content,"text");

    ActivityStream message = streamMessageFactory.createMessageForUpload(datei,
        ActivityStreamsDokumentTyp.BESCHREIBUNG, ActivityStreamAction.ADD, Arrays.asList(),"Konrad");

    assertNotNull(message);
    assertFalse(message.getId().isEmpty());
    assertEquals(ActivityStreamAction.ADD,message.getAction());
    assertNotNull(message.getPublished());
    assertEquals("Konrad",message.getActor().getName());
    assertEquals(1,message.getObjects().size());

    ActivityStreamObject object = message.getObjects().get(0);
    assertNotNull(object);
    assertEquals(ActivityStreamsDokumentTyp.BESCHREIBUNG,object.getType());
    assertNotNull(object.getTag());
    assertEquals(content.length,object.getContent().length);
  }

  @Test
  void testCreateMessageForImportVorgang() throws JsonProcessingException, ActivityStreamsException {
    String jsonJob = "{\"id\":\"1c8050b1-616c-4454-b441-9281a57bc99d\",\"creationDate\":\"2020-09-24T15:22:15.926333\",\"benutzerName\":\"Redakteur\",\"importFiles\":[{\"id\":\"bec5a8c1-4e01-4d82-b19f-8189bb6b8a12\",\"path\":\"file:///tmp/hsp-import-/2020.09.24.15.22.15.925/ms_1-wthout-BOM-translated.xml\",\"dateiTyp\":\"ms_1-wthout-BOM.xml\",\"dateiName\":\"ms_1.xml\",\"dateiFormat\":\"MXML\",\"error\":false,\"message\":null,\"importEntityData\":[{\"id\":\"HSP-c137570e-95b8-4af8-a92e-eae03d0363b2\",\"label\":\"Ms 1\",\"url\":null}]}],\"name\":\"ms_1.xml\",\"importDir\":\"/tmp/hsp-import-/2020.09.24.15.22.15.925\",\"result\":\"SUCCESS\",\"errorMessage\":\"\",\"datatype\":\"BESCHREIBUNG\"}";
    ImportVorgang importVorgang = ObjectMapperFactory.getObjectMapper()
        .readValue(jsonJob, ImportVorgang.class);
    ActivityStreamMessageFactory streamMessageFactory = new ActivityStreamMessageFactory();

    ActivityStream message = streamMessageFactory.createMessageForImportJob(importVorgang);

    assertNotNull(message);
    assertEquals(importVorgang.getId(),message.getId());
    assertEquals(ActivityStreamAction.UPDATE,message.getAction());
    assertNotNull(message.getPublished());
    assertEquals(1,message.getObjects().size());

    ActivityStreamObject object = message.getObjects().get(0);
    assertNotNull(object);
    assertEquals(ActivityStreamsDokumentTyp.IMPORT,object.getType());
    assertNotNull(object.getContent());
  }

  @Test
  void testCreateMessageForKOD() throws ActivityStreamsException {
    BeschreibungsRepository beschreibungsRepository = Mockito.mock(BeschreibungsRepository.class);
    KulturObjektDokument kulturObjektDokument = new KulturObjektDokumentBuilder().withId("KOD1")
        .withTEIXml("<xml></xml>")
        .build();
    Beschreibung external = new BeschreibungsBuilder().withId("BESCHREIBUNG1").withVerwaltungsTyp(
        VerwaltungsTyp.EXTERN).withTEIXml("<xml></xml>").build();
    Beschreibung internal= new BeschreibungsBuilder().withId("BESCHREIBUNG2").withVerwaltungsTyp(
        VerwaltungsTyp.INTERN).withTEIXml("<xml></xml>").build();
    kulturObjektDokument.addBeschreibungsdokument(external.getId());
    kulturObjektDokument.addBeschreibungsdokument(internal.getId());
    when(beschreibungsRepository.findByIdOptional(external.getId())).thenReturn(
        Optional.of(external));
    when(beschreibungsRepository.findByIdOptional(internal.getId())).thenReturn(
        Optional.of(internal));

    ActivityStream message = createMessageForKulturobjektDokument(kulturObjektDokument,ActivityStreamAction.ADD,true,
        "Konrad",beschreibungsRepository);

    assertEquals(ActivityStreamAction.ADD,message.getAction());
    assertTrue(message.getObjects().get(0).isCompressed());
    assertEquals(2, message.getObjects().size());
    assertEquals("Konrad",message.getActor().getName());
  }

  @Test
  void testCreateMessageForKatalog() throws ActivityStreamsException {

    Katalog katalog = new KatalogBuilder().withId("K1").withTEIXML("<xml></xml>").build();

    ActivityStream message = createMessageForKatalog(katalog,ActivityStreamAction.ADD,"Konrad",true);

    assertEquals(ActivityStreamAction.ADD,message.getAction());
    assertTrue(message.getObjects().get(0).isCompressed());
    assertEquals(1, message.getObjects().size());
    assertEquals("Konrad",message.getActor().getName());
    assertEquals("K1",message.getId());

    ActivityStreamObject object = message.getObjects().get(0);
    assertNotNull(object);
    assertEquals(ActivityStreamsDokumentTyp.KATALOG,object.getType());
    assertNotNull(object.getContent());

  }
}
