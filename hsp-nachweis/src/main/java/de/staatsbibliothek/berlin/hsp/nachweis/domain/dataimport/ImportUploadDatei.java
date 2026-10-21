package de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.05.2020.
 * @version 1.0
 */
public class ImportUploadDatei {

  private final String name;

  private final byte[] content;

  private final String contentType;

  public ImportUploadDatei(String name, byte[] content, String contentType) {
    this.name = name;
    this.content = content;
    this.contentType = contentType;
  }

  public String getName() {
    return name;
  }

  public byte[] getContent() {
    return content;
  }

  public String getContentType() {
    return contentType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ImportUploadDatei)) {
      return false;
    }
    ImportUploadDatei that = (ImportUploadDatei) o;
    return name.equals(that.name) &&
        Arrays.equals(content, that.content) &&
        contentType.equals(that.contentType);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(name, contentType);
    result = 31 * result + Arrays.hashCode(content);
    return result;
  }

  @Override
  public String toString() {
    return "ImportDatei{" +
        "name='" + name + '\'' +
        ", content=" + Arrays.toString(content) +
        ", contentType='" + contentType + '\'' +
        '}';
  }
}
