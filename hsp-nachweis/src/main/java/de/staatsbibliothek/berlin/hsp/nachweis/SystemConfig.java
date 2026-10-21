package de.staatsbibliothek.berlin.hsp.nachweis;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * @author munhal.mammadov@sbb.spk-berlin.de on 31.07.2025.
 * @version 1.0
 */

@Getter
@Named("systemConfig")
@ApplicationScoped
public class SystemConfig {

  @Inject
  @ConfigProperty(name = "praesentationbaseurl")
  String praesentationBaseUrl;

  @Inject
  @ConfigProperty(name = "feature.presentationIndexing.enabled")
  boolean presentationIndexingEnabled;

}

