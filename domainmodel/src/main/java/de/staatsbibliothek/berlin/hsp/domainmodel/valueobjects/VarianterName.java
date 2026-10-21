package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 20.07.21
 */
@Embeddable
@Cacheable
public class VarianterName implements Serializable {

  private static final long serialVersionUID = 3035872324443896908L;

  @Column(length = 1024, nullable = false)
  String name;

  @Column(length = 6)
  String languageCode;

  public VarianterName() {
  }

  public VarianterName(String name, String languageCode) {
    this.name = name;
    this.languageCode = languageCode;
  }

  public String getName() {
    return name;
  }

  public String getLanguageCode() {
    return languageCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VarianterName that = (VarianterName) o;
    return Objects.equals(name, that.name) && Objects.equals(languageCode, that.languageCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, languageCode);
  }

  @Override
  public String toString() {
    return "VarianterName{"
        + "name='"
        + name
        + '\''
        + ", languageCode='"
        + languageCode
        + '\''
        + '}';
  }
}
