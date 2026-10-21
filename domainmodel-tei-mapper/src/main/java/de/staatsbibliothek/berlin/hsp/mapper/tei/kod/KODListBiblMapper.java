package de.staatsbibliothek.berlin.hsp.mapper.tei.kod;

import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.BiblFactory;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.tei_c.ns._1.Additional;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.ListBibl;
import org.tei_c.ns._1.TEI;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 22.07.2026
 */
public class KODListBiblMapper {

  private KODListBiblMapper() {
  }

  /**
   * Rebuilds {@code <listBibl type="descriptions">} from the given ids.
   *
   * <p>Clearing and rebuilding is safe because the source of truth is the KOD's persisted
   * beschreibungenIDs, not the document. Externe Ressourcen live only in the TEI and are edited
   * surgically instead — see {@code ExterneRessourcenListBiblMapper}.</p>
   */
  public static void writeDescriptions(
      TEI tei,
      Set<String> beschreibungenIds) throws Exception {

    Additional additional = getRequiredAdditional(tei);
    ListBibl root = getRootListBibl(additional);

    if (beschreibungenIds.isEmpty()) {
      removeChildListBiblByType(
          root,
          TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS);
      applyChildListBiblDefaultMarker(root);
      removeRootListBiblIfEmpty(additional, root);
      return;
    }

    ListBibl descriptions = getChildListBibl(
        root,
        TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS);

    List<Object> descriptionsContent =
        descriptions.getMilestonesAndGbsAndPbs();

    descriptionsContent.clear();

    beschreibungenIds.forEach(referenzId ->
        descriptionsContent.add(BiblFactory.build(referenzId)));

    applyBiblDefaultMarker(descriptions);
    applyChildListBiblDefaultMarker(root);
  }

  public static Additional getRequiredAdditional(TEI tei) throws Exception {

    List<Additional> additionalList = new ArrayList<>();
    TEICommon.findAll(Additional.class, tei, additionalList);

    return additionalList.stream()
        .findFirst()
        .orElseThrow(() ->
            new IllegalArgumentException("Missing Additional in TEI XML"));
  }

  private static void migrateLegacyFlatDescriptions(ListBibl root) {

    boolean hasDescriptions = root.getMilestonesAndGbsAndPbs().stream()
        .filter(ListBibl.class::isInstance)
        .map(ListBibl.class::cast)
        .anyMatch(listBibl ->
            TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS.equals(listBibl.getType()));

    if (hasDescriptions) {
      return;
    }

    List<Bibl> legacyDescriptionBibls =
        root.getMilestonesAndGbsAndPbs().stream()
            .filter(Bibl.class::isInstance)
            .map(Bibl.class::cast)
            .toList();

    if (legacyDescriptionBibls.isEmpty()) {
      return;
    }

    ListBibl descriptions = new ListBibl();
    descriptions.setType(TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS);
    descriptions.setDefault(TEIValues.DEFAULT_TRUE);
    descriptions.getMilestonesAndGbsAndPbs().addAll(legacyDescriptionBibls);

    root.getMilestonesAndGbsAndPbs().removeIf(Bibl.class::isInstance);
    root.getMilestonesAndGbsAndPbs().add(descriptions);
  }

  public static Optional<ListBibl> findChildListBiblByType(
      ListBibl root,
      String type) {

    if (Objects.isNull(root)) {
      return Optional.empty();
    }

    return root.getMilestonesAndGbsAndPbs().stream()
        .filter(ListBibl.class::isInstance)
        .map(ListBibl.class::cast)
        .filter(listBibl -> type.equals(listBibl.getType()))
        .findFirst();
  }

  public static Optional<ListBibl> findRootListBibl(Additional additional) {

    if (Objects.isNull(additional)) {
      return Optional.empty();
    }

    return Optional.ofNullable(additional.getListBibl());
  }

