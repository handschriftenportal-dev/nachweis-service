package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon.findAll;
import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektTyp;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.05.2020.
 * @version 1.0
 */
public class BeschreibungsKomponenteKopfFactoryTest {

  private static final Logger log = LoggerFactory.getLogger(
      BeschreibungsKomponenteKopfFactoryTest.class);
  static TEI tei;
  static MsDesc msDesc;

  static {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei",
        "tei-msDesc_Westphal_head.xml");
    try (InputStream is = newInputStream(teiFilePath)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei = teiList.getFirst();

      List<MsDesc> msDescList = new ArrayList<>();
      findAll(MsDesc.class, tei, msDescList);

      msDesc = msDescList.getFirst();
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

  BeschreibungsKomponenteKopfFactory factory = new BeschreibungsKomponenteKopfFactory();

  @Test
  void testBuildKomponenteKopf() throws HSPMapperException {

    BeschreibungsKomponenteKopf kopf = factory.build(msDesc, tei);

    assertNotNull(kopf);

    assertNotNull(kopf.getId());

    assertEquals(KulturObjektTyp.CODEX, kopf.getKulturObjektTyp());

    assertEquals("Index term title", kopf.getTitel());

    assertEquals(2, kopf.getGrundsprachen().size());

    assertEquals("de",
        kopf.getGrundsprachen().stream().filter(s -> s.getName().equals("de"))
            .findFirst().get().getName());
  }

  @Test
  void testBuildTitleString() {

    assertEquals("Index term title",
        BeschreibungsKomponenteKopfFactory.buildTitleString(msDesc));
  }
}
