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

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 11.11.20
 */
public enum DigitalisatTyp {
  KOMPLETT_VOM_ORIGINAL, KOMPLETT_VON_REPRODUKTION, WASSERZEICHEN, TEIL_VOM_ORIGINAL, TEIL_VON_REPRODUKTION;

  @Override
  public String toString() {
    switch (this) {

      case KOMPLETT_VOM_ORIGINAL:
        return "completeFromOriginal";
      case KOMPLETT_VON_REPRODUKTION:
        return "completeFromSurrogate";
      case WASSERZEICHEN:
        return "watermark";
      case TEIL_VOM_ORIGINAL:
        return "partFromOriginal";
      case TEIL_VON_REPRODUKTION:
        return "partFromSurrogate";
      default:
        return "";
    }
  }

  public static DigitalisatTyp fromString(String value) {

    if ("komplettVomOriginal".equals(value) || "completeFromOriginal".equals(value)) {
      return DigitalisatTyp.KOMPLETT_VOM_ORIGINAL;
    }

    if ("komplettVonReproduktion".equals(value) || "completeFromSurrogate".equals(value)) {
      return DigitalisatTyp.KOMPLETT_VON_REPRODUKTION;
    }

    if ("Wasserzeichen".equals(value) || "watermark".equals(value)) {
      return DigitalisatTyp.WASSERZEICHEN;
    }

    if ("TeilVomOriginal".equals(value) || "partFromOriginal".equals(value)) {
      return DigitalisatTyp.TEIL_VOM_ORIGINAL;
    }

    if ("TeilVonReproduktion".equals(value) || "partFromSurrogate".equals(value)) {
      return DigitalisatTyp.TEIL_VON_REPRODUKTION;
    }

    return DigitalisatTyp.KOMPLETT_VOM_ORIGINAL;
  }

  public String toLabel() {
    switch (this) {

      case KOMPLETT_VOM_ORIGINAL:
        return "Komplett Vom Original";
      case KOMPLETT_VON_REPRODUKTION:
        return "Komplett Von Reproduktion";
      case WASSERZEICHEN:
        return "Wasserzeichen";
      case TEIL_VOM_ORIGINAL:
        return "Teil Vom Original";
      case TEIL_VON_REPRODUKTION:
        return "Teil Von Reproduktion";
      default:
        return "Unbekannt";
    }
  }
}
