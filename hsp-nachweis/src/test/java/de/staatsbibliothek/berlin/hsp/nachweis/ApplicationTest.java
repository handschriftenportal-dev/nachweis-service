package de.staatsbibliothek.berlin.hsp.nachweis;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.netflix.discovery.EurekaClient;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaImportConsumer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaNormdatenConsumer;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 11.06.2019.
 */
@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
class ApplicationTest {

  private final KafkaImportConsumer kafkaImportConsumer = Mockito.mock(KafkaImportConsumer.class);

  private final KafkaNormdatenConsumer kafkaNormdatenConsumer = Mockito.mock(
      KafkaNormdatenConsumer.class);
  private final EurekaClient eurekaClient = Mockito.mock(EurekaClient.class);

  @Inject
  Application application;


  @Test
  void test_GivenNoneStartAtive_Setup() {

    Application application = new Application(kafkaImportConsumer, eurekaClient,
        kafkaNormdatenConsumer, false, "1.0", "0.1", "./datensicherung/data");

    application.onStart(null);

    Mockito.verify(kafkaImportConsumer, Mockito.times(0)).createConsumer();
    Mockito.verify(kafkaImportConsumer, Mockito.times(0)).startConsumer();
  }


  @Test
  void test_GivenStartAtive_Setup() {

    Application application = new Application(kafkaImportConsumer, eurekaClient,
        kafkaNormdatenConsumer, true, "1.0", "0.1", "./datensicherung/data");

    application.onStart(null);

    Mockito.verify(kafkaImportConsumer, Mockito.times(1)).createConsumer();
    Mockito.verify(kafkaImportConsumer, Mockito.times(1)).startConsumer();
  }

  @Test
  void test_GivenNonPrivilegedFolder_Setup() {

    Application application = new Application(kafkaImportConsumer, eurekaClient,
        kafkaNormdatenConsumer, true, "1.0", "0.1",
        "/var/datensicherung/data");

    Assertions.assertThrows(IllegalArgumentException.class, () -> application.onStart(null));

    Mockito.verify(kafkaImportConsumer, Mockito.times(0)).createConsumer();
    Mockito.verify(kafkaImportConsumer, Mockito.times(0)).startConsumer();
  }

  @Test
  void Given_Application_Destroy() {

    Application application = new Application(kafkaImportConsumer, eurekaClient,
        kafkaNormdatenConsumer, true, "1.0", "0.1",
        "./var/datensicherung/data");

    application.onStop(null);

    Mockito.verify(kafkaImportConsumer, Mockito.times(1)).stopConsumer();
  }

  @Test
  void getVersion() {
    String version = application.getVersion();
    assertNotNull(version);
    assertTrue(version.split("\\.").length > 2);
    assertNotEquals("@project.version@", version);
  }

  @Test
  void getHspVersion() {
    String hspVersion = application.getHspVersion();
    assertNotNull(hspVersion);
    assertTrue(hspVersion.split("\\.").length > 2);
    assertNotEquals("@handschriftenportal.version@", hspVersion);
  }
}
