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

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsTyp;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
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

  private static final String BESCHREIBUNG_INITIAL_TEMPLATE_MEDIEVAL = "beschreibunginitial_medieval.xml";

  private static final String BESCHREIBUNG_INITIAL_TEMPLATE_ILLUMINIERT = "beschreibunginitial_illuminiert.xml";

  private static final Map<BeschreibungsTyp, String> templateMap;

  private static final Logger logger = LoggerFactory.getLogger(BeschreibungsTemplateMapper.class);

  static {
    templateMap = Map
        .of(BeschreibungsTyp.MEDIEVAL, BESCHREIBUNG_INITIAL_TEMPLATE_MEDIEVAL,
            BeschreibungsTyp.ILLUMINATED, BESCHREIBUNG_INITIAL_TEMPLATE_ILLUMINIERT);
  }

  public static TEI createTEIFromInitialTemplate(Beschreibung beschreibung) throws Exception {

    final String templateName = beschreibung.getBeschreibungsTyp() != null ? templateMap
        .get(beschreibung.getBeschreibungsTyp()) : BESCHREIBUNG_INITIAL_TEMPLATE_MEDIEVAL;

    logger.info("Creating TEI from template {} ", templateName);

    try (InputStream inputStream = BeschreibungsTemplateMapper.class.getClassLoader()
        .getResourceAsStream("tei/" + templateName)) {

      List<TEI> teiList = TEIObjectFactory.unmarshal(inputStream);

      if (teiList.size() == 1) {

        final TEI tei = teiList.get(0);

        FileDescFactory.buildWithLicenseAndAutor(TEICommon.findFirst(FileDesc.class, tei),
            beschreibung.getLizenz(),
            beschreibung.getAutoren());

        ProfilDescFactory.buildWithCreationDate(TEICommon.findFirst(ProfileDesc.class, tei),
            beschreibung.getErstellungsDatum());

        RevisionDescFactory.buildWithModificationDate(TEICommon.findFirst(RevisionDesc.class, tei),
            beschreibung.getAenderungsDatum());

        MsDescFactory.buildWithInitialBeschreibung(TEICommon.findFirst(MsDesc.class, tei),
            beschreibung);

        return teiList.get(0);
      }
    }

    return null;
  }
}
