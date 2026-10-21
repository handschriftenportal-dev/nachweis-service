package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Title;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.05.2020.
 * @version 1.0
 */
public class TitleFactoryTest {

  @Test
  void testBuild() {

    NormdatenReferenz besitzer = new NormdatenReferenz("12345", "Staatsbibliothek zu Berlin", "");
    NormdatenReferenz aufbewahrungsort = new NormdatenReferenz("234", "Berlin", "");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("MS 67")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsort)
        .build();

    Title title = TitleFactory.build(identifikation);

    Assertions
        .assertEquals(TitleFactory.KOD_TITLE_PREFIX + "Berlin, Staatsbibliothek zu Berlin, MS 67",
            title.getContent().get(0));
  }
}
