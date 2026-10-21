package de.staatsbibliothek.berlin.hsp.mapper.tei.kod;

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.ABMESSUNG;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.BESCHREIBSTOFF;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.BUCHSCHMUCK;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.ENTSTEHUNGSORT;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.ENTSTEHUNGSZEIT;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.FORMAT;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.GRUNDSPRACHE;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.HANDSCHRIFTENTYP;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.MUSIKNOTATION;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.STATUS;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.TITEL;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.UMFANG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AbmessungWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.EntstehungszeitWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPCommandException;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.tei_c.ns._1.Change;
import org.tei_c.ns._1.Head;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.PersName;
import org.tei_c.ns._1.RevisionDesc;
import org.tei_c.ns._1.SourceDesc;
import org.tei_c.ns._1.TEI;
import org.w3c.dom.Node;
import org.xmlunit.builder.Input;
import org.xmlunit.xpath.JAXPXPathEngine;

class TEIKODAttributsReferenzCommandTest {

  private static final Path KOD_ATTRIBUTSREFERENZEN = Paths.get("src", "test",
      "resources", "tei",
      "tei-kod-attributsreferenzen.xml");


  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";

  @RepeatedTest(2)
  void testUpdateAttributsReferenzenWithChangeID() throws Exception {

    Map<AttributsTyp, AttributsReferenz> attributsReferenzen = new HashMap<>();
    attributsReferenzen.put(AttributsTyp.TITEL,
        createTitel("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001", "Titel"));
    attributsReferenzen.put(BESCHREIBSTOFF,
        createBeschreibstoff("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001",
            "Beschreibstoff"));
    attributsReferenzen.put(UMFANG,
        createUmfang("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001", "Umfang"));
    attributsReferenzen.put(
        ABMESSUNG, createAbmessung("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001",
            "Abmessung"));
    attributsReferenzen.put(FORMAT,
        createFormat("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001", "Format"));
    attributsReferenzen.put(
        ENTSTEHUNGSORT,
        createEntstehungsort("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001",
            "Ort"));
    attributsReferenzen.put(
        ENTSTEHUNGSZEIT,
        createEntstehungszeit("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001",
            "Zeit"));
    attributsReferenzen.put(
        GRUNDSPRACHE,
        createGrundSprache("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001",
            "Grundsprache"));
    attributsReferenzen.put(
        BUCHSCHMUCK,
        createBuchschmuck("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001",
            "Buchschmuck"));
    attributsReferenzen.put(HANDSCHRIFTENTYP,
        createHandschriftentyp("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001"));
    attributsReferenzen.put(MUSIKNOTATION,
        createMusiknotation("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001",
            "Musiknotation"));
    attributsReferenzen.put(STATUS,
        createStatus("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001", "Status"));

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752")
        .withTEIXml(
            Files.readString(KOD_ATTRIBUTSREFERENZEN, StandardCharsets.UTF_8))
        .withBeschreibungsdokumentIDs(
            Set.of("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001"))
        .withAttributsReferenzen(attributsReferenzen)
        .build();

    Bearbeiter bearbeiter = Bearbeiter.newBuilder()
        .withBearbeitername("Konrad Eichstädt").withId("b-ke101").build();
    Change change = TEIKODAttributsReferenzCommand.createChangeElement(
        HSP_UUI_Factory.generate(), LocalDateTime.now(),
        AttributsReferenzStatusTyp.AUTOMATISCH, "Test", bearbeiter);

    TEIKODAttributsReferenzCommand.updateAttributsReferenzen(kod, change);

    assertNotNull(kod.getTeiXML());

    JAXPXPathEngine engine = new JAXPXPathEngine();
    engine.setNamespaceContext(Map.of("tei",
        "http://www.tei-c.org/ns/1.0")); // Namespace für "tei:" registrieren

    Iterable<Node> changeNodes = engine.selectNodes(
        "//tei:revisionDesc/tei:change",
        Input.fromString(kod.getTeiXML()).build());

    assertNotNull(changeNodes);
    assertEquals(1,
        StreamSupport.stream(changeNodes.spliterator(), false).count());
    assertEquals("TestKonrad Eichstädt",
        changeNodes.iterator().next().getTextContent());

    Iterable<Node> indexNodes = engine.selectNodes(
        "//tei:head/tei:index[@change]",
        Input.fromString(kod.getTeiXML()).build());

    assertEquals(12,
        StreamSupport.stream(indexNodes.spliterator(), false).count());
  }

