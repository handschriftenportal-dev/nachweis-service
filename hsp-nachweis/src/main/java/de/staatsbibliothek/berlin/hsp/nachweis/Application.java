package de.staatsbibliothek.berlin.hsp.nachweis;

import com.netflix.discovery.EurekaClient;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaImportConsumer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaNormdatenConsumer;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 04.06.2019.
 * <p>
 * Class for Bootstrap and configure the application
 */

@ApplicationScoped
@Named("appInfo")
public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);
  private EurekaClient eurekaClient;

  @Getter
  private String hspVersion;
  @Getter
  private String version;
  private KafkaImportConsumer kafkaImportConsumer;
  private KafkaNormdatenConsumer kafkaNormdatenConsumer;
  private boolean activeKafka;
  private String datensicherungFolderPath;

  Application() {
  }

  @Inject
  public Application(
      KafkaImportConsumer kafkaImportConsumer, EurekaClient eurekaClient,
      KafkaNormdatenConsumer kafkaNormdatenConsumer,
      @ConfigProperty(name = "kafka.active") boolean activeKafka,
      @ConfigProperty(name = "hsp.version") String hspVersion,
      @ConfigProperty(name = "nachweis.version") String version,
      @ConfigProperty(name = "datensicherung.path") String datensicherungFolderPath) {

    this.kafkaImportConsumer = kafkaImportConsumer;
    this.eurekaClient = eurekaClient;
    this.kafkaNormdatenConsumer = kafkaNormdatenConsumer;
    this.activeKafka = activeKafka;
    this.hspVersion = hspVersion;
    this.version = version;
    this.datensicherungFolderPath = datensicherungFolderPath;
  }

  public void onStart(@Observes StartupEvent ev) {

    creatConfiguredDirectories();
    logger.info(
        "------------------ Application HSP Nachweis Service has been started sucessfully ------------------");

    if (activeKafka) {
      kafkaImportConsumer.createConsumer();
      kafkaImportConsumer.startConsumer();
      kafkaNormdatenConsumer.startPollingMessages();
    }
  }

  public void onStop(@Observes ShutdownEvent ev) {

    if (activeKafka) {
      kafkaImportConsumer.stopConsumer();
      kafkaNormdatenConsumer.stopPollingMessages();
    }
    eurekaClient.shutdown();
    logger.info(
        "------------------ Application HSP Nachweis Service shutdown sucessfully ------------------");
  }

  private void creatConfiguredDirectories() {
    Path serverFolderPath = Paths.get(datensicherungFolderPath);

    if (Files.notExists(serverFolderPath)) {
      try {
        Files.createDirectories(serverFolderPath);
      } catch (IOException e) {
        throw new IllegalArgumentException("The folder in the path " + serverFolderPath.toString()
            + " could not be created. Make sure the path is correct and the application has sufficient privileges.",
            e);
      }
    }
  }
}
