/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.mapper.tei;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
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
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.EntstehungszeitWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPCommandException;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.tei_c.ns._1.Head;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.SourceDesc;
import org.tei_c.ns._1.TEI;

class TEIKODAttributsReferenzCommandTest {

  private static final Path KOD_ATTRIBUTSREFERENZEN = Paths.get("src", "test", "resources", "tei",
      "tei-kod-attributsreferenzen.xml");

  private static final Path KOD_ATTRIBUTSREFERENZEN_LEER = Paths.get("src", "test", "resources", "tei",
      "tei-kod-attributsreferenzen_leer.xml");

  private static final Path BESCHREIBUNG_ATTRIBUTSREFERENZEN = Paths.get("src", "test", "resources", "tei",
      "tei-beschreibung-attributsreferenzen.xml");

  private static final Path KOD_BESCHREIBUNG = Paths.get("src", "test", "resources", "tei",
      "tei-kod-beschreibung.xml");

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.spk.de/purl";
  @Test
  void testUpdateAttributsReferenzen_Beschreibung() throws Exception {
    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752")
        .withTEIXml(Files.readString(KOD_ATTRIBUTSREFERENZEN_LEER, StandardCharsets.UTF_8))
        .withBeschreibungsdokumentIDs(Set.of("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001"))
        .build();

    PURL beschreibungsPURL = new PURL(URI.create("https://resolver.url/HSP-123"),URI.create("https://target.url/HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001"),
        PURLTyp.INTERNAL);

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001")
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withKodID(kod.getId())
        .withTEIXml(Files.readString(BESCHREIBUNG_ATTRIBUTSREFERENZEN, StandardCharsets.UTF_8))
        .addPURL(beschreibungsPURL)
        .build();

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertTrue(kod.getTeiXML().contains("<index indexName=\"" + attributsTyp.getIndexName() + "\">"));
    }

