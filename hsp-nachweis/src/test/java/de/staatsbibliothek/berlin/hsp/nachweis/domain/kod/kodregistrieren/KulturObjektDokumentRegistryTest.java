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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 25.08.2020.
 * @version 1.0
 */
public class KulturObjektDokumentRegistryTest {

  static final String signaturen = "\"Cbm Cat. 1\"$\"Codices bavarici monacenses Catalogi 1\"$\"Cod. bav. monac. Cat. 1\"$\"Cbm. Cat. 1\"$\"Clm 1330\"\n";

  @Test
  void testCreate() throws KulturObjektDokumentRegistrierenException {

    KulturObjektDokument kod = KulturObjektDokumentRegistry.create(createOrt(), createKoerperschaft(),
        KulturObjektDokumentRegistry.splitSignaturen(signaturen));

    assertNotNull(kod);
    assertNotNull(kod.getGueltigeIdentifikation());
    assertEquals("Cbm Cat. 1", kod.getGueltigeIdentifikation().getIdent());
    assertNotNull(kod.getGueltigeIdentifikation().getBesitzer());

    Set<String> altSignature = kod.getAlternativeIdentifikationen().stream().map(Identifikation::getIdent)
        .collect(Collectors.toSet());

    assertTrue(altSignature.contains("Codices bavarici monacenses Catalogi 1"));
    assertTrue(altSignature.contains("Cod. bav. monac. Cat. 1"));
    assertTrue(altSignature.contains("Cbm. Cat. 1"));
    assertTrue(altSignature.contains("Clm 1330"));

  }

  @Test
  void testWithEmptyGueltigeSignature() {

    final String signaturen2 = "\"\"$\"Codices bavarici monacenses Catalogi 1\"$\"Cod. bav. monac. Cat. 1\"$\"Cbm. Cat. 1\"$\"Clm 1330\"\n";

    Exception exception = assertThrows(KulturObjektDokumentRegistrierenException.class, () -> {
      KulturObjektDokumentRegistry
          .create(createOrt(), createKoerperschaft(), KulturObjektDokumentRegistry.splitSignaturen(signaturen2));
    });

    assertEquals("Wrong CSV Signatur file. -> GültigeSignatur is leer", exception.getMessage());
  }

  @Test
  void testWithNotValidContent2GueltigeSignature() throws KulturObjektDokumentRegistrierenException {

    final String signaturen2 = "\"Codices\"$\"Codices bavarici monacenses Catalogi 1\"$\"Cod. bav. monac. Cat. 1\"$\"Cbm. Cat. 1\"$\"Clm 1330\"\n";

    KulturObjektDokument kod = KulturObjektDokumentRegistry
        .create(createOrt(), createKoerperschaft(), KulturObjektDokumentRegistry.splitSignaturen(signaturen2));

    assertNotNull(kod);
    assertNotNull(kod.getGueltigeIdentifikation());
    assertEquals("Codices", kod.getGueltigeIdentifikation().getIdent());
    assertNotNull(kod.getGueltigeIdentifikation().getBesitzer());

    Set<String> altSignature = kod.getAlternativeIdentifikationen().stream().map(Identifikation::getIdent)
        .collect(Collectors.toSet());

    assertTrue(altSignature.contains("Codices bavarici monacenses Catalogi 1"));
    assertTrue(altSignature.contains("Cod. bav. monac. Cat. 1"));
    assertTrue(altSignature.contains("Cbm. Cat. 1"));
    assertTrue(altSignature.contains("Clm 1330"));
  }

  @Test
  void testWith4QouatesNotValidContentGueltigeSignature() throws KulturObjektDokumentRegistrierenException {

    final String signaturen2 = "\"Cod. Guelf. 510 Helmst.\"$\n \"Cod. Guelf. 310 Helmst.\"";

    KulturObjektDokumentRegistrierenException kulturObjektDokumentRegistrierenException = assertThrows(
        KulturObjektDokumentRegistrierenException.class, () -> KulturObjektDokumentRegistry
            .create(createOrt(), createKoerperschaft(), KulturObjektDokumentRegistry.splitSignaturen(signaturen2)));
    assertEquals("More or less then two quotes. Line=" + KulturObjektDokumentRegistry.cleanUpLineCell(signaturen2),
        kulturObjektDokumentRegistrierenException.getMessage());
  }

