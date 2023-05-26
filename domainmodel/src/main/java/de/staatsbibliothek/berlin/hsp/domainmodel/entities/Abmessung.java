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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AbmessungWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class Abmessung extends AttributsReferenz {

  private static final long serialVersionUID = -8245489483621624837L;

  private final String text;
  private final List<AbmessungWert> abmessungWerte;

  private Abmessung(AbmessungBuilder builder) {
    super(builder);
    this.abmessungWerte = builder.abmessungWerte;
    this.text = builder.abmessungWerte.stream()
        .map(AbmessungWert::getText)
        .collect(Collectors.joining("; "));
  }

  public static Abmessung.AbmessungBuilder builder() {
    return new Abmessung.AbmessungBuilder();
  }

  @Override
  public String getText() {
    return text;
  }

  public List<AbmessungWert> getAbmessungWerte() {
    return abmessungWerte;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Abmessung.class.getSimpleName() + "[", "]")
        .add("id='" + getId() + "'")
        .add("attributsTyp=" + getAttributsTyp())
        .add("referenzId='" + getReferenzId() + "'")
        .add("referenzTyp=" + getReferenzTyp())
        .add("text='" + text + "'")
        .add("abmessungWerte=" + abmessungWerte)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Abmessung)) {
      return false;
    }
    Abmessung that = (Abmessung) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class AbmessungBuilder extends AttributsReferenzBuilder<Abmessung.AbmessungBuilder> {

    private final List<AbmessungWert> abmessungWerte = new ArrayList<>();

    protected AbmessungBuilder() {
      super(AttributsTyp.ABMESSUNG);
    }

    @Override
    public Abmessung.AbmessungBuilder getThis() {
      return this;
    }

    public Abmessung.AbmessungBuilder withAbmessungWerte(List<AbmessungWert> abmessungWerte) {
      this.abmessungWerte.clear();
      if (Objects.nonNull(abmessungWerte)) {
        this.abmessungWerte.addAll(abmessungWerte);
      }
      return this;
    }

    public Abmessung.AbmessungBuilder addAbmessungWert(AbmessungWert abmessungWert) {
      if (Objects.nonNull(abmessungWert)) {
        this.abmessungWerte.add(abmessungWert);
      }
      return this;
    }

    public Abmessung build() {
      return new Abmessung(this);
    }
  }

}
