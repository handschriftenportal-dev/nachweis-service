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
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 25.02.2022.
 * @version 1.0
 */
public enum DokumentObjektTyp {
  HSP_DESCRIPTION, HSP_DESCRIPTION_RETRO, HSP_OBJECT;

  @Override
  public String toString() {
    switch (this) {
      case HSP_OBJECT:
        return "hsp:object";
      case HSP_DESCRIPTION:
        return "hsp:description";
      case HSP_DESCRIPTION_RETRO:
        return "hsp:description_retro";
      default:
        return "";
    }
  }

  public static DokumentObjektTyp fromString(String type) {
    if ("hsp:object".equals(type)) {
      return DokumentObjektTyp.HSP_OBJECT;
    }

    if ("hsp:description".equals(type)) {
      return DokumentObjektTyp.HSP_DESCRIPTION;
    }

    if ("hsp:description_retro".equals(type)) {
      return DokumentObjektTyp.HSP_DESCRIPTION_RETRO;
    }

    return null;
  }
}
