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

public class FormatWert implements Serializable {

  private static final long serialVersionUID = -2608474152307474382L;

  private final String id;
  private final String text;
  private final String typ;

  private FormatWert(FormatWertBuilder builder) {
    this.id = HSP_UUI_Factory.generate();
    this.text = builder.text;
    this.typ = builder.typ;
  }

  public static FormatWertBuilder builder() {
    return new FormatWertBuilder();
  }

  public String getId() {
    return id;
  }

  public String getText() {
    return text;
  }

  public String getTyp() {
    return typ;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FormatWert)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    FormatWert that = (FormatWert) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), id);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", FormatWert.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("text='" + text + "'")
        .add("typ='" + typ + "'")
        .toString();
  }

  public static class FormatWertBuilder {

    private String text;
    private String typ;

    public FormatWertBuilder withText(String text) {
      this.text = text;
      if (Objects.nonNull(this.text)) {
        this.text = this.text.replace(" ", "_");
      }
      return this;
    }

    public FormatWertBuilder withTyp(String typ) {
      this.typ = typ;
      return this;
    }

    public FormatWert build() {
      return new FormatWert(this);
    }
  }
}
