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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class Grundsprache extends AttributsReferenz {

  private static final long serialVersionUID = 2937942008922697538L;

  private final String text;

  private final List<String> normdatenIds;

  private Grundsprache(GrundspracheBuilder builder) {
    super(builder);
    this.text = builder.text;
    this.normdatenIds = builder.normdatenIds;
  }

  public static GrundspracheBuilder builder() {
    return new GrundspracheBuilder();
  }

  public List<String> getNormdatenIds() {
    return normdatenIds;
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Grundsprache.class.getSimpleName() + "[", "]")
        .add("id='" + getId() + "'")
        .add("attributsTyp=" + getAttributsTyp())
        .add("referenzId='" + getReferenzId() + "'")
        .add("referenzTyp=" + getReferenzTyp())
        .add("text='" + text + "'")
        .add("normdatenIds=" + normdatenIds)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Grundsprache)) {
      return false;
    }
    Grundsprache that = (Grundsprache) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class GrundspracheBuilder extends AttributsReferenzBuilder<GrundspracheBuilder> {

    private final List<String> normdatenIds = new ArrayList<>();
    private String text = "";

    public GrundspracheBuilder() {
      super(AttributsTyp.GRUNDSPRACHE);
    }

    @Override
    public GrundspracheBuilder getThis() {
      return this;
    }

    public GrundspracheBuilder withText(String text) {
      this.text = text;
      return getThis();
    }

    public GrundspracheBuilder withNormdatenIds(List<String> normdatenIds) {
      this.normdatenIds.clear();
      if (Objects.nonNull(normdatenIds)) {
        this.normdatenIds.addAll(normdatenIds);
      }
      return this;
    }

    public GrundspracheBuilder addNormdatenId(String normdatenId) {
      if (Objects.nonNull(normdatenId) && !normdatenId.isEmpty()) {
        this.normdatenIds.add(normdatenId);
      }
      return this;
    }

    public Grundsprache build() {
      return new Grundsprache(this);
    }

  }

}