  @Test
  void testDeleteAttributsReferenzenForBeschreibung_ErsteBeschreibung()
      throws Exception {
    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752")
        .withTEIXml(
            Files.readString(KOD_ATTRIBUTSREFERENZEN, StandardCharsets.UTF_8))
        .withBeschreibungsdokumentIDs(
            Set.of("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001", "HSP-B2"))
        .build();

    JAXPXPathEngine engine = new JAXPXPathEngine();
    engine.setNamespaceContext(Map.of("tei", "http://www.tei-c.org/ns/1.0"));

    // Verify initial state: all index elements have copyOf="HSP-B2" and source="http://resolver.pk.de/HSP-B2"
    // and indexName is one of AttributsTyp values
    String xpathAllIndices = "//tei:index";
    Iterable<Node> allIndices = engine.selectNodes(xpathAllIndices,
        Input.fromString(kod.getTeiXML()).build());

    Set<String> validIndexNames = Arrays.stream(AttributsTyp.values())
        .map(AttributsTyp::getIndexName)
        .collect(Collectors.toSet());

    for (Node indexNode : allIndices) {
      org.w3c.dom.Element indexElement = (org.w3c.dom.Element) indexNode;
      String indexName = indexElement.getAttribute("indexName");
      String copyOf = indexElement.getAttribute("copyOf");
      String source = indexElement.getAttribute("source");

      assertTrue(validIndexNames.contains(indexName),
          "Index has invalid indexName: " + indexName);
      assertEquals("HSP-B2", copyOf,
          "Index " + indexName + " should have copyOf='HSP-B2' but has '" + copyOf + "'");
      assertEquals("http://resolver.pk.de/HSP-B2", source,
          "Index " + indexName + " should have source='http://resolver.pk.de/HSP-B2' but has '"
              + source + "'");
    }

    Change change = TEIKODAttributsReferenzCommand.createChangeElement(
        HSP_UUI_Factory.generate(), LocalDateTime.now(),
        AttributsReferenzStatusTyp.AUTOMATISCH,
        "Delete Beschreibung",
        Bearbeiter.newBuilder().withId("b-ke101").withBearbeitername("Konrad")
            .build());

    TEIKODAttributsReferenzCommand.deleteAttributsReferenzenForBeschreibung(kod,
        "HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001", change);

    // Verify after deletion: indices should have copyOf="#HSP-B2" (with # prefix for local reference)
    Iterable<Node> allIndicesAfter = engine.selectNodes(xpathAllIndices,
        Input.fromString(kod.getTeiXML()).build());

    for (Node indexNode : allIndicesAfter) {
      org.w3c.dom.Element indexElement = (org.w3c.dom.Element) indexNode;
      String indexName = indexElement.getAttribute("indexName");
      String copyOf = indexElement.getAttribute("copyOf");
      String source = indexElement.getAttribute("source");

      assertEquals("#HSP-B2", copyOf,
          "Index " + indexName + " should have copyOf='#HSP-B2' but has '" + copyOf + "'");
      assertEquals("http://resolver.pk.de/HSP-B2", source,
          "Index " + indexName + " should have source='http://resolver.pk.de/HSP-B2' but has '"
              + source + "'");

      // Verify no indices without copyOf remain
      assertFalse(copyOf.isEmpty(),
          "Index " + indexName + " should have copyOf attribute");
    }

    assertTrue(kod.getTeiXML().contains("Delete Beschreibung"));
  }

