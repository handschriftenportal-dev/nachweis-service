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

import java.util.Objects;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @author Konrad.Eichstaedt@sbb.spk-berlin.de
 * @since 10.05.19
 */
@Entity
@Table
@Cacheable
public class ReferenzQuelle {

  @Id
  private String typ;

  @Column
  private String label;

  protected ReferenzQuelle() {
  }

  public ReferenzQuelle(String typ, String label) {
    this.typ = typ;
    this.label = label;
  }

  /**
   * @return <code>String</code> The Typ of external System like E-Codices or Manumed.
   */

  public String getTyp() {
    return typ;
  }

  /**
   * @return <code>String</code> The label as title of the URL.
   */

  public String getLabel() {
    return label;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ReferenzQuelle)) {
      return false;
    }
    ReferenzQuelle that = (ReferenzQuelle) o;
    return Objects.equals(typ, that.typ) &&
        Objects.equals(label, that.label);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typ, label);
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("ReferenzQuelle{");
    sb.append("typ='").append(typ).append('\'');
    sb.append(", label='").append(label).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
