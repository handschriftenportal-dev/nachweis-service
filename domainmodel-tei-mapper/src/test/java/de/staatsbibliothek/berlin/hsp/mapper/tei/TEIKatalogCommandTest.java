package de.staatsbibliothek.berlin.hsp.mapper.tei;

import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.TEI;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 28.02.2022
 */

public class TEIKatalogCommandTest {

  static TEI tei;

  @BeforeEach
  void init() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-katalog_Aurich.xml");
    try (InputStream is = newInputStream(teiFilePath)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei = teiList.get(0);
    }
  }

  @Test
  void testUpdateId() throws Exception {
    Katalog katalog1 = new Katalog.KatalogBuilder()
        .withId("HSP-123")
        .withHSKID("440")
        .withTEIXML(TEIObjectFactory.marshal(tei))
        .build();

    assertTrue(katalog1.getTeiXML().contains("<idno type=\"hsk\">440</idno>"));
    assertFalse(
        katalog1.getTeiXML().contains("<idno type=\"hsp\">HSP-123</idno>"));

    TEIKatalogCommand.updateId(katalog1);

    assertTrue(katalog1.getTeiXML().contains("<idno type=\"hsk\">440</idno>"));
    assertTrue(katalog1.getTeiXML().contains("<idno type=\"hsp\">HSP-123</idno>"));

    Katalog katalog2 = new Katalog.KatalogBuilder()
        .withId("HSP-456")
        .withHSKID("440")
        .withTEIXML(katalog1.getTeiXML())
        .build();

    TEIKatalogCommand.updateId(katalog2);

    assertTrue(katalog2.getTeiXML().contains("<idno type=\"hsk\">440</idno>"));
    assertFalse(katalog2.getTeiXML().contains("<idno type=\"hsp\">HSP-123</idno>"));
    assertTrue(katalog2.getTeiXML().contains("<idno type=\"hsp\">HSP-456</idno>"));
  }

  @Test
  void testUpdateVerlagWithGNDID() throws Exception {
    Katalog katalog = new Katalog.KatalogBuilder()
        .withId("HSP-123")
        .withHSKID("440")
        .withVerlag(new NormdatenReferenz.NormdatenReferenzBuilder()
            .withGndID("GNDVerlag").withName("Verlag").withId("NORM-UUID").build())
        .withTEIXML(TEIObjectFactory.marshal(tei))
        .build();

    TEIKatalogCommand.updateVerlag(katalog);

    assertTrue(katalog.getTeiXML()
            .contains("<publisher ref=\"http://d-nb.info/gnd/GNDVerlag\">Verlag</publisher>"),
        katalog.getTeiXML());
  }

  @Test
  void testUpdateVerlagWithoutGNDID() throws Exception {
    Katalog katalog = new Katalog.KatalogBuilder()
        .withId("HSP-123")
        .withHSKID("440")
        .withVerlag(new NormdatenReferenz.NormdatenReferenzBuilder()
            .withName("Verlag").withId("NORM-UUID").build())
        .withTEIXML(TEIObjectFactory.marshal(tei))
        .build();

    TEIKatalogCommand.updateVerlag(katalog);

    assertTrue(katalog.getTeiXML()
            .contains("<publisher>Harrassowitz</publisher>"),
        katalog.getTeiXML());
  }
}
