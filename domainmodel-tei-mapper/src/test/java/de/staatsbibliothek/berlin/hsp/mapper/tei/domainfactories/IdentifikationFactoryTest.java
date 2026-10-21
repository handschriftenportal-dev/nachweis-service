package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon.findAll;
import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.MsIdentifierFactory;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Idno;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.MsIdentifier;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.05.2020.
 * @version 1.0
 */
class IdentifikationFactoryTest {

  static TEI tei;
  static MsDesc msDesc;
  IdentifikationFactory factory = new IdentifikationFactory();

  @BeforeAll
  static void beforeAll() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-msDesc_Westphal.xml");
    List<TEI> teiList = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    tei = teiList.getFirst();

    List<MsDesc> msDescList = new ArrayList<>();
    findAll(MsDesc.class, tei, msDescList);

    msDesc = msDescList.getFirst();
  }

  @Test
  void testgetAufbewahrungsOrt() {

    NormdatenReferenz aufbewahrendeInstitution = factory.findBesitzer(msDesc.getMsIdentifier());
    NormdatenReferenz aufbewahrungsOrt = factory.findAufbewahrungsOrt(msDesc.getMsIdentifier());

    assertNotNull(aufbewahrendeInstitution);

    assertEquals("HSP-1234", aufbewahrendeInstitution.getId());
    assertEquals("Herzog August Bibliothek", aufbewahrendeInstitution.getName());

    assertNotNull(aufbewahrungsOrt);

    assertEquals("Wolfenbüttel", aufbewahrungsOrt.getName());
    assertEquals("GND-ID3456", aufbewahrungsOrt.getGndID());
  }

  @Test
  void testgetIdent() {
    String signature = factory.getIdent(msDesc.getMsIdentifier());

    assertNotNull(signature);

    assertEquals("Cod. Guelf. 36.23 Aug. 2°", signature);

    assertEquals("", factory.getIdent(new MsIdentifier()));
  }

  /**
   * Regression test (sibling of {@code TEIQuery.extractSignature}): the {@code ident} field
   * produced here is what gets persisted into the {@code identifikation.ident} column. If
   * whitespace from the source {@code <idno>} leaks through, every subsequent exact-match DB lookup
   * for that signature fails. Verifies that {@code getIdent} strips leading and trailing
   * whitespace.
   */
  @Test
  void testgetIdent_trimsWhitespaceInIdnoContent() {
    Idno idno = new Idno();
    idno.getContent().add(" Becker-Sammlung, II 1 4° 1 ");

    MsIdentifier msIdentifier = new MsIdentifier();
    msIdentifier.getIdnos().add(idno);

    assertEquals("Becker-Sammlung, II 1 4° 1", factory.getIdent(msIdentifier));
  }

  @Test
  void testBuildIdentifikation() throws HSPMapperException {

    Identifikation identifikation = factory.build(msDesc, tei);

    assertNotNull(identifikation);

    assertNotNull(identifikation.getId());

    assertEquals("Cod. Guelf. 36.23 Aug. 2°", identifikation.getIdent());

    assertNotNull(identifikation.getAufbewahrungsOrt());

    assertEquals("Wolfenbüttel", identifikation.getAufbewahrungsOrt().getName());
    assertEquals("GND-ID3456", identifikation.getAufbewahrungsOrt().getGndID());

    assertNotNull(identifikation.getBesitzer());

    assertEquals("Herzog August Bibliothek", identifikation.getBesitzer().getName());
    assertEquals("GND-ID1234", identifikation.getBesitzer().getGndID());

    assertEquals(IdentTyp.GUELTIGE_SIGNATUR, identifikation.getIdentTyp());

    assertNotNull(identifikation.getSammlungIDs());

    assertEquals(1, identifikation.getSammlungIDs().size());

    assertEquals("Augusteer", identifikation.getSammlungIDs().iterator().next());
  }

  @Test
  void testBuildIdentifikation_withSortKey() throws HSPMapperException {

    Idno idno = new Idno();
    idno.getContent().add("MS 123");
    idno.setSortKey("MS_00123");

    MsIdentifier msIdentifier = new MsIdentifier();
    msIdentifier.getIdnos().add(idno);

    MsDesc msDesc = new MsDesc();
    msDesc.setMsIdentifier(msIdentifier);

    Identifikation identifikation = factory.build(msDesc, new TEI());

    assertEquals("MS_00123", identifikation.getSortKey());
  }

  @Test
  void testBuildIdentifikation_sortKeyIsTrimmed() throws HSPMapperException {

    Idno idno = new Idno();
    idno.getContent().add("MS 123");
    idno.setSortKey("   MS_00123  ");

    MsIdentifier msIdentifier = new MsIdentifier();
    msIdentifier.getIdnos().add(idno);

    MsDesc msDesc = new MsDesc();
    msDesc.setMsIdentifier(msIdentifier);

    Identifikation identifikation = factory.build(msDesc, new TEI());

    assertEquals("MS_00123", identifikation.getSortKey());
  }

  @Test
  void testBuildIdentifikation_blankSortKeyBecomesNull() throws HSPMapperException {

    Idno idno = new Idno();
    idno.getContent().add("MS 123");
    idno.setSortKey("  ");

    MsIdentifier msIdentifier = new MsIdentifier();
    msIdentifier.getIdnos().add(idno);

    MsDesc msDesc = new MsDesc();
    msDesc.setMsIdentifier(msIdentifier);

    Identifikation identifikation = factory.build(msDesc, new TEI());

    assertNull(identifikation.getSortKey());
  }

  @Test
  void testBuildIdentifikation_withoutSortKey() throws HSPMapperException {

    Idno idno = new Idno();
    idno.getContent().add("MS 123");

    MsIdentifier msIdentifier = new MsIdentifier();
    msIdentifier.getIdnos().add(idno);

    MsDesc msDesc = new MsDesc();
    msDesc.setMsIdentifier(msIdentifier);

    Identifikation identifikation = factory.build(msDesc, new TEI());

    assertNull(identifikation.getSortKey());
  }

  @Test
  void testRoundTripIdentifikation_withSortKey() throws Exception {

    Identifikation original =
        new Identifikation.IdentifikationBuilder()
            .withIdent("MS 67")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withSortKey("MS_67")
            .build();

    MsIdentifier msIdentifier =
        MsIdentifierFactory.build(original, Set.of());

    MsDesc msDesc = new MsDesc();
    msDesc.setMsIdentifier(msIdentifier);

    TEI tei = new TEI();

    IdentifikationFactory factory = new IdentifikationFactory();
    Identifikation parsed = factory.build(msDesc, tei);

    assertEquals(original.getIdent(), parsed.getIdent());
    assertEquals(original.getSortKey(), parsed.getSortKey());
  }

  @Test
  void testRoundTripIdentifikation_withoutSortKey() throws Exception {

    Identifikation original =
        new Identifikation.IdentifikationBuilder()
            .withIdent("MS 67")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    MsIdentifier msIdentifier =
        MsIdentifierFactory.build(original, Set.of());

    MsDesc msDesc = new MsDesc();
    msDesc.setMsIdentifier(msIdentifier);

    IdentifikationFactory factory = new IdentifikationFactory();
    Identifikation parsed = factory.build(msDesc, new TEI());

    assertNull(parsed.getSortKey());
  }
}
