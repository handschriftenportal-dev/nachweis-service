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

package de.staatsbibliothek.berlin.hsp.messaging.activitystreams;

import static java.nio.file.Files.newInputStream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamBuilder;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.ActivityStreamMessage;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.TEI;

/**
 * @author piotr.czarnecki@sbb.spk-berlin.de
 * @since 01.04.20
 */
class ActivityStreamMessageTest {

  private static final Logger log = LoggerFactory.getLogger(ActivityStreamMessageTest.class);

  @Test
  public void testSendTEI() throws IOException, JAXBException, ActivityStreamsException {
    Path teiFilePath = Paths.get("src", "main", "resources", "tei", "tei-msDesc.xml");
    try (InputStream is = newInputStream(teiFilePath)) {
      List<TEI> allTei = TEIObjectFactory.unmarshal(is);
      assertNotNull(allTei);
      assertEquals(1, allTei.size());
      TEI tei = allTei.get(0);
      String allTitels = tei.getTeiHeader().getFileDesc().getTitleStmt().getTitles()
          .stream()
          .map(t -> String.valueOf(t.getContent().get(0)))
          .collect(Collectors.joining(", ", "{", "}"));
      log.info("Titel: {}", allTitels);

      final String uuid = UUID.randomUUID().toString();
      LocalDateTime published = LocalDateTime.now();
      ActivityStreamObject activityStreamObject = ActivityStreamObject.builder()
          .withCompressed(true)
          .withContent(tei)
          .withType(ActivityStreamsDokumentTyp.KOD)
          .withUrl("http://localhost")
          .withId("1")
          .withGroupId("beschreibung1")
          .build();

      final ActivityStreamBuilder activityStreamBuilder = ActivityStream.builder()
          .withId(uuid)
          .withType(ActivityStreamAction.ADD)
          .addObject(activityStreamObject)
          .withPublished(published)
          .withActorName("Piotr Czarnecki")
          .withTargetName("tei-msDesc_Westphal.xml");

      final ActivityStream message = activityStreamBuilder.build();
      final Optional<String> messageString = serialize(message);
      assertTrue(messageString.isPresent());
      String json = messageString.get();
      log.info("JSON Length= {}", json.length());
      log.info(json);
      final ActivityStream stream = ObjectMapperFactory.getObjectMapper()
          .readValue(json, ActivityStreamMessage.class);
      final ActivityStreamsDokumentTyp type = stream.getObjects().get(0).getType();
      assertSame(ActivityStreamsDokumentTyp.KOD, type);

      assertFalse(stream.getObjects().isEmpty());

      final TEI kodObj = TEIObjectFactory.unmarshal(new ByteArrayInputStream(stream.getObjects().get(0).getContent()))
          .get(0);

      assertEquals(kodObj.getTeiHeader().getFileDesc().getTitleStmt().getTitles().size(),
          tei.getTeiHeader().getFileDesc()
              .getTitleStmt().getTitles().size());

      assertEquals(published, stream.getPublished());

      log.info("Generated TEI Document length= {}", TEIObjectFactory.marshal(kodObj).length());
    }
  }

  @Test
  public void deserializeKafkaMesssage() throws IOException, ActivityStreamsException, JAXBException {
    Path kafkaTestMessagePath = Paths.get("src", "test", "resources", "kafka-tei-activity.json");
    String messageStr = Files.readString(kafkaTestMessagePath);
    final ActivityStream stream = ObjectMapperFactory.getObjectMapper()
        .readValue(messageStr, new TypeReference<>() {
        });
    assertNotNull(stream);
    List<ActivityStreamObject> objects = stream.getObjects();
    for (ActivityStreamObject object : objects) {
      TEI content = TEIObjectFactory.unmarshal(new ByteArrayInputStream(object.getContent())).get(0);
      assertNotNull(content);
    }

    ActivityStreamMessage activityStreamMessage = ObjectMapperFactory.getObjectMapper()
        .readValue(messageStr, ActivityStreamMessage.class);
    assertNotNull(activityStreamMessage);
  }

  private static Optional<String> serialize(final Object obj) {
    try {
      return of(ObjectMapperFactory.getObjectMapper().writeValueAsString(obj));
    } catch (final JsonProcessingException ex) {
      return empty();
    }
  }

}
