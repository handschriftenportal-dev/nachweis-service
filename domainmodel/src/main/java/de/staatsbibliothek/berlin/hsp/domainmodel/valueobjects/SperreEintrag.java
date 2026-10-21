package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Holds the target Document to lock
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 17.08.21
 */
@Entity
@Table(name = "sperre_eintrag")
@IdClass(SperreEintragPK.class)
public class SperreEintrag implements Serializable, Comparable<SperreEintrag> {

  private static final long serialVersionUID = -5611339106072600690L;

  @Id
  @Column(name = "target_id", length = 64)
  String targetId;

  @Id
  @Column(name = "target_type", length = 32)
  @Enumerated(EnumType.STRING)
  SperreDokumentTyp targetType;

  @Version
  private int version;

  public SperreEintrag() {
  }

  public SperreEintrag(String targetId, SperreDokumentTyp targetType) {
    this.targetId = targetId;
    this.targetType = targetType;
  }

  public String getTargetId() {
    return targetId;
  }

  public SperreDokumentTyp getTargetType() {
    return targetType;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  @Override
  public int compareTo(SperreEintrag other) {
    if (other == null) {
      return 1;
    }
    return toString().compareTo(other.toString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SperreEintrag sperreEintrag = (SperreEintrag) o;
    return Objects.equals(targetId, sperreEintrag.targetId)
        && targetType == sperreEintrag.targetType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(targetId, targetType);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", SperreEintrag.class.getSimpleName() + "[", "]")
        .add("targetId='" + targetId + "'")
        .add("targetType=" + targetType)
        .toString();
  }
}
