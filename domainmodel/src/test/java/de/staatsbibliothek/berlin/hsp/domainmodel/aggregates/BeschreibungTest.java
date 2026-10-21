package de.staatsbibliothek.berlin.hsp.domainmodel.aggregates;

import static de.staatsbibliothek.berlin.hsp.domainmodel.entities.TestdatenTest.SPRACHE_DEUTSCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation.PublikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz.LizenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Referenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ReferenzQuelle;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 23.08.19
 */
public class BeschreibungTest {

  public static final String ID = "BS1";

  private static final Logger log = LoggerFactory.getLogger(BeschreibungTest.class);

  private static final Identifikation IDENTIFIKATION = new Identifikation.IdentifikationBuilder()
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
      .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
      .build();

  @Test
  void createWithMinimalValues() {
    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("1",
        "KOD_1",
        IDENTIFIKATION).build();

    assertEquals("1", beschreibung.getId());
    assertEquals("KOD_1", beschreibung.getKodID());
  }

  @Test
  void test_ToString() {

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("2",
        "kod-0", IDENTIFIKATION)
        .addBeschreibungsKomponente(
            new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder().build())
        .build();

    assertNotNull(beschreibung.toString());
  }


  @Test
  void testBuilder() {

    Referenz hsa = new Referenz("https://test.de", ReferenzTyp.BESCHREIBUNG,
        new ReferenzQuelle("Manumed", "Manuscripta Medivalia"));
    NormdatenReferenz author_becker = new NormdatenReferenz("1",
        "BECKER, Peter Jörg", "");
    NormdatenReferenz author_bandis = new NormdatenReferenz("2", "Tilo BRANDIS",
        "");
    LocalDateTime erstellungsDatum = LocalDateTime.of(2008, 5, 23, 0, 0);
    LocalDateTime aenderungsDatum = LocalDateTime.of(2019, 5, 23, 0, 0);
    Lizenz lizenz = new LizenzBuilder().addUri(
            "https://creativecommons.org/publicdomain/mark/1.0/")
        .build();

    PURL purl = new PURL(
        URI.create(
            "https://resolver.staatsbibliothek-berlin.de/HSP0000002300000000"),
        URI.create("https://handschriftenportal.de/HSP-123"), PURLTyp.INTERNAL);

    Publikation publikation = new PublikationBuilder()
        .withErstpublikationsDatum(LocalDateTime.of(2020, 1, 1, 0, 0))
        .withLizenz(lizenz)
        .addPURL(purl)
        .build();

    Bearbeiter bearbeiter = new Bearbeiter.BearbeiterBuilder().withBearbeitername(
        "1").build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("BS1",
        "THEOL. LAT. FOL. 718",
        IDENTIFIKATION)
        .addExterneID(hsa)
        .withTEIXml("<TEI></TEI>")
        .withBeschreibungssprache(SPRACHE_DEUTSCH)
        .withErstellungsDatum(erstellungsDatum)
        .withAenderungsDatum(aenderungsDatum)
        .addBeschreibungsBeteiligter(author_becker)
        .addBeschreibungsBeteiligter(author_bandis)
        .withPublikation(publikation)
        .withKatalog("Katalog 1")
        .addAltIdentifier("mstest")
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION)
        .withBearbeiterIds(bearbeiter)
        .build();

