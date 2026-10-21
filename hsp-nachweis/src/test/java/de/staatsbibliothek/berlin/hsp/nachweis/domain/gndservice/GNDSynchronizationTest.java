package de.staatsbibliothek.berlin.hsp.nachweis.domain.gndservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDServiceSubset;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 04.06.25.
 * @version 1.0
 */
public class GNDSynchronizationTest {

  public static final String ID = "1";
  public static final String NAME = "Test";

  @Test
  void testCreation() {
    GNDSynchronization gndSynchronization = new GNDSynchronization(ID, NAME,
        30, true, "topic",
        Set.of(), List.of());

    assertNotNull(gndSynchronization);
    assertEquals(ID, gndSynchronization.id());
    assertEquals(NAME, gndSynchronization.name());
  }

  @Test
  void testCreationFromSubset() {
    GNDSynchronization gndSynchronization = GNDSynchronization.fromSubset(
        new GNDServiceSubset(ID, NAME, true, Set.of(), 30, "kafka/test",
            Set.of()));

    assertNotNull(gndSynchronization);
    assertEquals(ID, gndSynchronization.id());
    assertEquals(NAME, gndSynchronization.name());
  }

  @Test
  void testnormdatenserviceForKafkaTopic() {
    GNDSynchronization gndSynchronization = GNDSynchronization.fromSubset(
        new GNDServiceSubset(ID, NAME, true, Set.of(), 30, "kafka/test",
            Set.of()));

    assertEquals("test", gndSynchronization.kafkaTopicName());
  }

  @Test
  void testnormdatenserviceBadValueKafkaTopic() {

    assertThrows(IllegalArgumentException.class,
        () -> GNDSynchronization.fromSubset(
            new GNDServiceSubset(ID, NAME, true, Set.of(), 30, null,
                Set.of())));
  }
}
