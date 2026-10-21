package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 11.11.22
 */
@Entity
@Table(name = "konfiguration")
public class Konfiguration {

  @Id
  @Column(name = "schluessel", length = 1024, nullable = false)
  String schluessel;

  @Column(name = "wert", length = 1024, nullable = false)
  String wert;

  public Konfiguration(String schluessel, String wert) {
    this.schluessel = schluessel;
    this.wert = wert;
  }

  public Konfiguration() {}

  public String getSchluessel() {
    return schluessel;
  }

  public String getWert() {
    return wert;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Konfiguration)) {
      return false;
    }
    Konfiguration that = (Konfiguration) o;
    return Objects.equals(schluessel, that.schluessel);
  }

  @Override
  public int hashCode() {
    return Objects.hash(schluessel);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Konfiguration.class.getSimpleName() + "[", "]")
        .add("schluessel='" + schluessel + "'")
        .add("wert='" + wert + "'")
        .toString();
  }
}
