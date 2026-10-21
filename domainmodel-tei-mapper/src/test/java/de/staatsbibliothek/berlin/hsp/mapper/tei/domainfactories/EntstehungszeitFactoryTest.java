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

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.EntstehungszeitWert;
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

class EntstehungszeitFactoryTest {

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";

  @Test
  void testBuild() {
    TEI tei = new TEI();

    EntstehungszeitFactory entstehungszeitFactory = new EntstehungszeitFactory();

    MsDesc msDesc = new MsDesc();
    assertNull(entstehungszeitFactory.build(msDesc, tei));

    msDesc = createMsDesc();
    Entstehungszeit entstehungszeit = entstehungszeitFactory.build(msDesc, tei);

    assertNotNull(entstehungszeit);
    assertEquals("HSP-15ade495-213e-393c-a3c1-f82bd70c2de2", entstehungszeit.getId());
    assertSame(AttributsTyp.ENTSTEHUNGSZEIT, entstehungszeit.getAttributsTyp());
    assertEquals("HSP-123", entstehungszeit.getReferenzId());
    assertSame(AttributsReferenzTyp.BESCHREIBUNG, entstehungszeit.getReferenzTyp());
    assertEquals("um 1210 (Fasz. I); um 1240 (Fasz. II)", entstehungszeit.getText());

    assertNotNull(entstehungszeit.getEntstehungszeitWerte());
    assertEquals(2, entstehungszeit.getEntstehungszeitWerte().size());
    assertTrue(checkEntstehungszeitWert(entstehungszeit.getEntstehungszeitWerte(),
        "um 1210 (Fasz. I)", "1200", "1220", "datable"));
    assertTrue(checkEntstehungszeitWert(entstehungszeit.getEntstehungszeitWerte(),
        "um 1240 (Fasz. II)", "1230", "1250", "dated"));
    assertEquals(HTTP_RESOLVER_SPK_DE_PURL, entstehungszeit.getReferenzURL());
  }

  private boolean checkEntstehungszeitWert(List<EntstehungszeitWert> werte,
      String text, String nichtVor, String nichtNach, String typ) {
    return werte.stream().anyMatch(wert ->
        text.equals(wert.getText())
            && nichtVor.equals(wert.getNichtVor())
            && nichtNach.equals(wert.getNichtNach())
            && typ.equals(wert.getTyp()));
  }

  private MsDesc createMsDesc() {
    MsDesc msDesc = new MsDesc();
    msDesc.setId("HSP-123");

    Head head = new Head();
    msDesc.getHeads().add(head);

    Index index_1 = createIndex(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName(), "HSP-123",
        HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index_1);

    addTermToIndex(index_1, TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TEXT, "um 1210 (Fasz. I)");
    addTermToIndex(index_1, TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_NOT_BEFORE, "1200");
    addTermToIndex(index_1, TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_NOT_AFTER, "1220");
    addTermToIndex(index_1, TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TYPE, "datable");

    Index index_2 = createIndex(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName(), "HSP-123",
        HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index_2);

    addTermToIndex(index_2, TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TEXT, "um 1240 (Fasz. II)");
    addTermToIndex(index_2, TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_NOT_BEFORE, "1230");
    addTermToIndex(index_2, TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_NOT_AFTER, "1250");
    addTermToIndex(index_2, TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TYPE, "dated");

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

    EntstehungszeitFactory entstehungszeitFactory = new EntstehungszeitFactory();

    MsDesc msDesc = new MsDesc();

    Head head = new Head();

    Index index = new Index();
    index.setIndexName(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Entstehungszeit entstehungszeit = entstehungszeitFactory.build(msDesc, tei);

    assertNotNull(entstehungszeit.getModificationDatum(),
        "Modification Datum darf nicht null sein!");
    assertEquals(LocalDateTime.of(2025, 2, 19, 0, 0), entstehungszeit.getModificationDatum());
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

    EntstehungszeitFactory entstehungszeitFactory = new EntstehungszeitFactory();

    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    Index index = new Index();

    index.setIndexName(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Entstehungszeit entstehungszeit = entstehungszeitFactory.build(msDesc, tei);

    assertNotNull(entstehungszeit.getBearbeiter(), "Bearbeiter darf nicht null sein!");
    assertEquals("Mammadov", entstehungszeit.getBearbeiter().getName(),
        "Der Name des Bearbeiters sollte korrekt übernommen werden!");
    assertEquals("{P00853}", entstehungszeit.getBearbeiter().getId(),
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

    EntstehungszeitFactory entstehungszeitFactory = new EntstehungszeitFactory();
    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    Index index = new Index();

    index.setIndexName(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Entstehungszeit entstehungszeit = entstehungszeitFactory.build(msDesc, tei);

    assertNotNull(entstehungszeit.getAttributsReferenzStatusTyp(),
        "AttributsReferenzStatusTyp darf nicht null sein!");

    AttributsReferenzStatusTyp expectedStatus = AttributsReferenzStatusTyp.MANUELL;

    assertEquals(expectedStatus, entstehungszeit.getAttributsReferenzStatusTyp(),
        "Der AttributsReferenzStatusTyp sollte MANUELL sein, wenn ein Bearbeiter vorhanden ist!");

    Optional<Change> updatedChange = findChangeID(index).flatMap(i -> findChangeWithID(tei, i));

    assertTrue(updatedChange.isPresent(), "Change sollte vorhanden sein!");
    assertEquals(expectedStatus.name(), updatedChange.get().getN(),
        "Das AttributsReferenzStatusTyp sollte korrekt in Change.N gesetzt sein!");
  }
}
