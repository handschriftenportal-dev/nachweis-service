package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
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

  static final String signaturen = """
      "Cbm Cat. 1"$"cbm_cat_1"$"Codices bavarici monacenses Catalogi 1"$"Cod. bav. monac. Cat. 1"$"Cbm. Cat. 1"$"Clm 1330"\n\
      """;

  @Test
  void testCreate() throws KulturObjektDokumentRegistrierenException {

    KulturObjektDokument kod = KulturObjektDokumentRegistry.create(createOrt(),
        createKoerperschaft(),
        KulturObjektDokumentRegistry.splitSignaturen(signaturen));

    assertNotNull(kod);
    assertNotNull(kod.getGueltigeIdentifikation());
    assertEquals("Cbm Cat. 1", kod.getGueltigeIdentifikation().getIdent());
    assertEquals("cbm_cat_1", kod.getGueltigeIdentifikation().getSortKey());
    assertNotNull(kod.getGueltigeIdentifikation().getBesitzer());

    Set<String> altSignature = kod.getAlternativeIdentifikationen().stream()
        .map(Identifikation::getIdent)
        .collect(Collectors.toSet());

    assertTrue(altSignature.contains("Codices bavarici monacenses Catalogi 1"));
    assertTrue(altSignature.contains("Cod. bav. monac. Cat. 1"));
    assertTrue(altSignature.contains("Cbm. Cat. 1"));
    assertTrue(altSignature.contains("Clm 1330"));

  }

  @Test
  void testWithEmptyGueltigeSignature() {

    final String signaturen2 = """
        ""$""$"Codices bavarici monacenses Catalogi 1"$"Cod. bav. monac. Cat. 1"$"Cbm. Cat. 1"$"Clm 1330"\n
        """;

    Exception exception = assertThrows(KulturObjektDokumentRegistrierenException.class, () -> {
      KulturObjektDokumentRegistry
          .create(createOrt(), createKoerperschaft(),
              KulturObjektDokumentRegistry.splitSignaturen(signaturen2));
    });

    assertEquals("Wrong CSV Signatur file. -> GültigeSignatur is leer", exception.getMessage());
  }

  @Test
  void testWithNotValidContent2GueltigeSignature()
      throws KulturObjektDokumentRegistrierenException {

    final String signaturen2 = """
        "Codices"$"codices"$"Codices bavarici monacenses Catalogi 1"$"Cod. bav. monac. Cat. 1"$"Cbm. Cat. 1"$"Clm 1330"\n
        """;

    KulturObjektDokument kod = KulturObjektDokumentRegistry
        .create(createOrt(), createKoerperschaft(),
            KulturObjektDokumentRegistry.splitSignaturen(signaturen2));

    assertNotNull(kod);
    assertNotNull(kod.getGueltigeIdentifikation());
    assertEquals("Codices", kod.getGueltigeIdentifikation().getIdent());
    assertEquals("codices", kod.getGueltigeIdentifikation().getSortKey());
    assertNotNull(kod.getGueltigeIdentifikation().getBesitzer());

    Set<String> altSignature = kod.getAlternativeIdentifikationen().stream()
        .map(Identifikation::getIdent)
        .collect(Collectors.toSet());

    assertTrue(altSignature.contains("Codices bavarici monacenses Catalogi 1"));
    assertTrue(altSignature.contains("Cod. bav. monac. Cat. 1"));
    assertTrue(altSignature.contains("Cbm. Cat. 1"));
    assertTrue(altSignature.contains("Clm 1330"));
  }

  @Test
  void testWith4QouatesNotValidContentGueltigeSignature()
      throws KulturObjektDokumentRegistrierenException {

    final String signaturen2 = """
        "Cod. Guelf. 510 Helmst."$
         "Cod. Guelf. 310 Helmst."
        """;

    KulturObjektDokumentRegistrierenException kulturObjektDokumentRegistrierenException = assertThrows(
        KulturObjektDokumentRegistrierenException.class, () -> KulturObjektDokumentRegistry
            .create(createOrt(), createKoerperschaft(),
                KulturObjektDokumentRegistry.splitSignaturen(signaturen2)));
    assertEquals(
        "More or less then two quotes. Line=" + KulturObjektDokumentRegistry.cleanUpLineCell(
            signaturen2),
        kulturObjektDokumentRegistrierenException.getMessage());
  }

  @Test
  void testWithWrongStartAndEndForGueltigeSignature()
      throws KulturObjektDokumentRegistrierenException {

    final String signaturen2 = """
        "Cod. Guelf. 510 Helmst."
        !
        """;

    KulturObjektDokumentRegistrierenException kulturObjektDokumentRegistrierenException = assertThrows(
        KulturObjektDokumentRegistrierenException.class, () -> KulturObjektDokumentRegistry
            .create(createOrt(), createKoerperschaft(),
                KulturObjektDokumentRegistry.splitSignaturen(signaturen2)));

    assertEquals(
        "Line has to start with \" and it has to end with \"  Line=" + KulturObjektDokumentRegistry
            .cleanUpLineCell(signaturen2), kulturObjektDokumentRegistrierenException.getMessage());
  }

  @Test
  void testWithNewLineAtStartAndEnd() throws KulturObjektDokumentRegistrierenException {

    final String signaturen2 = "\n\"Cod. Guelf. 510 Helmst.\"$\"cod_guelf_510\"\n";

    KulturObjektDokument kod = KulturObjektDokumentRegistry
        .create(createOrt(), createKoerperschaft(),
            KulturObjektDokumentRegistry.splitSignaturen(signaturen2));

    assertNotNull(kod);
    assertNotNull(kod.getGueltigeIdentifikation());
    assertEquals("Cod. Guelf. 510 Helmst.", kod.getGueltigeIdentifikation().getIdent());
    assertEquals("cod_guelf_510", kod.getGueltigeIdentifikation().getSortKey());
  }

  @Test
  void testCheckLineCellsForWrongSplitting() {

    final String signaturen1 = """
        "Cod. Guelf. 510 Helmst."
        !
        """;

    KulturObjektDokumentRegistrierenException kulturObjektDokumentRegistrierenException = assertThrows(
        KulturObjektDokumentRegistrierenException.class,
        () -> KulturObjektDokumentRegistry.checkLineCellsForWrongSplitting(signaturen1));

    assertEquals("Line has to start with \" and it has to end with \"  Line="
            + KulturObjektDokumentRegistry.cleanUpLineCell(signaturen1),
        kulturObjektDokumentRegistrierenException.getMessage());
  }

  @Test
  void testCheckLineCellsMoreThen2Quotes() {

    final String signaturen1 = """
        "Cod. Guelf. "510 Helmst."
        !
        """;

    KulturObjektDokumentRegistrierenException kulturObjektDokumentRegistrierenException = assertThrows(
        KulturObjektDokumentRegistrierenException.class,
        () -> KulturObjektDokumentRegistry.checkLineCellsForWrongSplitting(signaturen1));

    assertEquals(
        "More or less then two quotes. Line=" + KulturObjektDokumentRegistry.cleanUpLineCell(
            signaturen1),
        kulturObjektDokumentRegistrierenException.getMessage());
  }

  @Test
  void testSplitSignaturen() throws KulturObjektDokumentRegistrierenException {
    List<String> signaturenList = KulturObjektDokumentRegistry.splitSignaturen(signaturen);

    assertNotNull(signaturenList);
    assertEquals(6, signaturenList.size());

    assertEquals("Cbm Cat. 1", signaturenList.get(0));
    assertEquals("cbm_cat_1", signaturenList.get(1));

    StringJoiner joiner = new StringJoiner("\"$\"", "\"", "\"\n");
    signaturenList.forEach(s -> joiner.add(s == null ? "" : s));
    assertEquals(joiner.toString(), signaturen);
  }

  @Test
  void testSplitSignaturen_withEmptySortKey() throws Exception {

    final String signaturen2 = """
        "Signatur A"$""$"Alternative Signatur A1"$"Alternative Signatur A2"
        """;

    List<String> result =
        KulturObjektDokumentRegistry.splitSignaturen(signaturen2);

    assertEquals(4, result.size());

    assertEquals("Signatur A", result.get(0));
    assertNull(result.get(1));
    assertEquals("Alternative Signatur A1", result.get(2));
    assertEquals("Alternative Signatur A2", result.get(3));
  }

  @Test
  void testCreate_withNullSortKey() throws Exception {

    final String signaturen2 = """
        "Signatur A"$""$"Alternative Signatur A1"
        """;

    KulturObjektDokument kod = KulturObjektDokumentRegistry.create(
        createOrt(),
        createKoerperschaft(),
        KulturObjektDokumentRegistry.splitSignaturen(signaturen2)
    );

    assertEquals("Signatur A", kod.getGueltigeIdentifikation().getIdent());
    assertNull(kod.getGueltigeIdentifikation().getSortKey());
    assertEquals(1, kod.getAlternativeIdentifikationen().size());
  }

  @Test
  void testCreate_withIdnoRegistrierung() throws KulturObjektDokumentRegistrierenException {
    NormdatenReferenz ort = createOrt();
    NormdatenReferenz koerperschaft = createKoerperschaft();
    IdnoRegistrierung idno = new IdnoRegistrierung("Cbm Cat. 1", "cbm_cat_1",
        List.of("Codices bavarici monacenses Catalogi 1", "Cod. bav. monac. Cat. 1", "Cbm. Cat. 1",
            "Clm 1330"));

    KulturObjektDokument kod = KulturObjektDokumentRegistry.create(ort, koerperschaft, idno);

    Identifikation gueltigeIdentifikation = kod.getGueltigeIdentifikation();
    assertEquals(IdentTyp.GUELTIGE_SIGNATUR, gueltigeIdentifikation.getIdentTyp());
    assertEquals("Cbm Cat. 1", gueltigeIdentifikation.getIdent());
    assertEquals("cbm_cat_1", gueltigeIdentifikation.getSortKey());
    assertEquals(ort, gueltigeIdentifikation.getAufbewahrungsOrt());
    assertEquals(koerperschaft, gueltigeIdentifikation.getBesitzer());

    assertEquals(4, kod.getAlternativeIdentifikationen().size());
    assertEquals(Set.copyOf(idno.altSignaturen()), kod.getAlternativeIdentifikationen().stream()
        .map(Identifikation::getIdent)
        .collect(Collectors.toSet()));
    assertTrue(kod.getAlternativeIdentifikationen().stream()
        .allMatch(alt -> alt.getIdentTyp() == IdentTyp.ALTSIGNATUR && alt.getSortKey() == null));
  }

  @Test
  void testCreate_withIdnoRegistrierungHasSameIdAsSignaturenList()
      throws KulturObjektDokumentRegistrierenException {
    NormdatenReferenz ort = createOrt();
    NormdatenReferenz koerperschaft = createKoerperschaft();

    KulturObjektDokument kodFromIdno = KulturObjektDokumentRegistry.create(ort, koerperschaft,
        new IdnoRegistrierung("Cbm Cat. 1", "cbm_cat_1", List.of("Clm 1330")));
    KulturObjektDokument kodFromSignaturen = KulturObjektDokumentRegistry.create(ort, koerperschaft,
        KulturObjektDokumentRegistry.splitSignaturen(signaturen));

    assertEquals(kodFromSignaturen.getId(), kodFromIdno.getId());
  }

  @Test
  void testCreate_withIdnoRegistrierungWithoutSortKeyAndAltSignaturen()
      throws KulturObjektDokumentRegistrierenException {

    KulturObjektDokument kod = KulturObjektDokumentRegistry.create(createOrt(),
        createKoerperschaft(),
        new IdnoRegistrierung("Signatur A", null, List.of()));

    assertEquals("Signatur A", kod.getGueltigeIdentifikation().getIdent());
    assertNull(kod.getGueltigeIdentifikation().getSortKey());
    assertTrue(kod.getAlternativeIdentifikationen().isEmpty());
  }

  @Test
  void testCreate_withoutIdnoRegistrierung() {
    KulturObjektDokumentRegistrierenException exception = assertThrows(
        KulturObjektDokumentRegistrierenException.class,
        () -> KulturObjektDokumentRegistry.create(createOrt(), createKoerperschaft(),
            (IdnoRegistrierung) null));

    assertEquals("Keine Signaturen vorhanden!", exception.getMessage());
  }

  @Test
  void testCreate_withIdnoRegistrierungWithEmptyGueltigeSignatur() {
    IdnoRegistrierung idno = new IdnoRegistrierung("", "sort_key",
        List.of("Alternative Signatur A1"));

    KulturObjektDokumentRegistrierenException exception = assertThrows(
        KulturObjektDokumentRegistrierenException.class,
        () -> KulturObjektDokumentRegistry.create(createOrt(), createKoerperschaft(), idno));

    assertEquals("Wrong CSV Signatur file. -> GültigeSignatur is leer", exception.getMessage());
  }

  private NormdatenReferenz createKoerperschaft() {
    return new NormdatenReferenz(TEIValues.UUID_PREFIX + UUID.randomUUID(),
        "Staatsbibliothek zu Berlin", "234234-x");
  }

  private NormdatenReferenz createOrt() {
    return new NormdatenReferenz(TEIValues.UUID_PREFIX + UUID.randomUUID(), "Berlin", "4005728-8");
  }
}
