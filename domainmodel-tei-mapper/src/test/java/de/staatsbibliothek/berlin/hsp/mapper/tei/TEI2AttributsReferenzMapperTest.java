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

package de.staatsbibliothek.berlin.hsp.mapper.tei;


import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TEXT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AbmessungWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.EntstehungszeitWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.FileDesc;
import org.tei_c.ns._1.Head;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.SourceDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.TeiHeader;
import org.tei_c.ns._1.Term;

class TEI2AttributsReferenzMapperTest {

  private static final Path KOD_ATTRIBUTSREFERENZEN = Paths.get("src", "test", "resources", "tei",
      "tei-kod-attributsreferenzen.xml");

  @Test
  void testMap_String() throws Exception {
    String teiXML = Files.readString(KOD_ATTRIBUTSREFERENZEN, StandardCharsets.UTF_8);

    Map<AttributsTyp, AttributsReferenz> referenzen = TEI2AttributsReferenzMapper.map(teiXML);

    assertNotNull(referenzen);
    assertEquals(AttributsTyp.values().length, referenzen.size());

    checkTitel(referenzen);
    checkBeschreibstoff(referenzen);
    checkUmfang(referenzen);
    checkAbmessung(referenzen);
    checkFormat(referenzen);
    checkEntstehungsort(referenzen);
    checkEntstehungszeit(referenzen);
    checkGrundsprache(referenzen);
    checkBuchschmuck(referenzen);
    checkHandschriftentyp(referenzen);
    checkMusiknotation(referenzen);
    checkStatus(referenzen);
  }

  @Test
  void testMap_TeiMsDesc() throws Exception {
    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    tei.setTeiHeader(header);

    FileDesc fileDesc = new FileDesc();
    header.setFileDesc(fileDesc);

    SourceDesc sourceDesc = new SourceDesc();
    fileDesc.getSourceDescs().add(sourceDesc);

    MsDesc msDesc = createMsDescWithIndexOrigDate("HSP-123");
    sourceDesc.getBiblsAndBiblStructsAndListBibls().add(msDesc);

    Map<AttributsTyp, AttributsReferenz> referenzen = TEI2AttributsReferenzMapper.map(tei, msDesc);
    assertNotNull(referenzen);
    assertEquals(1, referenzen.size());
    assertTrue(referenzen.containsKey(AttributsTyp.ENTSTEHUNGSZEIT));

    AttributsReferenz attributsReferenz = referenzen.get(AttributsTyp.ENTSTEHUNGSZEIT);
    assertNotNull(attributsReferenz);
    assertEquals("HSP-123", attributsReferenz.getReferenzId());
    assertSame(AttributsTyp.ENTSTEHUNGSZEIT, attributsReferenz.getAttributsTyp());
    assertEquals("entstehungszeit", attributsReferenz.getText());
  }

  @Test
  void testMapAttributsReferenz() throws Exception {
    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    tei.setTeiHeader(header);

    FileDesc fileDesc = new FileDesc();
    header.setFileDesc(fileDesc);

    SourceDesc sourceDesc = new SourceDesc();
    fileDesc.getSourceDescs().add(sourceDesc);

    MsDesc emptyMsDesc = new MsDesc();
    sourceDesc.getBiblsAndBiblStructsAndListBibls().add(emptyMsDesc);

    assertNull(TEI2AttributsReferenzMapper.mapAttributsReferenz(emptyMsDesc, tei,
        Entstehungszeit.class));

    MsDesc msDesc = createMsDescWithIndexOrigDate("HSP-456");
    sourceDesc.getBiblsAndBiblStructsAndListBibls().clear();
    sourceDesc.getBiblsAndBiblStructsAndListBibls().add(msDesc);

    Entstehungszeit entstehungszeit = TEI2AttributsReferenzMapper.mapAttributsReferenz(msDesc, tei,
        Entstehungszeit.class);

    assertNotNull(entstehungszeit);

    assertEquals("HSP-456", entstehungszeit.getReferenzId());
    assertSame(AttributsTyp.ENTSTEHUNGSZEIT, entstehungszeit.getAttributsTyp());
    assertEquals("entstehungszeit", entstehungszeit.getText());
  }

  @Test
  void unmarshalTEI() throws IOException, HSPMapperException {
    String teiXML = Files.readString(KOD_ATTRIBUTSREFERENZEN, StandardCharsets.UTF_8);
    final TEI tei = TEI2AttributsReferenzMapper.unmarshalTEI(teiXML);

    assertNotNull(tei);
  }

