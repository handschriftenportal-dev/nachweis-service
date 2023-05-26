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

public class Beschreibstoff extends AttributsReferenz {

  private static final long serialVersionUID = 580583629974776067L;

  private final String text;

  private final List<String> typen;

  private Beschreibstoff(BeschreibstoffBuilder builder) {
    super(builder);
    this.text = builder.text;
    this.typen = builder.typen;
  }

  public static BeschreibstoffBuilder builder() {
    return new BeschreibstoffBuilder();
  }

  @Override
  public String getText() {
    return text;
  }

  public List<String> getTypen() {
    return typen;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Beschreibstoff.class.getSimpleName() + "[", "]")
        .add("id='" + getId() + "'")
        .add("attributsTyp=" + getAttributsTyp())
        .add("referenzId='" + getReferenzId() + "'")
        .add("referenzTyp=" + getReferenzTyp())
        .add("text='" + text + "'")
        .add("typen=" + typen)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Beschreibstoff)) {
      return false;
    }
    Beschreibstoff that = (Beschreibstoff) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class BeschreibstoffBuilder extends AttributsReferenzBuilder<BeschreibstoffBuilder> {

    private final List<String> typen = new ArrayList<>();
    private String text = "";

    public BeschreibstoffBuilder() {
      super(AttributsTyp.BESCHREIBSTOFF);
    }

    @Override
    public BeschreibstoffBuilder getThis() {
      return this;
    }

    public BeschreibstoffBuilder withText(String text) {
      this.text = text;
      return this;
    }

    public BeschreibstoffBuilder withTypen(List<String> typen) {
      this.typen.clear();
      if (Objects.nonNull(typen)) {
        this.typen.addAll(typen);
      }
      return this;
    }

    public BeschreibstoffBuilder addTyp(String typ) {
      if (Objects.nonNull(typ)) {
        this.typen.add(typ);
      }
      return this;
    }

    public Beschreibstoff build() {
      return new Beschreibstoff(this);
    }
  }

}
