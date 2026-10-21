package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.hsp_frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 24.04.23.
 * @version 1.0
 */
public class DiscoveryServiceInfoDTO {

  private String component;

  private String description;

  private String version;

  private StatisticItems items;

  public String getComponent() {
    return component;
  }

  public void setComponent(String component) {
    this.component = component;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public StatisticItems getItems() {
    return items;
  }

  public void setItems(
      StatisticItems items) {
    this.items = items;
  }

  public class StatisticItems {
    @JsonProperty("hsp:object")
    private int hspObjects;

    @JsonProperty("hsp:description")
    private int hspDescriptions;

    @JsonProperty("hsp:digitized")
    private int hspDigitized;

    @JsonProperty("hsp:description_retro")
    private int hspDescriptionsRetro;

    public int getHspObjects() {
      return hspObjects;
    }

    public void setHspObjects(int hspObjects) {
      this.hspObjects = hspObjects;
    }

    public int getHspDescriptions() {
      return hspDescriptions;
    }

    public void setHspDescriptions(int hspDescriptions) {
      this.hspDescriptions = hspDescriptions;
    }

    public int getHspDigitized() {
      return hspDigitized;
    }

    public void setHspDigitized(int hspDigitized) {
      this.hspDigitized = hspDigitized;
    }

    public int getHspDescriptionsRetro() {
      return hspDescriptionsRetro;
    }

    public void setHspDescriptionsRetro(int hspDescriptionsRetro) {
      this.hspDescriptionsRetro = hspDescriptionsRetro;
    }
  }
}
