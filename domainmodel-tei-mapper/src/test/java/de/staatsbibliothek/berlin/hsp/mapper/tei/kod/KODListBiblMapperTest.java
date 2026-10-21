package de.staatsbibliothek.berlin.hsp.mapper.tei.kod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.BiblFactory;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Additional;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.ListBibl;
import org.tei_c.ns._1.TEI;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 22.07.2026
 */
class KODListBiblMapperTest {

  private static final String HSC_URL = "https://handschriftencensus.de/123";
  private static final String ECODICES_URL = "https://e-codices.ch/abc";
  private static final String HSC_SECOND_URL = "https://handschriftencensus.de/456";

  private TEI kodTei;

  private static List<ListBibl> defaultMarkedChildren(ListBibl root) {
    return root.getMilestonesAndGbsAndPbs().stream()
        .filter(ListBibl.class::isInstance)
        .map(ListBibl.class::cast)
        .filter(child -> TEIValues.DEFAULT_TRUE.equals(child.getDefault()))
        .toList();
  }

  @BeforeEach
  void setUp() throws Exception {

    Path teiFilePath = Paths.get(
        "src",
        "test",
        "resources",
        "tei",
        "tei-HSP-kodms1.xml");

    try (InputStream inputStream = Files.newInputStream(teiFilePath)) {
      kodTei = TEIObjectFactory.unmarshal(inputStream).getFirst();
    }
  }

  @Test
  void shouldKeepExactlyOneDefaultChildWhenDescriptionsAreRemoved() throws Exception {

    Additional additional = KODListBiblMapper.getRequiredAdditional(kodTei);
    ListBibl root = KODListBiblMapper.getRootListBibl(additional);

    KODListBiblMapper.getChildListBibl(root, TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS);
    KODListBiblMapper.getChildListBibl(root, TEIValues.LIST_BIBL_TYPE_RESSOURCES);

    ListBibl bibliography = new ListBibl();
    bibliography.setType("bibliography");
    root.getMilestonesAndGbsAndPbs().add(bibliography);

    KODListBiblMapper.writeDescriptions(kodTei, Set.of());

    assertEquals(1, defaultMarkedChildren(root).size(),
        "removing descriptions must leave exactly one default among the remaining children");
  }

  @Test
  void testWriteDescriptions() throws Exception {

    KODListBiblMapper.writeDescriptions(kodTei,
        Set.of("HSP-description-1", "HSP-description-2"));

    Additional additional = TEICommon.findFirst(Additional.class, kodTei)
        .orElseThrow();

    ListBibl root = additional.getListBibl();

    assertNotNull(root);
    assertEquals(TEIValues.LIST_BIBL_TYPE_ROOT, root.getType());

    ListBibl descriptions = root.getMilestonesAndGbsAndPbs().stream()
        .filter(ListBibl.class::isInstance)
        .map(ListBibl.class::cast)
        .filter(listBibl ->
            TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS.equals(listBibl.getType()))
        .findFirst()
        .orElseThrow();

    assertEquals("true", descriptions.getDefault());

    List<Bibl> bibls = descriptions.getMilestonesAndGbsAndPbs().stream()
        .filter(Bibl.class::isInstance)
        .map(Bibl.class::cast)
        .toList();

    assertEquals(2, bibls.size());

    assertTrue(bibls.stream()
        .anyMatch(bibl -> "HSP-description-1".equals(bibl.getId())));

    assertTrue(bibls.stream()
        .anyMatch(bibl -> "HSP-description-2".equals(bibl.getId())));

    long defaultCount = bibls.stream()
        .filter(bibl -> "true".equals(bibl.getDefault()))
        .count();

    assertEquals(1, defaultCount);
  }


  @Test
  void findRootListBiblShouldToleratePartialInput() {
    assertTrue(KODListBiblMapper.findRootListBibl(null).isEmpty());
    assertTrue(KODListBiblMapper.findRootListBibl(new Additional()).isEmpty());
  }