  @Test
  void testWithWrongStartAndEndForGueltigeSignature() throws KulturObjektDokumentRegistrierenException {

    final String signaturen2 = "\"Cod. Guelf. 510 Helmst.\"\n!";

    KulturObjektDokumentRegistrierenException kulturObjektDokumentRegistrierenException = assertThrows(
        KulturObjektDokumentRegistrierenException.class, () -> KulturObjektDokumentRegistry
            .create(createOrt(), createKoerperschaft(), KulturObjektDokumentRegistry.splitSignaturen(signaturen2)));

    assertEquals("Line has to start with \" and it has to end with \"  Line=" + KulturObjektDokumentRegistry
        .cleanUpLineCell(signaturen2), kulturObjektDokumentRegistrierenException.getMessage());
  }

  @Test
  void testWithNewLineAtStartAndEnd() throws KulturObjektDokumentRegistrierenException {

    final String signaturen2 = "\n\"Cod. Guelf. 510 Helmst.\"\n";

    KulturObjektDokument kod = KulturObjektDokumentRegistry
        .create(createOrt(), createKoerperschaft(), KulturObjektDokumentRegistry.splitSignaturen(signaturen2));

    assertNotNull(kod);
    assertNotNull(kod.getGueltigeIdentifikation());
    assertEquals("Cod. Guelf. 510 Helmst.", kod.getGueltigeIdentifikation().getIdent());
  }

  @Test
  void testCheckLineCellsForWrongSplitting() {
    final String signaturen1 = "\"Cod. Guelf. 510 Helmst.\"\n!";

    KulturObjektDokumentRegistrierenException kulturObjektDokumentRegistrierenException = assertThrows(KulturObjektDokumentRegistrierenException.class, () -> KulturObjektDokumentRegistry.checkLineCellsForWrongSplitting(signaturen1));

    assertEquals("Line has to start with \" and it has to end with \"  Line=" + KulturObjektDokumentRegistry.cleanUpLineCell(signaturen1), kulturObjektDokumentRegistrierenException.getMessage());
  }

  @Test
  void testCheckLineCellsMoreThen2Quotes() {
    final String signaturen1 = "\"Cod. Guelf. \"510 Helmst.\"\n!";

    KulturObjektDokumentRegistrierenException kulturObjektDokumentRegistrierenException = assertThrows(
        KulturObjektDokumentRegistrierenException.class,
        () -> KulturObjektDokumentRegistry.checkLineCellsForWrongSplitting(signaturen1));

    assertEquals("More or less then two quotes. Line=" + KulturObjektDokumentRegistry.cleanUpLineCell(signaturen1),
        kulturObjektDokumentRegistrierenException.getMessage());
  }

  @Test
  void testSplitSignaturen() throws KulturObjektDokumentRegistrierenException {
    List<String> signaturenList = KulturObjektDokumentRegistry.splitSignaturen(signaturen);

    assertNotNull(signaturenList);
    assertEquals(5, signaturenList.size());

    StringJoiner joiner = new StringJoiner("\"$\"", "\"", "\"\n");
    signaturenList.forEach(s -> joiner.add(s));

    assertEquals(joiner.toString(), signaturen);
  }

  private NormdatenReferenz createKoerperschaft() {
    return new NormdatenReferenz(TEIValues.UUID_PREFIX + UUID.randomUUID(), "Staatsbibliothek zu Berlin", "234234-x");
  }

  private NormdatenReferenz createOrt() {
    return new NormdatenReferenz(TEIValues.UUID_PREFIX + UUID.randomUUID(), "Berlin", "4005728-8");
  }

}
