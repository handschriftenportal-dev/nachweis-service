package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;


import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;


/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 05.06.2019.
 */

@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
class KafkaImportProducerIT {

  @Inject
  KafkaImportProducer producer;

  @Test
  void testProperCreation() throws Exception {

    assertNotNull(producer);

    String txId = producer.beginTransaction();
    assertNotNull(txId);
    producer.rollbackTransaction(txId);
  }
}
