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

package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class Format extends AttributsReferenz {

  private static final long serialVersionUID = -4262551536537015661L;

  private final String text;
  private final List<FormatWert> formatWerte;

  private Format(FormatBuilder builder) {
    super(builder);
    this.formatWerte = builder.formatWerte;
    this.text = builder.formatWerte.stream()
        .map(FormatWert::getText)
        .collect(Collectors.joining(";"));
  }

  public static FormatBuilder builder() {
    return new FormatBuilder();
  }

  @Override
  public String getText() {
    return text;
  }

  public List<FormatWert> getFormatWerte() {
    return formatWerte;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Format.class.getSimpleName() + "[", "]")
        .add("id='" + getId() + "'")
        .add("attributsTyp=" + getAttributsTyp())
        .add("referenzId='" + getReferenzId() + "'")
        .add("referenzTyp=" + getReferenzTyp())
        .add("text='" + text + "'")
        .add("formatWerte=" + formatWerte)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Format)) {
      return false;
    }
    Format that = (Format) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class FormatBuilder extends AttributsReferenzBuilder<FormatBuilder> {

    private final List<FormatWert> formatWerte = new ArrayList<>();

    protected FormatBuilder() {
      super(AttributsTyp.FORMAT);
    }

    @Override
    public FormatBuilder getThis() {
      return this;
    }

    public FormatBuilder withFormatWerte(
        List<FormatWert> formatWerte) {
      this.formatWerte.clear();
      if (Objects.nonNull(formatWerte)) {
        this.formatWerte.addAll(formatWerte);
      }
      return this;
    }

    public FormatBuilder addFormatWert(FormatWert formatWert) {
      if (Objects.nonNull(formatWert)) {
        this.formatWerte.add(formatWert);
      }
      return this;
    }

    public Format build() {
      return new Format(this);
    }
  }
}