  /**
   * Returns the root listBibl, creating and attaching it when the additional element has none.
   *
   * <p>Reapplies the type and migrates the legacy flat layout on every call — a no-op for a
   * freshly created root.</p>
   */
  public static ListBibl getRootListBibl(Additional additional) {

    Objects.requireNonNull(additional, "additional is required");

    ListBibl root = findRootListBibl(additional)
        .orElseGet(() -> {
          ListBibl neuerRoot = new ListBibl();
          additional.setListBibl(neuerRoot);
          return neuerRoot;
        });

    root.setType(TEIValues.LIST_BIBL_TYPE_ROOT);

    migrateLegacyFlatDescriptions(root);

    return root;
  }

  /**
   * Returns the child listBibl of the given type, creating and attaching it when the root has
   * none.
   */
  public static ListBibl getChildListBibl(
      ListBibl root,
      String type) {

    Objects.requireNonNull(root, "root is required");

    return findChildListBiblByType(root, type)
        .orElseGet(() -> {
          ListBibl neuesChild = new ListBibl();
          neuesChild.setType(type);

          if (TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS.equals(type)) {
            neuesChild.setDefault(TEIValues.DEFAULT_TRUE);
          }

          root.getMilestonesAndGbsAndPbs().add(neuesChild);
          return neuesChild;
        });
  }

  /**
   * Ensures exactly one child listBibl carries {@code default="true"}, which the Schematron asserts
   * as soon as the root has more than one child (tei-odd@4.9.0, {@code listBibl-is-declarable};
   * dropped in 4.11.0, but a document valid under 4.9.0 stays valid afterwards).
   *
   * <p>Keeps an existing marker, otherwise prefers descriptions, otherwise the first child. Adding
   * the resources listBibl next to an existing descriptions listBibl is what makes the rule fire on
   * a document for the first time, so the marker has to be reapplied rather than only set on
   * creation.</p>
   */
  public static void applyChildListBiblDefaultMarker(ListBibl root) {

    Objects.requireNonNull(root, "root is required");

    List<ListBibl> children = root.getMilestonesAndGbsAndPbs().stream()
        .filter(ListBibl.class::isInstance)
        .map(ListBibl.class::cast)
        .toList();

    if (children.size() < 2) {
      return;
    }

    ListBibl marked = children.stream()
        .filter(child -> TEIValues.DEFAULT_TRUE.equals(child.getDefault()))
        .findFirst()
        .orElseGet(() -> children.stream()
            .filter(child ->
                TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS.equals(child.getType()))
            .findFirst()
            .orElse(children.getFirst()));

    children.forEach(child -> child.setDefault(null));
    marked.setDefault(TEIValues.DEFAULT_TRUE);
  }

  /**
   * Ensures that exactly one direct {@code bibl} child carries {@code default="true"}. An existing
   * marker is retained; otherwise the first {@code bibl} becomes the default.
   */
  static void applyBiblDefaultMarker(ListBibl listBibl) {

    Objects.requireNonNull(listBibl, "listBibl is required");

    List<Bibl> bibls = listBibl.getMilestonesAndGbsAndPbs().stream()
        .filter(Bibl.class::isInstance)
        .map(Bibl.class::cast)
        .toList();

    if (bibls.isEmpty()) {
      return;
    }

    Bibl marked = bibls.stream()
        .filter(bibl -> TEIValues.DEFAULT_TRUE.equals(bibl.getDefault()))
        .findFirst()
        .orElse(bibls.getFirst());

    bibls.forEach(bibl -> bibl.setDefault(null));
    marked.setDefault(TEIValues.DEFAULT_TRUE);
  }

  public static void removeChildListBiblByType(
      ListBibl root,
      String type) {

    root.getMilestonesAndGbsAndPbs()
        .removeIf(element ->
            element instanceof ListBibl listBibl
                && type.equals(listBibl.getType()));
  }

  /**
   * Detaches the root listBibl once it is completely empty.
   *
   * <p>Deliberately "no children at all" rather than "no child listBibl": a root can still hold
   * elements this mapper does not manage, and detaching it would silently delete them.</p>
   */
  public static void removeRootListBiblIfEmpty(
      Additional additional,
      ListBibl root) {

    Objects.requireNonNull(additional, "additional is required");
    Objects.requireNonNull(root, "root is required");

    if (root.getMilestonesAndGbsAndPbs().isEmpty()) {
      additional.setListBibl(null);
    }
  }

}
