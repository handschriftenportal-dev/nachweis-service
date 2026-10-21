package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * @author Konrad.Eichstaedt@sbb.spk-berlin.de
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 09.07.19
 */
public class IdentifikationTest {

  public static final String SAMMLUNG_ID = UUID.randomUUID().toString();
  private static final String ID = UUID.randomUUID().toString();
  private static final IdentTyp IDENT_TYP = IdentTyp.GUELTIGE_SIGNATUR;
  private static final String IDENT = "12345";
  private static final NormdatenReferenz STAABI = new NormdatenReferenz("1234",
      "Staatsbibliothek zu Berlin", "GND-53424");
  private static final NormdatenReferenz BERLIN = new NormdatenReferenz("123", "Berlin", "GND-X1");
  private static final String SORT_KEY = "mmm_12345";

  public static Identifikation createGueltigeSignatur() {
    return new IdentifikationBuilder()
        .withId(ID)
        .addSammlungID(SAMMLUNG_ID)
        .withIdent(IDENT)
        .withIdentTyp(IDENT_TYP)
        .withAufbewahrungsOrt(BERLIN)
        .withBesitzer(STAABI)
        .withGueltigBis(LocalDate.of(2002, 10, 10))
        .withSortKey(SORT_KEY)
        .build();
  }

  public static Identifikation createAlternativeSignatur() {
    return new IdentifikationBuilder()
        .withId(ID)
        .addSammlungID(SAMMLUNG_ID)
        .withIdent(IDENT)
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .withAufbewahrungsOrt(BERLIN)
        .withBesitzer(STAABI)
        .build();
  }

  @Test
  void testEquals() {

    Identifikation identifikation_s147 = new IdentifikationBuilder()
        .withId(ID)
        .withIdent("S 147")
        .withBesitzer(STAABI)
        .withAufbewahrungsOrt(BERLIN)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    Identifikation identifikation_s147E = new IdentifikationBuilder()
        .withId(ID)
        .withIdent("S 147")
        .withBesitzer(STAABI)
        .withAufbewahrungsOrt(BERLIN)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    Identifikation identifikation_s148 = new IdentifikationBuilder()
        .withId(ID)
        .withIdent("S 148")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    assertNotEquals(identifikation_s147, identifikation_s148);

    assertEquals(identifikation_s147, identifikation_s147E);
  }

  @Test
   void testFormatSignatureLabel() {
    Identifikation identifikation = createGueltigeSignatur();
    assertEquals("12345;Berlin;Staatsbibliothek zu Berlin",
        identifikation.formatSignatureLabel());
  }

  @Test
   void testIdentifikationBuilder() {
    Identifikation identifikation = createGueltigeSignatur();
    assertEquals(ID, identifikation.getId());
    assertEquals(1, identifikation.getSammlungIDs().size());
    assertEquals(IDENT, identifikation.getIdent());
    assertEquals(IDENT_TYP, identifikation.getIdentTyp());
    assertNotNull(identifikation.toString());
    assertEquals(BERLIN, identifikation.getAufbewahrungsOrt());
    assertEquals(STAABI, identifikation.getBesitzer());
    assertEquals(LocalDate.of(2002, 10, 10), identifikation.getGueltigBis());
    assertEquals(SORT_KEY, identifikation.getSortKey());
  }

  @Test
  void testSortKeyIsTrimmedCorrectly() {
    Identifikation identifikation = new IdentifikationBuilder()
        .withIdentTyp(IDENT_TYP)
        .withIdent(IDENT)
        .withSortKey("  " + SORT_KEY + "  ")
        .build();

    assertEquals(SORT_KEY, identifikation.getSortKey());
  }

  @Test
  void testSortKeyBlankIsConvertedToNull() {
    Identifikation identifikation = new IdentifikationBuilder()
        .withIdentTyp(IDENT_TYP)
        .withIdent(IDENT)
        .withSortKey("   ")
        .build();

    assertNull(identifikation.getSortKey());
  }
}
