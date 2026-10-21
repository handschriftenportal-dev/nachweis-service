package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 22.07.21
 */

public class IdentifikatorTest {

  public static final String TEXT_1 = "de";
  public static final String URL_1 = "http://id.loc.gov/vocabulary/iso639-1/de";
  public static final String TYPE_1 = "ISO_639-1";

  public static final String TEXT_2 = "de";
  public static final String URL_2 = null;
  public static final String TYPE_2 = null;

  @Test
  public void testEquals() {
    Identifikator identifikator_1 = new Identifikator(TEXT_1, URL_1, TYPE_1);
    Identifikator identifikator_2 = new Identifikator(TEXT_2, URL_2, TYPE_2);
    Identifikator identifikator_3 = new Identifikator(TEXT_1, URL_1, TYPE_1);
    Identifikator identifikator_4 = new Identifikator(TEXT_2, URL_2, TYPE_2);

    assertTrue(identifikator_1.equals(identifikator_1));
    assertTrue(identifikator_2.equals(identifikator_2));
    assertTrue(identifikator_1.equals(identifikator_3));
    assertTrue(identifikator_2.equals(identifikator_4));
    assertFalse(identifikator_1.equals(identifikator_2));
    assertFalse(identifikator_4.equals(identifikator_3));
  }
}
