package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 10.06.2020.
 * @version 1.0
 */
public class DataEntity {

  public DataEntity() {
  }

  public DataEntity(String id, String label, String url) {
    this.id = id;
    this.label = label;
    this.url = url;
  }

  @JsonProperty(ImportServiceAPI.IMPORTJOB_ID_FIELD)
  private String id;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_LABEL_FIELD)
  private String label;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_URL_FIELD)
  private String url;

  @JsonIgnore
  private String dateiName;

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public String getUrl() {
    return url;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getDateiName() {
    return dateiName;
  }

  public void setDateiName(String dateiName) {
    this.dateiName = dateiName;
  }
}
