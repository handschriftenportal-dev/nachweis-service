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

/**
 * SperreEintrag Primary Key transport object
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 17.08.21
 */
public class SperreEintragPK implements Serializable, Comparable<SperreEintragPK> {

  private static final long serialVersionUID = -5611339106072600690L;

  private String targetId;

  private SperreDokumentTyp targetType;

  public SperreEintragPK() {
  }

  public SperreEintragPK(String targetId, SperreDokumentTyp targetType) {
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
  public int compareTo(SperreEintragPK other) {
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
    SperreEintragPK that = (SperreEintragPK) o;
    return Objects.equals(targetId, that.targetId) && targetType == that.targetType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(targetId, targetType);
  }


  @Override
  public String toString() {
    return new StringJoiner(", ", SperreEintragPK.class.getSimpleName() + "[", "]")
        .add("targetId='" + targetId + "'")
        .add("targetType=" + targetType)
        .toString();
  }
}
