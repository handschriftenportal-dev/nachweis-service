package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.hsp_frontend;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 16.11.23.
 * @version 1.0
 */

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscoveryServiceStatsDTO {

  @JsonProperty("kulturobjekte")
  private Kod kods;

  @JsonProperty("digitalisate")
  private Digitalisate digitizedItems;

  @JsonProperty("beschreibungen")
  private Description description;

  @Getter
  public static class Digitalisate {

    @JsonProperty("all")
    private int all;
    @JsonProperty("institution")
    private Map<String, Integer> items;
  }

  @Getter
  public static class Kod {

    @JsonProperty("all")
    private int all;
    @JsonProperty("institution")
    private Map<String, Integer> items;
  }

  @Getter
  public static class Description {

    @JsonProperty("all")
    private int all;
    @JsonProperty("institution")
    private Map<String, Integer> items;
    @JsonProperty("extern")
    private int external;
    @JsonProperty("intern")
    private int internal;
    @JsonProperty("retro")
    private int descriptionRetro;

  }
}