  @Test
  void testFindAllUsedChangeIDs() throws Exception {

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752")
        .withTEIXml(
            Files.readString(Paths.get("src", "test", "resources", "tei",
                "kod_attributsreferenzen_man.xml"), StandardCharsets.UTF_8))
        .withBeschreibungsdokumentIDs(
            Set.of("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001", "HSP-B2"))
        .build();

    final TEI tei = TEIKODAttributsReferenzCommand.openTEIDokument(
        kod.getTeiXML());
    Set<String> changeids = TEIKODAttributsReferenzCommand.findAllUsedChangeIDs(
        tei);

    assertNotNull(changeids);
    assertEquals(2, changeids.size());
    Assertions.assertTrue(kod.getTeiXML()
            .contains("HSP-3dc48342-d6fa-4654-bf76-6c75ceee1af2"),
        "A Change which is not used");

    TEIKODAttributsReferenzCommand.addChangeRevisionStatement(tei,
        TEIKODAttributsReferenzCommand.createChangeElement("1",
            LocalDateTime.now(),
            AttributsReferenzStatusTyp.AUTOMATISCH, "TestChange",
            Bearbeiter.newBuilder().withId("b-ke101").withNachname("Eichstaedt")
                .build()));

    Assertions.assertFalse(TEIKODAttributsReferenzCommand.marshalTEI(tei)
            .contains("HSP-3dc48342-d6fa-4654-bf76-6c75ceee1af2"),
        "The change id is not used thats why they must be removed.");
  }

  @Test
  void testFindAllUsedChangeIDs2() throws Exception {

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752")
        .withTEIXml(
            Files.readString(Paths.get("src", "test", "resources", "tei",
                "kod_attributsreferenzen_no_index.xml"), StandardCharsets.UTF_8))
        .withBeschreibungsdokumentIDs(
            Set.of("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001", "HSP-B2"))
        .build();

    final TEI tei = TEIKODAttributsReferenzCommand.openTEIDokument(
        kod.getTeiXML());
    Set<String> changeids = TEIKODAttributsReferenzCommand.findAllUsedChangeIDs(
        tei);

    assertNotNull(changeids);
  }

  @Test
  void testDeleteAttributsReferenzenForBeschreibung_ZweiteBeschreibung()
      throws Exception {
    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752")
        .withTEIXml(
            Files.readString(KOD_ATTRIBUTSREFERENZEN, StandardCharsets.UTF_8))
        .withBeschreibungsdokumentIDs(
            Set.of("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001", "HSP-B2"))
        .build();

    JAXPXPathEngine engine = new JAXPXPathEngine();
    engine.setNamespaceContext(Map.of("tei", "http://www.tei-c.org/ns/1.0"));

    // Verify initial state: all index elements have copyOf="HSP-B2" and source
    String xpathAllIndices = "//tei:index";
    Iterable<Node> allIndices = engine.selectNodes(xpathAllIndices,
        Input.fromString(kod.getTeiXML()).build());

    Set<String> validIndexNames = Arrays.stream(AttributsTyp.values())
        .map(AttributsTyp::getIndexName)
        .collect(Collectors.toSet());

    for (Node indexNode : allIndices) {
      org.w3c.dom.Element indexElement = (org.w3c.dom.Element) indexNode;
      String indexName = indexElement.getAttribute("indexName");
      String copyOf = indexElement.getAttribute("copyOf");
      String source = indexElement.getAttribute("source");

      assertTrue(validIndexNames.contains(indexName),
          "Index has invalid indexName: " + indexName);
      assertEquals("HSP-B2", copyOf,
          "Index " + indexName + " should have copyOf='HSP-B2' but has '" + copyOf + "'");
      assertEquals("http://resolver.pk.de/HSP-B2", source,
          "Index " + indexName + " should have source='http://resolver.pk.de/HSP-B2' but has '"
              + source + "'");
    }

    Change change = TEIKODAttributsReferenzCommand.createChangeElement(
        HSP_UUI_Factory.generate(), LocalDateTime.now(),
        AttributsReferenzStatusTyp.AUTOMATISCH,
        "Delete Beschreibung ",
        Bearbeiter.newBuilder().withId("b-ke101").withBearbeitername("Konrad")
            .build());

    TEIKODAttributsReferenzCommand.deleteAttributsReferenzenForBeschreibung(kod,
        "HSP-B2", change);

    // Verify after deletion: no indices with copyOf="HSP-B2", all should have change attribute
    Iterable<Node> allIndicesAfter = engine.selectNodes(xpathAllIndices,
        Input.fromString(kod.getTeiXML()).build());

    for (Node indexNode : allIndicesAfter) {
      org.w3c.dom.Element indexElement = (org.w3c.dom.Element) indexNode;
      String indexName = indexElement.getAttribute("indexName");
      String copyOf = indexElement.getAttribute("copyOf");
      String changeValue = indexElement.getAttribute("change");

      // Should NOT have copyOf="HSP-B2" (without #)
      assertNotEquals("HSP-B2", copyOf,
          "Index " + indexName + " should not have copyOf='HSP-B2' but has '" + copyOf + "'");

      // Should have change attribute
      assertFalse(changeValue.isEmpty(),
          "Index " + indexName + " should have change attribute");
    }
  }

