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

public class EntstehungszeitWert implements Serializable {

  private static final long serialVersionUID = -8245489483621624837L;

  private final String id;
  private final String text;
  private final String nichtVor;
  private final String nichtNach;
  private final String typ;

  private EntstehungszeitWert(EntstehungszeitWertBuilder builder) {
    this.id = HSP_UUI_Factory.generate();
    this.text = builder.text;
    this.nichtVor = builder.nichtVor;
    this.nichtNach = builder.nichtNach;
    this.typ = builder.typ;
  }

  public static EntstehungszeitWertBuilder builder() {
    return new EntstehungszeitWertBuilder();
  }

  public String getId() {
    return id;
  }

  public String getText() {
    return text;
  }

  public String getNichtVor() {
    return nichtVor;
  }

  public String getNichtNach() {
    return nichtNach;
  }

  public String getTyp() {
    return typ;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EntstehungszeitWert)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    EntstehungszeitWert that = (EntstehungszeitWert) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), id);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", EntstehungszeitWert.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("text='" + text + "'")
        .add("nichtVor='" + nichtVor + "'")
        .add("nichtNach='" + nichtNach + "'")
        .add("typ='" + typ + "'")
        .toString();
  }

  public static class EntstehungszeitWertBuilder {

    private String text;
    private String nichtVor;
    private String nichtNach;
    private String typ;

    public EntstehungszeitWertBuilder withText(String text) {
      this.text = text;
      return this;
    }

    public EntstehungszeitWertBuilder withNichtVor(String nichtVor) {
      this.nichtVor = nichtVor;
      return this;
    }

    public EntstehungszeitWertBuilder withNichtNach(String nichtNach) {
      this.nichtNach = nichtNach;
      return this;
    }

    public EntstehungszeitWertBuilder withTyp(String typ) {
      this.typ = typ;
      return this;
    }

    public EntstehungszeitWert build() {
      return new EntstehungszeitWert(this);
    }
  }

}
