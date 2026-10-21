package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Erklaerung(String typ, String ursprung, String ziel, String algorithmus,
                         float konfidenz, float grundwert, float wertreduktion, String quelle) {
  public static final String QUELLE_GRAPH = "GRAPH";
  public static final String QUELLE_LOBID = "LOBID";
}
