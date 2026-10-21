package de.staatsbibliothek.berlin.hsp.nachweis.domain.gndservice;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.05.25.
 * @version 1.0
 */
public record GNDSynchronizationBatchJob(String id, LocalDateTime creationDate,
                                         LocalDateTime endDate,
                                         int numberOfExportedRecord,
                                         String status) {

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GNDSynchronizationBatchJob that = (GNDSynchronizationBatchJob) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "GNDSynchronizationBatchJob{" + "id='" + id + '\''
        + ", creationDate=" + creationDate
        + ", endDate=" + endDate
        + ", numberOfExportedRecord=" + numberOfExportedRecord
        + ", status='" + status + '\''
        + '}';
  }
}
