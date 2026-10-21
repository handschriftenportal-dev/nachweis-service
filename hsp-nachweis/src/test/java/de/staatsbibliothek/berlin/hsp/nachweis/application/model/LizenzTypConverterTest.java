package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.LizenzTyp;
import org.junit.jupiter.api.Test;

public class LizenzTypConverterTest {

  private final LizenzTypConverter converter = new LizenzTypConverter();

  // --- getAsObject tests ---

  @Test
  void testGetAsObject_nullValue_returnsNull() {
    LizenzTyp result = converter.getAsObject(null, null, null);
    assertNull(result);
  }

  @Test
  void testGetAsObject_emptyString_returnsNull() {
    LizenzTyp result = converter.getAsObject(null, null, "");
    assertNull(result);
  }

  @Test
  void testGetAsObject_validCcBy_returnsCcBy() {
    LizenzTyp result = converter.getAsObject(null, null, "CC_BY");
    assertEquals(LizenzTyp.CC_BY, result);
  }

  @Test
  void testGetAsObject_validCcBySa_returnsCcBySa() {
    LizenzTyp result = converter.getAsObject(null, null, "CC_BY_SA");
    assertEquals(LizenzTyp.CC_BY_SA, result);
  }

  @Test
  void testGetAsObject_validCcByNcSa_returnsCcByNcSa() {
    LizenzTyp result = converter.getAsObject(null, null, "CC_BY_NC_SA");
    assertEquals(LizenzTyp.CC_BY_NC_SA, result);
  }

  @Test
  void testGetAsObject_validCc0_returnsCc0() {
    LizenzTyp result = converter.getAsObject(null, null, "CC_0");
    assertEquals(LizenzTyp.CC_0, result);
  }

  @Test
  void testGetAsObject_invalidValue_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> converter.getAsObject(null, null, "INVALID_VALUE"));
  }

  @Test
  void testGetAsObject_labelInsteadOfName_throwsIllegalArgumentException() {
    // The label "CC-BY" is not the enum name "CC_BY"
    assertThrows(IllegalArgumentException.class,
        () -> converter.getAsObject(null, null, "CC-BY"));
  }

  @Test
  void testGetAsObject_lowercaseName_throwsIllegalArgumentException() {
    // Enum valueOf is case-sensitive
    assertThrows(IllegalArgumentException.class,
        () -> converter.getAsObject(null, null, "cc_by"));
  }

  @Test
  void testGetAsObject_whitespaceOnly_throwsIllegalArgumentException() {
    // " " is not empty, so it passes the null/empty check and hits valueOf
    assertThrows(IllegalArgumentException.class,
        () -> converter.getAsObject(null, null, " "));
  }

  // --- getAsString tests ---

  @Test
  void testGetAsString_nullValue_returnsEmptyString() {
    String result = converter.getAsString(null, null, null);
    assertEquals("", result);
  }

  @Test
  void testGetAsString_ccBy_returnsCcByName() {
    String result = converter.getAsString(null, null, LizenzTyp.CC_BY);
    assertEquals("CC_BY", result);
  }

  @Test
  void testGetAsString_ccBySa_returnsCcBySaName() {
    String result = converter.getAsString(null, null, LizenzTyp.CC_BY_SA);
    assertEquals("CC_BY_SA", result);
  }

  @Test
  void testGetAsString_ccByNcSa_returnsCcByNcSaName() {
    String result = converter.getAsString(null, null, LizenzTyp.CC_BY_NC_SA);
    assertEquals("CC_BY_NC_SA", result);
  }

  @Test
  void testGetAsString_cc0_returnsCc0Name() {
    String result = converter.getAsString(null, null, LizenzTyp.CC_0);
    assertEquals("CC_0", result);
  }

  // --- round-trip tests ---

  @Test
  void testRoundTrip_allEnumValues() {
    for (LizenzTyp typ : LizenzTyp.values()) {
      String asString = converter.getAsString(null, null, typ);
      LizenzTyp asObject = converter.getAsObject(null, null, asString);
      assertEquals(typ, asObject,
          "Round-trip failed for " + typ.name());
    }
  }

  @Test
  void testRoundTrip_fromString_ccBy() {
    String name = "CC_BY";
    LizenzTyp asObject = converter.getAsObject(null, null, name);
    String asString = converter.getAsString(null, null, asObject);
    assertEquals(name, asString);
  }
}