package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.AltIdentifier;
import org.tei_c.ns._1.MsIdentifier;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.05.2020.
 * @version 1.0
 */
public class MsIdentifierFactoryTest {

  @Test
  void testMappingIdentifier() {

    NormdatenReferenz besitzer = new NormdatenReferenz("12345", "Staatsbibliothek zu Berlin", "");
    NormdatenReferenz aufbewahrungsort = new NormdatenReferenz("234", "Berlin", "");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("MS 67")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsort)
        .build();

    Identifikation alternativeIdentifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("MS 67 Alternative")
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsort)
        .build();

    MsIdentifier msIdentifier = MsIdentifierFactory
        .build(identifikation, Set.of(alternativeIdentifikation));

    assertNotNull(msIdentifier.getIdnos());

    assertEquals(1, msIdentifier.getIdnos().size());

    assertEquals(identifikation.getIdent(),
        msIdentifier.getIdnos().getFirst().getContent().getFirst());

    assertNull(msIdentifier.getIdnos().getFirst().getSortKey());

    assertEquals(besitzer.getName(), msIdentifier.getRepository().getContent().getFirst());

    assertEquals(besitzer.getId(), msIdentifier.getRepository().getKey());

    assertEquals(aufbewahrungsort.getName(), msIdentifier.getSettlement().getContent().getFirst());

    assertEquals(aufbewahrungsort.getId(),
        msIdentifier.getSettlement().getKey());

    assertEquals(1, msIdentifier.getMsNamesAndObjectNamesAndAltIdentifiers().size());

    assertEquals(alternativeIdentifikation.getIdent(),
        ((AltIdentifier) msIdentifier.getMsNamesAndObjectNamesAndAltIdentifiers()
            .getFirst()).getIdno()
            .getContent().getFirst());

    assertEquals(TEIValues.ALTERNATIVE_TYPE_ALTERNATIV,
        ((AltIdentifier) msIdentifier.getMsNamesAndObjectNamesAndAltIdentifiers().getFirst())
            .getType());
  }

  @Test
  void testMappingIdentifier_withSortKey() {

    NormdatenReferenz besitzer = new NormdatenReferenz("12345", "SBB", "");
    NormdatenReferenz ort = new NormdatenReferenz("234", "Berlin", "");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("MS 67")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(ort)
        .withSortKey("MS_67")
        .build();

    MsIdentifier msIdentifier = MsIdentifierFactory
        .build(identifikation, Set.of());

    assertEquals(1, msIdentifier.getIdnos().size());

    assertEquals("MS 67",
        msIdentifier.getIdnos().getFirst().getContent().getFirst());

    assertEquals("MS_67",
        msIdentifier.getIdnos().getFirst().getSortKey());
  }

  @Test
  void testMappingIdentifier_blankSortKey_shouldNotBeSet() {

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("MS 67")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withSortKey("   ")
        .build();

    MsIdentifier msIdentifier = MsIdentifierFactory
        .build(identifikation, Set.of());

    assertNull(msIdentifier.getIdnos().getFirst().getSortKey());
  }
}
