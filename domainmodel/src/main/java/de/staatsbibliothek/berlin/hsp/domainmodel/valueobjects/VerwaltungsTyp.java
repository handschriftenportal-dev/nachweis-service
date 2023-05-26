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
import java.util.Optional;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 17.03.2021.
 * @version 1.0
 * <p>
 * This enumeration class shows if an entity is maintained within the handschriftenportal or within an external system
 */
public enum VerwaltungsTyp {
  INTERN, EXTERN;

  public static Optional<VerwaltungsTyp> fromString(String value) {
    if (Objects.isNull(value)) {
      return Optional.empty();
    }

    switch (value.toLowerCase()) {
      case "extern":
        return Optional.of(EXTERN);
      case "intern":
        return Optional.of(INTERN);
      default:
        return Optional.empty();
    }
  }

  @Override
  public String toString() {
    switch (this) {
      case EXTERN:
        return "extern";
      case INTERN:
        return "intern";
      default:
        return "";
    }
  }

}
