package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 20.07.21
 */

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Identifier {

  public static final String TEXT = "text";
  public static final String URL = "url";
  public static final String TYPE = "type";
  public static final String URI_TYPE = "URI";

  public static final Set<Field> FIELDS = new LinkedHashSet<>(Arrays.asList(
      Field.of(Identifier.TYPE),
      Field.of(Identifier.TEXT),
      Field.of(Identifier.URL)));

  @JsonProperty(TEXT)
  private String text;

  @JsonProperty(URL)
  private String url;

  @JsonProperty(TYPE)
  private String type;

  public Identifier() {
  }

  public Identifier(String text, String url, String type) {
    this.text = text;
    this.url = url;
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Identifier that = (Identifier) o;
    return Objects.equals(text, that.text) && Objects.equals(url, that.url) && Objects
        .equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, url, type);
  }

  @Override
  public String toString() {
    return "Identifier{" +
        "text='" + text + '\'' +
        ", url='" + url + '\'' +
        ", type='" + type + '\'' +
        '}';
  }
}
