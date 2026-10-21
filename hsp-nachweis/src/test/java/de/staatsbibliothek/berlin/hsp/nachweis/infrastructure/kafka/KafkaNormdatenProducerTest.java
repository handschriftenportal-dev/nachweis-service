package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.MockedTransactionManager;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 30.01.24.
 * @version 1.0
 */
public class KafkaNormdatenProducerTest extends MockedTransactionManager {

  @Test
  void testCreationWithManualCreation() {

    KafkaNormdatenProducer producer = new KafkaNormdatenProducer(null, "data-normdaten",
        "localhost:9002", "de/staatsbibliothek/berlin/hsp/nachweis", 2000);
    assertNotNull(producer);
    assertEquals("data-normdaten", producer.getTopic());
    assertEquals("localhost:9002", producer.getBootstrapserver());
    assertEquals("de/staatsbibliothek/berlin/hsp/nachweis", producer.getGroupid());
    assertEquals(2000, producer.getTransactionTimeout());
  }

  @Test
  void testSendingMessageForKOD()
      throws ActivityStreamsException, ExecutionException, InterruptedException, TimeoutException {
    KafkaNormdatenProducer producer = new KafkaNormdatenProducer(transactionManagerMOCK,
        "data-normdaten",
        "localhost:9002", "de/staatsbibliothek/berlin/hsp/nachweis", 10);
    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder().withId(
        "HSP-KOD1").withTEIXml("<xml></xml>").build();

    KafkaProducer kafkaProducer = mock(KafkaProducer.class);
    CompletableFuture record = mock(CompletableFuture.class);
    when(record.get(10, TimeUnit.SECONDS)).thenReturn(null);
    when(record.isDone()).thenReturn(true);
    producer.addProducer(txId, kafkaProducer);

    producer.sendKulturobjektDokumentAsActivityStreamMessage(kod, ActivityStreamAction.ADD, false,
        "Konrad");

    verify(kafkaProducer, times(1)).send(any(), any());
  }
}
