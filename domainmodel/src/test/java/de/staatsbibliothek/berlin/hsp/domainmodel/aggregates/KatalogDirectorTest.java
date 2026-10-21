package de.staatsbibliothek.berlin.hsp.domainmodel.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.02.2022.
 * @version 1.0
 */
public class KatalogDirectorTest {

  public static final String TITLE = "Title";
  public static final String TEI_XML = "XML";
  public static final String PUBLIKATIONS_JAHR = "1980";
  public static final String HSK_ID = "440";
  public static final NormdatenReferenz VERLAG = new NormdatenReferenz.NormdatenReferenzBuilder()
      .withName("Harrassowitz").withGndID("").build();

  @Test
  void testCreateSimpleHSKKatalog() {

    KatalogDirector director = new KatalogDirector(new KatalogBuilder());

    Katalog hskKatalog = director.createSimpleHSKKatalog(TITLE, TEI_XML, PUBLIKATIONS_JAHR, HSK_ID,
        VERLAG);

    assertNotNull(hskKatalog);

    assertEquals(TITLE, hskKatalog.getTitle());
    assertEquals(TEI_XML, hskKatalog.getTeiXML());
    assertEquals(PUBLIKATIONS_JAHR, hskKatalog.getPublikationsJahr());
    assertEquals(HSK_ID, hskKatalog.getHskID());
    assertEquals(VERLAG, hskKatalog.getVerlag());
  }
}
