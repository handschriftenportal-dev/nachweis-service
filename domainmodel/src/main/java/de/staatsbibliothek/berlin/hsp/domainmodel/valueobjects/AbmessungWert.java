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

import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class AbmessungWert implements Serializable {

  private static final long serialVersionUID = 2701734105358266259L;

  private String id;
  private String text;
  private String hoehe;
  private String breite;
  private String tiefe;
  private String typ;

  private AbmessungWert(AbmessungWertBuilder builder) {
    this.id = HSP_UUI_Factory.generate();
    this.text = builder.text;
    this.hoehe = builder.hoehe;
    this.breite = builder.breite;
    this.tiefe = builder.tiefe;
    this.typ = builder.typ;
  }

  public static AbmessungWertBuilder builder() {
    return new AbmessungWertBuilder();
  }

  public String getId() {
    return id;
  }

  public String getText() {
    return text;
  }

  public String getHoehe() {
    return hoehe;
  }

  public String getBreite() {
    return breite;
  }

  public String getTiefe() {
    return tiefe;
  }

  public String getTyp() {
    return typ;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AbmessungWert that = (AbmessungWert) o;

    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", AbmessungWert.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("text='" + text + "'")
        .add("hoehe='" + hoehe + "'")
        .add("breite='" + breite + "'")
        .add("tiefe='" + tiefe + "'")
        .add("typ='" + typ + "'")
        .toString();
  }

  public static class AbmessungWertBuilder {

    private String text;
    private String hoehe;
    private String breite;
    private String tiefe;
    private String typ;

    public AbmessungWertBuilder withText(String text) {
      this.text = text;
      return this;
    }

    public AbmessungWertBuilder withHoehe(String hoehe) {
      this.hoehe = hoehe;
      return this;
    }

    public AbmessungWertBuilder withBreite(String breite) {
      this.breite = breite;
      return this;
    }

    public AbmessungWertBuilder withTiefe(String tiefe) {
      this.tiefe = tiefe;
      return this;
    }

    public AbmessungWertBuilder withTyp(String typ) {
      this.typ = typ;
      return this;
    }

    public AbmessungWert build() {
      return new AbmessungWert(this);
    }
  }

}
