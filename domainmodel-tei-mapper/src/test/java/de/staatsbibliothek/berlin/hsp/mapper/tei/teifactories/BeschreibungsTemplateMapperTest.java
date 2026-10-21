package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation.PublikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.TEI;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 13.05.2020.
 * @version 1.0
 */
class BeschreibungsTemplateMapperTest {

  private static final Logger logger = LoggerFactory.getLogger(
      BeschreibungsTemplateMapperTest.class);

  Path beschreibungsinitialResult = Paths.get("src", "test", "resources", "tei",
      "beschreibunginitialResult.xml");

  @Test
  void testCreateTEIFromInitial() throws Exception {

    logger.info("Starting Test Intial Template");

    NormdatenReferenz besitzer = new NormdatenReferenz("",
        "Sächsische Landesbibliothek - Staats- und Universitätsbibliothek Dresden", "");
    NormdatenReferenz aufbewahrungsort = new NormdatenReferenz("", "Dresden", "");

    NormdatenReferenz deutsch = new NormdatenReferenzBuilder()
        .withId(UUID.randomUUID().toString())
        .withGndID("4113292-0")
        .withName("Deutsch")
        .withTypeName("Language")
        .addVarianterName(new VarianterName("Neuhochdeutsch", "de"))
        .addVarianterName(new VarianterName("Deutsche Sprache", "de"))
        .addVarianterName(new VarianterName("Hochdeutsch", "de"))
        .addIdentifikator(new Identifikator("de", null, null))
        .addIdentifikator(
            new Identifikator("de", "http://id.loc.gov/vocabulary/iso639-1/de", "ISO_639-1"))
        .addIdentifikator(
            new Identifikator("deu", "http://id.loc.gov/vocabulary/iso639-2/deu", "ISO_639-2"))
        .addIdentifikator(
            new Identifikator("ger", "http://id.loc.gov/vocabulary/iso639-2/ger", "ISO_639-2"))
        .build();

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer).withAufbewahrungsOrt(aufbewahrungsort)
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopfBuilder()
        .withTitel("Flavius Josephus, de bello Judaico")
        .build();

    Lizenz lizenz = new Lizenz.LizenzBuilder()
        .withBeschreibungsText("Die Katalogdaten sind Public Domain.")
        .addUri("https://creativecommons.org/publicdomain/mark/1.0/")
        .addUri("https://creativecommons.org/publicdomain/mark/1.0/deed.de")
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder("BS1", "KOD1", identifikation)
        .withErstellungsDatum(LocalDateTime.of(2021, 5, 4, 13, 0))
        .withAenderungsDatum(LocalDateTime.of(2021, 6, 4, 13, 0))
        .withBeschreibungssprache(deutsch)
        .addBeschreibungsKomponente(kopf)
        .addAutor(new NormdatenReferenz("45345345", "Konrad E.", "sdfsdfsdf"))
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withBeschreibungsTyp(BeschreibungsTyp.MEDIEVAL)
        .withPublikation(new PublikationBuilder().withLizenz(lizenz).build())
        .build();

    String templateXml;
    try (InputStream resourceAsStream = getClass()
        .getClassLoader()
        .getResourceAsStream("tei/beschreibunginitial_medieval.xml")) {

      assertNotNull(resourceAsStream);
      templateXml = new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
    }
    assertNotNull(templateXml);
    templateXml = templateXml.replace("__tei-odd-version__", "1.2.3");

    TEI tei = BeschreibungsTemplateMapper
        .createTEIFromInitialTemplate(beschreibung, templateXml);

    assertNotNull(tei);

    assertEquals(1, beschreibung.getAutoren().size());

    String teiString = TEIObjectFactory.marshal(tei);

    Diff beschreibungsDiff = DiffBuilder.compare(Input.fromString(teiString))
        .withTest(Input.fromFile(beschreibungsinitialResult.toFile()))
        .ignoreComments()
        .ignoreWhitespace()
        .build();

    assertFalse(beschreibungsDiff.hasDifferences(), beschreibungsDiff.toString());
  }
}
