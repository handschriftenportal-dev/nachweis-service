package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactory.findChangeID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactory.findChangeWithID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactoryTest.addTermToIndex;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactoryTest.createIndex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AbmessungWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.time.LocalDateTime;
import java.util.List;
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

class AbmessungFactoryTest {

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";

  @Test
  void testBuild() {
    TEI tei = new TEI();

    AbmessungFactory abmessungFactory = new AbmessungFactory();

    MsDesc msDesc = new MsDesc();
    assertNull(abmessungFactory.build(msDesc, tei));

    msDesc = createMsDesc();
    Abmessung abmessung = abmessungFactory.build(msDesc, tei);

    assertNotNull(abmessung);
    assertEquals("HSP-a492b67e-78f7-3121-803a-ebadc1357ecf", abmessung.getId());
    assertSame(AttributsTyp.ABMESSUNG, abmessung.getAttributsTyp());
    assertEquals("HSP-123", abmessung.getReferenzId());
    assertSame(AttributsReferenzTyp.BESCHREIBUNG, abmessung.getReferenzTyp());
    assertEquals("23,5 × 16,5 x 2,5; 25 × 15 x 5", abmessung.getText());

    assertNotNull(abmessung.getAbmessungWerte());
    assertEquals(2, abmessung.getAbmessungWerte().size());
    assertTrue(checkAbmessungWert(abmessung.getAbmessungWerte(),
        "23,5 × 16,5 x 2,5", "23,5", "16,5", "2,5", "factual"));
    assertTrue(checkAbmessungWert(abmessung.getAbmessungWerte(),
        "25 × 15 x 5", "25", "15", "5", "deduced"));

    assertEquals(HTTP_RESOLVER_SPK_DE_PURL, abmessung.getReferenzURL());
  }

  private boolean checkAbmessungWert(List<AbmessungWert> werte,
      String text, String hoehe, String breite, String tiefe, String typ) {
    return werte.stream().anyMatch(wert ->
        text.equals(wert.getText())
            && hoehe.equals(wert.getHoehe())
            && breite.equals(wert.getBreite())
            && tiefe.equals(wert.getTiefe())
            && typ.equals(wert.getTyp()));
  }

  private MsDesc createMsDesc() {
    MsDesc msDesc = new MsDesc();
    msDesc.setId("HSP-123");

    Head head = new Head();
    msDesc.getHeads().add(head);

    Index index_1 = createIndex(AttributsTyp.ABMESSUNG.getIndexName(), "HSP-123",
        HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index_1);

    addTermToIndex(index_1, TEIValues.ABMESSUNG_TERM_TYP_TEXT, "23,5 × 16,5 x 2,5");
    addTermToIndex(index_1, TEIValues.ABMESSUNG_TERM_TYP_HEIGHT, "23,5");
    addTermToIndex(index_1, TEIValues.ABMESSUNG_TERM_TYP_WIDTH, "16,5");
    addTermToIndex(index_1, TEIValues.ABMESSUNG_TERM_TYP_DEPTH, "2,5");
    addTermToIndex(index_1, TEIValues.ABMESSUNG_TERM_TYP_TYPE, "factual");

    Index index_2 = createIndex(AttributsTyp.ABMESSUNG.getIndexName(), "HSP-123",
        HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index_2);

    addTermToIndex(index_2, TEIValues.ABMESSUNG_TERM_TYP_TEXT, "25 × 15 x 5");
    addTermToIndex(index_2, TEIValues.ABMESSUNG_TERM_TYP_HEIGHT, "25");
    addTermToIndex(index_2, TEIValues.ABMESSUNG_TERM_TYP_WIDTH, "15");
    addTermToIndex(index_2, TEIValues.ABMESSUNG_TERM_TYP_DEPTH, "5");
    addTermToIndex(index_2, TEIValues.ABMESSUNG_TERM_TYP_TYPE, "deduced");

    return msDesc;
  }

