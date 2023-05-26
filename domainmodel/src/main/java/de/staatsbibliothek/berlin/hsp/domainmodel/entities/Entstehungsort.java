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
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class Entstehungsort extends AttributsReferenz {

  private static final long serialVersionUID = 7913375471947530821L;

  private final String text;

  private final List<NormdatenReferenz> normdatenReferenzen;

  private Entstehungsort(EntstehungsortBuilder builder) {
    super(builder);
    this.text = builder.text;
    this.normdatenReferenzen = builder.normdatenReferenzen;
  }

  public static EntstehungsortBuilder builder() {
    return new EntstehungsortBuilder();
  }

  public List<NormdatenReferenz> getNormdatenReferenzen() {
    return normdatenReferenzen;
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Entstehungsort.class.getSimpleName() + "[", "]")
        .add("id='" + getId() + "'")
        .add("attributsTyp=" + getAttributsTyp())
        .add("referenzId='" + getReferenzId() + "'")
        .add("referenzTyp=" + getReferenzTyp())
        .add("text='" + text + "'")
        .add("normdatenReferenzen=" + normdatenReferenzen)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Entstehungsort)) {
      return false;
    }
    Entstehungsort that = (Entstehungsort) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class EntstehungsortBuilder extends AttributsReferenzBuilder<EntstehungsortBuilder> {

    private final List<NormdatenReferenz> normdatenReferenzen = new ArrayList<>();
    private String text = "";

    public EntstehungsortBuilder() {
      super(AttributsTyp.ENTSTEHUNGSORT);
    }

    @Override
    public EntstehungsortBuilder getThis() {
      return this;
    }

    public EntstehungsortBuilder withText(String text) {
      this.text = text;
      return this;
    }

    public EntstehungsortBuilder withNormdatenReferenzen(List<NormdatenReferenz> normdatenReferenzen) {
      this.normdatenReferenzen.clear();
      if (Objects.nonNull(normdatenReferenzen)) {
        this.normdatenReferenzen.addAll(normdatenReferenzen);
      }
      return this;
    }

    public EntstehungsortBuilder addNormdatenReferenz(NormdatenReferenz normdatenReferenz) {
      if (Objects.nonNull(normdatenReferenz)) {
        this.normdatenReferenzen.add(normdatenReferenz);
      }
      return this;
    }

    public Entstehungsort build() {
      return new Entstehungsort(this);
    }

  }
}
