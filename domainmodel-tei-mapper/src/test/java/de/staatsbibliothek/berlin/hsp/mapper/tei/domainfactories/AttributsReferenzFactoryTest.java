package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactory.findChangeID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactory.findChangeWithID;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.AttributsReferenzFactory.getCopyOfValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
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

class AttributsReferenzFactoryTest {

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";

  static Term createTerm(String termType, String termContent) {
    Term term = new Term();
    term.setType(termType);
    Optional.ofNullable(termContent).ifPresent(term.getContent()::add);
    return term;
  }

  static Index createIndex(String indexName, String id, String url) {
    Index index = new Index();
    index.setIndexName(indexName);
    index.setCopyOf(TEIValues.TEI_ANKER_TAG + id);
    index.getSources().add(url);
    return index;
  }

  static void addTermToIndex(Index index, String termType, String termContent) {
    Term term = new Term();
    term.setType(termType);
    Optional.ofNullable(termContent).ifPresent(term.getContent()::add);
    index.getTermsAndIndices().add(term);
  }

  @Test
  void testFindIndices() {
    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    msDesc.getHeads().add(head);
    head.getContent()
        .add(createIndex(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName(), "HSP-123",
            HTTP_RESOLVER_SPK_DE_PURL));
    head.getContent()
        .add(createIndex(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName(), "HSP-123",
            HTTP_RESOLVER_SPK_DE_PURL));
    head.getContent()
        .add(createIndex(AttributsTyp.ENTSTEHUNGSORT.getIndexName(), "HSP-123",
            HTTP_RESOLVER_SPK_DE_PURL));

    List<Index> zeiten = AttributsReferenzFactory.findIndices(msDesc,
        AttributsTyp.ENTSTEHUNGSZEIT.getIndexName());
    assertNotNull(zeiten);
    assertEquals(2, zeiten.size());

    List<Index> orte = AttributsReferenzFactory.findIndices(msDesc,
        AttributsTyp.ENTSTEHUNGSORT.getIndexName());
    assertNotNull(orte);
    assertEquals(1, orte.size());

    List<Index> other = AttributsReferenzFactory.findIndices(msDesc, "other");
    assertNotNull(other);
    assertTrue(other.isEmpty());
  }

  @Test
  void testFindIndex() {
    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    msDesc.getHeads().add(head);
    head.getContent()
        .add(createIndex(AttributsTyp.ENTSTEHUNGSORT.getIndexName(), "HSP-123",
            HTTP_RESOLVER_SPK_DE_PURL));

    Optional<Index> indexOrt = AttributsReferenzFactory.findIndex(msDesc,
        AttributsTyp.ENTSTEHUNGSORT.getIndexName());
    assertNotNull(indexOrt);
    assertTrue(indexOrt.isPresent());

    Optional<Index> indexOther = AttributsReferenzFactory.findIndex(msDesc,
        "other");
    assertNotNull(indexOther);
    assertTrue(indexOther.isEmpty());
  }

  @Test
  void testFindTerme() {
    Index index = createIndex(AttributsTyp.ENTSTEHUNGSORT.getIndexName(),
        "HSP-123",
        HTTP_RESOLVER_SPK_DE_PURL);
    index.getTermsAndIndices().add(createTerm("origPlace", "Hildesheim"));
    index.getTermsAndIndices()
        .add(createIndex("other", "HSP-123", HTTP_RESOLVER_SPK_DE_PURL));

    List<Term> terme = AttributsReferenzFactory.findTerme(index);
    assertNotNull(terme);
    assertEquals(1, terme.size());
  }

  @Test
  void testMapTermText() {
    Index index = new Index();
    index.setIndexName("indexName");

    Term term = new Term();
    term.setType("termType");
    term.getContent().add("termContent");
    index.getTermsAndIndices().add(term);

    assertEquals("termContent",
        AttributsReferenzFactory.mapTermText(index, "termType"));
    assertNull(AttributsReferenzFactory.mapTermText(index, "otherType"));
  }

  @Test
  void testGetCopyOfValue() {
    MsDesc msDesc = new MsDesc();
    msDesc.setType(DokumentObjektTyp.HSP_DESCRIPTION.toString());
    msDesc.setId("MSDesc1");

    assertEquals("MSDesc1", getCopyOfValue("", msDesc));
    assertEquals("CP1", getCopyOfValue("CP1", msDesc));
    assertEquals("CP1", getCopyOfValue("#CP1", msDesc));
  }

  @Test
  void testFindChangeID_WithValidChange() {
    Index index = new Index();
    index.getChanges().add("#change_01");

    Optional<String> result = findChangeID(index);

    assertTrue(result.isPresent());
    assertEquals("change_01", result.get());
  }

  @Test
  void testFindChangeID_WithMultipleChanges() {
    Index index = new Index();
    index.getChanges().add("#change_02");
    index.getChanges().add("#change_01");

    Optional<String> result = findChangeID(index);

    assertTrue(result.isPresent());
    assertEquals("change_02", result.get());
  }