  @Test
  void testBuildWithModificationDatum() {
    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    RevisionDesc revisionDesc = new RevisionDesc();

    Change change = new Change();
    final String changeID = "change_01";

    change.setId(changeID);
    change.setWhen("2025-02-19T10:00:00");
    revisionDesc.getRevisionChangeAttribute().add(change);
    header.setRevisionDesc(revisionDesc);
    tei.setTeiHeader(header);

    AbmessungFactory abmessungFactory = new AbmessungFactory();

    MsDesc msDesc = new MsDesc();

    Head head = new Head();

    Index index = new Index();
    index.setIndexName(AttributsTyp.ABMESSUNG.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Abmessung abmessung = abmessungFactory.build(msDesc, tei);

    assertNotNull(abmessung.getModificationDatum(), "Modification Datum darf nicht null sein!");
    assertEquals(LocalDateTime.of(2025, 2, 19, 10, 0,0), abmessung.getModificationDatum());
    assertEquals(changeID,abmessung.getChangeID());
  }

  @Test
  void testBuildWithBearbeiter() {

    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    RevisionDesc revisionDesc = new RevisionDesc();

    Change change = new Change();
    change.setId("change_01");
    change.setWhen("2025-02-19T10:00:00");

    PersName persName = new PersName();
    persName.getContent().add("Mammadov");
    persName.setKey("{P00853}");

    change.getContent().add(persName);
    revisionDesc.getRevisionChangeAttribute().add(change);
    header.setRevisionDesc(revisionDesc);
    tei.setTeiHeader(header);

    AbmessungFactory abmessungFactory = new AbmessungFactory();

    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    Index index = new Index();

    index.setIndexName(AttributsTyp.ABMESSUNG.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Abmessung abmessung = abmessungFactory.build(msDesc, tei);

    assertNotNull(abmessung.getBearbeiter(), "Bearbeiter darf nicht null sein!");
    assertEquals("Mammadov", abmessung.getBearbeiter().getName(),
        "Der Name des Bearbeiters sollte korrekt übernommen werden!");
    assertEquals("{P00853}", abmessung.getBearbeiter().getId(),
        "Die ID des Bearbeiters sollte mit dem ref-Attribut übereinstimmen!");
  }

  @Test
  void testBuildWithAttributsReferenzStatusTyp() {

    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    RevisionDesc revisionDesc = new RevisionDesc();

    Change change = new Change();
    change.setId("change_01");
    change.setWhen("2025-02-19T10:00:00");
    change.setN("MANUELL");

    PersName persName = new PersName();
    persName.setFull("Mammadov");
    persName.setId("{P00853}");

    change.getContent().add(persName);
    revisionDesc.getRevisionChangeAttribute().add(change);
    header.setRevisionDesc(revisionDesc);
    tei.setTeiHeader(header);

    AbmessungFactory abmessungFactory = new AbmessungFactory();
    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    Index index = new Index();

    index.setIndexName(AttributsTyp.ABMESSUNG.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Abmessung abmessung = abmessungFactory.build(msDesc, tei);

    assertNotNull(abmessung.getAttributsReferenzStatusTyp(),
        "AttributsReferenzStatusTyp darf nicht null sein!");

    AttributsReferenzStatusTyp expectedStatus = AttributsReferenzStatusTyp.MANUELL;

    assertEquals(expectedStatus, abmessung.getAttributsReferenzStatusTyp(),
        "Der AttributsReferenzStatusTyp sollte MANUELL sein, wenn ein Bearbeiter vorhanden ist!");

    Optional<Change> updatedChange = findChangeID(index).flatMap(i -> findChangeWithID(tei, i));

    assertTrue(updatedChange.isPresent(), "Change sollte vorhanden sein!");
    assertEquals(expectedStatus.name(), updatedChange.get().getN(),
        "Das AttributsReferenzStatusTyp sollte korrekt in Change.N gesetzt sein!");
  }
}
