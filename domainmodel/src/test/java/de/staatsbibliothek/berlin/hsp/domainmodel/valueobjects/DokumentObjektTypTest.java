package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 25.02.2022.
 * @version 1.0
 */
public class DokumentObjektTypTest {

  @Test
  void testToString() {
    assertEquals("hsp:object", DokumentObjektTyp.HSP_OBJECT.toString());
    assertEquals("hsp:description", DokumentObjektTyp.HSP_DESCRIPTION.toString());
    assertEquals("hsp:description_retro", DokumentObjektTyp.HSP_DESCRIPTION_RETRO.toString());
  }

  @Test
  void testFromString() {
    assertEquals(DokumentObjektTyp.HSP_OBJECT, DokumentObjektTyp.fromString("hsp:object"));
    assertEquals(DokumentObjektTyp.HSP_DESCRIPTION,
        DokumentObjektTyp.fromString("hsp:description"));
    assertEquals(DokumentObjektTyp.HSP_DESCRIPTION_RETRO,
        DokumentObjektTyp.fromString("hsp:description_retro"));
  }
}
