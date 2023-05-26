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

package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIUpdatePURLsCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.tei_c.ns._1.Additional;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.ListBibl;
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

    List<TEI> teiList = TEIObjectFactory
        .unmarshal(KODTemplateMapper.class.getClassLoader()
            .getResourceAsStream("tei/" + KOD_INITIAL_TEMPLATE));

    if (teiList.size() == 1) {

      TEI tei = teiList.get(0);

      List<MsDesc> msDescList = new ArrayList<>();
      List<PublicationStmt> publicationStmtList = new ArrayList<>();
      List<TitleStmt> titles = new ArrayList<>();

      TEICommon.findAll(MsDesc.class, tei, msDescList);
      TEICommon.findAll(PublicationStmt.class, tei, publicationStmtList);
      TEICommon.findAll(TitleStmt.class, tei, titles);
      Optional<Additional> additional = TEICommon.findFirst(Additional.class, tei);

      kod.getBeschreibungenIDs().stream().forEach(b -> additional.ifPresent(addi -> {
        if (addi.getListBibl() == null) {
          addi.setListBibl(new ListBibl());
        }
        addi.getListBibl().getBiblsAndBiblStructsAndListBibls().clear();
        addi.getListBibl().getBiblsAndBiblStructsAndListBibls().add(BiblFactory.build(b));
      }));

      msDescList.stream().findFirst().ifPresent(m -> {
        m.setId(kod.getId());

        if (kod.getGueltigeIdentifikation() != null) {
          m.setMsIdentifier(MsIdentifierFactory
              .build(kod.getGueltigeIdentifikation(), kod.getAlternativeIdentifikationen()));
          titles.stream().findFirst().ifPresent(titleStmt -> {
            titleStmt.getTitles().clear();
            titleStmt.getTitles().add(TitleFactory.build(kod.getGueltigeIdentifikation()));
          });
        }

      });

      publicationStmtList.stream().findFirst().ifPresent(publicationStmt -> {
        publicationStmt.getPublishersAndDistributorsAndAuthorities().stream()
            .filter(o -> o instanceof Date)
            .forEach(d -> {
              ((Date) d).setWhen(DateTimeFormatter.ISO_DATE.format(LocalDate.now()));
              ((Date) d).setType(TEIValues.DATE_ISSUED);
              ((Date) d).getContent().clear();
              ((Date) d).getContent().add(DateTimeFormatter.ISO_DATE.format(LocalDate.now()));
            });
      });

      TEIUpdatePURLsCommand.updatePURLs(tei, kod.getPURLs());

      return tei;
    }

    return null;
  }
}
