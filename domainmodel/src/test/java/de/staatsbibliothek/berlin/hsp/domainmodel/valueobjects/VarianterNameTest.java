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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 22.07.21
 */

public class VarianterNameTest {

  public static final String NAME_1 = "Deutsch";
  public static final String LANGUAGE_CODE_1 = "de";

  public static final String NAME_2 = "Deutsch";
  public static final String LANGUAGE_CODE_2 = null;

  @Test
  public void testEquals() {
    VarianterName varianterName_1 = new VarianterName(NAME_1, LANGUAGE_CODE_1);
    VarianterName varianterName_2 = new VarianterName(NAME_2, LANGUAGE_CODE_2);
    VarianterName varianterName_3 = new VarianterName(NAME_1, LANGUAGE_CODE_1);
    VarianterName varianterName_4 = new VarianterName(NAME_2, LANGUAGE_CODE_2);

    assertTrue(varianterName_1.equals(varianterName_1));
    assertTrue(varianterName_2.equals(varianterName_2));
    assertTrue(varianterName_1.equals(varianterName_3));
    assertTrue(varianterName_2.equals(varianterName_4));
    assertFalse(varianterName_1.equals(varianterName_2));
    assertFalse(varianterName_4.equals(varianterName_3));
  }
}