  @Test
  void getRootListBiblShouldCreateAndAttachWhenMissing() {
    Additional additional = new Additional();

    ListBibl root = KODListBiblMapper.getRootListBibl(additional);

    assertNotNull(root);
    assertEquals(TEIValues.LIST_BIBL_TYPE_ROOT, root.getType());
    assertEquals(root, additional.getListBibl());
    assertEquals(root, KODListBiblMapper.getRootListBibl(additional), "must be idempotent");
  }

  @Test
  void getListBiblShouldRejectNullInsteadOfFailingWithANullPointerLater() {
    NullPointerException additionalNpe = assertThrows(NullPointerException.class,
        () -> KODListBiblMapper.getRootListBibl(null));
    assertEquals("additional is required", additionalNpe.getMessage());

    NullPointerException rootNpe = assertThrows(NullPointerException.class,
        () -> KODListBiblMapper.getChildListBibl(null, TEIValues.LIST_BIBL_TYPE_RESSOURCES));
    assertEquals("root is required", rootNpe.getMessage());
  }

  // --- getRequiredAdditional ------------------------------------------------

  @Test
  void getRequiredAdditionalShouldFailForATeiWithoutAdditional() throws Exception {
    assertThrows(IllegalArgumentException.class,
        () -> KODListBiblMapper.getRequiredAdditional(new TEI()));

    assertNotNull(KODListBiblMapper.getRequiredAdditional(kodTei));
  }

  // --- findRootListBibl / getRootListBibl -----------------------------------

  @Test
  void findRootListBiblShouldFindAnAttachedRoot() {
    Additional additional = new Additional();
    ListBibl root = new ListBibl();
    additional.setListBibl(root);

    assertEquals(root, KODListBiblMapper.findRootListBibl(additional).orElseThrow());
  }

  @Test
  void findRootListBiblShouldNotCreateAnything() {
    Additional additional = new Additional();

    KODListBiblMapper.findRootListBibl(additional);

    assertNull(additional.getListBibl(), "a find must never modify the document");
  }

  @Test
  void getRootListBiblShouldNormalizeTheTypeOfAnExistingRoot() {
    Additional additional = new Additional();
    ListBibl root = new ListBibl();
    root.setType("etwas-anderes");
    additional.setListBibl(root);

    assertEquals(root, KODListBiblMapper.getRootListBibl(additional));
    assertEquals(TEIValues.LIST_BIBL_TYPE_ROOT, root.getType());
  }

  @Test
  void getRootListBiblShouldMigrateLegacyFlatDescriptions() {
    Additional additional = new Additional();
    ListBibl legacy = new ListBibl();
    legacy.getMilestonesAndGbsAndPbs().add(BiblFactory.build("HSP-legacy-1"));
    additional.setListBibl(legacy);

    ListBibl root = KODListBiblMapper.getRootListBibl(additional);

    assertTrue(root.getMilestonesAndGbsAndPbs().stream().noneMatch(Bibl.class::isInstance));

    ListBibl descriptions = KODListBiblMapper
        .findChildListBiblByType(root, TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS).orElseThrow();

    assertEquals(1, descriptions.getMilestonesAndGbsAndPbs().stream()
        .filter(Bibl.class::isInstance).count());
  }

  // --- findChildListBiblByType / getChildListBibl ---------------------------

  @Test
  void findChildListBiblByTypeShouldTolerateNullAndMissingChildren() {
    assertTrue(KODListBiblMapper
        .findChildListBiblByType(null, TEIValues.LIST_BIBL_TYPE_RESSOURCES).isEmpty());

    assertTrue(KODListBiblMapper
        .findChildListBiblByType(new ListBibl(), TEIValues.LIST_BIBL_TYPE_RESSOURCES).isEmpty());
  }

  @Test
  void findChildListBiblByTypeShouldNotMatchAnotherType() {
    ListBibl root = new ListBibl();
    KODListBiblMapper.getChildListBibl(root, TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS);

    assertTrue(KODListBiblMapper
        .findChildListBiblByType(root, TEIValues.LIST_BIBL_TYPE_RESSOURCES).isEmpty());
  }

