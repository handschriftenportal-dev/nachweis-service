package de.staatsbibliothek.berlin.hsp.mapper.tei.kod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessource;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessourceTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Forschungsdokumentation;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEICommandException;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.mapper.tei.common.TEIDokumentIO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Additional;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.ListBibl;
import org.tei_c.ns._1.Ref;
import org.tei_c.ns._1.TEI;

class ForschungsdokumentationListBiblMapperTest {

  private static final Path TEI_FILE_PATH =
      Paths.get("src", "test", "resources", "tei", "tei-HSP-kodms1.xml");
  private static final String RESSOURCE = "Handschriftenforschung und ihr Weg ins Digitale";
  private static final String LINK = "https://example.org/forschungsdokumentation/1";
  private static final ExterneRessource EXTERNE_RESSOURCE =
      new ExterneRessource(ExterneRessourceTyp.HSC, "https://example.org/externe-ressource/1");

  private TEI tei;

  @BeforeEach
  void setUp() throws IOException {
    tei = TEIDokumentIO.unmarshal(Files.readString(TEI_FILE_PATH, StandardCharsets.UTF_8));
  }

  @Test
  void shouldAddForschungsdokumentationWithLink() {
    assertTrue(ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation(RESSOURCE, LINK)));

    Ref ref = refs().getFirst();
    assertEquals(List.of(LINK), ref.getTargets());
    assertEquals(List.of(RESSOURCE), ref.getContent());
  }

  @Test
  void shouldAddForschungsdokumentationWithoutLink() {
    assertTrue(ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation(RESSOURCE, null)));

    assertEquals(List.of(RESSOURCE), bibls().getFirst().getContent());
  }

  @Test
  void shouldRemoveForschungsdokumentationWithLink() {
    Forschungsdokumentation documentation = new Forschungsdokumentation(RESSOURCE, LINK);
    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(tei, documentation);
    String before = TEIDokumentIO.marshal(tei);

    assertTrue(ForschungsdokumentationListBiblMapper.removeForschungsdokumentation(
        tei, documentation));
    assertTrue(ForschungsdokumentationListBiblMapper.readForschungsdokumentationen(tei).isEmpty());
    assertNotEquals(before, TEIDokumentIO.marshal(tei));
  }

  @Test
  void shouldRemoveForschungsdokumentationWithoutLink() {
    Forschungsdokumentation documentation = new Forschungsdokumentation(RESSOURCE, null);
    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(tei, documentation);

    assertTrue(ForschungsdokumentationListBiblMapper.removeForschungsdokumentation(
        tei, documentation));
    assertTrue(ForschungsdokumentationListBiblMapper.readForschungsdokumentationen(tei).isEmpty());
  }

  @Test
  void shouldRemoveOnlyMatchingForschungsdokumentation() {
    Forschungsdokumentation first = new Forschungsdokumentation(RESSOURCE, LINK);
    Forschungsdokumentation second = new Forschungsdokumentation(
        "Weitere Forschung", "https://example.org/other");
    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(tei, first);
    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(tei, second);

    assertTrue(ForschungsdokumentationListBiblMapper.removeForschungsdokumentation(tei, first));
    assertEquals(List.of(second),
        ForschungsdokumentationListBiblMapper.readForschungsdokumentationen(tei));
  }

  @Test
  void shouldRemoveEmptyBibliographyButKeepRootWithSiblingList() {
    Forschungsdokumentation documentation = new Forschungsdokumentation(RESSOURCE, null);
    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(tei, documentation);
    ListBibl root = additional().getListBibl();

    assertTrue(ForschungsdokumentationListBiblMapper.removeForschungsdokumentation(
        tei, documentation));
    assertTrue(KODListBiblMapper.findChildListBiblByType(
        root, TEIValues.LIST_BIBL_TYPE_BIBLIOGRAPHY).isEmpty());
    assertTrue(KODListBiblMapper.findChildListBiblByType(
        root, TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS).isPresent());
    assertEquals(root, additional().getListBibl());
  }

  @Test
  void shouldRemoveRootWhenLastBibliographyIsRemoved() {
    ListBibl root = additional().getListBibl();
    root.getMilestonesAndGbsAndPbs().clear();
    Forschungsdokumentation documentation = new Forschungsdokumentation(RESSOURCE, null);
    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(tei, documentation);

    assertTrue(ForschungsdokumentationListBiblMapper.removeForschungsdokumentation(
        tei, documentation));
    assertNull(additional().getListBibl());
  }

  @Test
  void shouldLeaveTeiUnchangedWhenForschungsdokumentationIsAbsent() {
    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei, new Forschungsdokumentation(RESSOURCE, LINK));
    String before = TEIDokumentIO.marshal(tei);

    assertFalse(ForschungsdokumentationListBiblMapper.removeForschungsdokumentation(
        tei, new Forschungsdokumentation("Unknown", "https://example.org/unknown")));
    assertEquals(before, TEIDokumentIO.marshal(tei));
  }

  @Test
  void shouldRejectDuplicateLinkAndLeaveDocumentUnchanged() {
    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation(RESSOURCE, LINK));
    String before = TEIDokumentIO.marshal(tei);

    assertFalse(ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation("Anderer Text", LINK)));

    assertEquals(before, TEIDokumentIO.marshal(tei));
  }

  @Test
  void shouldAllowSeveralEntriesWithoutLink() {
    assertTrue(ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation(RESSOURCE, null)));
    assertTrue(ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation("Weitere Dokumentation", null)));

    assertEquals(2, bibls().size());
  }

  @Test
  void shouldRejectDuplicateNormalizedRessourceWhenBothLinksAreMissing() {
    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation(RESSOURCE, null));
    String before = TEIDokumentIO.marshal(tei);

    assertFalse(ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation(
            "  Handschriftenforschung  und ihr Weg\nins Digitale  ",
            null)));

    assertEquals(before, TEIDokumentIO.marshal(tei));
  }

  @Test
  void shouldAllowSameRessourceWhenLinksAreDifferent() {
    assertTrue(ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation(RESSOURCE, LINK)));
    assertTrue(ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation(
            RESSOURCE,
            "https://example.org/forschungsdokumentation/2")));

    assertEquals(2, bibls().size());
  }

  @Test
  void shouldAllowSameRessourceWithAndWithoutLink() {
    assertTrue(ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation(RESSOURCE, LINK)));
    assertTrue(ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation(RESSOURCE, null)));

    assertEquals(2, bibls().size());
  }

  @Test
  void shouldPreserveExterneRessourcenWhenAddingForschungsdokumentation() {
    ExterneRessourcenListBiblMapper.addRessource(tei, EXTERNE_RESSOURCE);

    assertTrue(ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation(RESSOURCE, LINK)));

    assertEquals(
        List.of(EXTERNE_RESSOURCE),
        ExterneRessourcenListBiblMapper.readRessourcen(tei));
    ListBibl root = additional().getListBibl();
    assertTrue(KODListBiblMapper.findChildListBiblByType(
        root,
        TEIValues.LIST_BIBL_TYPE_RESSOURCES).isPresent());
    assertTrue(KODListBiblMapper.findChildListBiblByType(
        root,
        TEIValues.LIST_BIBL_TYPE_BIBLIOGRAPHY).isPresent());
  }

  @Test
  void shouldPreserveForeignContentWhenAdding() {
    ListBibl bibliography = bibliography();
    Ref foreignRef = new Ref();
    foreignRef.getTargets().add("https://example.org/foreign");
    foreignRef.getContent().add("Fremder Inhalt");
    Bibl foreignBibl = new Bibl();
    foreignBibl.getContent().add(foreignRef);
    bibliography.getMilestonesAndGbsAndPbs().add(foreignBibl);

    assertTrue(ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation(RESSOURCE, LINK)));

    assertTrue(bibliography.getMilestonesAndGbsAndPbs().contains(foreignBibl));
    assertEquals("Fremder Inhalt", foreignRef.getContent().getFirst());
  }

  @Test
  void shouldMarkExactlyOneBiblAsDefault() {
    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation(RESSOURCE, LINK));
    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation("Weitere Dokumentation", null));

    assertEquals(1, defaultMarkedBibls().size());
    assertEquals(TEIValues.DEFAULT_TRUE, bibls().getFirst().getDefault());
  }

  @Test
  void shouldKeepExistingDefaultMarkerWhenAdding() {
    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation(RESSOURCE, LINK));
    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation("Zweite Dokumentation", null));

    bibls().getFirst().setDefault(null);
    bibls().get(1).setDefault(TEIValues.DEFAULT_TRUE);

    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation("Dritte Dokumentation", null));

    assertEquals(1, defaultMarkedBibls().size());
    assertNotEquals(TEIValues.DEFAULT_TRUE, bibls().getFirst().getDefault());
    assertEquals(TEIValues.DEFAULT_TRUE, bibls().get(1).getDefault());
  }

  @Test
  void shouldKeepDescriptionsAsDefaultChildListBibl() {
    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(
        tei,
        new Forschungsdokumentation(RESSOURCE, LINK));

    ListBibl root = additional().getListBibl();
    ListBibl descriptions = KODListBiblMapper.findChildListBiblByType(
        root,
        TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS).orElseThrow();

    assertEquals(TEIValues.DEFAULT_TRUE, descriptions.getDefault());
    assertNotEquals(TEIValues.DEFAULT_TRUE, bibliography().getDefault());
  }

  @Test
  void shouldReadMixedEntriesInDocumentOrderWithoutChangingXml() {
    Forschungsdokumentation first = new Forschungsdokumentation("Z Forschung", LINK);
    Forschungsdokumentation second = new Forschungsdokumentation("A Forschung", null);
    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(tei, first);
    ForschungsdokumentationListBiblMapper.addForschungsdokumentation(tei, second);
    ExterneRessourcenListBiblMapper.addRessource(tei, EXTERNE_RESSOURCE);
    String before = TEIDokumentIO.marshal(tei);

    assertEquals(List.of(first, second),
        ForschungsdokumentationListBiblMapper.readForschungsdokumentationen(tei));
    assertEquals(before, TEIDokumentIO.marshal(tei));
  }

  @Test
  void shouldReadDirectTextAndNormalizeWhitespace() {
    Bibl bibl = new Bibl();
    bibl.getContent().add("  Forschung  ");
    bibl.getContent().add("\n Digital  ");
    bibliography().getMilestonesAndGbsAndPbs().add(bibl);

    assertEquals(List.of(new Forschungsdokumentation("Forschung Digital", null)),
        ForschungsdokumentationListBiblMapper.readForschungsdokumentationen(tei));
  }

  @Test
  void shouldReadHttpLinkAndPreserveSpecialCharactersAndDuplicateEntries() {
    Ref ref = new Ref();
    ref.getContent().add("Forschung <Digital> & Handschriften");
    ref.getTargets().add("http://example.org/research");
    Bibl bibl = new Bibl();
    bibl.getContent().add(ref);
    bibliography().getMilestonesAndGbsAndPbs().add(bibl);
    bibliography().getMilestonesAndGbsAndPbs().add(bibl);
    Forschungsdokumentation expected = new Forschungsdokumentation(
        "Forschung <Digital> & Handschriften", "http://example.org/research");

    assertEquals(List.of(expected, expected),
        ForschungsdokumentationListBiblMapper.readForschungsdokumentationen(tei));
  }

  @Test
  void shouldIgnoreEmptyEntriesAndNestedBibliography() {
    Bibl empty = new Bibl();
    empty.getContent().add("   ");
    ListBibl nested = new ListBibl();
    Bibl nestedBibl = new Bibl();
    nestedBibl.getContent().add("Nested resource");
    nested.getMilestonesAndGbsAndPbs().add(nestedBibl);
    bibliography().getMilestonesAndGbsAndPbs().addAll(List.of(empty, nested));
    String before = TEIDokumentIO.marshal(tei);

    assertEquals(List.of(),
        ForschungsdokumentationListBiblMapper.readForschungsdokumentationen(tei));
    assertEquals(before, TEIDokumentIO.marshal(tei));
  }

  @Test
  void shouldReturnEmptyWhenBibliographyIsMissingWithoutChangingXml() {
    ExterneRessourcenListBiblMapper.addRessource(tei, EXTERNE_RESSOURCE);
    String before = TEIDokumentIO.marshal(tei);

    assertEquals(List.of(),
        ForschungsdokumentationListBiblMapper.readForschungsdokumentationen(tei));
    assertEquals(before, TEIDokumentIO.marshal(tei));
  }

  @Test
  void shouldReturnEmptyWhenRootIsMissingWithoutCreatingIt() {
    additional().setListBibl(null);
    String before = TEIDokumentIO.marshal(tei);

    assertEquals(List.of(),
        ForschungsdokumentationListBiblMapper.readForschungsdokumentationen(tei));
    assertEquals(before, TEIDokumentIO.marshal(tei));
  }

  @Test
  void shouldThrowWhenAdditionalIsMissing() {
    TEICommandException exception = assertThrows(TEICommandException.class,
        () -> ForschungsdokumentationListBiblMapper.readForschungsdokumentationen(new TEI()));

    assertEquals("Missing additional element in TEI-XML", exception.getMessage());
  }

  @Test
  void shouldRejectInvalidLinkInsteadOfTreatingItAsMissing() {
    Ref ref = new Ref();
    ref.getContent().add(RESSOURCE);
    ref.getTargets().add("invalid-link");
    Bibl bibl = new Bibl();
    bibl.getContent().add(ref);
    bibliography().getMilestonesAndGbsAndPbs().add(bibl);
    String before = TEIDokumentIO.marshal(tei);

    assertThrows(Forschungsdokumentation.InvalidLinkException.class,
        () -> ForschungsdokumentationListBiblMapper.readForschungsdokumentationen(tei));
    assertEquals(before, TEIDokumentIO.marshal(tei));
  }

  private List<Ref> refs() {
    return bibls().stream()
        .flatMap(bibl -> bibl.getContent().stream())
        .filter(Ref.class::isInstance)
        .map(Ref.class::cast)
        .toList();
  }

  private List<Bibl> bibls() {
    return bibliography().getMilestonesAndGbsAndPbs().stream()
        .filter(Bibl.class::isInstance)
        .map(Bibl.class::cast)
        .toList();
  }

  private List<Bibl> defaultMarkedBibls() {
    return bibls().stream()
        .filter(bibl -> TEIValues.DEFAULT_TRUE.equals(bibl.getDefault()))
        .toList();
  }

  private ListBibl bibliography() {
    return KODListBiblMapper.getChildListBibl(
        KODListBiblMapper.getRootListBibl(additional()),
        TEIValues.LIST_BIBL_TYPE_BIBLIOGRAPHY);
  }

  private Additional additional() {
    try {
      return KODListBiblMapper.getRequiredAdditional(tei);
    } catch (Exception exception) {
      throw new AssertionError(exception);
    }
  }
}
