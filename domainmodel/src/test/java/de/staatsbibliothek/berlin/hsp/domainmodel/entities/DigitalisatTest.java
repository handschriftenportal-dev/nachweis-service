package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 08.10.2020.
 * @version 1.0
 */
public class DigitalisatTest {

  @Test
  void testMinimalConstruction() {

    Digitalisat digitalisat = Digitalisat.DigitalisatBuilder()
        .build();
    assertNotNull(digitalisat);

  }

  @Test
  void testAdvancedConstruction() {

    Digitalisat digitalisat = Digitalisat.DigitalisatBuilder()
        .withErstellungsdatum(LocalDate.now())
        .withTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL)
        .withManifest("http://manifest.json")
        .build();
    assertNotNull(digitalisat);
    assertNotNull(digitalisat.getErstellungsDatum());

    assertEquals(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL, digitalisat.getTyp());
    assertEquals("http://manifest.json", digitalisat.getManifestURL().toASCIIString());

  }

  @Test
  void testDigitalisatTypfromString() {

    assertEquals(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL,
        DigitalisatTyp.fromString("completeFromOriginal"));
    assertEquals(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL,
        DigitalisatTyp.fromString("komplettVomOriginal"));

    assertEquals(DigitalisatTyp.KOMPLETT_VON_REPRODUKTION,
        DigitalisatTyp.fromString("komplettVonReproduktion"));
    assertEquals(DigitalisatTyp.KOMPLETT_VON_REPRODUKTION,
        DigitalisatTyp.fromString("completeFromSurrogate"));

    assertEquals(DigitalisatTyp.WASSERZEICHEN, DigitalisatTyp.fromString("Wasserzeichen"));
    assertEquals(DigitalisatTyp.WASSERZEICHEN, DigitalisatTyp.fromString("watermark"));

    assertEquals(DigitalisatTyp.UNBEKANNT, DigitalisatTyp.fromString(""));
  }

  @Test
  void testDigitalisatsTypToLabel() {

    DigitalisatTyp komplettVomOriginal = DigitalisatTyp.KOMPLETT_VOM_ORIGINAL;

    assertEquals("Komplett Vom Original", komplettVomOriginal.toLabel());

    DigitalisatTyp unbekannt = DigitalisatTyp.UNBEKANNT;

    assertEquals("Unbekannt", unbekannt.toLabel());
  }

  @Test
  void testBuilderWithKonzepteAsTyp() {
    Digitalisat digitalisat = Digitalisat.DigitalisatBuilder()
        .withErstellungsdatum(LocalDate.now())
        .withManifest("http://manifest.json")
        .addDigitalisatsKonzept(new NormdatenReferenz("NORM-4e0922bc-5733-3213-a4bd-a2c020004fe6",
            "complete_bookblock_cover", ""))
        .build();

    assertNotNull(digitalisat);

    digitalisat.getDigitalisatsKonzepte().stream().findFirst()
        .ifPresentOrElse(n -> assertEquals("complete_bookblock_cover",
            n.getName()), () -> new IllegalArgumentException("Bad Test"));
  }
}
