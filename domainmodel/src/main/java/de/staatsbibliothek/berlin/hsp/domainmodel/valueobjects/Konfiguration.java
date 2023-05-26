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

import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 11.11.22
 */
@Entity
@Table(name = "konfiguration")
public class Konfiguration {

  @Id
  @Column(name = "schluessel", length = 1024, nullable = false)
  private String schluessel;

  @Column(name = "wert", length = 1024, nullable = false)
  private String wert;

  public Konfiguration(String schluessel, String wert) {
    this.schluessel = schluessel;
    this.wert = wert;
  }

  public Konfiguration() {
  }

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
