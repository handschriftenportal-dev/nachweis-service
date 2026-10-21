package de.staatsbibliothek.berlin.hsp.domainmodel.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.01.2020.
 * @version 1.0
 */
public class KulturObjektKomponenteTest {

  @Test
  void testConstructorCreation() {

    KulturObjektKomponente komponente = new KulturObjektKomponente("1");

    assertEquals("1", komponente.getId());
    assertNotNull(komponente.getRegistrierungsDatum());
    assertNotNull(komponente.getAlternativeIdentifikationen());
    assertNotNull(komponente.getBeschreibungenIDs());
    assertNotNull(komponente.getDigitalisate());
    assertNotNull(komponente.getExterneReferenzen());
  }

  @Test
  void testNotAllowedMethodCaller() {

    KulturObjektKomponente komponente = new KulturObjektKomponente("1");

    Assertions.assertThrows(IllegalCallerException.class, () -> {
      komponente.setTeiXML("<xml></xml>");
    });

    Assertions.assertThrows(IllegalCallerException.class, () -> {
      komponente.setGndIdentifier("");
    });

    Assertions.assertThrows(IllegalCallerException.class, () -> {
      komponente.setRegistrierungsDatum(LocalDateTime.now());
    });

    Assertions.assertThrows(IllegalCallerException.class, () -> {
      komponente.setKomponenten(Collections.emptySet());
    });

  }
}
