package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import java.io.Serial;
import java.io.Serializable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Named
@ApplicationScoped
public class PraesentationsController implements Serializable {

  @Serial
  private static final long serialVersionUID = -4512878466536183739L;
  public static final String ERFASSUNGSHILFE_URL = "/erfassungshilfe.xhtml";

  private final String praesentationBaseUrl;

  public PraesentationsController(
      @ConfigProperty(name = "praesentationbaseurl") String praesentationBaseUrl) {
    this.praesentationBaseUrl = praesentationBaseUrl;
  }

  public String getPraesentationBaseUrl() {
    return praesentationBaseUrl;
  }

  public String getErfassungsGuideUrl() {
    return ERFASSUNGSHILFE_URL;
  }
}
