package de.staatsbibliothek.berlin.hsp.nachweis.domain.gndservice;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDServiceSubset;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.05.25.
 * @version 1.0
 */

public record GNDSynchronization(String id, String name, int intervalInMinutes,
                                 boolean active,
                                 String kafkaTopicName,
                                 Set<String> gndids,
                                 List<GNDSynchronizationBatchJob> jobs) implements
    Serializable {

  @Serial
  private static final long serialVersionUID = 6608707508444672956L;

  public static GNDSynchronization fromSubset(
      GNDServiceSubset subset) {

    if (subset == null || subset.getStorageTarget() == null) {
      throw new IllegalArgumentException("GNDServiceSubset needs to be set");
    }

    return new GNDSynchronization(subset.getId(), subset.getName(),
        subset.getIntervalInMinutes(),
        subset.isActive(), removePrefixFromStorageTarget(subset),
        subset.getGndIDs(), new ArrayList<>(subset.getJobs()));
  }

  static String removePrefixFromStorageTarget(GNDServiceSubset subset) {
    return subset.getStorageTarget().replace("kafka/", "");
  }


  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GNDSynchronization that = (GNDSynchronization) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "GNDSynchronization{" + "id='" + id + '\''
        + ", name='" + name + '\''
        + ", intervalInMinutes=" + intervalInMinutes
        + ", active=" + active
        + ", kafkaTopicName='" + kafkaTopicName + '\''
        + ", gndids=" + gndids
        + ", jobs=" + jobs
        + '}';
  }
}
