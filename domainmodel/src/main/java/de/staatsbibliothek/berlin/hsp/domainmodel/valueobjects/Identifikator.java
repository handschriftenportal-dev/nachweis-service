/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.hibernate.annotations.SelectBeforeUpdate;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 20.07.21
 */

@Embeddable
@SelectBeforeUpdate
@Cacheable
public class Identifikator implements Serializable {

  public static final String GEONAMES_TYPE = "Geonames";
  public static final String GETTY_TYPE = "Getty";
  public static final String ISIL_IDENTIFIER_TYPE = "Isil";
  public static final String ISO_639_1_TYPE = "ISO_639-1";
  public static final String ISO_639_2_TYPE = "ISO_639-2";

  private static final long serialVersionUID = -5181223872228870395L;

  @Column(length = 1024, nullable = false)
  private String text;

  @Column(length = 1024)
  private String url;

  @Column(length = 255, nullable = false)
  private String type;

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
    return Objects.equals(text, that.text) && Objects.equals(url, that.url) && Objects
        .equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, url, type);
  }

  @Override
  public String toString() {
    return "Identifikator{" +
        "text='" + text + '\'' +
        ", url='" + url + '\'' +
        ", type='" + type + '\'' +
        '}';
  }
}
