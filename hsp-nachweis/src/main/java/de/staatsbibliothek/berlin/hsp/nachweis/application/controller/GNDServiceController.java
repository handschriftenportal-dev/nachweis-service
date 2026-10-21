package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.GNDServiceRechte.PERMISSION_CONTROL_GNDSERVICE;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.gndservice.GNDSynchronisationService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.gndservice.GNDSynchronization;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.05.25.
 * @version 1.0
 * <p>
 * This class represents the MVC Controller for Handling all server stage of the page gndservice.xhtml
 */

@ViewScoped
@Named("gNDServiceController")
@LoginCheck
@Slf4j
public class GNDServiceController implements Serializable {

  @Serial
  private static final long serialVersionUID = -8738180893505556958L;

  private final GNDSynchronisationService gndSynchronisationService;

  @Getter
  private GNDSynchronization synchronization;

  @Getter
  private int intervalInMinutes;

  @Getter
  private boolean active;

  @Getter
  private String kafkaTopicName;

  @Getter
  private Exception error;

  @Getter
  @Setter
  private boolean subsetFormEditable;

  @Inject
  public GNDServiceController(
      GNDSynchronisationService gndSynchronisationService) {
    this.gndSynchronisationService = gndSynchronisationService;
  }

  @PostConstruct
  public void setup() {
    log.info("Setting up GNDServiceController trying to find Synchronisation");
    try {
      final Optional<GNDSynchronization> optionalGNDSynchronization = this.gndSynchronisationService.findSynchronisation();
      optionalGNDSynchronization.ifPresentOrElse(
          this::updateControllerSubsetState,
          () -> synchronization = null);
      log.debug("Found synchronisation:  {}", synchronization);
    } catch (Exception exception) {
      this.error = exception;
      log.error("Error during find Synchronisation ", exception);
    }
  }

  @CheckPermission(permission = PERMISSION_CONTROL_GNDSERVICE)
  void updateControllerSubsetState(GNDSynchronization gndSynchronization) {
    synchronization = gndSynchronization;
    intervalInMinutes = synchronization.intervalInMinutes();
    active = synchronization.active();
    kafkaTopicName = synchronization.kafkaTopicName();
  }

  @CheckPermission(permission = PERMISSION_CONTROL_GNDSERVICE)
  public void loeschen() {

    log.info("Call Delete synchronisation");

    try {
      this.gndSynchronisationService.unregisterSynchronisation();
      setup();
    } catch (Exception exception) {
      this.error = exception;
      log.error("Error during find Synchronisation ", exception);
    }
  }

  @CheckPermission(permission = PERMISSION_CONTROL_GNDSERVICE)
  public void anlegen() {
    log.info(
        "Call create synchronisation with interval {} minutes and active {} and kafkaTopic {}",
        this.intervalInMinutes,
        this.active, this.kafkaTopicName);
    try {
      this.gndSynchronisationService.registerSynchronisation(
          intervalInMinutes,
          active, kafkaTopicName);
      setup();
    } catch (Exception exception) {
      this.error = exception;
      log.error("Error during create Synchronisation ", exception);
    }
  }

  @CheckPermission(permission = PERMISSION_CONTROL_GNDSERVICE)
  public void aendern() {
    log.info(
        "Call change synchronisation with interval {} minutes and active {} and kafkaTopic {}",
        this.intervalInMinutes,
        this.active, this.kafkaTopicName);
    try {
      this.updateControllerSubsetState(
          this.gndSynchronisationService.changeSynchronisation(
              intervalInMinutes,
              active, kafkaTopicName));
      setSubsetFormEditable(false);
      setup();
    } catch (Exception exception) {
      this.error = exception;
      log.error("Error during change Synchronisation ", exception);
    }
  }

  public void setActive(boolean active) {
    log.info("Set active to {}", active);
    this.active = active;
  }

  public void setKafkaTopicName(String kafkaTopicName) {
    log.info("Set Kafka Topic Name to {}", kafkaTopicName);
    this.kafkaTopicName = kafkaTopicName;
  }

  public void setIntervalInMinutes(int intervalInMinutes) {
    log.info("Set interval in minutes to {}", intervalInMinutes);
    this.intervalInMinutes = intervalInMinutes;
  }
}
