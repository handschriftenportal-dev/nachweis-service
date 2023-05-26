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

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * Holds the target Document to lock
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 17.08.21
 */
@Entity
@Table(name = "sperre_eintrag")
@IdClass(SperreEintragPK.class)
public class SperreEintrag implements Serializable, Comparable<SperreEintrag> {

  private static final long serialVersionUID = -5611339106072600690L;

  @Id
  @Column(name = "target_id", length = 64)
  private String targetId;

  @Id
  @Column(name = "target_type", length = 32)
  @Enumerated(EnumType.STRING)
  private SperreDokumentTyp targetType;

  public SperreEintrag() {
  }

  public SperreEintrag(String targetId, SperreDokumentTyp targetType) {
    this.targetId = targetId;
    this.targetType = targetType;
  }

  public String getTargetId() {
    return targetId;
  }

  public SperreDokumentTyp getTargetType() {
    return targetType;
  }

  @Override
  public int compareTo(SperreEintrag other) {
    if (other == null) {
      return 1;
    }
    return toString().compareTo(other.toString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SperreEintrag sperreEintrag = (SperreEintrag) o;
    return Objects.equals(targetId, sperreEintrag.targetId) && targetType == sperreEintrag.targetType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(targetId, targetType);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", SperreEintrag.class.getSimpleName() + "[", "]")
        .add("targetId='" + targetId + "'")
        .add("targetType=" + targetType)
        .toString();
  }
}
