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
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.EntstehungszeitWert;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class Entstehungszeit extends AttributsReferenz {

  private static final long serialVersionUID = -8245489483621624837L;

  private final String text;
  private final List<EntstehungszeitWert> entstehungszeitWerte;

  private Entstehungszeit(EntstehungszeitBuilder builder) {
    super(builder);
    this.entstehungszeitWerte = builder.entstehungszeitWerte;
    this.text = builder.entstehungszeitWerte.stream()
        .map(EntstehungszeitWert::getText)
        .collect(Collectors.joining("; "));
  }

  public static EntstehungszeitBuilder builder() {
    return new EntstehungszeitBuilder();
  }

  @Override
  public String getText() {
    return text;
  }

  public List<EntstehungszeitWert> getEntstehungszeitWerte() {
    return entstehungszeitWerte;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Entstehungszeit.class.getSimpleName() + "[", "]")
        .add("id='" + getId() + "'")
        .add("attributsTyp=" + getAttributsTyp())
        .add("referenzId='" + getReferenzId() + "'")
        .add("referenzTyp=" + getReferenzTyp())
        .add("text='" + text + "'")
        .add("entstehungszeitWerte=" + entstehungszeitWerte)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Entstehungszeit)) {
      return false;
    }
    Entstehungszeit that = (Entstehungszeit) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class EntstehungszeitBuilder extends AttributsReferenzBuilder<EntstehungszeitBuilder> {

    private final List<EntstehungszeitWert> entstehungszeitWerte = new ArrayList<>();

    protected EntstehungszeitBuilder() {
      super(AttributsTyp.ENTSTEHUNGSZEIT);
    }

    @Override
    public EntstehungszeitBuilder getThis() {
      return this;
    }

    public EntstehungszeitBuilder withEntstehungszeitWerte(List<EntstehungszeitWert> entstehungszeitWerte) {
      this.entstehungszeitWerte.clear();
      if (Objects.nonNull(entstehungszeitWerte)) {
        this.entstehungszeitWerte.addAll(entstehungszeitWerte);
      }
      return this;
    }

    public EntstehungszeitBuilder addEntstehungszeitWert(EntstehungszeitWert entstehungszeitWert) {
      if (Objects.nonNull(entstehungszeitWert)) {
        this.entstehungszeitWerte.add(entstehungszeitWert);
      }
      return this;
    }

    public Entstehungszeit build() {
      return new Entstehungszeit(this);
    }
  }

}
