package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;


import static org.junit.jupiter.api.Assertions.assertEquals;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 22.07.21
 */

public class NormdatenReferenzTest {

  public static final String ID = UUID.randomUUID().toString();
  public static final String TYPE_NAME = "Language";
  public static final String PREFERRED_NAME = "Deutsch";
  public static final String GND_ID = "4113292-0";
  public static final VarianterName VARIANT_NAME_1 = new VarianterName("Neuhochdeutsch", "de");
  public static final VarianterName VARIANT_NAME_2 = new VarianterName("Deutsche Sprache", "de");
  public static final VarianterName VARIANT_NAME_3 = new VarianterName("Hochdeutsch", "");
  public static final Identifikator IDENTIFIKATOR_1
      = new Identifikator("deu", "http://id.loc.gov/vocabulary/iso639-2/deu", "ISO_639-2");
  public static final Identifikator IDENTIFIKATOR_2
      = new Identifikator("ger", "http://id.loc.gov/vocabulary/iso639-2/ger", "ISO_639-2");
  public static final Identifikator IDENTIFIKATOR_3 = new Identifikator("de", "", "");
  public static final String KONZEPT_URI = "https://normdaten.staatsbibliothek-berlin.de/vocabulary/sourceType/original";

  @Test
  public void testNormdatenReferenzBuilder() {
    NormdatenReferenz german = new NormdatenReferenzBuilder()
        .withId(ID)
        .withTypeName(TYPE_NAME)
        .withName(PREFERRED_NAME)
        .withGndID(GND_ID)
        .withIdentifikator(new HashSet<>(Arrays.asList(IDENTIFIKATOR_1, IDENTIFIKATOR_2)))
        .addIdentifikator(IDENTIFIKATOR_3)
        .addIdentifikator(null)
        .withVarianterName(new HashSet<>(Arrays.asList(VARIANT_NAME_1, VARIANT_NAME_2)))
        .addVarianterName(VARIANT_NAME_3)
        .addVarianterName(null)
        .withUri(KONZEPT_URI)
        .build();

    assertEquals(ID, german.getId());
    assertEquals(TYPE_NAME, german.getTypeName());
    assertEquals(PREFERRED_NAME, german.getName());
    assertEquals(GND_ID, german.getGndID());
    assertEquals(3, german.getIdentifikator().size());
    assertEquals(3, german.getVarianterName().size());
    assertEquals(1, german.getIdentifikatorByTypeName("").size());
    assertEquals(2, german.getIdentifikatorByTypeName("ISO_639-2").size());
    assertEquals(1, german.getVarianterNameBylanguageCode("").size());
    assertEquals(2, german.getVarianterNameBylanguageCode("de").size());
    assertEquals("Neuhochdeutsch; Deutsche Sprache; Hochdeutsch",
        german.getVarianteNamenAlsString());
    assertEquals(KONZEPT_URI, german.getUri());
  }

  @Test
  void testgetVarianeName() {
    NormdatenReferenz german = new NormdatenReferenzBuilder()
        .withId(ID)
        .withTypeName(TYPE_NAME)
        .withName(PREFERRED_NAME)
        .withGndID(GND_ID)
        .withIdentifikator(new HashSet<>(Arrays.asList(IDENTIFIKATOR_1, IDENTIFIKATOR_2)))
        .addIdentifikator(IDENTIFIKATOR_3)
        .addIdentifikator(null)
        .withVarianterName(new HashSet<>(Arrays.asList(VARIANT_NAME_1, VARIANT_NAME_2))).build();

    assertEquals("Neuhochdeutsch", german.getVariantName("de"));
  }
  @Test
  void testgetVarianeNameWithoutLanguageCode() {
    NormdatenReferenz german = new NormdatenReferenzBuilder()
            .withId(ID)
            .withTypeName(TYPE_NAME)
            .withName(PREFERRED_NAME)
            .withGndID(GND_ID)
            .withIdentifikator(new HashSet<>(Arrays.asList(IDENTIFIKATOR_1, IDENTIFIKATOR_2)))
            .addIdentifikator(IDENTIFIKATOR_3)
            .addIdentifikator(null)
            .withVarianterName(new HashSet<>(Arrays.asList(null, null))).build();
    assertEquals(german.getName(), german.getVariantName(null));
    assertEquals("Deutsch", german.getVariantName(null));

  }
}
