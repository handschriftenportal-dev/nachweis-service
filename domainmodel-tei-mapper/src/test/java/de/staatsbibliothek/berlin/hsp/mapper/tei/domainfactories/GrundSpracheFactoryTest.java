package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactory.findChangeID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactory.findChangeWithID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactoryTest.createTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.time.LocalDateTime;
import java.util.Optional;
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

class GrundSpracheFactoryTest {

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";

  @Test
  void testBuild() {
    TEI tei = new TEI();

    GrundspracheFactory grundspracheFactory = new GrundspracheFactory();

    MsDesc msDesc = new MsDesc();
    assertNull(grundspracheFactory.build(msDesc, tei));

    msDesc = createMsDesc();
    Grundsprache grundsprache = grundspracheFactory.build(msDesc, tei);

    assertNotNull(grundsprache);
    assertEquals("HSP-c78baa8d-9f4a-3aa7-a8f9-b429eea63244",
        grundsprache.getId());
    assertSame(AttributsTyp.GRUNDSPRACHE, grundsprache.getAttributsTyp());
    assertEquals("HSP-123", grundsprache.getReferenzId());
    assertSame(AttributsReferenzTyp.BESCHREIBUNG,
        grundsprache.getReferenzTyp());
    assertEquals("Alt- und Mittelhochdeutsch", grundsprache.getText());
    assertNotNull(grundsprache.getNormdatenReferenzen());
    assertEquals(2, grundsprache.getNormdatenReferenzen().size());
  }

  private MsDesc createMsDesc() {
    MsDesc msDesc = new MsDesc();
    msDesc.setId("HSP-123");

    Head head = new Head();
    msDesc.getHeads().add(head);

    Index index = new Index();
    index.setIndexName(AttributsTyp.GRUNDSPRACHE.getIndexName());
    index.getSources().add(HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index);

    Term textLang = createTerm(TEIValues.GRUNDSPRACHE_TERM_TEXT,
        "Alt- und Mittelhochdeutsch");
    index.getTermsAndIndices().add(textLang);

    index.getTermsAndIndices()
        .add(createTerm(TEIValues.GRUNDSPRACHE_TERM_NORM,
            "4001523-3"));
    index.getTermsAndIndices()
        .add(createTerm(TEIValues.GRUNDSPRACHE_TERM_NORM,
            "4039687-3"));

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

    GrundspracheFactory grundspracheFactory = new GrundspracheFactory();

    MsDesc msDesc = new MsDesc();

    Head head = new Head();

    Index index = new Index();
    index.setIndexName(AttributsTyp.GRUNDSPRACHE.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Grundsprache grundsprache = grundspracheFactory.build(msDesc, tei);

    assertNotNull(grundsprache.getModificationDatum(),
        "Modification Datum darf nicht null sein!");
    assertEquals(LocalDateTime.of(2025, 2, 19, 0, 0),
        grundsprache.getModificationDatum());
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

    GrundspracheFactory grundspracheFactory = new GrundspracheFactory();

    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    Index index = new Index();

    index.setIndexName(AttributsTyp.GRUNDSPRACHE.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Grundsprache grundsprache = grundspracheFactory.build(msDesc, tei);

    assertNotNull(grundsprache.getBearbeiter(),
        "Bearbeiter darf nicht null sein!");
    assertEquals("Mammadov", grundsprache.getBearbeiter().getName(),
        "Der Name des Bearbeiters sollte korrekt übernommen werden!");
    assertEquals("{P00853}", grundsprache.getBearbeiter().getId(),
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

    GrundspracheFactory grundspracheFactory = new GrundspracheFactory();
    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    Index index = new Index();

    index.setIndexName(AttributsTyp.GRUNDSPRACHE.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Grundsprache grundsprache = grundspracheFactory.build(msDesc, tei);

    assertNotNull(grundsprache.getAttributsReferenzStatusTyp(),
        "AttributsReferenzStatusTyp darf nicht null sein!");

    AttributsReferenzStatusTyp expectedStatus = AttributsReferenzStatusTyp.MANUELL;

    assertEquals(expectedStatus, grundsprache.getAttributsReferenzStatusTyp(),
        "Der AttributsReferenzStatusTyp sollte MANUELL sein, wenn ein Bearbeiter vorhanden ist!");

    Optional<Change> updatedChange = findChangeID(index).flatMap(
        i -> findChangeWithID(tei, i));

    assertTrue(updatedChange.isPresent(), "Change sollte vorhanden sein!");
    assertEquals(expectedStatus.name(), updatedChange.get().getN(),
        "Das AttributsReferenzStatusTyp sollte korrekt in Change.N gesetzt sein!");
  }
}
