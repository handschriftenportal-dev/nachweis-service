package de.staatsbibliothek.berlin.hsp.nachweis.domain.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import org.junit.jupiter.api.Test;

class TEIUtilityTest {

  @Test
  void isNotNullOrEmpty() {
    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("123")
        .withTEIXml("TEI")
        .build();
    assertFalse(TEIUtility.isTEINullOrEmpty(kod));
  }

  @Test
  void isNullOrEmpty() {
    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId(null)
        .withTEIXml("TEI")
        .build();
    assertTrue(TEIUtility.isTEINullOrEmpty(kod));
  }

  @Test
  void isEmpty() {
    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("123")
        .withTEIXml("")
        .build();
    assertTrue(TEIUtility.isTEINullOrEmpty(kod));
  }

  @Test
  void isTEINull() {
    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId(null)
        .withTEIXml(null)
        .build();
    assertTrue(TEIUtility.isTEINullOrEmpty(kod));
  }
  
  @Test
  void isNull() {
    assertTrue(TEIUtility.isTEINullOrEmpty(null));
  }
}