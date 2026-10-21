package de.staatsbibliothek.berlin.hsp.nachweis.domain.gndservice;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDServiceSubset;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenGNDServicePort;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenserviceSync;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.05.25.
 * @version 1.0
 * <p>
 * This class represents a service object which should arrange the communication with the Normdatenservice
 * and arrange the Normdaten-Synchronization
 */

@Dependent
@Slf4j
public class GNDSynchronisationService {

  private final NormdatenGNDServicePort normdatenGNDServicePort;

  @Inject
  public GNDSynchronisationService(
      @RestClient NormdatenGNDServicePort normdatenGNDServicePort) {
    this.normdatenGNDServicePort = normdatenGNDServicePort;
  }

  public NormdatenGNDServicePort getNormdatenGNDServicePort() {
    return normdatenGNDServicePort;
  }

  public void registerSynchronisation(int intervalInMinutes,
      boolean active, String kafkaTopicName) {

    NormdatenserviceSync postData = new NormdatenserviceSync(intervalInMinutes,
        active, kafkaTopicName);

    log.info("Registering synchronisation with {}", postData);

    normdatenGNDServicePort.registerSynchronistation(postData);
  }

  public GNDSynchronization changeSynchronisation(int intervalInMinutes,
      boolean active, String kafkaTopicName) {

    NormdatenserviceSync putData = new NormdatenserviceSync(intervalInMinutes,
        active, kafkaTopicName);

    log.info("Change synchronisation with {}", putData);

    GNDServiceSubset subset = normdatenGNDServicePort.updateSynchronistation(
        putData);

    return GNDSynchronization.fromSubset(subset);
  }

  public Optional<GNDSynchronization> findSynchronisation() {

    log.info("Finding synchronisation within the normdatenservice");
    try {
      GNDServiceSubset subset = normdatenGNDServicePort.findGNDServiceSubset();
      return Optional.of(GNDSynchronization.fromSubset(subset));
    } catch (WebApplicationException error) {
      if (error.getResponse().getStatus() == 404) {
        return Optional.empty();
      } else {
        throw new WebApplicationException(error);
      }
    }
  }

  public void unregisterSynchronisation() {
    log.info("Unregistering synchronisation within the normdatenservice");
    normdatenGNDServicePort.unregisterSynchronistation();
  }
}
