package de.staatsbibliothek.berlin.hsp.mapper.tei.kod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessource;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessourceTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.mapper.tei.common.TEIDokumentIO;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.BiblFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Additional;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.BiblStruct;
import org.tei_c.ns._1.ListBibl;
import org.tei_c.ns._1.Ref;
import org.tei_c.ns._1.TEI;

class ExterneRessourcenListBiblMapperTest {

  private static final Path TEI_FILE_PATH =
      Paths.get("src", "test", "resources", "tei", "tei-HSP-kodms1.xml");

  private static final ExterneRessource HSC =
      new ExterneRessource(ExterneRessourceTyp.HSC, "https://hsc.example.org/objekt/1");
  private static final ExterneRessource HSC_SECOND =
      new ExterneRessource(ExterneRessourceTyp.HSC, "https://hsc.example.org/objekt/2");
  private static final ExterneRessource ECODICES =
      new ExterneRessource(ExterneRessourceTyp.ECODICES, "https://e-codices.example.org/objekt/1");

  private TEI tei;

  private static Bibl bibl(String type, String target, String label) {
    Bibl bibl = new Bibl();
    bibl.setType(type);

    Ref ref = new Ref();
    ref.getTargets().add(target);
    ref.getContent().add(label);
    bibl.getContent().add(ref);

    return bibl;
  }

  // --- add ------------------------------------------------------------------

  private static Ref ref(String target, String label) {
    Ref ref = new Ref();
    ref.getTargets().add(target);
    ref.getContent().add(label);

    return ref;
  }

  @BeforeEach
  void setUp() throws IOException {
    tei = TEIDokumentIO.unmarshal(Files.readString(TEI_FILE_PATH, StandardCharsets.UTF_8));
  }

  @Test
  void shouldAddRessource() {
    assertTrue(ExterneRessourcenListBiblMapper.addRessource(tei, HSC));

    assertEquals(List.of(HSC), ExterneRessourcenListBiblMapper.readRessourcen(tei));
  }

  @Test
  void shouldRejectDuplicateAndLeaveDocumentUnchanged() {
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);
    String before = TEIDokumentIO.marshal(tei);

    assertFalse(ExterneRessourcenListBiblMapper.addRessource(tei, HSC));

