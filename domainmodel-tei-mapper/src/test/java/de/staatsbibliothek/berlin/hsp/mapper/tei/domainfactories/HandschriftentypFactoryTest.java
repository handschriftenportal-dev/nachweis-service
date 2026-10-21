package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactory.findChangeID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactory.findChangeWithID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Change;
import org.tei_c.ns._1.Head;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.PersName;
import org.tei_c.ns._1.RevisionDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.TeiHeader;
import org.tei_c.ns._1.Term;

class HandschriftentypFactoryTest {

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";

  @Test
  void testBuild() {
    TEI tei = new TEI();

    HandschriftentypFactory handschriftentypFactory = new HandschriftentypFactory();

    MsDesc msDesc = new MsDesc();
    assertNull(handschriftentypFactory.build(msDesc, tei));

    msDesc = createMsDesc();
    Handschriftentyp handschriftentyp = handschriftentypFactory.build(msDesc, tei);

    assertNotNull(handschriftentyp);
    assertEquals("HSP-2fc83d8b-bebb-3852-a293-70520150fd7e", handschriftentyp.getId());
    assertSame(AttributsTyp.HANDSCHRIFTENTYP, handschriftentyp.getAttributsTyp());
    assertEquals("HSP-123", handschriftentyp.getReferenzId());
    assertSame(AttributsReferenzTyp.BESCHREIBUNG, handschriftentyp.getReferenzTyp());
    assertEquals("", handschriftentyp.getText());
    assertEquals(HTTP_RESOLVER_SPK_DE_PURL, handschriftentyp.getReferenzURL());
  }

  private MsDesc createMsDesc() {
    MsDesc msDesc = new MsDesc();
    msDesc.setId("HSP-123");

    Head head = new Head();
    msDesc.getHeads().add(head);

    Index index = new Index();
    index.setIndexName(AttributsTyp.HANDSCHRIFTENTYP.getIndexName());
    index.getSources().add(HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index);

    Term term = new Term();
    term.setType(TEIValues.HANDSCHRIFTENTYP_TERM_NORM);
    index.getTermsAndIndices().add(term);

    return msDesc;
  }