  @Test
  void testFindFirstMsDesc() throws HSPMapperException {
    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    tei.setTeiHeader(header);

    FileDesc fileDesc = new FileDesc();
    header.setFileDesc(fileDesc);

    SourceDesc sourceDesc = new SourceDesc();
    fileDesc.getSourceDescs().add(sourceDesc);

    MsDesc firstMsDesc = new MsDesc();
    firstMsDesc.setId("HSP-123");
    sourceDesc.getBiblsAndBiblStructsAndListBibls().add(firstMsDesc);

    MsDesc secondMsDesc = new MsDesc();
    secondMsDesc.setId("HSP-456");
    sourceDesc.getBiblsAndBiblStructsAndListBibls().add(secondMsDesc);

    MsDesc msDesc = TEI2AttributsReferenzMapper.findFirstMsDesc(tei);
    assertNotNull(msDesc);
    assertEquals("HSP-123", msDesc.getId());
  }

  void checkTitel(Map<AttributsTyp, AttributsReferenz> referenzen) {
    Titel titel = findAttributsReferenz(referenzen, Titel.class).orElse(null);
    assertNotNull(titel);

    assertEquals("HSP-B2", titel.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, titel.getReferenzTyp());
    assertEquals("Johannes Cassianus: Collationes, pars II, III", titel.getText());
  }

  void checkBeschreibstoff(Map<AttributsTyp, AttributsReferenz> referenzen) {
    Beschreibstoff beschreibstoff = findAttributsReferenz(referenzen, Beschreibstoff.class).orElse(null);
    assertNotNull(beschreibstoff);

    assertEquals("HSP-B2", beschreibstoff.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, beschreibstoff.getReferenzTyp());
    assertEquals("Pergament, Papyrus", beschreibstoff.getText());
    assertNotNull(beschreibstoff.getTypen());
    assertEquals(2, beschreibstoff.getTypen().size());

    assertTrue(beschreibstoff.getTypen().contains("parchment"), "expected: parchment");
    assertTrue(beschreibstoff.getTypen().contains("papyrus"), "expected: papyrus");
  }

  void checkUmfang(Map<AttributsTyp, AttributsReferenz> referenzen) {
    Umfang umfang = findAttributsReferenz(referenzen, Umfang.class).orElse(null);
    assertNotNull(umfang);

    assertEquals("HSP-B2", umfang.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, umfang.getReferenzTyp());
    assertEquals("I + 208 Bl.", umfang.getText());
    assertEquals("208", umfang.getBlattzahl());
  }

  void checkAbmessung(Map<AttributsTyp, AttributsReferenz> referenzen) {
    Abmessung abmessung = findAttributsReferenz(referenzen, Abmessung.class).orElse(null);
    assertNotNull(abmessung);

    assertEquals("HSP-B2", abmessung.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, abmessung.getReferenzTyp());
    assertEquals("24 × 18,5; 22 × 16,5", abmessung.getText());
    assertNotNull(abmessung.getAbmessungWerte());
    assertEquals(2, abmessung.getAbmessungWerte().size());

    assertTrue(checkAbmessungWert(abmessung.getAbmessungWerte().get(0),
        "24 × 18,5", "24", "18,5", "1,5", "factual"));
    assertTrue(checkAbmessungWert(abmessung.getAbmessungWerte().get(1),
        "22 × 16,5", "22", "16,5", "2,5", "deduced"));
  }

  void checkFormat(Map<AttributsTyp, AttributsReferenz> referenzen) {
    Format format = findAttributsReferenz(referenzen, Format.class).orElse(null);
    assertNotNull(format);

    assertEquals("HSP-B2", format.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, format.getReferenzTyp());
    assertEquals("smaller_than_octavo;long_and_narrow", format.getText());
    assertNotNull(format.getFormatWerte());
    assertEquals(2, format.getFormatWerte().size());

    assertTrue(checkFormatWert(format.getFormatWerte().get(0), "smaller_than_octavo", "factual"));
    assertTrue(checkFormatWert(format.getFormatWerte().get(1), "long_and_narrow", "deduced"));
  }

  void checkEntstehungsort(Map<AttributsTyp, AttributsReferenz> referenzen) {
    Entstehungsort entstehungsort = findAttributsReferenz(referenzen, Entstehungsort.class).orElse(null);
    assertNotNull(entstehungsort);

    assertEquals("HSP-B2", entstehungsort.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, entstehungsort.getReferenzTyp());
    assertEquals("Regensburg", entstehungsort.getText());
    assertNotNull(entstehungsort.getNormdatenReferenzen());
    assertEquals(1, entstehungsort.getNormdatenReferenzen().size());

    assertTrue(entstehungsort.getNormdatenReferenzen().stream()
        .anyMatch(nr -> "NORM-26cf9267-82fe-3bf1-a37a-c9960658499f".equals(nr.getId())
            && "4048989-9".equals(nr.getGndID())
            && "Regensburg".equals(nr.getName())
            && NormdatenReferenz.ORT_TYPE_NAME.equals(nr.getTypeName())));
  }

