package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactory.findChangeID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactory.findChangeWithID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactoryTest.createTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
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

class BeschreibstoffFactoryTest {

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";

  @Test
  void testBuild() {
    TEI tei = new TEI();

    BeschreibstoffFactory beschreibstoffFactory = new BeschreibstoffFactory();

    MsDesc msDesc = new MsDesc();
    assertNull(beschreibstoffFactory.build(msDesc, tei));

    msDesc = createMsDesc();
    Beschreibstoff beschreibstoff = beschreibstoffFactory.build(msDesc, tei);

    assertNotNull(beschreibstoff);
    assertEquals("HSP-7a9ad7b9-a6b0-3479-a6ae-b5e67f482eb8", beschreibstoff.getId());
    assertSame(AttributsTyp.BESCHREIBSTOFF, beschreibstoff.getAttributsTyp());
    assertEquals("HSP-123", beschreibstoff.getReferenzId());
    assertSame(AttributsReferenzTyp.BESCHREIBUNG, beschreibstoff.getReferenzTyp());
    assertEquals("Pergament, Papyrus", beschreibstoff.getText());
    assertEquals(HTTP_RESOLVER_SPK_DE_PURL, beschreibstoff.getReferenzURL());
  }

  private MsDesc createMsDesc() {
    MsDesc msDesc = new MsDesc();
    msDesc.setId("HSP-123");

    Head head = new Head();
    msDesc.getHeads().add(head);

    Index index = new Index();
    index.setIndexName(AttributsTyp.BESCHREIBSTOFF.getIndexName());
    index.setCopyOf("HSP-123");
    index.getSources().add(HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index);

    index.getTermsAndIndices()
        .add(createTerm(TEIValues.BESCHREIBSTOFF_TERM_TEXT, "Pergament, Papyrus"));
    index.getTermsAndIndices().add(createTerm(TEIValues.BESCHREIBSTOFF_TERM_NORM, "parchment"));
    index.getTermsAndIndices().add(createTerm(TEIValues.BESCHREIBSTOFF_TERM_NORM, "papyrus"));

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

    BeschreibstoffFactory beschreibstoffFactory = new BeschreibstoffFactory();

    MsDesc msDesc = new MsDesc();

    Head head = new Head();

    Index index = new Index();
    index.setIndexName(AttributsTyp.BESCHREIBSTOFF.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Beschreibstoff beschreibstoff = beschreibstoffFactory.build(msDesc, tei);

    assertNotNull(beschreibstoff.getModificationDatum(),
        "Modification Datum darf nicht null sein!");
    assertEquals(LocalDateTime.of(2025, 2, 19, 0, 0), beschreibstoff.getModificationDatum());
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

    BeschreibstoffFactory beschreibstoffFactory = new BeschreibstoffFactory();

    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    Index index = new Index();

    index.setIndexName(AttributsTyp.BESCHREIBSTOFF.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Beschreibstoff beschreibstoff = beschreibstoffFactory.build(msDesc, tei);

    assertNotNull(beschreibstoff.getBearbeiter(), "Bearbeiter darf nicht null sein!");
    assertEquals("Mammadov", beschreibstoff.getBearbeiter().getName(),
        "Der Name des Bearbeiters sollte korrekt übernommen werden!");
    assertEquals("{P00853}", beschreibstoff.getBearbeiter().getId(),
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

    BeschreibstoffFactory beschreibstoffFactory = new BeschreibstoffFactory();
    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    Index index = new Index();

    index.setIndexName(AttributsTyp.BESCHREIBSTOFF.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Beschreibstoff beschreibstoff = beschreibstoffFactory.build(msDesc, tei);

    assertNotNull(beschreibstoff.getAttributsReferenzStatusTyp(),
        "AttributsReferenzStatusTyp darf nicht null sein!");

    AttributsReferenzStatusTyp expectedStatus = AttributsReferenzStatusTyp.MANUELL;

    assertEquals(expectedStatus, beschreibstoff.getAttributsReferenzStatusTyp(),
        "Der AttributsReferenzStatusTyp sollte MANUELL sein, wenn ein Bearbeiter vorhanden ist!");

    Optional<Change> updatedChange = findChangeID(index).flatMap(i -> findChangeWithID(tei, i));

    assertTrue(updatedChange.isPresent(), "Change sollte vorhanden sein!");
    assertEquals(expectedStatus.name(), updatedChange.get().getN(),
        "Das AttributsReferenzStatusTyp sollte korrekt in Change.N gesetzt sein!");
  }

  @Test
  public void testIndexTermMapping() throws Exception {
    File teiFile = Paths.get("src", "test", "resources", "tei", "beschreibung_bautzen_2022.xml")
        .toFile();

    var tei = TEIObjectFactory.unmarshal(
        new ByteArrayInputStream(new FileInputStream(teiFile).readAllBytes())).getFirst();
    var msDesc = TEICommon.findFirst(MsDesc.class, tei).orElseGet(Assertions::fail);

    var beschreibstoff = new BeschreibstoffFactory().build(msDesc, tei);

    assertNotNull(beschreibstoff);
    assertEquals("Pergament (Freitext)", beschreibstoff.getText());

    var normdatenReferenzen = beschreibstoff.getNormdatenReferenzen();
    assertEquals(2, normdatenReferenzen.size());

    var normdatenReferenz = normdatenReferenzen.stream()
        .filter(
            n -> n.getId().equals("NORM-e7b1c101-919e-3fdb-983b-762c305fdcc1"))
        .findFirst()
        .orElseGet(Assertions::fail);
    assertEquals("NORM-e7b1c101-919e-3fdb-983b-762c305fdcc1", normdatenReferenz.getId());
    assertEquals("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/CODC-A800",
        normdatenReferenz.getUri());
    assertEquals("Pergament", normdatenReferenz.getName());

    var normdatenReferenz2 = normdatenReferenzen.stream()
        .filter(
            n -> n.getId().equals("NORM-e7b1c101-919e-3fdb-983b-762c305fddd2"))
        .findFirst()
        .orElseGet(Assertions::fail);
    assertEquals("NORM-e7b1c101-919e-3fdb-983b-762c305fddd2", normdatenReferenz2.getId());
    assertEquals("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/CODC-A801",
        normdatenReferenz2.getUri());
    assertEquals("Pergament2", normdatenReferenz2.getName());
  }
}