  @Test
  void testBuildWithModificationDatum() {
    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    RevisionDesc revisionDesc = new RevisionDesc();

    Change change = new Change();
    change.setId("change_01");
    change.setWhen("2025-02-19T00:00:00");

    revisionDesc.getRevisionChangeAttribute().add(change);
    header.setRevisionDesc(revisionDesc);
    tei.setTeiHeader(header);

    HandschriftentypFactory handschriftentypFactory = new HandschriftentypFactory();

    MsDesc msDesc = new MsDesc();

    Head head = new Head();

    Index index = new Index();
    index.setIndexName(AttributsTyp.HANDSCHRIFTENTYP.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Handschriftentyp handschriftentyp = handschriftentypFactory.build(msDesc, tei);

    assertNotNull(handschriftentyp.getModificationDatum(),
        "Modification Datum darf nicht null sein!");
    assertEquals(LocalDateTime.of(2025, 2, 19, 0, 0), handschriftentyp.getModificationDatum());
  }

  @Test
  void testBuildWithBearbeiter() {

    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    RevisionDesc revisionDesc = new RevisionDesc();

    Change change = new Change();
    change.setId("change_01");
    change.setWhen("2025-02-19T00:00:00");

    PersName persName = new PersName();
    persName.getContent().add("Mammadov");
    persName.setKey("{P00853}");

    change.getContent().add(persName);
    revisionDesc.getRevisionChangeAttribute().add(change);
    header.setRevisionDesc(revisionDesc);
    tei.setTeiHeader(header);

    HandschriftentypFactory handschriftentypFactory = new HandschriftentypFactory();

    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    Index index = new Index();

    index.setIndexName(AttributsTyp.HANDSCHRIFTENTYP.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Handschriftentyp handschriftentyp = handschriftentypFactory.build(msDesc, tei);

    assertNotNull(handschriftentyp.getBearbeiter(), "Bearbeiter darf nicht null sein!");
    assertEquals("Mammadov", handschriftentyp.getBearbeiter().getName(),
        "Der Name des Bearbeiters sollte korrekt übernommen werden!");
    assertEquals("{P00853}", handschriftentyp.getBearbeiter().getId(),
        "Die ID des Bearbeiters sollte mit dem ref-Attribut übereinstimmen!");
  }

  @Test
  void testBuildWithAttributsReferenzStatusTyp() {

    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    RevisionDesc revisionDesc = new RevisionDesc();

    Change change = new Change();
    change.setId("change_01");
    change.setWhen("2025-02-19T00:00:00");
    change.setN("MANUELL");

    PersName persName = new PersName();
    persName.getContent().add("Mammadov");
    persName.setKey("{P00853}");

    change.getContent().add(persName);
    revisionDesc.getRevisionChangeAttribute().add(change);
    header.setRevisionDesc(revisionDesc);
    tei.setTeiHeader(header);

    HandschriftentypFactory handschriftentypFactory = new HandschriftentypFactory();
    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    Index index = new Index();

    index.setIndexName(AttributsTyp.HANDSCHRIFTENTYP.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Handschriftentyp handschriftentyp = handschriftentypFactory.build(msDesc, tei);

    assertNotNull(handschriftentyp.getAttributsReferenzStatusTyp(),
        "AttributsReferenzStatusTyp darf nicht null sein!");

    AttributsReferenzStatusTyp expectedStatus = AttributsReferenzStatusTyp.MANUELL;

    assertEquals(expectedStatus, handschriftentyp.getAttributsReferenzStatusTyp(),
        "Der AttributsReferenzStatusTyp sollte MANUELL sein, wenn ein Bearbeiter vorhanden ist!");

    Optional<Change> updatedChange = findChangeID(index).flatMap(i -> findChangeWithID(tei, i));

    assertTrue(updatedChange.isPresent(), "Change sollte vorhanden sein!");
    assertEquals(expectedStatus.name(), updatedChange.get().getN(),
        "Das AttributsReferenzStatusTyp sollte korrekt in Change.N gesetzt sein!");
  }

  @Test
  public void testIndexTermMapping_Handschriftentyp() throws Exception {
    File teiFile = Paths.get("src", "test", "resources", "tei", "beschreibung_bautzen_2022.xml")
        .toFile();

    var tei = TEIObjectFactory.unmarshal(
            new ByteArrayInputStream(new FileInputStream(teiFile).readAllBytes()))
        .getFirst();
    var msDesc = TEICommon.findFirst(MsDesc.class, tei).orElseGet(Assertions::fail);

    var h = new HandschriftentypFactory().build(msDesc, tei);

    assertNotNull(h, "Handschriftentyp darf nicht null sein!");
    assertEquals(AttributsTyp.HANDSCHRIFTENTYP, h.getAttributsTyp());
    assertEquals("fragment", h.getText(), "Freitext (erstes <term>) sollte übernommen sein.");

    var refs = h.getNormdatenReferenzen();
    assertNotNull(refs, "NormdatenReferenzen dürfen nicht null sein!");
    assertEquals(3, refs.size(), "Es sollten drei NormdatenReferenzen erkannt werden!");

    var r1 = refs.stream()
        .filter(n -> "NORM-8edc413b-0b70-3366-b78c-fdc091ab3ab2".equals(n.getId()))
        .findFirst().orElseGet(Assertions::fail);
    assertEquals("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/FORM-X709",
        r1.getUri());

    var r2 = refs.stream()
        .filter(n -> "NORM-fcdc9965-677e-3c69-9a70-0c266893e376".equals(n.getId()))
        .findFirst().orElseGet(Assertions::fail);
    assertEquals("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/FORM-A890",
        r2.getUri());

    var r3 = refs.stream()
        .filter(n -> "NORM-5dcac9ee-2a31-3245-bddf-6e1f4c22478e".equals(n.getId()))
        .findFirst().orElseGet(Assertions::fail);
    assertEquals("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/FORM-E350",
        r3.getUri());
  }
}
