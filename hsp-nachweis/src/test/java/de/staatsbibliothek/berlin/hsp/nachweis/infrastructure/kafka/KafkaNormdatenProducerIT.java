package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 31.01.24.
 * @version 1.0
 */

@QuarkusTest
public class KafkaNormdatenProducerIT {

  @Inject
  KafkaNormdatenProducer kafkaNormdatenProducer;

  @Test
  void testCreationAndInjection() {
    assertNotNull(kafkaNormdatenProducer);
    assertEquals("data-normdaten-integration", kafkaNormdatenProducer.getTopic());
    assertTrue(kafkaNormdatenProducer.getBootstrapserver().contains("localhost"));
    assertEquals("nachweis-normdaten-integration", kafkaNormdatenProducer.getGroupid());
    assertEquals(900, kafkaNormdatenProducer.getTransactionTimeout());
  }
}
