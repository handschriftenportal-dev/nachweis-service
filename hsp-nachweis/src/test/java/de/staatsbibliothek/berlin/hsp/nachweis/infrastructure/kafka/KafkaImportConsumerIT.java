package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 05.06.2019.
 */

@QuarkusTest
class KafkaImportConsumerIT {

  @Inject
  KafkaImportConsumer consumer;

  @Test
  void testProperCreationFoConsumer() {

    assertNotNull(consumer);

    assertFalse(consumer.getTopic().isEmpty());

    assertFalse(consumer.getBootstrapServer().isEmpty());

    assertFalse(consumer.getGroupId().isEmpty());

  }

  @Test
  void test_givenSetup_CreateCustomer() {

    consumer.createConsumer();

    assertNotNull(consumer.getConsumer());
  }
}
