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
