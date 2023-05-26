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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 11.08.21
 */
public class SignatureValue implements Serializable {

  private static final long serialVersionUID = -428394530604773136L;

  public final String dokumentId;
  public final String signature;
  public final String aufbewaehrungsOrt;
  public final String besitzer;

  public SignatureValue(String dokumentId, String signature, String aufbewaehrungsOrt,
      String besitzer) {
    this.dokumentId = dokumentId;
    this.signature = signature;
    this.aufbewaehrungsOrt = aufbewaehrungsOrt;
    this.besitzer = besitzer;
  }

  public String getDokumentId() {
    return dokumentId;
  }

  public String getSignature() {
    return signature;
  }

  public String getAufbewaehrungsOrt() {
    return aufbewaehrungsOrt;
  }

  public String getBesitzer() {
    return besitzer;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SignatureValue that = (SignatureValue) o;
    return Objects.equals(signature, that.signature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(signature);
  }


  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("SignatureValue{");
    sb.append("dokumentId='").append(dokumentId).append('\'');
    sb.append(", signature='").append(signature).append('\'');
    sb.append(", aufbewahrungsOrt='").append(aufbewaehrungsOrt).append('\'');
    sb.append(", besitzer='").append(besitzer).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
