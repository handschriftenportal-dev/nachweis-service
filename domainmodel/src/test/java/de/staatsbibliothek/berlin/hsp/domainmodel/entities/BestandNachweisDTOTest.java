package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.04.23.
 * @version 1.0
 */
public class BestandNachweisDTOTest {

  @Test
  void testCreation() {
    BestandNachweisDTO bestandNachweisDTO = new BestandNachweisDTO();
    assertNotNull(bestandNachweisDTO);

    BestandNachweisDTO bestandNachweisDTO1 = new BestandNachweisDTO(10,20,30,40,50,60,80);
    assertNotNull(bestandNachweisDTO1);
    assertEquals(10 ,bestandNachweisDTO1.getAnzahlKOD());
    assertEquals(20 ,bestandNachweisDTO1.getAnzahlBeschreibungen());
    assertEquals(30 ,bestandNachweisDTO1.getAnzahlBeschreibungenIntern());
  }
}
