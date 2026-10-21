package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.gndservice.GNDSynchronizationBatchJob;
import java.util.Objects;
import java.util.Set;
import lombok.Data;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.05.25.
 * @version 1.0
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GNDServiceSubset {

  private String id;
  private String name;
  private boolean active;
  @JsonProperty("gndIDs")
  private Set<String> gndIDs;
  @JsonProperty("intervalInMinutes")
  private int intervalInMinutes;

  @JsonProperty("storageTarget")
  private String storageTarget;

  @JsonProperty("jobs")
  private Set<GNDSynchronizationBatchJob> jobs;

  GNDServiceSubset() {
  }

  public GNDServiceSubset(String id, String name, boolean active,
      Set<String> gndIDs,
      int intervalInMinutes, String storageTarget,
      Set<GNDSynchronizationBatchJob> jobs) {
    this.id = id;
    this.name = name;
    this.active = active;
    this.gndIDs = gndIDs;
    this.intervalInMinutes = intervalInMinutes;
    this.storageTarget = storageTarget;
    this.jobs = jobs;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    var that = (GNDServiceSubset) obj;
    return Objects.equals(this.id, that.id) &&
        Objects.equals(this.name, that.name) &&
        this.active == that.active &&
        Objects.equals(this.gndIDs, that.gndIDs) &&
        this.intervalInMinutes == that.intervalInMinutes;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, active, gndIDs, intervalInMinutes);
  }

  @Override
  public String toString() {
    return "GNDServiceSubset[" +
        "id=" + id + ", " +
        "name=" + name + ", " +
        "active=" + active + ", " +
        "gndIDs=" + gndIDs + ", " +
        "intervalInMinutes=" + intervalInMinutes + ']';
  }


}
