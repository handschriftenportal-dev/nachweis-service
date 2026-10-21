package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.AltIdentifier;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.05.2020.
 * @version 1.0
 */
public class AltIdentifierFactoryTest {

  @Test
  void testBuild() {

    NormdatenReferenz besitzer = new NormdatenReferenz.NormdatenReferenzBuilder().withId("1")
        .withName("BSB München").withGndID("GND1212").build();

    NormdatenReferenz ort = new NormdatenReferenz.NormdatenReferenzBuilder().withId("2")
        .withName("München").withGndID("GND1212X").build();

    AltIdentifier altIdentifier = AltIdentifierFactory.build(
        new Identifikation.IdentifikationBuilder()
            .withBesitzer(besitzer)
            .withAufbewahrungsOrt(ort)
            .withIdentTyp(IdentTyp.HSP_ALTIDENTIFIER_MXML)
            .withIdent("123").build());

    assertEquals(TEIValues.HSP_ALTIDENTIFIER_MXML, altIdentifier.getType());

    assertEquals("123", altIdentifier.getIdno().getContent().get(0));
    assertNotNull(altIdentifier.getRepository());
    assertNotNull(altIdentifier.getSettlement());
    assertEquals(besitzer.getName(),
        TEICommon.getContentAsString(altIdentifier.getRepository().getContent()));
    assertEquals(ort.getName(),
        TEICommon.getContentAsString(altIdentifier.getSettlement().getContent()));
  }

  @Test
  void testBuildWithCollection() {

    AltIdentifier altIdentifier = AltIdentifierFactory.build(
        new Identifikation.IdentifikationBuilder()
            .withIdentTyp(IdentTyp.HSP_ALTIDENTIFIER_MXML)
            .addSammlungID(TEIValues.KOD_ID_COLLECTION).withIdent("123").build());

    assertEquals(TEIValues.HSP_ALTIDENTIFIER_MXML, altIdentifier.getType());

    assertEquals("123", altIdentifier.getIdno().getContent().get(0));

    assertEquals(TEIValues.KOD_ID_COLLECTION, altIdentifier.getCollection().getContent().get(0));
  }

  @Test
  void testBuild_withSortKey() {

    Identifikation identifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdentTyp(IdentTyp.HSP_ALTIDENTIFIER_MXML)
            .withIdent("123")
            .withSortKey("ALT_123")
            .build();

    AltIdentifier altIdentifier = AltIdentifierFactory.build(identifikation);

    assertEquals("ALT_123", altIdentifier.getIdno().getSortKey());
  }

  @Test
  void testBuild_sortKeyIsTrimmedCorrectly() {

    Identifikation identifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdentTyp(IdentTyp.HSP_ALTIDENTIFIER_MXML)
            .withIdent("123")
            .withSortKey("   ALT_123   ")
            .build();

    AltIdentifier altIdentifier = AltIdentifierFactory.build(identifikation);

    assertEquals("ALT_123", altIdentifier.getIdno().getSortKey());
  }

  @Test
  void testBuild_blankSortKey_shouldNotBeWritten() {

    Identifikation identifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdentTyp(IdentTyp.HSP_ALTIDENTIFIER_MXML)
            .withIdent("123")
            .withSortKey("   ")
            .build();

    AltIdentifier altIdentifier = AltIdentifierFactory.build(identifikation);

    assertNull(altIdentifier.getIdno().getSortKey());
  }

  @Test
  void testBuild_withoutSortKey() {

    Identifikation identifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdentTyp(IdentTyp.HSP_ALTIDENTIFIER_MXML)
            .withIdent("123")
            .build();

    AltIdentifier altIdentifier = AltIdentifierFactory.build(identifikation);

    assertNull(altIdentifier.getIdno().getSortKey());
  }
}
