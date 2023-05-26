/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KatalogBeschreibungReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogRepositoryAdapter;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.time.LocalDateTime;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.02.2022.
 * @version 1.0
 */

@QuarkusTest
public class KatalogRepositoryAdapterTest {

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
  public static final NormdatenReferenz AUTOR = new NormdatenReferenz("Stahl, Irene",
      "GND14546456-X");
  public static final NormdatenReferenz VERLAG = new NormdatenReferenz("Harrassowitz ",
      "1068107626");
  @Inject
  private KatalogRepositoryAdapter katalogRepository;

  @Test
  @TestTransaction
  void testSaveKatalog() {

    Katalog katalog = createKatalog();

    katalogRepository.save(katalog);

    Assertions.assertEquals(katalog, katalogRepository.findById("1"));
  }

  Katalog createKatalog() {
    return new KatalogBuilder().withId(ID).withTitle(KATALOG_TITLE)
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
  }
}