    assertEquals(before, TEIDokumentIO.marshal(tei));
  }

  @Test
  void shouldRejectDuplicateUrlWithDifferentTypeAndLeaveDocumentUnchanged() {
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);
    String before = TEIDokumentIO.marshal(tei);
    ExterneRessource sameUrlWithDifferentType =
        new ExterneRessource(ExterneRessourceTyp.ECODICES, HSC.url());

    assertFalse(
        ExterneRessourcenListBiblMapper.addRessource(tei, sameUrlWithDifferentType));

    assertEquals(before, TEIDokumentIO.marshal(tei));
  }

  // --- the data-loss regression --------------------------------------------

  @Test
  void shouldKeepSeveralUrlsPerTyp() {
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC_SECOND);

    assertEquals(List.of(HSC, HSC_SECOND), ExterneRessourcenListBiblMapper.readRessourcen(tei));
  }

  @Test
  void shouldAppendInDocumentOrder() {
    ExterneRessourcenListBiblMapper.addRessource(tei, ECODICES);
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);

    assertEquals(List.of(ECODICES, HSC), ExterneRessourcenListBiblMapper.readRessourcen(tei));
  }

  @Test
  void shouldPreserveForeignContentWhenAdding() {
    seedForeignContent();

    assertTrue(ExterneRessourcenListBiblMapper.addRessource(tei, HSC));

    assertForeignContentIsIntact();
  }

  @Test
  void shouldPreserveForeignContentWhenRemoving() {
    seedForeignContent();
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);

    assertTrue(ExterneRessourcenListBiblMapper.removeRessource(tei, HSC));

    assertForeignContentIsIntact();
  }

  // --- remove ---------------------------------------------------------------

  @Test
  void shouldPreserveHandwrittenRefLabelOfAnUnrelatedRessource() {
    ListBibl ressourcen = ressourcenListBibl();
    ressourcen.getMilestonesAndGbsAndPbs()
        .add(bibl(ExterneRessourceTyp.KDIH.getTeiType(),
            "https://kdih.example.org/objekt/9", "Eigenes Label"));

    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);

    assertTrue(TEIDokumentIO.marshal(tei).contains(">Eigenes Label</ref>"),
        TEIDokumentIO.marshal(tei));
  }

  @Test
  void shouldNotPruneRessourcenListBiblWhileForeignContentRemains() {
    seedForeignContent();
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);

    ExterneRessourcenListBiblMapper.removeRessource(tei, HSC);

    assertTrue(findRessourcen().isPresent(),
        "listBibl must survive as long as it holds foreign content");
  }

  @Test
  void shouldRemoveRessource() {
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);
    ExterneRessourcenListBiblMapper.addRessource(tei, ECODICES);

    assertTrue(ExterneRessourcenListBiblMapper.removeRessource(tei, HSC));

    assertEquals(List.of(ECODICES), ExterneRessourcenListBiblMapper.readRessourcen(tei));
  }

  // --- default marker -------------------------------------------------------

  @Test
  void shouldReturnFalseWhenRemovingAbsentRessource() {
    String before = TEIDokumentIO.marshal(tei);

    assertFalse(ExterneRessourcenListBiblMapper.removeRessource(tei, HSC));

    assertEquals(before, TEIDokumentIO.marshal(tei));
  }

  @Test
  void shouldPruneRessourcenListBiblWhenLastRessourceIsRemoved() {
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);

    ExterneRessourcenListBiblMapper.removeRessource(tei, HSC);

    assertTrue(findRessourcen().isEmpty());
  }

  @Test
  void shouldMarkExactlyOneBiblAsDefault() {
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC_SECOND);
    ExterneRessourcenListBiblMapper.addRessource(tei, ECODICES);

    assertEquals(1, defaultMarkedBibls().size());
    assertEquals(TEIValues.DEFAULT_TRUE, bibls().getFirst().getDefault());
  }

  @Test
  void shouldKeepAnExistingDefaultMarkerWhenAdding() {
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC_SECOND);

    bibls().getFirst().setDefault(null);
    bibls().get(1).setDefault(TEIValues.DEFAULT_TRUE);

    ExterneRessourcenListBiblMapper.addRessource(tei, ECODICES);

    assertEquals(1, defaultMarkedBibls().size());
    assertEquals(TEIValues.DEFAULT_TRUE, bibls().get(1).getDefault());
    assertNotEquals(TEIValues.DEFAULT_TRUE, bibls().getFirst().getDefault());
  }

  // --- read -----------------------------------------------------------------

  @Test
  void shouldOmitTheDefaultAttributeOnUnmarkedBibls() {
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC_SECOND);

    bibls().getFirst().setDefault(null);
    bibls().get(1).setDefault(TEIValues.DEFAULT_TRUE);
    ExterneRessourcenListBiblMapper.addRessource(tei, ECODICES);

    // Clearing must drop the attribute entirely, not write default="false".
    // Bibl.getDefault() falls back to "false" when absent, so this can only be seen in the XML.
    assertFalse(TEIDokumentIO.marshal(tei).contains("default=\"false\""),
        TEIDokumentIO.marshal(tei));
  }

  @Test
  void shouldReassignDefaultMarkerWhenTheMarkedRessourceIsRemoved() {
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);
    ExterneRessourcenListBiblMapper.addRessource(tei, ECODICES);

    ExterneRessourcenListBiblMapper.removeRessource(tei, HSC);

    assertEquals(1, defaultMarkedBibls().size());
    assertEquals(TEIValues.DEFAULT_TRUE, bibls().getFirst().getDefault());
  }

  // --- listBibl lifecycle ---------------------------------------------------

  @Test
  void shouldReturnEmptyListWhenTeiHasNoRessourcen() {
    assertTrue(ExterneRessourcenListBiblMapper.readRessourcen(tei).isEmpty());
  }

  @Test
  void shouldSkipUnknownTypAndInvalidTargetWhenReading() {
    ListBibl ressourcen = ressourcenListBibl();
    ressourcen.getMilestonesAndGbsAndPbs()
        .add(bibl("unbekannt", "https://unbekannt.example.org/objekt/1", "Unbekannt"));
    ressourcen.getMilestonesAndGbsAndPbs()
        .add(bibl(ExterneRessourceTyp.QALAMOS.getTeiType(), "kein-url", "Qalamos"));

    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);

    assertEquals(List.of(HSC), ExterneRessourcenListBiblMapper.readRessourcen(tei));
  }

  // --- helpers --------------------------------------------------------------

  @Test
  void shouldMigrateLegacyFlatListBiblWhenAdding() {
    ListBibl legacy = new ListBibl();
    legacy.getMilestonesAndGbsAndPbs().add(BiblFactory.build("HSP-legacy-description-1"));
    legacy.getMilestonesAndGbsAndPbs().add(BiblFactory.build("HSP-legacy-description-2"));
    additional().setListBibl(legacy);

    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);

    ListBibl root = additional().getListBibl();
    assertEquals(TEIValues.LIST_BIBL_TYPE_ROOT, root.getType());
    assertTrue(root.getMilestonesAndGbsAndPbs().stream().noneMatch(Bibl.class::isInstance),
        "legacy bibls must be moved into the descriptions listBibl");

    ListBibl descriptions = KODListBiblMapper
        .findChildListBiblByType(root, TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS)
        .orElseThrow();

    assertEquals(2, descriptions.getMilestonesAndGbsAndPbs().stream()
        .filter(Bibl.class::isInstance)
        .count());
    assertEquals(List.of(HSC), ExterneRessourcenListBiblMapper.readRessourcen(tei));
  }

  // --- one bibl carrying several targets ------------------------------------

  @Test
  void shouldKeepDescriptionsWhenLastRessourceIsRemoved() {
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);

    ExterneRessourcenListBiblMapper.removeRessource(tei, HSC);

    ListBibl root = additional().getListBibl();
    assertTrue(KODListBiblMapper
            .findChildListBiblByType(root, TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS).isPresent(),
        "removing the last ressource must not touch the descriptions");
  }

  @Test
  void shouldRemoveOnlyTheTargetedRefFromABiblWithSeveralRefs() {
    Bibl shared = bibl(ExterneRessourceTyp.HSC.getTeiType(), HSC.url(), "HSC");
    shared.getContent().add(ref(HSC_SECOND.url(), "HSC"));
    ressourcenListBibl().getMilestonesAndGbsAndPbs().add(shared);

    assertEquals(List.of(HSC, HSC_SECOND),
        ExterneRessourcenListBiblMapper.readRessourcen(tei),
        "precondition: one bibl with two refs reads as two ressourcen");

    assertTrue(ExterneRessourcenListBiblMapper.removeRessource(tei, HSC));

    assertEquals(List.of(HSC_SECOND),
        ExterneRessourcenListBiblMapper.readRessourcen(tei),
        "removing one ref must not delete its sibling ref");
  }

  @Test
  void shouldRemoveOnlyTheTargetedTargetFromAMultiTargetRef() {
    Bibl multiTarget = new Bibl();
    multiTarget.setType(ExterneRessourceTyp.HSC.getTeiType());
    Ref shared = ref(HSC.url(), "HSC");
    shared.getTargets().add(HSC_SECOND.url());
    multiTarget.getContent().add(shared);
    ressourcenListBibl().getMilestonesAndGbsAndPbs().add(multiTarget);

    assertEquals(List.of(HSC, HSC_SECOND),
        ExterneRessourcenListBiblMapper.readRessourcen(tei),
        "precondition: a ref with two targets reads as two ressourcen");

    assertTrue(ExterneRessourcenListBiblMapper.removeRessource(tei, HSC));

    assertEquals(List.of(HSC_SECOND),
        ExterneRessourcenListBiblMapper.readRessourcen(tei),
        "removing one target must not delete the others of the same ref");
  }

  @Test
  void shouldDropTheBiblOnceItsLastRefIsRemoved() {
    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);

    assertTrue(ExterneRessourcenListBiblMapper.removeRessource(tei, HSC));

    assertTrue(findRessourcen().isEmpty(),
        "a bibl without any ref must not survive as an empty shell");
  }

  // --- schematron listBibl-is-declarable (tei-odd@4.9.0) --------------------

  @Test
  void shouldMarkExactlyOneChildListBiblAsDefaultWhenAddingRessourcenSibling() {
    ListBibl root = KODListBiblMapper.getRootListBibl(additional());
    KODListBiblMapper.getChildListBibl(root, TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS)
        .setDefault(null);

    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);

    assertEquals(1, defaultMarkedChildListBibls().size(),
        "exactly one child listBibl must carry default=\"true\"");
    assertEquals(TEIValues.DEFAULT_TRUE,
        KODListBiblMapper.findChildListBiblByType(
                additional().getListBibl(), TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS)
            .orElseThrow().getDefault(),
        "descriptions is the preferred default");
  }

  @Test
  void shouldNotMoveAnExistingChildListBiblDefaultMarker() {
    ListBibl root = KODListBiblMapper.getRootListBibl(additional());
    KODListBiblMapper.getChildListBibl(root, TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS);

    ExterneRessourcenListBiblMapper.addRessource(tei, HSC);

    assertEquals(1, defaultMarkedChildListBibls().size());
    assertNotEquals(TEIValues.DEFAULT_TRUE,
        findRessourcen().orElseThrow().getDefault(),
        "resources must not become the default listBibl");
  }

  private List<ListBibl> defaultMarkedChildListBibls() {
    return additional().getListBibl().getMilestonesAndGbsAndPbs().stream()
        .filter(ListBibl.class::isInstance)
        .map(ListBibl.class::cast)
        .filter(child -> TEIValues.DEFAULT_TRUE.equals(child.getDefault()))
        .toList();
  }

  private void seedForeignContent() {
    ListBibl ressourcen = ressourcenListBibl();
    ressourcen.getMilestonesAndGbsAndPbs()
        .add(bibl("unbekannt", "https://unbekannt.example.org/objekt/1", "Unbekannt"));
    ressourcen.getMilestonesAndGbsAndPbs().add(new BiblStruct());
  }

  private void assertForeignContentIsIntact() {
    ListBibl ressourcen = findRessourcen().orElseThrow();

    assertTrue(ressourcen.getMilestonesAndGbsAndPbs().stream()
            .anyMatch(BiblStruct.class::isInstance),
        "biblStruct must survive a modify cycle");

    assertTrue(bibls().stream().anyMatch(bibl -> "unbekannt".equals(bibl.getType())),
        "bibl with an unknown type must survive a modify cycle");
  }

  private ListBibl ressourcenListBibl() {
    return KODListBiblMapper.getChildListBibl(
        KODListBiblMapper.getRootListBibl(additional()),
        TEIValues.LIST_BIBL_TYPE_RESSOURCES);
  }

  private Optional<ListBibl> findRessourcen() {
    ListBibl root = additional().getListBibl();

    return root == null
        ? Optional.empty()
        : KODListBiblMapper.findChildListBiblByType(root, TEIValues.LIST_BIBL_TYPE_RESSOURCES);
  }

  private Additional additional() {
    try {
      return KODListBiblMapper.getRequiredAdditional(tei);
    } catch (Exception exception) {
      throw new IllegalStateException(exception);
    }
  }

  private List<Bibl> bibls() {
    return findRessourcen().orElseThrow().getMilestonesAndGbsAndPbs().stream()
        .filter(Bibl.class::isInstance)
        .map(Bibl.class::cast)
        .toList();
  }

  private List<Bibl> defaultMarkedBibls() {
    return bibls().stream()
        .filter(bibl -> TEIValues.DEFAULT_TRUE.equals(bibl.getDefault()))
        .toList();
  }
}
