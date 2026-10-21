package de.staatsbibliothek.berlin.hsp.domainmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.08.22.
 * @version 1.0
 */
public class HSP_UUI_FactoryTest {

  @Test
  void testGenerate() {
    assertNotNull(HSP_UUI_Factory.generate());
    assertTrue(HSP_UUI_Factory.generate().contains("HSP-"));
  }

  @Test
  void testGenerateFromByte() {
    assertNotNull(HSP_UUI_Factory.generate("3434".getBytes(StandardCharsets.UTF_8)));
    assertTrue(HSP_UUI_Factory.generate("3434".getBytes(StandardCharsets.UTF_8)).contains("HSP-"));
    assertEquals("HSP-14c879f3-f5d8-3d93-a09f-6090d77c2cc3",HSP_UUI_Factory.generate("3434".getBytes(StandardCharsets.UTF_8)));
  }
}
