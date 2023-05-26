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
import java.util.Objects;
import java.util.StringJoiner;

public class Umfang extends AttributsReferenz {

  private static final long serialVersionUID = 580583629974776067L;

  private final String text;

  private final String blattzahl;

  private Umfang(UmfangBuilder builder) {
    super(builder);
    this.text = builder.text;
    this.blattzahl = builder.blattzahl;
  }

  public static UmfangBuilder builder() {
    return new UmfangBuilder();
  }

  @Override
  public String getText() {
    return text;
  }

  public String getBlattzahl() {
    return blattzahl;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Umfang.class.getSimpleName() + "[", "]")
        .add("id='" + getId() + "'")
        .add("attributsTyp=" + getAttributsTyp())
        .add("referenzId='" + getReferenzId() + "'")
        .add("referenzTyp=" + getReferenzTyp())
        .add("text='" + text + "'")
        .add("blattzahl=" + blattzahl)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Umfang)) {
      return false;
    }
    Umfang that = (Umfang) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class UmfangBuilder extends AttributsReferenzBuilder<UmfangBuilder> {

    private String text = "";
    private String blattzahl;

    public UmfangBuilder() {
      super(AttributsTyp.UMFANG);
    }

    @Override
    public UmfangBuilder getThis() {
      return this;
    }

    public UmfangBuilder withText(String text) {
      this.text = text;
      return this;
    }

    public UmfangBuilder withBlattzahl(String blattzahl) {
      this.blattzahl = blattzahl;
      return this;
    }

    public Umfang build() {
      return new Umfang(this);
    }
  }

}
