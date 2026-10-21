package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Treffer(Erklaerung erklaerung, GNDEntityFact gndEntityFact) {
  public Erklaerung getErklaerung() { return erklaerung; }
  public GNDEntityFact getGndEntityFact() { return gndEntityFact; }
}
