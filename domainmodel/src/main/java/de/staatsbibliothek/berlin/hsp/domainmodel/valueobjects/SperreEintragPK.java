package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * SperreEintrag Primary Key transport object
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 17.08.21
 */
public class SperreEintragPK implements Serializable, Comparable<SperreEintragPK> {

  private static final long serialVersionUID = -5611339106072600690L;

  String targetId;

  SperreDokumentTyp targetType;

  public SperreEintragPK() {
  }

  public SperreEintragPK(String targetId, SperreDokumentTyp targetType) {
    this.targetId = targetId;
    this.targetType = targetType;
  }

  public String getTargetId() {
    return targetId;
  }

  public SperreDokumentTyp getTargetType() {
    return targetType;
  }

  @Override
  public int compareTo(SperreEintragPK other) {
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
    SperreEintragPK that = (SperreEintragPK) o;
    return Objects.equals(targetId, that.targetId) && targetType == that.targetType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(targetId, targetType);
  }


  @Override
  public String toString() {
    return new StringJoiner(", ", SperreEintragPK.class.getSimpleName() + "[", "]")
        .add("targetId='" + targetId + "'")
        .add("targetType=" + targetType)
        .toString();
  }
}
