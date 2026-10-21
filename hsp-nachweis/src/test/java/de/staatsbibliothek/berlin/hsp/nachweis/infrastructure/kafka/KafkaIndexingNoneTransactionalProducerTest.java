package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.10.22.
 * @version 1.0
 */
public class KafkaIndexingNoneTransactionalProducerTest {

  private BeschreibungsRepository beschreibungsRepository = Mockito.mock(BeschreibungsRepository.class);
  @Test
  void testCreation() {

    KafkaIndexingNoneTransactionalProducer producer = new KafkaIndexingNoneTransactionalProducer("index",
        "localhost:9092,localhost:9092,localhost:9092", "de/staatsbibliothek/berlin/hsp/nachweis",
        beschreibungsRepository);

    Assertions.assertNotNull(producer);

  }
}
