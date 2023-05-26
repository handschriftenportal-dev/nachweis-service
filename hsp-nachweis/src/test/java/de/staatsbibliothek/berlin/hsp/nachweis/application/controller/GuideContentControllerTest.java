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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 03.06.2020.
 * @version 1.0
 */
public class GuideContentControllerTest {

  @Test
  void testSetup() {

    GuideContentController contentController = new GuideContentController();

    assertNotNull(contentController.getKodteivorlage());

    assertEquals("hsp_kodvorlage.xml", contentController.getKodteivorlage().getName());

    assertNotNull(contentController.getBeschreibungteivorlage());

    assertEquals("hsp_beschreibungvorlage.xml", contentController.getBeschreibungteivorlage().getName());

    assertNotNull(contentController.getOrtteivorlage());

    assertEquals("hsp_ortvorlage.xml", contentController.getOrtteivorlage().getName());

    assertNotNull(contentController.getKoerperschaftteivorlage());

    assertEquals("hsp_koerperschaftvorlage.xml", contentController.getKoerperschaftteivorlage().getName());

    assertNotNull(contentController.getBeziehungenteivorlage());

    assertEquals("hsp_beziehungenvorlage.xml", contentController.getBeziehungenteivorlage().getName());

    assertNotNull(contentController.getDigitalisatteiVorlage());

    assertEquals("hsp_kodsurrogatevorlage.xml", contentController.getDigitalisatteiVorlage().getName());

    assertNotNull(contentController.getPersonteiVorlage());

    assertEquals("hsp_personvorlage.xml", contentController.getPersonteiVorlage().getName());

    assertNotNull(contentController.getSpracheteiVorlage());

    assertEquals("hsp_sprachevorlage.xml", contentController.getSpracheteiVorlage().getName());
  }
}
