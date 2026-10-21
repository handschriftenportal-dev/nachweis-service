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

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
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

class FormatFactoryTest {

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";

  @Test
  void testBuild() {
    TEI tei = new TEI();

    FormatFactory formatFactory = new FormatFactory();

    MsDesc msDesc = new MsDesc();
    assertNull(formatFactory.build(msDesc, tei));

    msDesc = createMsDesc();
    Format format = formatFactory.build(msDesc, tei);

    assertNotNull(format);
    assertEquals("HSP-a44d2fd1-3ce6-34cf-8501-2e8833ba964f", format.getId());
    assertSame(AttributsTyp.FORMAT, format.getAttributsTyp());
    assertEquals("HSP-123", format.getReferenzId());
    assertSame(AttributsReferenzTyp.BESCHREIBUNG, format.getReferenzTyp());
    assertEquals("smaller than octavo;long and narrow", format.getText());

    assertNotNull(format.getFormatWerte());
    assertEquals(2, format.getFormatWerte().size());
    assertTrue(checkFormatWert(format.getFormatWerte(), "smaller than octavo", "deduced"));
    assertTrue(checkFormatWert(format.getFormatWerte(), "long and narrow", "factual"));
    assertEquals(HTTP_RESOLVER_SPK_DE_PURL, format.getReferenzURL());
  }

  private boolean checkFormatWert(List<FormatWert> werte, String text, String typ) {
    return werte.stream()
        .anyMatch(wert -> text.equals(wert.getText()) && typ.equals(wert.getTyp()));
  }

  private MsDesc createMsDesc() {
    MsDesc msDesc = new MsDesc();
    msDesc.setId("HSP-123");

    Head head = new Head();
    msDesc.getHeads().add(head);

    // First format index with NormdatenReferenz
    Index index_1 = createIndex(AttributsTyp.FORMAT.getIndexName(), "HSP-123",
        HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index_1);

    addNormdatenTermToIndex(index_1, TEIValues.FORMAT_TERM_NORM,
        "NORM-id1", "https://normdaten.example.com/vocab/1", "smaller than octavo");
    addTermToIndex(index_1, TEIValues.FORMAT_TERM_TEXT_TYPE, "deduced");

    // Second format index with NormdatenReferenz
    Index index_2 = createIndex(AttributsTyp.FORMAT.getIndexName(), "HSP-123",
        HTTP_RESOLVER_SPK_DE_PURL);
    head.getContent().add(index_2);

    addNormdatenTermToIndex(index_2, TEIValues.FORMAT_TERM_NORM,
        "NORM-id2", "https://normdaten.example.com/vocab/2", "long and narrow");
    addTermToIndex(index_2, TEIValues.FORMAT_TERM_TEXT_TYPE, "factual");

    return msDesc;
  }

  private void addNormdatenTermToIndex(Index index, String termType, String key, String ref,
      String content) {
    Term term = new Term();
    term.setType(termType);
    term.setKey(key);
    term.getReves().add(ref);
    if (content != null) {
      term.getContent().add(content);
    }
    index.getTermsAndIndices().add(term);
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

    FormatFactory formatFactory = new FormatFactory();

    MsDesc msDesc = new MsDesc();

    Head head = new Head();

    Index index = new Index();
    index.setIndexName(AttributsTyp.FORMAT.getIndexName());
    index.getChanges().add("#change_01");

    addNormdatenTermToIndex(index, TEIValues.FORMAT_TERM_NORM,
        "NORM-test", "https://normdaten.example.com/test", "test format");
    addTermToIndex(index, TEIValues.FORMAT_TERM_TEXT_TYPE, "factual");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Format format = formatFactory.build(msDesc, tei);

    assertNotNull(format.getModificationDatum(), "Modification Datum darf nicht null sein!");
    assertEquals(LocalDateTime.of(2025, 2, 19, 0, 0), format.getModificationDatum());
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

    FormatFactory formatFactory = new FormatFactory();

    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    Index index = new Index();

    index.setIndexName(AttributsTyp.FORMAT.getIndexName());
    index.getChanges().add("#change_01");

    addNormdatenTermToIndex(index, TEIValues.FORMAT_TERM_NORM,
        "NORM-test", "https://normdaten.example.com/test", "test format");
    addTermToIndex(index, TEIValues.FORMAT_TERM_TEXT_TYPE, "factual");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Format format = formatFactory.build(msDesc, tei);

    assertNotNull(format.getBearbeiter(), "Bearbeiter darf nicht null sein!");
    assertEquals("Mammadov", format.getBearbeiter().getName(),
        "Der Name des Bearbeiters sollte korrekt übernommen werden!");
    assertEquals("{P00853}", format.getBearbeiter().getId(),
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
    persName.setFull("Mammadov");
    persName.setId("{P00853}");

    change.getContent().add(persName);
    revisionDesc.getRevisionChangeAttribute().add(change);
    header.setRevisionDesc(revisionDesc);
    tei.setTeiHeader(header);

    FormatFactory formatFactory = new FormatFactory();
    MsDesc msDesc = new MsDesc();
    Head head = new Head();
    Index index = new Index();

    index.setIndexName(AttributsTyp.FORMAT.getIndexName());
    index.getChanges().add("#change_01");

    addNormdatenTermToIndex(index, TEIValues.FORMAT_TERM_NORM,
        "NORM-test", "https://normdaten.example.com/test", "test format");
    addTermToIndex(index, TEIValues.FORMAT_TERM_TEXT_TYPE, "factual");

    head.getContent().add(index);
    msDesc.getHeads().add(head);

    Format format = formatFactory.build(msDesc, tei);

    assertNotNull(format.getAttributsReferenzStatusTyp(),
        "AttributsReferenzStatusTyp darf nicht null sein!");

    AttributsReferenzStatusTyp expectedStatus = AttributsReferenzStatusTyp.MANUELL;

    assertEquals(expectedStatus, format.getAttributsReferenzStatusTyp(),
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

    var format = new FormatFactory().build(msDesc, tei);

    assertNotNull(format);
    assertEquals("folio;folio2", format.getText());

    var formatWerte = format.getFormatWerte();
    assertEquals(2, formatWerte.size());

    // First FormatWert
    var formatWert1 = formatWerte.get(0);
    assertEquals("folio", formatWert1.getText());
    assertEquals("factual", formatWert1.getTyp());

    var normdatenReferenz1 = formatWert1.getNormdatenReferenz();
    assertNotNull(normdatenReferenz1);
    assertEquals("NORM-1aef1e53-277d-3398-ada7-1c1b28a7e0c6", normdatenReferenz1.getId());
    assertEquals("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/CODC-B727",
        normdatenReferenz1.getUri());
    assertEquals("folio", normdatenReferenz1.getName());

    // Second FormatWert
    var formatWert2 = formatWerte.get(1);
    assertEquals("folio2", formatWert2.getText());
    assertEquals("deduced", formatWert2.getTyp());

    var normdatenReferenz2 = formatWert2.getNormdatenReferenz();
    assertNotNull(normdatenReferenz2);
    assertEquals("NORM-1aef1e53-277d-3398-ada7-1c1b28a7e0c8", normdatenReferenz2.getId());
    assertEquals("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/CODC-B728",
        normdatenReferenz2.getUri());
    assertEquals("folio2", normdatenReferenz2.getName());
  }
}
