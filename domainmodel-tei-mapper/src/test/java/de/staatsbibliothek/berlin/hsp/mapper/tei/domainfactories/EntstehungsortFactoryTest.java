package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactory.findChangeID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactory.findChangeWithID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactoryTest.createTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
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
import org.tei_c.ns._1.Term;

class EntstehungsortFactoryTest {

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";

  @Test
  void testBuild() {
    TEI tei = new TEI();

    EntstehungsortFactory entstehungsortFactory = new EntstehungsortFactory();

    MsDesc msDesc = new MsDesc();
    assertNull(entstehungsortFactory.build(msDesc, tei));

    msDesc = createMsDesc();
    Entstehungsort entstehungsort = entstehungsortFactory.build(msDesc, tei);

    assertNotNull(entstehungsort);
    assertEquals("HSP-f12c2125-44b7-3050-b37e-4e71da8ab10f", entstehungsort.getId());
    assertSame(AttributsTyp.ENTSTEHUNGSORT, entstehungsort.getAttributsTyp());
    assertEquals("HSP-123", entstehungsort.getReferenzId());
    assertSame(AttributsReferenzTyp.BESCHREIBUNG, entstehungsort.getReferenzTyp());
    assertEquals("Berlin (Fasz. 1), Potsdam (Fasz. 2)", entstehungsort.getText());
    assertNotNull(entstehungsort.getNormdatenReferenzen());
    assertEquals(2, entstehungsort.getNormdatenReferenzen().size());

    assertTrue(checkNormdatenReferenz(entstehungsort.getNormdatenReferenzen(),
        "NORM-123", "4005728-8", "Berlin"));

    assertTrue(checkNormdatenReferenz(entstehungsort.getNormdatenReferenzen(),
        "NORM-456", "4046948-7", "Potsdam"));
    assertEquals(HTTP_RESOLVER_SPK_DE_PURL, entstehungsort.getReferenzURL());
  }

  @Test
  void testMapToNormdatenReferenz() {
    Term term = new Term();
    term.setKey("NORM-123");
    term.getReves().add("https://d-nb.info/gnd/4046948-7");
    term.getContent().add("Potsdam");

    EntstehungsortFactory entstehungsortFactory = new EntstehungsortFactory();
    NormdatenReferenz normdatenReferenz =
        entstehungsortFactory.mapTermToNormdatenReferenz(term, NormdatenReferenz.ORT_TYPE_NAME);

    assertNotNull(normdatenReferenz);
    assertEquals("NORM-123", normdatenReferenz.getId());
    assertEquals("4046948-7", normdatenReferenz.getGndID());
    assertEquals("Potsdam", normdatenReferenz.getName());
  }

  private boolean checkNormdatenReferenz(List<NormdatenReferenz> normdatenReferenzen,
      String id, String gndId, String name) {
    return normdatenReferenzen.stream()
        .anyMatch(nr -> id.equals(nr.getId())
            && gndId.equals(nr.getGndID())
            && name.equals(nr.getName())
            && NormdatenReferenz.ORT_TYPE_NAME.equals(nr.getTypeName()));
  }

  private MsDesc createMsDesc() {
    MsDesc msDesc = new MsDesc();
    msDesc.setId("HSP-123");

    Head head = new Head();
    msDesc.getHeads().add(head);

    Index index = new Index();
    index.setIndexName(AttributsTyp.ENTSTEHUNGSORT.getIndexName());
    index.getSources().add(HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index);

    Term origPlace = createTerm(TEIValues.ENTSTEHUNGSORT_TERM_TYP_TEXT,
        "Berlin (Fasz. 1), Potsdam (Fasz. 2)");
    index.getTermsAndIndices().add(origPlace);

    Term normBerlin = createTerm(TEIValues.ENTSTEHUNGSORT_TERM_TYP_NORM, "Berlin");
    normBerlin.setKey("NORM-123");
    normBerlin.getReves().add("https://d-nb.info/gnd/4005728-8");
    index.getTermsAndIndices().add(normBerlin);

    Term normPotsdam = createTerm(TEIValues.ENTSTEHUNGSORT_TERM_TYP_NORM, "Potsdam");
    normPotsdam.setKey("NORM-456");
    normPotsdam.getReves().add("https://d-nb.info/gnd/4046948-7");
    index.getTermsAndIndices().add(normPotsdam);

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

    EntstehungsortFactory entstehungsortFactory = new EntstehungsortFactory();

    MsDesc msDesc = new MsDesc();

    Head head = new Head();

    Index index = new Index();
    index.setIndexName(AttributsTyp.ENTSTEHUNGSORT.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Entstehungsort entstehungsort = entstehungsortFactory.build(msDesc, tei);

    assertNotNull(entstehungsort.getModificationDatum(),
        "Modification Datum darf nicht null sein!");
    assertEquals(LocalDateTime.of(2025, 2, 19, 0, 0), entstehungsort.getModificationDatum());
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

    EntstehungsortFactory entstehungsortFactory = new EntstehungsortFactory();

    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    Index index = new Index();

    index.setIndexName(AttributsTyp.ENTSTEHUNGSORT.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Entstehungsort entstehungsort = entstehungsortFactory.build(msDesc, tei);

    assertNotNull(entstehungsort.getBearbeiter(), "Bearbeiter darf nicht null sein!");
    assertEquals("Mammadov", entstehungsort.getBearbeiter().getName(),
        "Der Name des Bearbeiters sollte korrekt übernommen werden!");
    assertEquals("{P00853}", entstehungsort.getBearbeiter().getId(),
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

    EntstehungsortFactory entstehungsortFactory = new EntstehungsortFactory();
    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    Index index = new Index();

    index.setIndexName(AttributsTyp.ENTSTEHUNGSORT.getIndexName());
    index.getChanges().add("#change_01");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Entstehungsort entstehungsort = entstehungsortFactory.build(msDesc, tei);

    assertNotNull(entstehungsort.getAttributsReferenzStatusTyp(),
        "AttributsReferenzStatusTyp darf nicht null sein!");

    AttributsReferenzStatusTyp expectedStatus = AttributsReferenzStatusTyp.MANUELL;

    assertEquals(expectedStatus, entstehungsort.getAttributsReferenzStatusTyp(),
        "Der AttributsReferenzStatusTyp sollte MANUELL sein, wenn ein Bearbeiter vorhanden ist!");

    Optional<Change> updatedChange = findChangeID(index).flatMap(i -> findChangeWithID(tei, i));

    assertTrue(updatedChange.isPresent(), "Change sollte vorhanden sein!");
    assertEquals(expectedStatus.name(), updatedChange.get().getN(),
        "Das AttributsReferenzStatusTyp sollte korrekt in Change.N gesetzt sein!");
  }
}