    TEIKODAttributsReferenzCommand.updateAttributsReferenzen(kod, beschreibung);

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {

      assertTrue(kod.getTeiXML().contains(
              "<index indexName=\"" + attributsTyp.getIndexName() + "\" copyOf=\"#" + beschreibung.getId() + "\" source=\""+beschreibungsPURL
                  .getPurl().toString()+"\">"),
          kod.getTeiXML());
    }
  }

  @Test
  void testUpdateAttributsReferenzen_Auswahl() throws IOException, HSPCommandException, HSPMapperException {
    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752")
        .withTEIXml(Files.readString(KOD_ATTRIBUTSREFERENZEN, StandardCharsets.UTF_8))
        .withBeschreibungsdokumentIDs(Set.of("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001", "HSP-B2"))
        .build();

    Set<AttributsReferenz> attributsReferenzen = new LinkedHashSet<>();
    attributsReferenzen.add(createTitel("HSP-B2", "Titel"));
    attributsReferenzen.add(createBeschreibstoff("HSP-B2", "Beschreibstoff"));
    attributsReferenzen.add(createUmfang("HSP-B2", "Umfang"));
    attributsReferenzen.add(createAbmessung("HSP-B2", "Abmessung"));
    attributsReferenzen.add(createFormat("HSP-B2", "Format"));
    attributsReferenzen.add(createEntstehungsort("HSP-B2", "Ort"));
    attributsReferenzen.add(createEntstehungszeit("HSP-B2", "Zeit"));
    attributsReferenzen.add(createGrundSprache("HSP-B2", "Grundsprache"));
    attributsReferenzen.add(createBuchschmuck("HSP-B2", "Buchschmuck"));
    attributsReferenzen.add(createHandschriftentyp("HSP-B2", "Handschriftentyp"));
    attributsReferenzen.add(createMusiknotation("HSP-B2", "Musiknotation"));
    attributsReferenzen.add(createStatus("HSP-B2", "Status"));

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertTrue(kod.getTeiXML().contains("<index indexName=\"" + attributsTyp.getIndexName() + "\" copyOf=\"HSP-B2\" source=\"http://resolver.pk.de/HSP-B2\">"));
    }



    TEIKODAttributsReferenzCommand.updateAttributsReferenzen(kod, attributsReferenzen, new HashMap<>());

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {

      assertTrue(kod.getTeiXML().contains(
              "<index indexName=\"" + attributsTyp.getIndexName() + "\" copyOf=\"#HSP-B2\" source=\""+HTTP_RESOLVER_SPK_DE_PURL+"\">"),
          kod.getTeiXML());
    }
  }

  @Test
  void testDeleteAttributsReferenzenForBeschreibung_ErsteBeschreibung()
      throws IOException, HSPCommandException, HSPMapperException {
    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752")
        .withTEIXml(Files.readString(KOD_ATTRIBUTSREFERENZEN, StandardCharsets.UTF_8))
        .withBeschreibungsdokumentIDs(Set.of("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001", "HSP-B2"))
        .build();

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertTrue(kod.getTeiXML().contains(
              "<index indexName=\"" + attributsTyp.getIndexName() + "\" copyOf=\"HSP-B2\" source=\"http://resolver.pk.de/HSP-B2\">"),
          attributsTyp.name());
    }

    TEIKODAttributsReferenzCommand.deleteAttributsReferenzenForBeschreibung(kod,
        "HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001");

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertTrue(kod.getTeiXML().contains(
              "<index indexName=\"" + attributsTyp.getIndexName() + "\" copyOf=\"HSP-B2\" source=\"http://resolver.pk.de/HSP-B2\">"),
          attributsTyp.name());
      assertFalse(kod.getTeiXML().contains("<index indexName=\"" + attributsTyp.getIndexName() + "\">"),
          kod.getTeiXML());
    }
  }

  @Test
  void testDeleteAttributsReferenzenForBeschreibung_ZweiteBeschreibung()
      throws IOException, HSPCommandException, HSPMapperException {
    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752")
        .withTEIXml(Files.readString(KOD_ATTRIBUTSREFERENZEN, StandardCharsets.UTF_8))
        .withBeschreibungsdokumentIDs(Set.of("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001", "HSP-B2"))
        .build();

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertTrue(kod.getTeiXML().contains(
              "<index indexName=\"" + attributsTyp.getIndexName() + "\" copyOf=\"HSP-B2\" source=\"http://resolver.pk.de/HSP-B2\">"),
          attributsTyp.name());
    }

    TEIKODAttributsReferenzCommand.deleteAttributsReferenzenForBeschreibung(kod, "HSP-B2");

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertFalse(kod.getTeiXML().contains(
              "<index indexName=\"" + attributsTyp.getIndexName() + "\" copyOf=\"HSP-B2\">"),
          attributsTyp.name());
      assertTrue(kod.getTeiXML().contains("<index indexName=\"" + attributsTyp.getIndexName() + "\">"),
          kod.getTeiXML());
    }
  }

  @Test
  void testMigrateAttributsReferenzen() throws IOException, HSPCommandException, HSPMapperException {
    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752")
        .withTEIXml(Files.readString(KOD_BESCHREIBUNG, StandardCharsets.UTF_8))
        .withBeschreibungsdokumentIDs(
            Set.of("HSP-f9f6eb32-068c-3089-a7d4-bbc3e55f3235", "HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001"))
        .build();

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertFalse(kod.getTeiXML().contains(
              "<index indexName=\"" + attributsTyp.getIndexName() + "\" copyOf=\"HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001\">"),
          attributsTyp.name());
      assertTrue(kod.getTeiXML().contains("<index indexName=\"" + attributsTyp.getIndexName() + "\">"),
          attributsTyp.name());
    }

    PURL beschreibungsPURL = new PURL(URI.create("https://resolver.url/HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001"),URI.create("https://target.url/HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001"),
        PURLTyp.INTERNAL);

    TEIKODAttributsReferenzCommand.migrateAttributsReferenzen(kod,Map.of("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001",beschreibungsPURL));

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertTrue(kod.getTeiXML().contains(
              "<index indexName=\"" + attributsTyp.getIndexName() + "\" copyOf=\"#HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001\" source=\"https://resolver.url/HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001\">"),
          kod.getTeiXML());
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
    attributsReferenzen.add(createHandschriftentyp("B-1", "Handschriftentyp"));
    attributsReferenzen.add(createMusiknotation("B-1", "Musiknotation"));
    attributsReferenzen.add(createStatus("B-1", "Status"));

    MsDesc msDesc = new MsDesc();

    TEIKODAttributsReferenzCommand.updateIndices(attributsReferenzen, msDesc);

    assertNotNull(msDesc.getHeads());
    assertEquals(1, msDesc.getHeads().size());

    Head head = msDesc.getHeads().get(0);

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertTrue(checkIndexExists(head.getContent(), attributsTyp.getIndexName()));
    }
  }

  @ParameterizedTest
  @EnumSource(value = AttributsTyp.class)
  void testBuildEmptyAttributsReferenz(AttributsTyp attributsTyp) {
    AttributsReferenz attributsReferenz = TEIKODAttributsReferenzCommand.buildEmptyAttributsReferenz(attributsTyp);
    assertNotNull(attributsReferenz);
    assertEquals(attributsTyp, attributsReferenz.getAttributsTyp());
    assertNull(attributsReferenz.getReferenzId());
    assertEquals("", attributsReferenz.getText());
  }

  @Test
  void testMergeAttributsReferenzen_ErsteBeschreibung() {
    Map<AttributsTyp, AttributsReferenz> kodReferenzen = new EnumMap<>(AttributsTyp.class);

    Map<AttributsTyp, AttributsReferenz> beschreibungReferenzen = new EnumMap<>(AttributsTyp.class);
    beschreibungReferenzen.put(TITEL, createTitel("B-1", "Titel"));
    beschreibungReferenzen.put(BESCHREIBSTOFF, createBeschreibstoff("B-1", "Beschreibstoff"));
    beschreibungReferenzen.put(UMFANG, createUmfang("B-1", "Umfang"));
    beschreibungReferenzen.put(ABMESSUNG, createAbmessung("B-1", "Abmessung"));
    beschreibungReferenzen.put(FORMAT, createFormat("B-1", "Format"));
    beschreibungReferenzen.put(ENTSTEHUNGSORT, createEntstehungsort("B-1", "Ort"));
    beschreibungReferenzen.put(ENTSTEHUNGSZEIT, createEntstehungszeit("B-1", "Zeit"));
    beschreibungReferenzen.put(GRUNDSPRACHE, createGrundSprache("B-1", "Grundsprache"));
    beschreibungReferenzen.put(BUCHSCHMUCK, createBuchschmuck("B-1", "Buchschmuck"));
    beschreibungReferenzen.put(HANDSCHRIFTENTYP, createHandschriftentyp("B-1", "Handschriftentyp"));
    beschreibungReferenzen.put(MUSIKNOTATION, createMusiknotation("B-1", "Musiknotation"));
    beschreibungReferenzen.put(STATUS, createStatus("B-1", "Status"));

    Set<AttributsReferenz> result = TEIKODAttributsReferenzCommand.
        mergeAttributsReferenzen(kodReferenzen, beschreibungReferenzen, true, new HashMap<>());

    assertNotNull(result);
    assertEquals(AttributsTyp.values().length, result.size());
    assertTrue(containsAttributsReferenz(result, TITEL, "B-1", "Titel"));
    assertTrue(containsAttributsReferenz(result, BESCHREIBSTOFF, "B-1", "Beschreibstoff"));
    assertTrue(containsAttributsReferenz(result, UMFANG, "B-1", "Umfang"));
    assertTrue(containsAttributsReferenz(result, ABMESSUNG, "B-1", "Abmessung"));
    assertTrue(containsAttributsReferenz(result, FORMAT, "B-1", "Format"));
    assertTrue(containsAttributsReferenz(result, ENTSTEHUNGSORT, "B-1", "Ort"));
    assertTrue(containsAttributsReferenz(result, ENTSTEHUNGSZEIT, "B-1", "Zeit"));
    assertTrue(containsAttributsReferenz(result, GRUNDSPRACHE, "B-1", "Grundsprache"));
    assertTrue(containsAttributsReferenz(result, BUCHSCHMUCK, "B-1", "Buchschmuck"));
    assertTrue(containsAttributsReferenz(result, HANDSCHRIFTENTYP, "B-1", "Handschriftentyp"));
    assertTrue(containsAttributsReferenz(result, MUSIKNOTATION, "B-1", "Musiknotation"));
    assertTrue(containsAttributsReferenz(result, STATUS, "B-1", "Status"));
  }

  @Test
  void testMergeAttributsReferenzen_ErsteBeschreibungUpdate() {
    Map<AttributsTyp, AttributsReferenz> kodReferenzen = new EnumMap<>(AttributsTyp.class);
    kodReferenzen.put(TITEL, createTitel("B-1", "Titel"));
    kodReferenzen.put(BESCHREIBSTOFF, createBeschreibstoff("B-1", "Beschreibstoff"));
    kodReferenzen.put(UMFANG, createUmfang("B-1", "Umfang"));
    kodReferenzen.put(ABMESSUNG, createAbmessung("B-1", "Abmessung"));
    kodReferenzen.put(FORMAT, createFormat("B-1", "Format"));
    kodReferenzen.put(ENTSTEHUNGSORT, createEntstehungsort("B-1", "Ort"));
    kodReferenzen.put(ENTSTEHUNGSZEIT, createEntstehungszeit("B-1", "Zeit"));
    kodReferenzen.put(GRUNDSPRACHE, createGrundSprache("B-1", "Grundsprache"));
    kodReferenzen.put(BUCHSCHMUCK, createBuchschmuck("B-1", "Buchschmuck"));
    kodReferenzen.put(HANDSCHRIFTENTYP, createHandschriftentyp("B-1", "Handschriftentyp"));
    kodReferenzen.put(MUSIKNOTATION, createMusiknotation("B-1", "Musiknotation"));
    kodReferenzen.put(STATUS, createStatus("B-1", "Status"));

    Map<AttributsTyp, AttributsReferenz> beschreibungReferenzen = new EnumMap<>(AttributsTyp.class);
    beschreibungReferenzen.put(TITEL, createTitel("B-1", "Titel neu"));
    beschreibungReferenzen.put(BESCHREIBSTOFF, createBeschreibstoff("B-1", "Beschreibstoff neu"));
    beschreibungReferenzen.put(UMFANG, createUmfang("B-1", "Umfang neu"));
    beschreibungReferenzen.put(ABMESSUNG, createAbmessung("B-1", "Abmessung neu"));
    beschreibungReferenzen.put(FORMAT, createFormat("B-1", "Format neu"));
    beschreibungReferenzen.put(ENTSTEHUNGSORT, createEntstehungsort("B-1", "Ort neu"));
    beschreibungReferenzen.put(ENTSTEHUNGSZEIT, createEntstehungszeit("B-1", "Zeit neu"));
    beschreibungReferenzen.put(GRUNDSPRACHE, createGrundSprache("B-1", "Grundsprache neu"));
    beschreibungReferenzen.put(BUCHSCHMUCK, createBuchschmuck("B-1", "Buchschmuck neu"));
    beschreibungReferenzen.put(HANDSCHRIFTENTYP, createHandschriftentyp("B-1", "Handschriftentyp neu"));
    beschreibungReferenzen.put(MUSIKNOTATION, createMusiknotation("B-1", "Musiknotation neu"));
    beschreibungReferenzen.put(STATUS, createStatus("B-1", "Status neu"));

    Set<AttributsReferenz> result = TEIKODAttributsReferenzCommand.
        mergeAttributsReferenzen(kodReferenzen, beschreibungReferenzen, true,new HashMap<>());

    assertNotNull(result);
    assertEquals(AttributsTyp.values().length, result.size());
    assertTrue(containsAttributsReferenz(result, TITEL, "B-1", "Titel neu"));
    assertTrue(containsAttributsReferenz(result, BESCHREIBSTOFF, "B-1", "Beschreibstoff neu"));
    assertTrue(containsAttributsReferenz(result, UMFANG, "B-1", "Umfang neu"));
    assertTrue(containsAttributsReferenz(result, ABMESSUNG, "B-1", "Abmessung neu"));
    assertTrue(containsAttributsReferenz(result, FORMAT, "B-1", "Format_neu"));
    assertTrue(containsAttributsReferenz(result, ENTSTEHUNGSORT, "B-1", "Ort neu"));
    assertTrue(containsAttributsReferenz(result, ENTSTEHUNGSZEIT, "B-1", "Zeit neu"));
    assertTrue(containsAttributsReferenz(result, GRUNDSPRACHE, "B-1", "Grundsprache neu"));
    assertTrue(containsAttributsReferenz(result, BUCHSCHMUCK, "B-1", "Buchschmuck neu"));
    assertTrue(containsAttributsReferenz(result, HANDSCHRIFTENTYP, "B-1", "Handschriftentyp neu"));
    assertTrue(containsAttributsReferenz(result, MUSIKNOTATION, "B-1", "Musiknotation neu"));
    assertTrue(containsAttributsReferenz(result, STATUS, "B-1", "Status neu"));
  }

  @Test
  void testMergeAttributsReferenzen_ZweiteBeschreibung() {
    Map<AttributsTyp, AttributsReferenz> kodReferenzen = new EnumMap<>(AttributsTyp.class);
    kodReferenzen.put(ENTSTEHUNGSORT, createEntstehungsort("B-1", "Ort"));

    Map<AttributsTyp, AttributsReferenz> beschreibungReferenzen = new EnumMap<>(AttributsTyp.class);
    beschreibungReferenzen.put(TITEL, createTitel("B-2", "Titel zwei"));
    beschreibungReferenzen.put(BESCHREIBSTOFF, createBeschreibstoff("B-2", "Beschreibstoff zwei"));
    beschreibungReferenzen.put(UMFANG, createUmfang("B-2", "Umfang zwei"));
    beschreibungReferenzen.put(ABMESSUNG, createAbmessung("B-2", "Abmessung zwei"));
    beschreibungReferenzen.put(FORMAT, createFormat("B-2", "Format zwei"));
    beschreibungReferenzen.put(ENTSTEHUNGSORT, createEntstehungsort("B-2", "Ort zwei"));
    beschreibungReferenzen.put(ENTSTEHUNGSZEIT, createEntstehungszeit("B-2", "Zeit zwei"));
    beschreibungReferenzen.put(GRUNDSPRACHE, createGrundSprache("B-2", "Grundsprache zwei"));
    beschreibungReferenzen.put(BUCHSCHMUCK, createBuchschmuck("B-2", "Buchschmuck zwei"));
    beschreibungReferenzen.put(HANDSCHRIFTENTYP, createHandschriftentyp("B-2", "Handschriftentyp zwei"));
    beschreibungReferenzen.put(MUSIKNOTATION, createMusiknotation("B-2", "Musiknotation zwei"));
    beschreibungReferenzen.put(STATUS, createStatus("B-2", "Status zwei"));

    Set<AttributsReferenz> result = TEIKODAttributsReferenzCommand.
        mergeAttributsReferenzen(kodReferenzen, beschreibungReferenzen, false,new HashMap<>());

    assertNotNull(result);
    assertEquals(AttributsTyp.values().length, result.size());

    assertTrue(containsAttributsReferenz(result, TITEL, null, ""));
    assertTrue(containsAttributsReferenz(result, BESCHREIBSTOFF, null, ""));
    assertTrue(containsAttributsReferenz(result, UMFANG, null, ""));
    assertTrue(containsAttributsReferenz(result, ABMESSUNG, null, ""));
    assertTrue(containsAttributsReferenz(result, FORMAT, null, ""));
    assertTrue(containsAttributsReferenz(result, ENTSTEHUNGSORT, "B-1", "Ort"));
    assertTrue(containsAttributsReferenz(result, ENTSTEHUNGSZEIT, null, ""));
    assertTrue(containsAttributsReferenz(result, GRUNDSPRACHE, null, ""));
    assertTrue(containsAttributsReferenz(result, BUCHSCHMUCK, null, ""));
    assertTrue(containsAttributsReferenz(result, HANDSCHRIFTENTYP, null, ""));
    assertTrue(containsAttributsReferenz(result, MUSIKNOTATION, null, ""));
    assertTrue(containsAttributsReferenz(result, STATUS, null, ""));
  }

  @Test
  void testMergeAttributsReferenzen_UpdateAuswahl() {
    Map<AttributsTyp, AttributsReferenz> kodReferenzen = new EnumMap<>(AttributsTyp.class);
    kodReferenzen.put(ENTSTEHUNGSORT, createEntstehungsort("B-1", "Ort"));

    Map<AttributsTyp, AttributsReferenz> beschreibungReferenzen = new EnumMap<>(AttributsTyp.class);
    beschreibungReferenzen.put(TITEL, createTitel("B-1", "Titel eins"));
    beschreibungReferenzen.put(BESCHREIBSTOFF, createBeschreibstoff("B-2", "Beschreibstoff zwei"));
    beschreibungReferenzen.put(UMFANG, createUmfang("B-3", "Umfang drei"));
    beschreibungReferenzen.put(ABMESSUNG, createAbmessung("B-4", "Abmessung vier"));
    beschreibungReferenzen.put(FORMAT, createFormat("B-5", "Format fünf"));
    beschreibungReferenzen.put(ENTSTEHUNGSORT, createEntstehungsort("B-6", "Ort sechs"));
    beschreibungReferenzen.put(ENTSTEHUNGSZEIT, createEntstehungszeit("B-7", "Zeit sieben"));
    beschreibungReferenzen.put(GRUNDSPRACHE, createGrundSprache("B-8", "Grundsprache acht"));
    beschreibungReferenzen.put(BUCHSCHMUCK, createBuchschmuck("B-9", "Buchschmuck neun"));
    beschreibungReferenzen.put(HANDSCHRIFTENTYP, createHandschriftentyp("B-10", "Handschriftentyp zehn"));
    beschreibungReferenzen.put(MUSIKNOTATION, createMusiknotation("B-11", "Musiknotation elf"));
    beschreibungReferenzen.put(STATUS, createStatus("B-12", "Status zwölf"));

    Set<AttributsReferenz> result = TEIKODAttributsReferenzCommand.
        mergeAttributsReferenzen(kodReferenzen, beschreibungReferenzen, true,new HashMap<>());

    assertNotNull(result);
    assertEquals(AttributsTyp.values().length, result.size());

    assertTrue(containsAttributsReferenz(result, TITEL, "B-1", "Titel eins"));
    assertTrue(containsAttributsReferenz(result, BESCHREIBSTOFF, "B-2", "Beschreibstoff zwei"));
    assertTrue(containsAttributsReferenz(result, UMFANG, "B-3", "Umfang drei"));
    assertTrue(containsAttributsReferenz(result, ABMESSUNG, "B-4", "Abmessung vier"));
    assertTrue(containsAttributsReferenz(result, FORMAT, "B-5", "Format_fünf"));
    assertTrue(containsAttributsReferenz(result, ENTSTEHUNGSORT, "B-6", "Ort sechs"));
    assertTrue(containsAttributsReferenz(result, ENTSTEHUNGSZEIT, "B-7", "Zeit sieben"));
    assertTrue(containsAttributsReferenz(result, GRUNDSPRACHE, "B-8", "Grundsprache acht"));
    assertTrue(containsAttributsReferenz(result, BUCHSCHMUCK, "B-9", "Buchschmuck neun"));
    assertTrue(containsAttributsReferenz(result, HANDSCHRIFTENTYP, "B-10", "Handschriftentyp zehn"));
    assertTrue(containsAttributsReferenz(result, MUSIKNOTATION, "B-11", "Musiknotation elf"));
    assertTrue(containsAttributsReferenz(result, STATUS, "B-12", "Status zwölf"));
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

    Set<String> result = TEIKODAttributsReferenzCommand.getIndexNames(attributsReferenzen);

    assertNotNull(result);
    assertEquals(AttributsTyp.values().length, result.size());

    Arrays.stream(AttributsTyp.values())
        .forEach(attributsTyp ->
            assertTrue(result.stream().anyMatch(value -> attributsTyp.getIndexName().equals(value)),
                attributsTyp.name()));

    Set<String> emptyResult = TEIKODAttributsReferenzCommand.getIndexNames(null);
    assertNotNull(emptyResult);
    assertTrue(emptyResult.isEmpty());
  }

  @Test
  void testFindSourceDesc() throws Exception {
    TEI tei;
    try (InputStream inputStream = Files.newInputStream(KOD_ATTRIBUTSREFERENZEN)) {
      tei = TEIObjectFactory.unmarshalOne(inputStream)
          .orElseThrow(() -> new Exception(KOD_ATTRIBUTSREFERENZEN + " contains no TEI!"));
    }

    SourceDesc sourceDesc =
        TEIKODAttributsReferenzCommand.findSourceDesc(tei, "HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752");

    assertNotNull(sourceDesc);

    TEI emptyTEI = new TEI();

    HSPCommandException hspCommandException = assertThrows(HSPCommandException.class,
        () -> TEIKODAttributsReferenzCommand.findSourceDesc(emptyTEI, "HSP-123"));

    assertEquals("No sourceDesc in TEI of KOD HSP-123", hspCommandException.getMessage());
  }

  @Test
  void testFindKodMsDescInSourceDesc() {
    SourceDesc sourceDesc = new SourceDesc();

    assertFalse(TEIKODAttributsReferenzCommand.findKodMsDescInSourceDesc(sourceDesc).isPresent());

    MsDesc msDesc = new MsDesc();
    sourceDesc.getBiblsAndBiblStructsAndListBibls().add(msDesc);

    assertFalse(TEIKODAttributsReferenzCommand.findKodMsDescInSourceDesc(sourceDesc).isPresent());

    msDesc.setType(DokumentObjektTyp.HSP_OBJECT.toString());

    assertTrue(TEIKODAttributsReferenzCommand.findKodMsDescInSourceDesc(sourceDesc).isPresent());
  }

  @Test
  void testFindBeschreibungMsDescInSourceDesc() {
    SourceDesc sourceDesc = new SourceDesc();

    assertFalse(TEIKODAttributsReferenzCommand.findBeschreibungMsDescInSourceDesc(sourceDesc).isPresent());

    MsDesc msDesc = new MsDesc();
    sourceDesc.getBiblsAndBiblStructsAndListBibls().add(msDesc);

    assertFalse(TEIKODAttributsReferenzCommand.findBeschreibungMsDescInSourceDesc(sourceDesc).isPresent());

    msDesc.setType(DokumentObjektTyp.HSP_DESCRIPTION.toString());
    assertTrue(TEIKODAttributsReferenzCommand.findBeschreibungMsDescInSourceDesc(sourceDesc).isPresent());

    msDesc.setType(DokumentObjektTyp.HSP_DESCRIPTION_RETRO.toString());
    assertTrue(TEIKODAttributsReferenzCommand.findBeschreibungMsDescInSourceDesc(sourceDesc).isPresent());
  }

  @Test
  void testAddPURLToAttributsreferenzen() {
    PURL beschreibungsPURL = new PURL(URI.create("https://resolver.url/HSP-123"),URI.create("https://target.url/HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001"),
        PURLTyp.INTERNAL);

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001")
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .addPURL(beschreibungsPURL)
        .build();

    Map<AttributsTyp, AttributsReferenz> beschreibungReferenzen = new EnumMap<>(AttributsTyp.class);
    beschreibungReferenzen.put(TITEL, createTitel("B-1", "Titel eins"));

    TEIKODAttributsReferenzCommand.addPURLToAttributsreferenzen(beschreibung,beschreibungReferenzen);

    assertEquals("https://resolver.url/HSP-123",beschreibungReferenzen.get(TITEL).getReferenzURL());
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

    TEIKODAttributsReferenzCommand.replaceAllHeadIndexElements(attributsReferenzen,head);

    assertEquals(12,head.getContent().size());

    Head headNotEmpty = new Head();
    Index titleIndex = new Index();
    titleIndex.setIndexName(TITEL.getIndexName());
    headNotEmpty.getContent().add(titleIndex);
    assertTrue(headNotEmpty.getContent().contains(titleIndex));

    TEIKODAttributsReferenzCommand.replaceAllHeadIndexElements(attributsReferenzen,headNotEmpty);
    assertEquals(12,head.getContent().size());
    assertFalse(headNotEmpty.getContent().contains(titleIndex));
  }

  private Titel createTitel(String referenzId, String text) {
    return Titel.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId(referenzId)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withText(text)
        .build();
  }

  private Beschreibstoff createBeschreibstoff(String referenzId, String text) {
    return Beschreibstoff.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId(referenzId)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withText(text)
        .withTypen(List.of("parchment", "papyrus"))
        .build();
  }

  private Umfang createUmfang(String referenzId, String text) {
    return Umfang.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId(referenzId)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withText(text)
        .withBlattzahl("394")
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
        .build();
  }

  private Format createFormat(String referenzId, String text) {
    return Format.builder()
        .withReferenzId(referenzId)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .addFormatWert(FormatWert.builder()
            .withText(text)
            .withTyp("deduced")
            .build())
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
        ).build();
  }

  private Entstehungszeit createEntstehungszeit(String referenzId, String text) {
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
        .build();
  }

  private Grundsprache createGrundSprache(String referenzId, String text) {
    return Grundsprache.builder()
        .withReferenzId(referenzId)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText(text)
        .addNormdatenId("4001523-3")
        .build();
  }

  private Buchschmuck createBuchschmuck(String referenzId, String text) {
    return Buchschmuck.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withReferenzId(referenzId)
        .withText(text)
        .build();
  }

  private Handschriftentyp createHandschriftentyp(String referenzId, String text) {
    return Handschriftentyp.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withReferenzId(referenzId)
        .withText(text)
        .build();
  }

  private Musiknotation createMusiknotation(String referenzId, String text) {
    return Musiknotation.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withReferenzId(referenzId)
        .withText(text)
        .build();
  }

  private Status createStatus(String referenzId, String text) {
    return Status.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzURL(HTTP_RESOLVER_SPK_DE_PURL)
        .withReferenzId(referenzId)
        .withText(text)
        .build();
  }

  private boolean containsAttributsReferenz(Set<AttributsReferenz> attributsReferenzen, AttributsTyp attributsTyp,
      String referenzId, String text) {
    return attributsReferenzen.stream().anyMatch(referenz ->
        attributsTyp == referenz.getAttributsTyp()
            && Objects.equals(referenzId, referenz.getReferenzId())
            && Objects.equals(text, referenz.getText()));
  }

  private boolean checkIndexExists(List<Object> content, String indexName) {
    return content.stream()
        .filter(Index.class::isInstance)
        .map(Index.class::cast)
        .map(Index::getIndexName)
        .anyMatch(value -> Objects.equals(value, indexName));
  }

}