  @Test
  void testFindChangeID_WithNoChanges() {
    Index index = new Index();

    Optional<String> result = findChangeID(index);

    assertFalse(result.isPresent());
  }

  @Test
  void testFindChangeWithID_ValidID() {

    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    RevisionDesc revisionDesc = new RevisionDesc();

    Change change1 = new Change();
    change1.setId("change_01");
    Change change2 = new Change();
    change2.setId("change_02");

    revisionDesc.getRevisionChangeAttribute().add(change1);
    revisionDesc.getRevisionChangeAttribute().add(change2);
    header.setRevisionDesc(revisionDesc);
    tei.setTeiHeader(header);

    Optional<Change> result = findChangeWithID(tei, "change_01");

    assertTrue(result.isPresent());
    assertEquals("change_01", result.get().getId());
  }

  @Test
  void testFindChangeWithID_NonExistingID() {

    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    RevisionDesc revisionDesc = new RevisionDesc();

    Change change = new Change();
    change.setId("change_01");

    revisionDesc.getRevisionChangeAttribute().add(change);
    header.setRevisionDesc(revisionDesc);
    tei.setTeiHeader(header);

    Optional<Change> result = findChangeWithID(tei, "change_99");

    assertFalse(result.isPresent());
  }

  @Test
  void testFindChangeWithID_NullTEI() {
    assertThrows(IllegalArgumentException.class, () ->
        findChangeWithID(null, "change_01")
    );
  }

  @Test
  void testFindChangeWithID_NullTeiHeader() {
    TEI tei = new TEI();
    assertThrows(IllegalArgumentException.class, () ->
        findChangeWithID(tei, "change_01")
    );
  }

  @Test
  void testFindChangeWithID_NullRevisionDesc() {
    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    tei.setTeiHeader(header);

    assertThrows(IllegalArgumentException.class, () ->
        findChangeWithID(tei, "change_01")
    );
  }

  @Test
  void testMapModificationDate_ValidChange() {

    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    RevisionDesc revisionDesc = new RevisionDesc();

    Change change = new Change();
    change.setId("change_01");
    change.setWhen("2025-02-19T00:00:00");

    revisionDesc.getRevisionChangeAttribute().add(change);
    header.setRevisionDesc(revisionDesc);
    tei.setTeiHeader(header);

    Index index = new Index();
    index.getChanges().add("#change_01");

    Optional<LocalDateTime> result = AttributsReferenzFactory.mapModificationDate(
        tei, index);

    assertTrue(result.isPresent());
    assertEquals(LocalDateTime.of(2025, 2, 19, 0, 0), result.get());
  }

  @Test
  void testMapModificationDate_NoChangeInIndex() {
    TEI tei = new TEI();
    Index index = new Index();

    Optional<LocalDateTime> result = AttributsReferenzFactory.mapModificationDate(
        tei, index);

    assertFalse(result.isPresent());
  }

  @Test
  void testMapModificationDate_NoMatchingChange() {
    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    RevisionDesc revisionDesc = new RevisionDesc();
    header.setRevisionDesc(revisionDesc);
    tei.setTeiHeader(header);

    Change change = new Change();
    change.setId("change_99");
    change.setWhen("2025-02-19T00:00:00");
    revisionDesc.getRevisionChangeAttribute().add(change);

    Index index = new Index();
    index.getChanges().add("#change_01");

    Optional<LocalDateTime> result = AttributsReferenzFactory.mapModificationDate(
        tei, index);

    assertFalse(result.isPresent());
  }

  @Test
  void testMapModificationDate_NullTEI() {
    Index index = new Index();
    index.getChanges().add("#change_01");

    assertThrows(IllegalArgumentException.class, () ->
        AttributsReferenzFactory.mapModificationDate(null, index)
    );
  }

  @Test
  void testMapModificationDate_NullIndex() {
    TEI tei = new TEI();

    assertThrows(IllegalArgumentException.class, () ->
        AttributsReferenzFactory.mapModificationDate(tei, null)
    );
  }

  @Test
  void testMapBearbeiter_ValidChange() {

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

    Index index = new Index();
    index.setIndexName(AttributsTyp.TITEL.getIndexName());
    index.getChanges().add("#change_01");

    Optional<Bearbeiter> result = AttributsReferenzFactory.mapBearbeiter(tei,
        index);

    assertTrue(result.isPresent(), "Bearbeiter sollte gefunden werden!");
    assertEquals("{P00853}", result.get().getId(),
        "Die ID des Bearbeiters sollte mit dem ref-Attribut übereinstimmen!");
    assertEquals("Mammadov", result.get().getName(),
        "Der Name des Bearbeiters sollte korrekt übernommen werden!");
  }

  @Test
  void testMapBearbeiter_ChangeNotFound() {

    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    RevisionDesc revisionDesc = new RevisionDesc();

    header.setRevisionDesc(revisionDesc);
    tei.setTeiHeader(header);

    Index index = new Index();
    index.setIndexName(AttributsTyp.TITEL.getIndexName());
    index.getChanges().add("#change_02");

    Optional<Bearbeiter> result = AttributsReferenzFactory.mapBearbeiter(tei,
        index);

    assertFalse(result.isPresent());
  }