    assertEquals("BS1", beschreibung.getId());
    assertEquals("THEOL. LAT. FOL. 718", beschreibung.getKodID());
    assertTrue(beschreibung.getExterneIDs().contains(hsa));
    assertEquals(SPRACHE_DEUTSCH, beschreibung.getBeschreibungsSprache());
    assertEquals(erstellungsDatum, beschreibung.getErstellungsDatum());
    assertEquals(aenderungsDatum, beschreibung.getAenderungsDatum());
    assertEquals(2, beschreibung.getBeschreibungsBeteiligte().size());
    beschreibung.removeBeschreibungsBeteiligter(author_becker);
    assertEquals(1, beschreibung.getBeschreibungsBeteiligte().size());
    beschreibung.removeBeschreibungsBeteiligter(author_bandis);
    assertEquals(0, beschreibung.getBeschreibungsBeteiligte().size());
    assertEquals(publikation, beschreibung.getPublikation());
    assertEquals("<TEI></TEI>", beschreibung.getTeiXML());
    assertNotNull(beschreibung.toString());
    assertTrue(beschreibung.getAltIdentifier().contains("mstest"));
    assertEquals("Katalog 1", beschreibung.getKatalogID());
    assertEquals(VerwaltungsTyp.INTERN, beschreibung.getVerwaltungsTyp());
    assertEquals(lizenz, beschreibung.getLizenz());
    assertEquals(DokumentObjektTyp.HSP_DESCRIPTION,
        beschreibung.getDokumentObjektTyp());
    assertNotNull(beschreibung.getPURLs());
    assertEquals(1, beschreibung.getPURLs().size());
    assertTrue(beschreibung.getPURLs().contains(purl));
    assertEquals(1, beschreibung.getBearbeiterIDs().size());

