package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIUpdatePURLsCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.KODListBiblMapper;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.PublicationStmt;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.TitleStmt;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 18.05.2020.
 * @version 1.0
 */
public class KODTemplateMapper {

  private static final String KOD_INITIAL_TEMPLATE = "kodinitial.xml";

  public static TEI createTEIFromInitialTemplate(KulturObjektDokument kod) throws Exception {

    Optional<TEI> teiOpt = loadTemplate();
    if (teiOpt.isEmpty()) {
      return null;
    }
    TEI tei = teiOpt.get();

    KODListBiblMapper.writeDescriptions(tei, kod.getBeschreibungenIDs());

    Optional<TitleStmt> titleStmtOpt = TEICommon.findFirst(TitleStmt.class, tei);
    TEICommon.findFirst(MsDesc.class, tei)
        .ifPresent(msDesc -> applyIdentifikation(msDesc, titleStmtOpt, kod));

    TEICommon.findFirst(PublicationStmt.class, tei)
        .ifPresent(KODTemplateMapper::applyPublicationDate);

    TEIUpdatePURLsCommand.updatePURLs(tei, kod.getPURLs());

    return tei;
  }

  private static Optional<TEI> loadTemplate() throws Exception {
    List<TEI> teiList = TEIObjectFactory.unmarshal(
        KODTemplateMapper.class.getClassLoader()
            .getResourceAsStream("tei/" + KOD_INITIAL_TEMPLATE));
    return teiList.size() == 1 ? Optional.of(teiList.getFirst()) : Optional.empty();
  }

  private static void applyIdentifikation(
      MsDesc msDesc, Optional<TitleStmt> titleStmtOpt, KulturObjektDokument kod) {

    msDesc.setId(kod.getId());

    if (Objects.isNull(kod.getGueltigeIdentifikation())) {
      return;
    }

    msDesc.setMsIdentifier(MsIdentifierFactory.build(
        kod.getGueltigeIdentifikation(), kod.getAlternativeIdentifikationen()));

    titleStmtOpt.ifPresent(titleStmt -> {
      titleStmt.getTitles().clear();
      titleStmt.getTitles().add(TitleFactory.build(kod.getGueltigeIdentifikation()));
    });
  }

  private static void applyPublicationDate(PublicationStmt publicationStmt) {
    String today = DateTimeFormatter.ISO_DATE.format(LocalDate.now());
    publicationStmt.getPublishersAndDistributorsAndAuthorities().stream()
        .filter(o -> o instanceof Date)
        .forEach(d -> {
          ((Date) d).setWhen(today);
          ((Date) d).setType(TEIValues.DATE_ISSUED);
          ((Date) d).getContent().clear();
          ((Date) d).getContent().add(today);
        });
  }
}
