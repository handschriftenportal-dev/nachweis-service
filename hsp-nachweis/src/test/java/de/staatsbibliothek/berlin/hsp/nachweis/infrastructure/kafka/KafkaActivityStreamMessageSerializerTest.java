package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 13.09.2019.
 */
public class KafkaActivityStreamMessageSerializerTest {

  @Test
  public void testGivenActivityStreamMessageSerialize() throws ActivityStreamsException {

    try (KafkaActivityStreamMessageSerializer serializer = new KafkaActivityStreamMessageSerializer()) {

      ActivityStream message = ActivityStream.builder()
          .withId("1")
          .withType(ActivityStreamAction.ADD)
          .withTargetName("INDEX")
          .withActorName("konrad")
          .build();

      Assertions.assertTrue(serializer.serialize("test", message).length > 0);
    }
  }
}
