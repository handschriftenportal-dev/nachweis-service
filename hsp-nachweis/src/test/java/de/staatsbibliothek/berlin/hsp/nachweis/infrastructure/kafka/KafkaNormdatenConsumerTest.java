package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.05.24.
 * @version 1.0
 */
public class KafkaNormdatenConsumerTest {

  protected KafkaNormdatenConsumer kafkaNormdatenConsumer;

  @BeforeEach
  void setUp() {

    ManagedExecutor managedExecutor = Mockito.mock(ManagedExecutor.class);

    kafkaNormdatenConsumer = new KafkaNormdatenConsumer("test-topic", "localhost:9092",
        "de/staatsbibliothek/berlin/hsp/nachweis",
        managedExecutor, null);
  }

  @Test
  void testCreateKafkaNormdatenConsumer() {
    assertNotNull(kafkaNormdatenConsumer);
    assertNotNull(kafkaNormdatenConsumer.getTopic());
    assertNotNull(kafkaNormdatenConsumer.getBootstrapServer());
    assertNotNull(kafkaNormdatenConsumer.getGroupId());
  }

  @Test
  void testSetup() {
    kafkaNormdatenConsumer.setup();
    assertNotNull(kafkaNormdatenConsumer.getKafkaConsumerClient());
  }

  @Test
  void testStartPolling() {
    kafkaNormdatenConsumer.setup();
    kafkaNormdatenConsumer.startPollingMessages();
    assertNotNull(kafkaNormdatenConsumer.getKafkaConsumerClient());
    assertTrue(kafkaNormdatenConsumer.getPolling().get());
    kafkaNormdatenConsumer.stopPollingMessages();
    assertFalse(kafkaNormdatenConsumer.getPolling().get());
  }
}
