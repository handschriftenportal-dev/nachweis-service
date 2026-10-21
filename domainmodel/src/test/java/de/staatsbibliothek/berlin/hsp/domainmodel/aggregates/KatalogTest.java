package de.staatsbibliothek.berlin.hsp.domainmodel.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KatalogBeschreibungReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 11.07.19
 */
public class KatalogTest {

  public static final String ID = "1";
  public static final String KATALOG_TITLE = "Katalog Title";
  public static final String PUBLIKATIONS_JAHR = "1965";
  public static final String LIZENZ_URI = "https://rightsstatements.org/page/InC/1.0/";
  public static final String HSK_ID = "440";
  public static final LocalDateTime ERSTELL_DATUM = LocalDateTime.of(2022, 2, 21, 8, 2, 30);
  public static final LocalDateTime AENDERUNGS_DATUM = LocalDateTime.of(2022, 2, 21, 8, 51, 30);
  public static final KatalogBeschreibungReferenz KATALOG_BESCHREIBUNG_REFERENZ = new KatalogBeschreibungReferenz(
      "BeschreibungsID", "1", "20", null, null);
  public static final String TEI_XML = "<TEI></TEI>";
  public static final NormdatenReferenz AUTOR = new NormdatenReferenz.NormdatenReferenzBuilder()
      .withName("Stahl, Irene").withGndID("GND14546456-X").build();
  public static final NormdatenReferenz VERLAG = new NormdatenReferenz.NormdatenReferenzBuilder().withName(
          "Harrassowitz ")
      .withGndID("1068107626").build();

  @Test
  public void testBuilder() {
    Katalog katalog = createTestKatalog();
    assertEquals(ID, katalog.getId());
    assertEquals(KATALOG_TITLE, katalog.getTitle());
    assertTrue(katalog.getAutoren().contains(AUTOR));
    assertEquals(VERLAG, katalog.getVerlag());
    assertNotNull(katalog.getDigitalisat());
    assertEquals(PUBLIKATIONS_JAHR, katalog.getPublikationsJahr());
    assertEquals(LIZENZ_URI, katalog.getLizenzUri());
    assertEquals(HSK_ID, katalog.getHskID());
    assertEquals(ERSTELL_DATUM, katalog.getErstellDatum());
    assertEquals(AENDERUNGS_DATUM, katalog.getAenderungsDatum());
    assertTrue(katalog.getBeschreibungen().contains(KATALOG_BESCHREIBUNG_REFERENZ));
    assertEquals(TEI_XML, katalog.getTeiXML());
  }

  public static Katalog createTestKatalog() {
    Katalog katalog = new KatalogBuilder().withId(ID).withTitle(KATALOG_TITLE)
        .withVerlag(VERLAG)
        .addAutor(AUTOR).withDigitalisat(
            Digitalisat.DigitalisatBuilder().withTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL)
                .withManifest("").build()).withPublikationsJahr(PUBLIKATIONS_JAHR)
        .withLizenzURI(LIZENZ_URI)
        .withHSKID(HSK_ID)
        .withErstelldatum(ERSTELL_DATUM)
        .withAenderungsdatum(AENDERUNGS_DATUM).addKatalogBeschreibungReferenz(
            KATALOG_BESCHREIBUNG_REFERENZ)
        .withTEIXML(TEI_XML)
        .build();
    return katalog;
  }

}
