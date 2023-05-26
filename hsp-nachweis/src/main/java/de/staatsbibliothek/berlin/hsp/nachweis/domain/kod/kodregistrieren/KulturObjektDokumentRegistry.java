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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.KODTemplateMapper;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 25.08.2020.
 * @version 1.0
 */

public class KulturObjektDokumentRegistry {

  public static final Pattern PATTERN_CSV_LINE_SPLIT = Pattern.compile("(?<=\")(\\$)(?=\")");
  public static final Pattern PATTERN_TEXT_BETWEAN_QUOTES = Pattern.compile("(?<=\")[^\"]+(?=\")");


  private static Logger logger = LoggerFactory.getLogger(KulturObjektDokumentRegistry.class);

  public static KulturObjektDokument create(NormdatenReferenz ort,
      NormdatenReferenz koerperschaft, List<String> signaturen)
      throws KulturObjektDokumentRegistrierenException {

    logger.info("Creating KOD with {} {} {} ", ort, koerperschaft, signaturen);

    if (Objects.isNull(signaturen) || signaturen.isEmpty()) {
      throw new KulturObjektDokumentRegistrierenException("Keine Signaturen vorhanden!");
    }

    checkForValidGueltigeSignature(signaturen.get(0));

    String kodId = TEIValues.UUID_PREFIX + UUID.nameUUIDFromBytes(
        (ort.getId() + "$" + koerperschaft.getId() + "$" + signaturen.get(0))
            .getBytes(StandardCharsets.UTF_8)).toString();

    KulturObjektDokumentBuilder kodBuilder = new KulturObjektDokumentBuilder()
        .withId(kodId)
        .withRegistrierungsDatum(LocalDateTime.now());

    kodBuilder.withGueltigerIdentifikation(
        buildIdentifikation(ort, koerperschaft, IdentTyp.GUELTIGE_SIGNATUR, signaturen.get(0)));

    signaturen.stream().skip(1).forEach(
        s -> kodBuilder.addAlternativeIdentifikation(
            buildIdentifikation(ort, koerperschaft, IdentTyp.ALTSIGNATUR, s)));

    KulturObjektDokument kulturObjektDokument = kodBuilder.build();

    logger.debug("Created KOD {} ", kulturObjektDokument);

    return kulturObjektDokument;
  }

  static String cleanUpLineCell(String line) {
    if(line == null) {
      return null;
    }
    return line.replaceAll("([\\r\\n\\uFEFF])", "").trim();
  }

  static void checkLineCellsForWrongSplitting(String line) throws KulturObjektDokumentRegistrierenException {
    line =cleanUpLineCell(line);

    if (StringUtils.countMatches(line, "\"") != 2) {
      throw new KulturObjektDokumentRegistrierenException("More or less then two quotes. Line=" + line);
    }

    if (!line.startsWith("\"") || !line.endsWith("\"")) {
      throw new KulturObjektDokumentRegistrierenException("Line has to start with \" and it has to end with \"  Line=" + line);
    }
  }

  private static void checkForValidGueltigeSignature(String identValue)
      throws KulturObjektDokumentRegistrierenException {
    if (identValue.isEmpty()) {
      throw new KulturObjektDokumentRegistrierenException(
          "Wrong CSV Signatur file. -> GültigeSignatur is leer" + identValue);
    }
  }

  private static Identifikation buildIdentifikation(NormdatenReferenz ort,
      NormdatenReferenz koerperschaft, IdentTyp identTyp, String identValue) {
    Identifikation identifikation = new Identifikation
        .IdentifikationBuilder()
        .withId(UUID.randomUUID().toString())
        .withIdentTyp(identTyp)
        .withIdent(identValue)
        .build();

    if (ort != null) {
      identifikation.setAufbewahrungsOrt(ort);
    }

    if (koerperschaft != null) {
      identifikation.setBesitzer(koerperschaft);
    }

    return identifikation;
  }

  public static void addTEI(KulturObjektDokument kulturObjektDokument)
      throws KulturObjektDokumentRegistrierenException {
    try {
      TEI teiDocument = KODTemplateMapper.createTEIFromInitialTemplate(kulturObjektDokument);
      String xml = TEIObjectFactory.marshal(teiDocument);
      kulturObjektDokument.setTeiXML(xml);

    } catch (Exception e) {
      throw new KulturObjektDokumentRegistrierenException("Can't create TEI XML For KOD!", e);
    }
  }

  public static List<String> splitSignaturen(String signaturenLine) throws KulturObjektDokumentRegistrierenException {
    String[] lineCells = PATTERN_CSV_LINE_SPLIT.split(signaturenLine);
    int columnsCount = lineCells.length;
    List<String> signaturen = new ArrayList<>(columnsCount);

    for (int columnIndex = 0; columnIndex < columnsCount; columnIndex++) {
      checkLineCellsForWrongSplitting(lineCells[columnIndex]);
      Matcher matcher = PATTERN_TEXT_BETWEAN_QUOTES.matcher(lineCells[columnIndex]);
      String identValue;
      if (matcher.find()) {
        identValue = matcher.group();
      } else {
        identValue = lineCells[columnIndex];
      }
      identValue = identValue.replaceAll("(^\"|\"$)", "");

      if (columnIndex == 0) {
        checkForValidGueltigeSignature(identValue);
      }

      if (identValue.isEmpty()) {
        break;
      }
      signaturen.add(identValue);

    }
    return signaturen;
  }

}
