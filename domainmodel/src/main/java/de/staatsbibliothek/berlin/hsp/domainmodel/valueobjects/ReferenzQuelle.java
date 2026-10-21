package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @author Konrad.Eichstaedt@sbb.spk-berlin.de
 * @since 10.05.19
 */
@Entity
@Table
@Cacheable
public class ReferenzQuelle {

  @Id
  String typ;

  @Column
  String label;

  protected ReferenzQuelle() {}

  public ReferenzQuelle(String typ, String label) {
    this.typ = typ;
    this.label = label;
  }

  /**
   * @return <code>String</code> The Typ of external System like E-Codices or Manumed.
   */
  public String getTyp() {
    return typ;
  }

  /**
   * @return <code>String</code> The label as title of the URL.
   */
  public String getLabel() {
    return label;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ReferenzQuelle)) {
      return false;
    }
    ReferenzQuelle that = (ReferenzQuelle) o;
    return Objects.equals(typ, that.typ) && Objects.equals(label, that.label);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typ, label);
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("ReferenzQuelle{");
    sb.append("typ='").append(typ).append('\'');
    sb.append(", label='").append(label).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
