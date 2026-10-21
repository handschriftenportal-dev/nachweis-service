package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.FileDesc;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.ProfileDesc;
import org.tei_c.ns._1.RevisionDesc;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 13.05.2020.
 * @version 1.0
 */
public class BeschreibungsTemplateMapper {

  private static final Logger logger = LoggerFactory.getLogger(BeschreibungsTemplateMapper.class);

  private BeschreibungsTemplateMapper() {
  }

  public static TEI createTEIFromInitialTemplate(Beschreibung beschreibung, String templateXml)
      throws Exception {

    if (templateXml == null || templateXml.isBlank()) {
      throw new IllegalArgumentException("templateXml is empty");
    }

    logger.info("Creating TEI from provided template XML");

    try (InputStream inputStream =
        new ByteArrayInputStream(templateXml.getBytes(StandardCharsets.UTF_8))) {

      List<TEI> teiList = TEIObjectFactory.unmarshal(inputStream);

      if (teiList.size() == 1) {

        final TEI tei = teiList.getFirst();

        FileDescFactory.buildWithLicenseAndAutor(TEICommon.findFirst(FileDesc.class, tei),
            beschreibung.getLizenz(),
            beschreibung.getAutoren());

        ProfilDescFactory.buildWithCreationDate(TEICommon.findFirst(ProfileDesc.class, tei),
            beschreibung.getErstellungsDatum());

        RevisionDescFactory.buildWithModificationDate(TEICommon.findFirst(RevisionDesc.class, tei),
            beschreibung.getAenderungsDatum());

        TEICommon.findFirst(MsDesc.class, tei)
            .ifPresent(msDesc -> MsDescFactory.buildWithInitialBeschreibung(msDesc, beschreibung));

        return tei;
      }
    }

    return null;
  }
}