  @Test
  void testUpdateIndices() {
    Set<AttributsReferenz> attributsReferenzen = new LinkedHashSet<>();
    attributsReferenzen.add(createTitel("B-1", "Titel"));
    attributsReferenzen.add(createBeschreibstoff("B-1", "Beschreibstoff"));
    attributsReferenzen.add(createUmfang("B-1", "Umfang"));
    attributsReferenzen.add(createAbmessung("B-1", "Abmessung"));
    attributsReferenzen.add(createFormat("B-1", "Format"));
    attributsReferenzen.add(createEntstehungsort("B-1", "Ort"));
    attributsReferenzen.add(createEntstehungszeit("B-1", "Zeit"));
    attributsReferenzen.add(createGrundSprache("B-1", "GrundSprache"));
    attributsReferenzen.add(createBuchschmuck("B-1", "Buchschmuck"));
    attributsReferenzen.add(createHandschriftentyp("B-1"));
    attributsReferenzen.add(createMusiknotation("B-1", "Musiknotation"));
    attributsReferenzen.add(createStatus("B-1", "Status"));

    MsDesc msDesc = new MsDesc();

    TEIKODAttributsReferenzCommand.updateIndices(attributsReferenzen, msDesc);

    assertNotNull(msDesc.getHeads());
    assertEquals(1, msDesc.getHeads().size());

    Head head = msDesc.getHeads().getFirst();

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertTrue(
          checkIndexExists(head.getContent(), attributsTyp.getIndexName()));
    }
  }

  @ParameterizedTest
  @EnumSource(value = AttributsTyp.class)
  void testBuildEmptyAttributsReferenz(AttributsTyp attributsTyp) {
    AttributsReferenz attributsReferenz = TEIKODAttributsReferenzCommand.buildEmptyAttributsReferenz(
        attributsTyp);
    assertNotNull(attributsReferenz);
    assertEquals(attributsTyp, attributsReferenz.getAttributsTyp());
    assertNull(attributsReferenz.getReferenzId());
    assertEquals("", attributsReferenz.getText());
  }

  @Test
  void testGetIndexNames() {
    Set<AttributsReferenz> attributsReferenzen = new LinkedHashSet<>();
    attributsReferenzen.add(Titel.builder().build());
    attributsReferenzen.add(Beschreibstoff.builder().build());
    attributsReferenzen.add(Umfang.builder().build());
    attributsReferenzen.add(Abmessung.builder().build());
    attributsReferenzen.add(Format.builder().build());
    attributsReferenzen.add(Entstehungsort.builder().build());
    attributsReferenzen.add(Entstehungszeit.builder().build());
    attributsReferenzen.add(Grundsprache.builder().build());
    attributsReferenzen.add(Buchschmuck.builder().build());
    attributsReferenzen.add(Handschriftentyp.builder().build());
    attributsReferenzen.add(Musiknotation.builder().build());
    attributsReferenzen.add(Status.builder().build());

    Set<String> result = TEIKODAttributsReferenzCommand.getIndexNames(
        attributsReferenzen);

    assertNotNull(result);
    assertEquals(AttributsTyp.values().length, result.size());

    Arrays.stream(AttributsTyp.values())
        .forEach(attributsTyp ->
            assertTrue(result.stream()
                    .anyMatch(value -> attributsTyp.getIndexName().equals(value)),
                attributsTyp.name()));

    Set<String> emptyResult = TEIKODAttributsReferenzCommand.getIndexNames(
        null);
    assertNotNull(emptyResult);
    assertTrue(emptyResult.isEmpty());
  }

  @Test
  void testFindSourceDesc() throws Exception {
    TEI tei;
    try (InputStream inputStream = Files.newInputStream(
        KOD_ATTRIBUTSREFERENZEN)) {
      tei = TEIObjectFactory.unmarshalOne(inputStream)
          .orElseThrow(() -> new Exception(
              KOD_ATTRIBUTSREFERENZEN + " contains no TEI!"));
    }

    SourceDesc sourceDesc =
        TEIKODAttributsReferenzCommand.findSourceDesc(tei,
            "HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752");

    assertNotNull(sourceDesc);

    TEI emptyTEI = new TEI();

    HSPCommandException hspCommandException = assertThrows(
        HSPCommandException.class,
        () -> TEIKODAttributsReferenzCommand.findSourceDesc(emptyTEI,
            "HSP-123"));

    assertEquals("No sourceDesc in TEI of KOD HSP-123",
        hspCommandException.getMessage());
  }

  @Test
  void testFindKodMsDescInSourceDesc() {
    SourceDesc sourceDesc = new SourceDesc();

    assertFalse(
        TEIKODAttributsReferenzCommand.findKodMsDescInSourceDesc(sourceDesc)
            .isPresent());

    MsDesc msDesc = new MsDesc();
    sourceDesc.getBiblsAndBiblStructsAndListBibls().add(msDesc);

    assertFalse(
        TEIKODAttributsReferenzCommand.findKodMsDescInSourceDesc(sourceDesc)
            .isPresent());

    msDesc.setType(DokumentObjektTyp.HSP_OBJECT.toString());

    assertTrue(
        TEIKODAttributsReferenzCommand.findKodMsDescInSourceDesc(sourceDesc)
            .isPresent());
  }

  @Test
  void testFindBeschreibungMsDescInSourceDesc() {
    SourceDesc sourceDesc = new SourceDesc();

    assertFalse(
        TEIKODAttributsReferenzCommand.findBeschreibungMsDescInSourceDesc(
            sourceDesc).isPresent());

    MsDesc msDesc = new MsDesc();
    sourceDesc.getBiblsAndBiblStructsAndListBibls().add(msDesc);

    assertFalse(
        TEIKODAttributsReferenzCommand.findBeschreibungMsDescInSourceDesc(
            sourceDesc).isPresent());

    msDesc.setType(DokumentObjektTyp.HSP_DESCRIPTION.toString());
    assertTrue(
        TEIKODAttributsReferenzCommand.findBeschreibungMsDescInSourceDesc(
            sourceDesc).isPresent());

    msDesc.setType(DokumentObjektTyp.HSP_DESCRIPTION_RETRO.toString());
    assertTrue(
        TEIKODAttributsReferenzCommand.findBeschreibungMsDescInSourceDesc(
            sourceDesc).isPresent());
  }

  @Test
  void testReplaceAllHeadIndexElements() {

    Head head = new Head();
    Set<AttributsReferenz> attributsReferenzen = new LinkedHashSet<>();
    attributsReferenzen.add(Titel.builder().build());
    attributsReferenzen.add(Beschreibstoff.builder().build());
    attributsReferenzen.add(Umfang.builder().build());
    attributsReferenzen.add(Abmessung.builder().build());
    attributsReferenzen.add(Format.builder().build());
    attributsReferenzen.add(Entstehungsort.builder().build());
    attributsReferenzen.add(Entstehungszeit.builder().build());
    attributsReferenzen.add(Grundsprache.builder().build());
    attributsReferenzen.add(Buchschmuck.builder().build());
    attributsReferenzen.add(Handschriftentyp.builder().build());
    attributsReferenzen.add(Musiknotation.builder().build());
    attributsReferenzen.add(Status.builder().build());

    TEIKODAttributsReferenzCommand.replaceAllHeadIndexElements(
        attributsReferenzen, head);

    assertEquals(12, head.getContent().size());

    Head headNotEmpty = new Head();
    Index titleIndex = new Index();
    titleIndex.setIndexName(TITEL.getIndexName());
    titleIndex.setCopyOf("#HSP-94ce5e2e-06da-3314-bd57-2d06bfe09925");
    headNotEmpty.getContent().add(titleIndex);
    assertTrue(headNotEmpty.getContent().contains(titleIndex));

    TEIKODAttributsReferenzCommand.replaceAllHeadIndexElements(Set.of(),
        headNotEmpty);
    assertEquals(1, headNotEmpty.getContent().size());
    assertEquals("#HSP-94ce5e2e-06da-3314-bd57-2d06bfe09925",
        ((Index) headNotEmpty.getContent().get(0)).getCopyOf());
  }

  private Titel createTitel(String referenzId, String text) {
    return Titel.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId(referenzId)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withText(text)
        .withChangeId("ChangeID")
        .build();
  }

  private Beschreibstoff createBeschreibstoff(String referenzId, String text) {
    return Beschreibstoff.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId(referenzId)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withText(text)
        .withChangeId("ChangeID")
        .build();
  }

  private Umfang createUmfang(String referenzId, String text) {
    return Umfang.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId(referenzId)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withText(text)
        .withBlattzahl("394")
        .withChangeId("ChangeID")
        .build();
  }

  private Abmessung createAbmessung(String referenzId, String text) {
    return Abmessung.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withReferenzId(referenzId)
        .addAbmessungWert(AbmessungWert.builder()
            .withText(text)
            .withHoehe("23,5")
            .withBreite("16,5")
            .withTiefe("2,5")
            .withTyp("factual")
            .build())
        .withChangeId("ChangeID")
        .build();
  }

  private Format createFormat(String referenzId, String text) {
    return Format.builder()
        .withReferenzId(referenzId)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .addFormatWert(FormatWert.builder()
            .withReferenz(new NormdatenReferenz("id-1", text, "1"))
            .withTyp("deduced")
            .build())
        .withChangeId("ChangeID")
        .build();
  }

  private Entstehungsort createEntstehungsort(String referenzId, String text) {
    return Entstehungsort.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withReferenzId(referenzId)
        .withText(text)
        .addNormdatenReferenz(
            new NormdatenReferenz("NORM-f5c6758c-3c33-3785-933a-999b0fa2a4a0",
                "Weißenburg (Elsass)",
                "https://d-nb.info/gnd/4079134-8",
                NormdatenReferenz.ORT_TYPE_NAME)
        )
        .withChangeId("ChangeID")
        .build();
  }

  private Entstehungszeit createEntstehungszeit(String referenzId,
      String text) {
    return Entstehungszeit.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId(referenzId)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .addEntstehungszeitWert(EntstehungszeitWert.builder()
            .withText(text)
            .withNichtVor("1426")
            .withNichtNach("1446")
            .withTyp("datable")
            .build())
        .withChangeId("ChangeID")
        .build();
  }

  private Grundsprache createGrundSprache(String referenzId, String text) {
    return Grundsprache.builder()
        .withReferenzId(referenzId)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText(text)
        .addReferenz(new NormdatenReferenz("NORM-123", "deutsch", "4001523-3"))
        .withChangeId("ChangeID")
        .build();
  }

  private Buchschmuck createBuchschmuck(String referenzId, String text) {
    return Buchschmuck.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withReferenzId(referenzId)
        .withText(text)
        .withChangeId("ChangeID")
        .build();
  }

  private Handschriftentyp createHandschriftentyp(String referenzId) {
    return Handschriftentyp.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withReferenzId(referenzId)
        .withChangeId("ChangeID")
        .build();
  }

  private Musiknotation createMusiknotation(String referenzId, String text) {
    return Musiknotation.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withReferenzId(referenzId)
        .withText(text)
        .withChangeId("ChangeID")
        .build();
  }

  private Status createStatus(String referenzId, String text) {
    return Status.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withReferenzId(referenzId)
        .withText(text)
        .withChangeId("ChangeID")
        .build();
  }

  private boolean checkIndexExists(List<Object> content, String indexName) {
    return content.stream()
        .filter(Index.class::isInstance)
        .map(Index.class::cast)
        .map(Index::getIndexName)
        .anyMatch(value -> Objects.equals(value, indexName));
  }

  @Test
  void testAddChangeRevisionStatement() throws Exception {

    TEI tei = new TEI();

    String changeId = "change_01";
    LocalDateTime changeDate = LocalDateTime.of(2025, 2, 24, 10, 0);
    AttributsReferenzStatusTyp changeStatus = AttributsReferenzStatusTyp.MANUELL;
    String changeText = "Attributsreferenz 'norm_title' ersetzt durch";
    Bearbeiter bearbeiter = Bearbeiter.newBuilder().withId("P00853")
        .withBearbeitername("Mammadov")
        .withNachname("Mammadov")
        .withPerson(
            new NormdatenReferenz("NORM-123", "Konrad Eichstädt", "GND-ID"))
        .build();

    Change change = TEIKODAttributsReferenzCommand.createChangeElement(changeId,
        changeDate,
        changeStatus, changeText, bearbeiter);

    TEIKODAttributsReferenzCommand.addChangeRevisionStatement(tei, change);

    assertNotNull(tei.getTeiHeader(), "TeiHeader sollte nicht null sein!");

    RevisionDesc revisionDesc = tei.getTeiHeader().getRevisionDesc();
    assertNotNull(revisionDesc, "RevisionDesc sollte nicht null sein!");
    assertFalse(revisionDesc.getRevisionChangeAttribute().isEmpty(),
        "RevisionDesc sollte mindestens ein Change enthalten!");

    Optional<Change> optionalChange = revisionDesc.getRevisionChangeAttribute()
        .stream()
        .filter(c -> c.getId().equals(changeId))
        .findFirst();

    assertTrue(optionalChange.isPresent(),
        "Change mit der ID sollte existieren!");

    Change changeOptional = optionalChange.get();
    assertEquals(changeId, changeOptional.getId(),
        "Change-ID sollte korrekt sein!");
    assertEquals(changeDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        changeOptional.getWhen(),
        "Datum sollte korrekt sein!");
    assertEquals(changeStatus.name(), changeOptional.getN(),
        "Change-Status sollte korrekt sein!");
    assertTrue(changeOptional.getContent().contains(changeText),
        "Change-Text sollte enthalten sein!");

    Optional<PersName> optionalPersName = changeOptional.getContent().stream()
        .filter(PersName.class::isInstance)
        .map(PersName.class::cast)
        .findFirst();

    assertTrue(optionalPersName.isPresent(), "PersName sollte existieren!");
    PersName persName = optionalPersName.get();
    assertEquals(bearbeiter.getPerson().getId(), persName.getKey(),
        "Bearbeiter-ID sollte korrekt sein!");
    assertEquals(bearbeiter.getPerson().getName(),
        persName.getContent().stream().map(String::valueOf)
            .collect(Collectors.joining()),
        "Bearbeiter-Name sollte korrekt sein!");
  }
}
