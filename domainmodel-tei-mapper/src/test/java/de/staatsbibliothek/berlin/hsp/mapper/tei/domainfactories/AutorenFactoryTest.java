package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.FileDesc;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 11.11.2021.
 * @version 1.0
 */
public class AutorenFactoryTest {

  private static TEI tei;

  @BeforeAll
  public static void setup() throws IOException, JAXBException {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-for-Autoren.xml");
    try (InputStream is = newInputStream(teiFilePath)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei = teiList.getFirst();
    }
  }

  @Test
  void testbuildAutoren() throws Exception {

    Set<NormdatenReferenz> autoren =
        AutorenFactory.buildAutoren(TEICommon.findFirst(FileDesc.class, tei));

    assertNotNull(autoren);
    assertEquals(1, autoren.size());

    autoren.stream()
        .findFirst()
        .ifPresentOrElse(
            hoffman -> {
              assertEquals("Werner Hoffmann", hoffman.getName());
              assertEquals("Person", hoffman.getTypeName());
            },
            Assertions::fail);
  }
}
