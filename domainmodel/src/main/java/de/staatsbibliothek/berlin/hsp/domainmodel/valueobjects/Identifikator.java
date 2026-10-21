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
public class Identifikator implements Serializable {

  public static final String GEONAMES_TYPE = "Geonames";
  public static final String GETTY_TYPE = "Getty";
  public static final String ISIL_IDENTIFIER_TYPE = "Isil";
  public static final String ISO_639_1_TYPE = "ISO_639-1";
  public static final String ISO_639_2_TYPE = "ISO_639-2";

  private static final long serialVersionUID = -5181223872228870395L;

  @Column(length = 1024, nullable = false)
  String text;

  @Column(length = 1024)
  String url;

  @Column(length = 255, nullable = false)
  String type;

  public Identifikator() {
  }

  public Identifikator(String text, String url, String type) {
    this.text = text;
    this.url = url;
    this.type = type;
  }

  public String getText() {
    return text;
  }

  public String getUrl() {
    return url;
  }

  public String getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Identifikator that = (Identifikator) o;
    return Objects.equals(text, that.text)
        && Objects.equals(url, that.url)
        && Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, url, type);
  }

  @Override
  public String toString() {
    return "Identifikator{"
        + "text='"
        + text
        + '\''
        + ", url='"
        + url
        + '\''
        + ", type='"
        + type
        + '\''
        + '}';
  }
}
