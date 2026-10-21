package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 22.07.21
 */

public class VarianterNameTest {

  public static final String NAME_1 = "Deutsch";
  public static final String LANGUAGE_CODE_1 = "de";

  public static final String NAME_2 = "Deutsch";
  public static final String LANGUAGE_CODE_2 = null;

  @Test
  public void testEquals() {
    VarianterName varianterName_1 = new VarianterName(NAME_1, LANGUAGE_CODE_1);
    VarianterName varianterName_2 = new VarianterName(NAME_2, LANGUAGE_CODE_2);
    VarianterName varianterName_3 = new VarianterName(NAME_1, LANGUAGE_CODE_1);
    VarianterName varianterName_4 = new VarianterName(NAME_2, LANGUAGE_CODE_2);

    assertTrue(varianterName_1.equals(varianterName_1));
    assertTrue(varianterName_2.equals(varianterName_2));
    assertTrue(varianterName_1.equals(varianterName_3));
    assertTrue(varianterName_2.equals(varianterName_4));
    assertFalse(varianterName_1.equals(varianterName_2));
    assertFalse(varianterName_4.equals(varianterName_3));
  }
}