  @Test
  void getChildListBiblShouldReturnTheExistingChildInsteadOfAddingASecondOne() {
    ListBibl root = new ListBibl();

    ListBibl first = KODListBiblMapper.getChildListBibl(root, TEIValues.LIST_BIBL_TYPE_RESSOURCES);
    ListBibl second = KODListBiblMapper.getChildListBibl(root, TEIValues.LIST_BIBL_TYPE_RESSOURCES);

    assertEquals(first, second);
    assertEquals(1, root.getMilestonesAndGbsAndPbs().size());
  }

  @Test
  void getChildListBiblShouldMarkOnlyDescriptionsAsDefault() {
    ListBibl root = new ListBibl();

    assertEquals(TEIValues.DEFAULT_TRUE,
        KODListBiblMapper.getChildListBibl(root, TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS)
            .getDefault());

    assertNotEquals(TEIValues.DEFAULT_TRUE,
        KODListBiblMapper.getChildListBibl(root, TEIValues.LIST_BIBL_TYPE_RESSOURCES)
            .getDefault());
  }

  // --- removeChildListBiblByType / removeRootListBiblIfEmpty ----------------

  @Test
  void removeChildListBiblByTypeShouldOnlyRemoveTheRequestedType() {
    ListBibl root = new ListBibl();
    KODListBiblMapper.getChildListBibl(root, TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS);
    KODListBiblMapper.getChildListBibl(root, TEIValues.LIST_BIBL_TYPE_RESSOURCES);

    KODListBiblMapper.removeChildListBiblByType(root, TEIValues.LIST_BIBL_TYPE_RESSOURCES);

    assertTrue(KODListBiblMapper
        .findChildListBiblByType(root, TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS).isPresent());
    assertTrue(KODListBiblMapper
        .findChildListBiblByType(root, TEIValues.LIST_BIBL_TYPE_RESSOURCES).isEmpty());
  }

  @Test
  void removeChildListBiblByTypeShouldBeANoOpWhenTheTypeIsAbsent() {
    ListBibl root = new ListBibl();
    KODListBiblMapper.getChildListBibl(root, TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS);

    KODListBiblMapper.removeChildListBiblByType(root, TEIValues.LIST_BIBL_TYPE_RESSOURCES);

    assertEquals(1, root.getMilestonesAndGbsAndPbs().size());
  }

  @Test
  void removeRootListBiblIfEmptyShouldKeepARootThatStillHasChildren() {
    Additional additional = new Additional();
    ListBibl root = KODListBiblMapper.getRootListBibl(additional);
    KODListBiblMapper.getChildListBibl(root, TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS);

    KODListBiblMapper.removeRootListBiblIfEmpty(additional, root);

    assertNotNull(additional.getListBibl());
  }

  @Test
  void removeRootListBiblIfEmptyShouldDetachAnEmptyRoot() {
    Additional additional = new Additional();
    ListBibl root = KODListBiblMapper.getRootListBibl(additional);

    KODListBiblMapper.removeRootListBiblIfEmpty(additional, root);

    assertNull(additional.getListBibl());
  }

  @Test
  void removeRootListBiblIfEmptyMustNotDropContentItDoesNotUnderstand() {
    Additional additional = new Additional();
    ListBibl root = KODListBiblMapper.getRootListBibl(additional);
    root.getMilestonesAndGbsAndPbs().add(BiblFactory.build("HSP-fremd-1"));

    KODListBiblMapper.removeRootListBiblIfEmpty(additional, root);

    assertNotNull(additional.getListBibl(),
        "a root holding foreign content must survive, otherwise that content is lost");
  }

  @Test
  void removeRootListBiblIfEmptyShouldRejectNull() {
    Additional additional = new Additional();
    ListBibl root = KODListBiblMapper.getRootListBibl(additional);

    assertThrows(NullPointerException.class,
        () -> KODListBiblMapper.removeRootListBiblIfEmpty(null, root));
    assertThrows(NullPointerException.class,
        () -> KODListBiblMapper.removeRootListBiblIfEmpty(additional, null));
  }
}