  @Test
  void testMapBearbeiter_NullTEIOrIndex() {
    assertThrows(IllegalArgumentException.class,
        () -> AttributsReferenzFactory.mapBearbeiter(null, new Index()));
    assertThrows(IllegalArgumentException.class,
        () -> AttributsReferenzFactory.mapBearbeiter(new TEI(), null));
  }

  @Test
  void testMapAttributsReferenzStatusTyp_Leer() {
    TEI tei = new TEI();
    Index index = new Index();
    index.setIndexName(AttributsTyp.TITEL.getIndexName());

    AttributsReferenzFactory.mapAttributsReferenzStatusTyp(tei, index);

    assertTrue(
        findChangeID(index).flatMap(i -> findChangeWithID(tei, i))
            .map(Change::getN).isEmpty(),
        "Wenn kein Change vorhanden ist, sollte kein Status gesetzt sein.");
  }

  @Test
  void testMapAttributsReferenzStatusTyp_Automatisch() {
    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    RevisionDesc revisionDesc = new RevisionDesc();

    Change change = new Change();
    change.setId("change_01");
    change.setWhen("2025-02-19T00:00:00");
    change.setN("AUTOMATISCH");

    PersName persName = new PersName();
    persName.getContent().add("User");
    persName.setKey("System User");
    change.getContent().add(persName);

    revisionDesc.getRevisionChangeAttribute().add(change);
    header.setRevisionDesc(revisionDesc);
    tei.setTeiHeader(header);

    Index index = new Index();
    index.setIndexName(AttributsTyp.TITEL.getIndexName());
    index.getChanges().add("#change_01");

    Optional<AttributsReferenzStatusTyp> result = AttributsReferenzFactory.mapAttributsReferenzStatusTyp(
        tei,
        index);

    assertEquals(AttributsReferenzStatusTyp.AUTOMATISCH, result.get(),
        "Wenn Bearbeiter 'System User' ist, sollte der Status AUTOMATISCH sein.");

    Optional<Change> updatedChange = findChangeID(index).flatMap(
        i -> findChangeWithID(tei, i));
    assertTrue(updatedChange.isPresent(), "Change sollte vorhanden sein.");
    assertEquals(AttributsReferenzStatusTyp.AUTOMATISCH,
        AttributsReferenzStatusTyp.valueOf(updatedChange.get().getN()),
        "Der Status im Change sollte AUTOMATISCH sein.");
  }

  @Test
  void testMapAttributsReferenzStatusTyp_Manuell() {
    TEI tei = new TEI();
    TeiHeader header = new TeiHeader();
    RevisionDesc revisionDesc = new RevisionDesc();

    Change change = new Change();
    change.setId("change_02");
    change.setWhen("2025-02-20");
    change.setN("MANUELL");

    PersName persName = new PersName();
    persName.getContent().add("Mammadov");
    persName.setKey("P00853");
    change.getContent().add(persName);

    revisionDesc.getRevisionChangeAttribute().add(change);
    header.setRevisionDesc(revisionDesc);
    tei.setTeiHeader(header);

    Index index = new Index();
    index.setIndexName(AttributsTyp.TITEL.getIndexName());
    index.getChanges().add("#change_02");

    Optional<AttributsReferenzStatusTyp> result = AttributsReferenzFactory.mapAttributsReferenzStatusTyp(
        tei,
        index);

    assertEquals(AttributsReferenzStatusTyp.MANUELL, result.get(),
        "Wenn Bearbeiter ein normaler Benutzer ist, sollte der Status MANUELL sein.");

    Optional<Change> updatedChange = findChangeID(index).flatMap(
        i -> findChangeWithID(tei, i));
    assertTrue(updatedChange.isPresent(), "Change sollte vorhanden sein.");

    assertEquals(AttributsReferenzStatusTyp.MANUELL,
        AttributsReferenzStatusTyp.valueOf(updatedChange.get().getN()),
        "Der Status im Change sollte MANUELL sein.");
  }

  @Test
  void testMapToNormdatenReferenz() {
    Term textLang = new Term();
    textLang.setKey("NORM-1");
    textLang.getReves().add("http://d-nb.info/gnd_1234567");
    textLang.getContent().add("Name");

    GrundspracheFactory factory = new GrundspracheFactory();
    NormdatenReferenz normdatenReferenz = factory.mapTermToNormdatenReferenz(
        textLang, "Language");

    assertNotNull(normdatenReferenz);
    assertEquals("NORM-1", normdatenReferenz.getId());
    assertEquals("http://d-nb.info/gnd/gnd_1234567",
        normdatenReferenz.getGndUrl());
    assertEquals("gnd_1234567", normdatenReferenz.getGndID());
    assertEquals("Name", normdatenReferenz.getName());
  }
}