  void checkEntstehungszeit(Map<AttributsTyp, AttributsReferenz> referenzen) {
    Entstehungszeit entstehungszeit = findAttributsReferenz(referenzen, Entstehungszeit.class).orElse(null);
    assertNotNull(entstehungszeit);

    assertEquals("HSP-B2", entstehungszeit.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, entstehungszeit.getReferenzTyp());
    assertEquals("11. Jh., Mitte; um 11. Jh., Mitte", entstehungszeit.getText());
    assertNotNull(entstehungszeit.getEntstehungszeitWerte());
    assertEquals(2, entstehungszeit.getEntstehungszeitWerte().size());

    assertTrue(entstehungszeit.getEntstehungszeitWerte().stream()
        .anyMatch(wert ->
            checkEntstehungszeitWert(wert, "11. Jh., Mitte", "1046", "1055")));

    assertTrue(entstehungszeit.getEntstehungszeitWerte().stream()
        .anyMatch(wert ->
            checkEntstehungszeitWert(wert, "um 11. Jh., Mitte", "1026", "1075")));
  }

  void checkGrundsprache(Map<AttributsTyp, AttributsReferenz> referenzen) {
    Grundsprache grundSprache = findAttributsReferenz(referenzen, Grundsprache.class).orElse(null);
    assertNotNull(grundSprache);

    assertEquals("HSP-B2", grundSprache.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, grundSprache.getReferenzTyp());
    assertEquals("Mittelhochdeutsch", grundSprache.getText());
    assertNotNull(grundSprache.getNormdatenIds());
    assertEquals(1, grundSprache.getNormdatenIds().size());

    assertTrue(grundSprache.getNormdatenIds().stream().anyMatch("4039687-3"::equals));
  }

  void checkBuchschmuck(Map<AttributsTyp, AttributsReferenz> referenzen) {
    Buchschmuck buchschmuck = findAttributsReferenz(referenzen, Buchschmuck.class).orElse(null);
    assertNotNull(buchschmuck);

    assertEquals("HSP-B2", buchschmuck.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, buchschmuck.getReferenzTyp());
    assertEquals("yes", buchschmuck.getText());
  }

  void checkHandschriftentyp(Map<AttributsTyp, AttributsReferenz> referenzen) {
    Handschriftentyp handschriftentyp = findAttributsReferenz(referenzen, Handschriftentyp.class).orElse(null);
    assertNotNull(handschriftentyp);

    assertEquals("HSP-B2", handschriftentyp.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, handschriftentyp.getReferenzTyp());
    assertEquals("codex", handschriftentyp.getText());
  }

  void checkMusiknotation(Map<AttributsTyp, AttributsReferenz> referenzen) {
    Musiknotation musiknotation = findAttributsReferenz(referenzen, Musiknotation.class).orElse(null);
    assertNotNull(musiknotation);

    assertEquals("HSP-B2", musiknotation.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, musiknotation.getReferenzTyp());
    assertEquals("no", musiknotation.getText());
  }

  void checkStatus(Map<AttributsTyp, AttributsReferenz> referenzen) {
    Status status = findAttributsReferenz(referenzen, Status.class).orElse(null);
    assertNotNull(status);

    assertEquals("HSP-B2", status.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, status.getReferenzTyp());
    assertEquals("displaced", status.getText());
  }

  boolean checkEntstehungszeitWert(EntstehungszeitWert wert, String text, String nichtVor, String nichtNach) {
    return Objects.nonNull(wert)
        && Objects.nonNull(wert.getId())
        && List.of(text, nichtVor, nichtNach, "datable").equals(
        List.of(wert.getText(), wert.getNichtVor(), wert.getNichtNach(), wert.getTyp()));
  }

  boolean checkAbmessungWert(AbmessungWert wert, String text, String hoehe, String breite, String tiefe, String typ) {
    return Objects.nonNull(wert)
        && Objects.nonNull(wert.getId())
        && List.of(text, hoehe, breite, tiefe, typ).equals(
        List.of(wert.getText(), wert.getHoehe(), wert.getBreite(), wert.getTiefe(), wert.getTyp()));
  }

  boolean checkFormatWert(FormatWert wert, String text, String typ) {
    return Objects.nonNull(wert)
        && Objects.nonNull(wert.getId())
        && List.of(text, typ).equals(List.of(wert.getText(), wert.getTyp()));
  }

  private MsDesc createMsDescWithIndexOrigDate(String msDescId) {
    MsDesc msDesc = new MsDesc();
    msDesc.setId(msDescId);

    Head head = new Head();
    msDesc.getHeads().add(head);

    Index index = new Index();
    index.setIndexName(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName());
    head.getContent().add(index);

    Term termText = new Term();
    termText.setType(ENTSTEHUNGSZEIT_TERM_TYP_TEXT);
    termText.getContent().add("entstehungszeit");
    index.getTermsAndIndices().add(termText);

    return msDesc;
  }

  private <T> Optional<T> findAttributsReferenz(Map<AttributsTyp, AttributsReferenz> referenzen, Class<T> clazz) {
    return referenzen.values().stream()
        .filter(clazz::isInstance)
        .map(clazz::cast)
        .findFirst();
  }

}
