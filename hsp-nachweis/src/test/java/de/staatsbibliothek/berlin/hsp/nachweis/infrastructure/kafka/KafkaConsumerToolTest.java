package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 31.05.24.
 * @version 1.0
 */
public class KafkaConsumerToolTest {

  @Test
  void testCreateProperties() {
    Properties properties = KafkaConsumerTool.createConsumerProperties("bootstrap", "groupid", 1);
    assertNotNull(properties);
    assertEquals("bootstrap",
        properties.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
    assertEquals("groupid",
        properties.getProperty(ConsumerConfig.GROUP_ID_CONFIG));
    assertEquals("1",
        properties.getProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG));
  }

  @Test
  void testCreateOffSet() {

    ConsumerRecord<String, ActivityStream> record = mock(
        ConsumerRecord.class);
    when(record.offset()).thenReturn(1L);
    when(record.partition()).thenReturn(0);
    when(record.topic()).thenReturn("test");

    Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = KafkaConsumerTool.createOffset(record);

    assertNotNull(offsetsToCommit);
    assertEquals(1, offsetsToCommit.size());
    offsetsToCommit.values().stream().findFirst().ifPresentOrElse(
        offsetAndMetadata -> assertEquals(2, offsetAndMetadata.offset()), Assertions::fail
    );
  }
}