    log.info("Beschreibungs Test {}", beschreibung);
  }

  @Test
  void testisBeschreibungIntern() {

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("2",
        "kod-0",
        IDENTIFIKATION).build();

    assertFalse(beschreibung.isBeschreibungIntern());

    beschreibung.makeBeschreibungIntern();

    assertTrue(beschreibung.isBeschreibungIntern());

    beschreibung.makeBeschreibungExtern();

    assertFalse(beschreibung.isBeschreibungIntern());
  }

  @Test
  void testGetGueltigeIdentifikation() {

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("2",
        "kod-0",
        IDENTIFIKATION)
        .build();

    assertTrue(beschreibung.getGueltigeIdentifikation().isPresent());
    assertEquals(IDENTIFIKATION.getIdentTyp(),
        beschreibung.getGueltigeIdentifikation().get().getIdentTyp());
    assertEquals(IDENTIFIKATION.getBesitzer(),
        beschreibung.getGueltigeIdentifikation().get().getBesitzer());
  }

  @Test
  void testBeschreibungWithAutoren() {
    BeschreibungsBuilder builder = new Beschreibung.BeschreibungsBuilder("2",
        "kod-0",
        IDENTIFIKATION);
    Beschreibung beschreibung = builder.build();

    assertNotNull(beschreibung);
    assertNotNull(beschreibung.getAutoren());

    builder.addAutor(
        new NormdatenReferenz.NormdatenReferenzBuilder().withName("Test")
            .withGndID("GNDID")
            .build());

    beschreibung = builder.build();

    assertEquals(1, beschreibung.getAutoren().size());
  }

  @Test
  void testBeschreibungWithBearbeiterID() {
    BeschreibungsBuilder builder = new Beschreibung.BeschreibungsBuilder("2",
        "kod-0",
        IDENTIFIKATION);
    Beschreibung beschreibung = builder.build();

    assertNotNull(beschreibung);

    Bearbeiter bearbeiter = Bearbeiter.newBuilder()
        .withBearbeitername("b-123")
        .withVorname("test")
        .withNachname("test")
        .withEmail("test@test.de")
        .withRolle("Redakteur")
        .build();

    Set<Bearbeiter> list = new LinkedHashSet<>();
    list.add(bearbeiter);
    builder.withBearbeiterIds(list.stream().findAny().orElse(null));

    beschreibung = builder.build();

    assertEquals(1, beschreibung.getBearbeiterIDs().size());
  }

  @Test
  void testGueltigeIdentifikationIsNotGueltig() {
    Identifikation ungueltigeIdentifikation = new Identifikation.IdentifikationBuilder()
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
        .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
        .build();
    assertThrows(IllegalArgumentException.class,
        () -> new Beschreibung.BeschreibungsBuilder("2", "kod-0",
            ungueltigeIdentifikation).build());
  }

  @Test
  void testisGueltigeIdentifikationValid() {
    Identifikation gueltigeIdentifikation = new Identifikation.IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
        .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("2",
        "kod-0",
        gueltigeIdentifikation).build();

    assertTrue(beschreibung.isGueltigeIdentifikationValid());

    gueltigeIdentifikation.setAufbewahrungsOrt(null);

    assertFalse(beschreibung.isGueltigeIdentifikationValid());

    gueltigeIdentifikation.setBesitzer(null);

    assertFalse(beschreibung.isGueltigeIdentifikationValid());

  }

  @Test
  void testGetLatestPublikationDatum_NoPublikationen_ReturnsMin() {
    Beschreibung beschreibung = new Beschreibung();

    LocalDateTime result = beschreibung.getErstpublikationsDatum();

    assertEquals(LocalDateTime.of(1970, 1, 1, 0, 0, 0), result,
        "Wenn keine Publikationen vorhanden sind, sollte LocalDateTime.MIN zurückgegeben werden.");
  }

  @Test
  void testGetLatestPublikationDatum_OnePublikation_ReturnsItsDate() {
    LocalDateTime date = LocalDateTime.of(2023, 1, 1, 10, 0);
    Publikation publikation = new PublikationBuilder()
        .withErstpublikationsDatum(date)
        .build();
    Beschreibung beschreibung = new Beschreibung();
    beschreibung.setPublikation(publikation);

    LocalDateTime result = beschreibung.getErstpublikationsDatum();

    assertEquals(date, result,
        "Wenn es nur eine Publikation gibt, sollte deren Datum zurückgegeben werden.");
  }

  @Test
  void testGetLatestPublikationDatum_WithPreviousPublikationen() {
    LocalDateTime previousDate = LocalDateTime.of(2020, 5, 10, 12, 0);
    LocalDateTime currentDate = LocalDateTime.of(2022, 8, 15, 14, 0);

    Publikation previousPublikation = new PublikationBuilder()
        .withErstpublikationsDatum(previousDate)
        .build();

    Publikation currentPublikation = new PublikationBuilder()
        .withPrevious(previousPublikation)
        .withErstpublikationsDatum(currentDate)
        .build();

    Beschreibung beschreibung = new Beschreibung();
    beschreibung.setPublikation(currentPublikation);

    LocalDateTime result = beschreibung.getErstpublikationsDatum();

    assertEquals(currentDate, result,
        "Das Veröffentlichungsdatum der aktuellen Publikation sollte zurückgegeben werden.");
  }

  @Test
  void testGetLatestPublikationDatum_WithNullDate() {
    Publikation publikation = new PublikationBuilder()
        .build();

    Beschreibung beschreibung = new Beschreibung();
    beschreibung.setPublikation(publikation);

    LocalDateTime result = beschreibung.getErstpublikationsDatum();

    assertEquals(LocalDateTime.of(1970, 1, 1, 0, 0, 0), result,
        "Wenn das Erstpublikationsdatum null ist, sollte der Standardwert zurückgegeben werden.");
  }

  @Test
  void testFindKopf() {
    BeschreibungsBuilder builder = new Beschreibung.BeschreibungsBuilder("2",
        "kod-0",
        IDENTIFIKATION);
    Beschreibung beschreibung = builder.build();

    assertFalse(beschreibung.findKopf().isPresent());

    beschreibung.getBeschreibungsStruktur().add(
        new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder().build());

    assertTrue(beschreibung.findKopf().isPresent());
  }

  @Test
  void testFormatSignatureLabel_withGueltige() {
    NormdatenReferenz besitzer = new NormdatenReferenz("1", "SBB", "GND-1");
    NormdatenReferenz ort = new NormdatenReferenz("2", "Berlin", "GND-2");
    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("HS. 1")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(ort)
        .build();
    Beschreibung beschreibung = new BeschreibungsBuilder("b1", "kod1", identifikation).build();

    assertEquals("HS. 1;Berlin;SBB", beschreibung.formatSignatureLabel());
  }

  @Test
  void testFormatSignatureLabel_withoutGueltige() {
    Beschreibung beschreibung = new BeschreibungsBuilder("b1", null, null).build();

    assertEquals("ohne Signatur", beschreibung.formatSignatureLabel());
  }
}
