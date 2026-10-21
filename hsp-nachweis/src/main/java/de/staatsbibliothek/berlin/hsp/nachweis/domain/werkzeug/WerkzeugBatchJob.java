package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 08.06.23.
 * @version 1.0
 */
@Getter
public class WerkzeugBatchJob {

  private final String name;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private String successMessage;

  private WerkzeugBatchJobStatus status;

  private Bearbeiter bearbeiter;

  private byte[] download;

  public WerkzeugBatchJob(String name) {
    this.name = name;
    this.status = WerkzeugBatchJobStatus.NOT_RUNNING;
    this.successMessage = "";
  }

  public void start(Bearbeiter bearbeiter) {
    this.startTime = LocalDateTime.now();
    this.status = WerkzeugBatchJobStatus.RUNNING;
    this.bearbeiter = bearbeiter;
    this.endTime = null;
    this.successMessage = null;
  }

  public void end(String successMessage) {
    this.endTime = LocalDateTime.now();
    this.status = WerkzeugBatchJobStatus.NOT_RUNNING;
    this.successMessage = successMessage;
  }

  public void endWithContent(String successMessage, byte[] download) {
    this.endTime = LocalDateTime.now();
    this.status = WerkzeugBatchJobStatus.NOT_RUNNING;
    this.successMessage = successMessage;
    this.download = download;
  }

  public void fail(String successMessage) {
    this.endTime = LocalDateTime.now();
    this.status = WerkzeugBatchJobStatus.ERROR;
    this.successMessage = successMessage;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("WerkzeugEvent{");
    sb.append("name='").append(name).append('\'');
    sb.append(", startTime=").append(startTime);
    sb.append(", endTime=").append(endTime);
    sb.append(", sucessMessage='").append(successMessage).append('\'');
    sb.append(", status='").append(status).append('\'');
    sb.append(", bearbeiter='").append(bearbeiter).append('\'');
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WerkzeugBatchJob that = (WerkzeugBatchJob) o;
    return Objects.equals(name, that.name) && Objects.equals(startTime,
        that.startTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, startTime);
  }
}
