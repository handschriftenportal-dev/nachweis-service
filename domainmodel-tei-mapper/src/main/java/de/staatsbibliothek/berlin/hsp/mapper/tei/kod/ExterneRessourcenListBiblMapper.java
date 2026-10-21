package de.staatsbibliothek.berlin.hsp.mapper.tei.kod;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessource;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessourceTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEICommandException;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.BiblFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.tei_c.ns._1.Additional;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.ListBibl;
import org.tei_c.ns._1.Ref;
import org.tei_c.ns._1.TEI;

/**
 * Reads and writes the {@code <listBibl type="resources">} section of a KOD TEI document.
 *
 * <p>Writes are surgical — only the targeted {@code <bibl>} is appended or removed, so unknown
 * {@code @type} values, {@code <biblStruct>} children and hand-written {@code <ref>} labels survive
 * a modify cycle.</p>
 */
final class ExterneRessourcenListBiblMapper {

  private ExterneRessourcenListBiblMapper() {
  }

  /**
   * @return false when an equal {@code <bibl>} is already present, leaving the document untouched
   */
  static boolean addRessource(TEI tei, ExterneRessource externeRessource) {

    Objects.requireNonNull(externeRessource, "externeRessource is required");

    ListBibl root = KODListBiblMapper.getRootListBibl(getAdditional(tei));

    ListBibl ressourcen = KODListBiblMapper.getChildListBibl(
        root,
        TEIValues.LIST_BIBL_TYPE_RESSOURCES);

    if (findMatching(ressourcen, externeRessource).findAny().isPresent()) {
      return false;
    }

    ressourcen.getMilestonesAndGbsAndPbs()
        .add(BiblFactory.build(externeRessource));
    KODListBiblMapper.applyBiblDefaultMarker(ressourcen);
    KODListBiblMapper.applyChildListBiblDefaultMarker(root);
    return true;
  }

  /**
   * @return false when no matching {@code <bibl>} was present, leaving the document untouched
   */
  static boolean removeRessource(TEI tei, ExterneRessource externeRessource) {

    Objects.requireNonNull(externeRessource, "externeRessource is required");
    Additional additional = getAdditional(tei);
    Optional<ListBibl> ressourcen = KODListBiblMapper.findRootListBibl(additional)
        .flatMap(root -> KODListBiblMapper.findChildListBiblByType(
            root, TEIValues.LIST_BIBL_TYPE_RESSOURCES));

    if (ressourcen.isEmpty()) {
      return false;
    }

    ListBibl root = additional.getListBibl();
    List<Object> content = ressourcen.get().getMilestonesAndGbsAndPbs();

    if (!removeTarget(content, externeRessource)) {
      return false;
    }

    KODListBiblMapper.applyBiblDefaultMarker(ressourcen.get());

    // Only prune when no bibl is left. Foreign content such as <biblStruct> keeps the section alive.
    if (content.stream().noneMatch(Bibl.class::isInstance)) {
      KODListBiblMapper.removeChildListBiblByType(
          root, TEIValues.LIST_BIBL_TYPE_RESSOURCES);
      KODListBiblMapper.removeRootListBiblIfEmpty(additional, root);
    }

    KODListBiblMapper.applyChildListBiblDefaultMarker(root);

    return true;
  }

  /**
   * Display projection, lossy by design: unknown {@code @type} values and invalid URLs are skipped
   * here but stay in the document. Never use the result as a writing base.
   *
   * @return the externe Ressourcen in document order; the first element is the default one
   */
  static List<ExterneRessource> readRessourcen(TEI tei) {

    List<ExterneRessource> result = new ArrayList<>();

    findRessourcen(tei).ifPresent(ressourcen ->
        ressourcen.getMilestonesAndGbsAndPbs().stream()
            .filter(Bibl.class::isInstance)
            .map(Bibl.class::cast)
            .forEach(bibl ->
                ExterneRessourceTyp.fromTeiType(bibl.getType())
                    .ifPresent(typ ->
                        targetsOf(bibl).forEach(url ->
                            toExterneRessource(typ, url)
                                .ifPresent(externeRessource ->
                                    addIfAbsent(result, externeRessource))))));

    return result;
  }

  /**
   * Foreign documents may carry targets that are not absolute http(s) URLs — reading must not fail
   * on them.
   */
  private static Optional<ExterneRessource> toExterneRessource(
      ExterneRessourceTyp typ,
      String url) {

    try {
      return Optional.of(new ExterneRessource(typ, url));

    } catch (IllegalArgumentException | NullPointerException _) {
      return Optional.empty();
    }
  }

  private static Optional<ListBibl> findRessourcen(TEI tei) {

    return KODListBiblMapper.findRootListBibl(getAdditional(tei))
        .flatMap(root -> KODListBiblMapper.findChildListBiblByType(
            root, TEIValues.LIST_BIBL_TYPE_RESSOURCES));
  }

  private static Additional getAdditional(TEI tei) {

    Objects.requireNonNull(tei, "tei is required");

    try {
      return KODListBiblMapper.getRequiredAdditional(tei);

    } catch (Exception exception) {
      throw new TEICommandException("Missing additional element in TEI-XML", exception);
    }
  }

  /**
   * Removes the url from every bibl of the matching type, dropping a ref once it has no target left
   * and the bibl once it has no ref left.
   *
   * <p>A bibl can carry several refs and a ref several targets — each of which reads as its own
   * ExterneRessource — so removing the whole bibl would take unrelated ressourcen with it.</p>
   *
   * @return true when at least one target was removed
   */
  private static boolean removeTarget(
      List<Object> content,
      ExterneRessource externeRessource) {

    List<Bibl> kandidaten = content.stream()
        .filter(Bibl.class::isInstance)
        .map(Bibl.class::cast)
        .filter(bibl -> externeRessource.typ().getTeiType().equals(bibl.getType()))
        .toList();

    boolean removed = false;

    for (Bibl bibl : kandidaten) {

      if (!removeTargetFromRefs(bibl, externeRessource.url())) {
        continue;
      }

      removed = true;

      if (bibl.getContent().stream().noneMatch(Ref.class::isInstance)) {
        content.remove(bibl);
      }
    }

    return removed;
  }

  private static boolean removeTargetFromRefs(Bibl bibl, String url) {

    List<Ref> refs = bibl.getContent().stream()
        .filter(Ref.class::isInstance)
        .map(Ref.class::cast)
        .toList();

    boolean removed = false;

    for (Ref ref : refs) {

      if (!ref.getTargets().removeIf(url::equals)) {
        continue;
      }

      removed = true;

      if (ref.getTargets().isEmpty()) {
        bibl.getContent().remove(ref);
      }
    }

    return removed;
  }

  private static Stream<Bibl> findMatching(
      ListBibl ressourcen,
      ExterneRessource externeRessource) {

    return ressourcen.getMilestonesAndGbsAndPbs().stream()
        .filter(Bibl.class::isInstance)
        .map(Bibl.class::cast)
        .filter(bibl -> matches(bibl, externeRessource));
  }

  private static boolean matches(Bibl bibl, ExterneRessource externeRessource) {

    return targetsOf(bibl).anyMatch(externeRessource.url()::equals);
  }

  private static Stream<String> targetsOf(Bibl bibl) {

    return bibl.getContent().stream()
        .filter(Ref.class::isInstance)
        .map(Ref.class::cast)
        .flatMap(ref -> ref.getTargets().stream());
  }

  private static void addIfAbsent(List<ExterneRessource> result,
      ExterneRessource externeRessource) {

    if (!result.contains(externeRessource)) {
      result.add(externeRessource);
    }
  }
}
