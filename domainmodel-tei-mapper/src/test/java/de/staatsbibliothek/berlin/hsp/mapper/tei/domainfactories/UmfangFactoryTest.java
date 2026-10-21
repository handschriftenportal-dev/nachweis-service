package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactory.findChangeID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactory.findChangeWithID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactoryTest.createTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
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

class UmfangFactoryTest {

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";

  @Test
  void testBuild() {
    TEI tei = new TEI();

    UmfangFactory umfangFactory = new UmfangFactory();

    MsDesc msDesc = new MsDesc();
    assertNull(umfangFactory.build(msDesc, tei));

    msDesc = createMsDesc();
    Umfang umfang = umfangFactory.build(msDesc, tei);

    assertNotNull(umfang);
    assertEquals("HSP-3214d290-638c-3519-ba97-d99fbe8787e3", umfang.getId());
    assertSame(AttributsTyp.UMFANG, umfang.getAttributsTyp());
    assertEquals("HSP-123", umfang.getReferenzId());
    assertSame(AttributsReferenzTyp.BESCHREIBUNG, umfang.getReferenzTyp());
    assertEquals("I + 394 Bl.", umfang.getText());
    assertEquals("394", umfang.getBlattzahl());
    assertEquals(HTTP_RESOLVER_SPK_DE_PURL, umfang.getReferenzURL());
  }

  private MsDesc createMsDesc() {
    MsDesc msDesc = new MsDesc();
    msDesc.setId("HSP-123");

    Head head = new Head();
    msDesc.getHeads().add(head);

    Index index = new Index();
    index.setIndexName(AttributsTyp.UMFANG.getIndexName());
    index.getSources().add(HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index);

    index.getTermsAndIndices().add(createTerm(TEIValues.UMFANG_TERM_TYP_TEXT, "I + 394 Bl."));
    index.getTermsAndIndices().add(createTerm(TEIValues.UMFANG_TERM_TYP_NO_OF_LEAVES, "394"));

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

    UmfangFactory umfangFactory = new UmfangFactory();

    MsDesc msDesc = new MsDesc();

    Head head = new Head();

    Index index = new Index();
    index.setIndexName(AttributsTyp.UMFANG.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Umfang umfang = umfangFactory.build(msDesc, tei);

    assertNotNull(umfang.getModificationDatum(), "Modification Datum darf nicht null sein!");
    assertEquals(LocalDateTime.of(2025, 2, 19, 0, 0), umfang.getModificationDatum());
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

    UmfangFactory umfangFactory = new UmfangFactory();

    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    Index index = new Index();

    index.setIndexName(AttributsTyp.UMFANG.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Umfang umfang = umfangFactory.build(msDesc, tei);

    assertNotNull(umfang.getBearbeiter(), "Bearbeiter darf nicht null sein!");
    assertEquals("Mammadov", umfang.getBearbeiter().getName(),
        "Der Name des Bearbeiters sollte korrekt übernommen werden!");
    assertEquals("{P00853}", umfang.getBearbeiter().getId(),
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

    UmfangFactory umfangFactory = new UmfangFactory();
    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    Index index = new Index();

    index.setIndexName(AttributsTyp.UMFANG.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Umfang umfang = umfangFactory.build(msDesc, tei);

    assertNotNull(umfang.getAttributsReferenzStatusTyp(),
        "AttributsReferenzStatusTyp darf nicht null sein!");

    AttributsReferenzStatusTyp expectedStatus = AttributsReferenzStatusTyp.MANUELL;

    assertEquals(expectedStatus, umfang.getAttributsReferenzStatusTyp(),
        "Der AttributsReferenzStatusTyp sollte MANUELL sein, wenn ein Bearbeiter vorhanden ist!");

    Optional<Change> updatedChange = findChangeID(index).flatMap(i -> findChangeWithID(tei, i));

    assertTrue(updatedChange.isPresent(), "Change sollte vorhanden sein!");
    assertEquals(expectedStatus.name(), updatedChange.get().getN(),
        "Das AttributsReferenzStatusTyp sollte korrekt in Change.N gesetzt sein!");
  }

}
