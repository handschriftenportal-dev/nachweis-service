package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 20.07.21
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class VariantName {

  public static final String NAME = "name";
  public static final String LANGUAGE_CODE = "languageCode";

  public static final Set<Field> FIELDS = new LinkedHashSet<>(Arrays.asList(
      Field.of(VariantName.NAME),
      Field.of(VariantName.LANGUAGE_CODE)));

  @JsonProperty(NAME)
  private String name;

  @JsonProperty(LANGUAGE_CODE)
  private String languageCode;

  public VariantName() {
  }

  public VariantName(String name) {
    this.name = name;
  }

  public VariantName(String name, String languageCode) {
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
    if (!(o instanceof VariantName)) {
      return false;
    }
    VariantName that = (VariantName) o;
    return name.equals(that.name) && Objects.equals(languageCode, that.languageCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, languageCode);
  }

  @Override
  public String toString() {
    return "VariantName{" +
        "name='" + name + '\'' +
        ", languageCode='" + languageCode + '\'' +
        '}';
  }
}
