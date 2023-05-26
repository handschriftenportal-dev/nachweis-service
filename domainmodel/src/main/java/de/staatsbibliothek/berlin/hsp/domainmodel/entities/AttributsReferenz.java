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

import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public abstract class AttributsReferenz implements Serializable {

  private static final long serialVersionUID = 2388197042451507870L;

  private final String id;

  private final AttributsTyp attributsTyp;

  private final String referenzId;

  private String referenzURL;

  private final AttributsReferenzTyp referenzTyp;

  protected AttributsReferenz(AttributsReferenzBuilder<?> builder) {
    Objects.requireNonNull(builder.attributsTyp, "attributsTyp is required!");

    this.id = HSP_UUI_Factory.generate(String.join("_",
        Objects.nonNull(builder.referenzId) ? builder.referenzId : UUID.randomUUID().toString(),
        builder.attributsTyp.name()).getBytes(StandardCharsets.UTF_8));

    this.attributsTyp = builder.attributsTyp;
    this.referenzId = builder.referenzId;
    this.referenzTyp = builder.referenzTyp;
    this.referenzURL = builder.referenzURL;
  }

  public String getId() {
    return id;
  }

  public AttributsTyp getAttributsTyp() {
    return attributsTyp;
  }

  public String getReferenzId() {
    return referenzId;
  }

  public AttributsReferenzTyp getReferenzTyp() {
    return referenzTyp;
  }

  public String getReferenzURL() {
    return referenzURL;
  }

  public void setReferenzURL(String referenzURL) {
    this.referenzURL = referenzURL;
  }

  public abstract String getText();

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("AttributsReferenz{");
    sb.append("id='").append(id).append('\'');
    sb.append(", attributsTyp=").append(attributsTyp);
    sb.append(", referenzId='").append(referenzId).append('\'');
    sb.append(", referenzURL='").append(referenzURL).append('\'');
    sb.append(", referenzTyp=").append(referenzTyp);
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AttributsReferenz)) {
      return false;
    }
    AttributsReferenz that = (AttributsReferenz) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public abstract static class AttributsReferenzBuilder<T extends AttributsReferenzBuilder<T>> {

    protected final AttributsTyp attributsTyp;
    protected String referenzId;
    protected String referenzURL;
    protected AttributsReferenzTyp referenzTyp;

    protected AttributsReferenzBuilder(AttributsTyp attributsTyp) {
      this.attributsTyp = attributsTyp;
    }

    public abstract T getThis();

    public T withReferenzId(String referenzId) {
      this.referenzId = referenzId;
      return getThis();
    }

    public T withReferenzURL(String referenzURL) {
      this.referenzURL = referenzURL;
      return getThis();
    }

    public T withReferenzTyp(AttributsReferenzTyp referenzTyp) {
      this.referenzTyp = referenzTyp;
      return getThis();
    }

  }


}
