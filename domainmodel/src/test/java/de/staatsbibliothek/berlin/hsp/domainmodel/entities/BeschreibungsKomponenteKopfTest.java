package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static de.staatsbibliothek.berlin.hsp.domainmodel.entities.TestdatenTest.MITTELLATEIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 22.01.2020.
 * @version 1.0
 */
public class BeschreibungsKomponenteKopfTest {

  private static final Identifikation IDENTIFIKATION = new Identifikation.IdentifikationBuilder()
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
      .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
      .build();
  private final Logger logger = LoggerFactory.getLogger(
      BeschreibungsKomponenteKopfTest.class);

  @Test
  void testCreationWithBuilder() {

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withId("1")
        .withStatus(Status.VORHANDEN)
        .withKulturObjektTyp(KulturObjektTyp.DRUCK_MIT_HSL_ANTEILEN)
        .withTitel("Titel")
        .addGrundSprache(MITTELLATEIN)
        .build();

    assertNotNull(kopf);

    assertEquals("1", kopf.getId());

    assertEquals("Titel", kopf.getTitel());

    assertEquals(Status.VORHANDEN, kopf.getStatus());

    assertEquals(KulturObjektTyp.DRUCK_MIT_HSL_ANTEILEN,
        kopf.getKulturObjektTyp());

    assertNotNull(kopf.getGrundsprachen());

    assertTrue(kopf.getGrundsprachen().contains(MITTELLATEIN));

  }

  @Test
  void testToString() {

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withId("1")
        .withStatus(Status.VORHANDEN)
        .withKulturObjektTyp(KulturObjektTyp.DRUCK_MIT_HSL_ANTEILEN)
        .withTitel("Titel")
        .build();

    logger.info("Beschreibung Kopf {}", kopf.toString());

    assertTrue(kopf.toString().contains("id='1'"));

    assertTrue(kopf.toString().contains("BeschreibungsKomponenteKopf"));
  }

  @Test
  void testEquals() {
    BeschreibungsKomponenteKopf kopf1 = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withId("1")
        .withStatus(Status.VORHANDEN)
        .withKulturObjektTyp(KulturObjektTyp.DRUCK_MIT_HSL_ANTEILEN)
        .withTitel("Titel")
        .build();

    BeschreibungsKomponenteKopf kopf2 = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withId("2")
        .withStatus(Status.VORHANDEN)
        .withKulturObjektTyp(KulturObjektTyp.DRUCK_MIT_HSL_ANTEILEN)
        .withTitel("Titel")
        .build();

    assertNotEquals(kopf1, kopf2);
  }

  @Test
  void testRemoveGrundspracheWithoutNORMUUID() {
    BeschreibungsBuilder builder = new Beschreibung.BeschreibungsBuilder("2",
        "kod-0",
        IDENTIFIKATION);
    Beschreibung beschreibung = builder.build();

    final BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopfBuilder()
        .addGrundSprache(
            new NormdatenReferenz("NORM-1", "Gueltig", "GND-ID"))
        .addGrundSprache(new NormdatenReferenz(null, "", "ungueltig"))
        .addGrundSprache(new NormdatenReferenz("", "", "ungueltig"))
        .addGrundSprache(
            new NormdatenReferenz("griechisch", "", "ungueltig"))
        .build();
    beschreibung.getBeschreibungsStruktur().add(
        kopf);

    kopf.removeGrundspracheWithoutNORMUUID();

    beschreibung.findKopf().ifPresentOrElse(
        k -> Assertions.assertEquals(1, k.getGrundsprachen().size()),
        Assertions::fail);


  }
}
