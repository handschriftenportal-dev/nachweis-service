package de.staatsbibliothek.berlin.hsp.mapper.tei.kod;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Forschungsdokumentation;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEICommandException;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.BiblFactory;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.tei_c.ns._1.Additional;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.ListBibl;
import org.tei_c.ns._1.Ref;
import org.tei_c.ns._1.TEI;

/** Reads and adds Forschungsdokumentationen in {@code <listBibl type="bibliography">}.
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 01.09.2026
 */
final class ForschungsdokumentationListBiblMapper {

  private ForschungsdokumentationListBiblMapper() {
  }

  /**
   * Adds one Forschungsdokumentation to the bibliography.
   *
   * @param tei the TEI document to modify
   * @param forschungsdokumentation the Forschungsdokumentation to add
   * @return {@code true} when the entry was added; {@code false} when an equal entry already
   *     exists
   * @throws TEICommandException when the required additional element is missing
   */
  static boolean addForschungsdokumentation(
      TEI tei,
      Forschungsdokumentation forschungsdokumentation) {

    Objects.requireNonNull(forschungsdokumentation, "forschungsdokumentation is required");

    ListBibl root = KODListBiblMapper.getRootListBibl(getAdditional(tei));
    ListBibl bibliography = KODListBiblMapper.getChildListBibl(
        root,
        TEIValues.LIST_BIBL_TYPE_BIBLIOGRAPHY);

    if (isDuplicate(bibliography, forschungsdokumentation)) {
      return false;
    }

    bibliography.getMilestonesAndGbsAndPbs().add(
        BiblFactory.build(forschungsdokumentation));

    KODListBiblMapper.applyBiblDefaultMarker(bibliography);
    KODListBiblMapper.applyChildListBiblDefaultMarker(root);
    return true;
  }

  /**
   * Removes one canonical Forschungsdokumentation from the bibliography.
   *
   * @param tei the TEI document to modify
   * @param forschungsdokumentation the Forschungsdokumentation to remove
   * @return {@code true} when the entry was removed and the TEI changed; {@code false} when no
   *     matching entry exists
   * @throws TEICommandException when the required additional element is missing
   */
  static boolean removeForschungsdokumentation(
      TEI tei,
      Forschungsdokumentation forschungsdokumentation) {
    Objects.requireNonNull(forschungsdokumentation, "forschungsdokumentation is required");

    Additional additional = getAdditional(tei);
    Optional<ListBibl> bibliography = KODListBiblMapper.findRootListBibl(additional)
        .flatMap(root -> KODListBiblMapper.findChildListBiblByType(
            root, TEIValues.LIST_BIBL_TYPE_BIBLIOGRAPHY));
    if (bibliography.isEmpty()) {
      return false;
    }

    ListBibl listBibl = bibliography.get();
    Optional<Bibl> matchingBibl = listBibl.getMilestonesAndGbsAndPbs().stream()
        .filter(Bibl.class::isInstance)
        .map(Bibl.class::cast)
        .filter(bibl -> matches(bibl, forschungsdokumentation))
        .findFirst();
    if (matchingBibl.isEmpty()) {
      return false;
    }

    listBibl.getMilestonesAndGbsAndPbs().remove(matchingBibl.get());
    KODListBiblMapper.applyBiblDefaultMarker(listBibl);

    ListBibl root = additional.getListBibl();
    if (listBibl.getMilestonesAndGbsAndPbs().stream().noneMatch(Bibl.class::isInstance)) {
      KODListBiblMapper.removeChildListBiblByType(
          root, TEIValues.LIST_BIBL_TYPE_BIBLIOGRAPHY);
      KODListBiblMapper.removeRootListBiblIfEmpty(additional, root);
    }
    KODListBiblMapper.applyChildListBiblDefaultMarker(root);
    return true;
  }

  /**
   * Reads direct bibliography entries in document order without changing the TEI.
   *
   * @param tei the TEI document to read
   * @return the Forschungsdokumentationen found in the bibliography
   * @throws TEICommandException when the required additional element is missing
   */
  static List<Forschungsdokumentation> readForschungsdokumentationen(TEI tei) {
    return findBibliography(tei)
        .stream()
        .flatMap(bibliography -> bibliography.getMilestonesAndGbsAndPbs().stream())
        .filter(Bibl.class::isInstance)
        .map(Bibl.class::cast)
        .map(ForschungsdokumentationListBiblMapper::toForschungsdokumentation)
        .flatMap(Optional::stream)
        .toList();
  }

  private static Optional<Forschungsdokumentation> toForschungsdokumentation(Bibl bibl) {
    Ref ref = bibl.getContent().stream()
        .filter(Ref.class::isInstance)
        .map(Ref.class::cast)
        .findFirst()
        .orElse(null);
    List<Object> content = ref == null ? bibl.getContent() : ref.getContent();
    String ressource = content.stream()
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .collect(Collectors.joining());

    if (StringUtils.isBlank(StringUtils.normalizeSpace(ressource))) {
      return Optional.empty();
    }

    String link = ref == null || !ref.isSetTargets() ? null : ref.getTargets().stream()
        .filter(StringUtils::isNotBlank)
        .findFirst()
        .orElse(null);
    if (ref != null && link == null) {
      return Optional.empty();
    }
    return Optional.of(new Forschungsdokumentation(ressource, link));
  }

  private static Optional<ListBibl> findBibliography(TEI tei) {

    return KODListBiblMapper.findRootListBibl(getAdditional(tei))
        .flatMap(root -> KODListBiblMapper.findChildListBiblByType(
            root, TEIValues.LIST_BIBL_TYPE_BIBLIOGRAPHY));
  }

  private static boolean isDuplicate(
      ListBibl bibliography,
      Forschungsdokumentation forschungsdokumentation) {

    if (forschungsdokumentation.hasLink()) {
      return containsLink(bibliography, forschungsdokumentation.link());
    }

    return containsRessourceWithoutLink(
        bibliography,
        forschungsdokumentation.ressource());
  }

  private static boolean matches(
      Bibl bibl,
      Forschungsdokumentation forschungsdokumentation) {
    Ref ref = bibl.getContent().stream()
        .filter(Ref.class::isInstance)
        .map(Ref.class::cast)
        .findFirst()
        .orElse(null);

    if (forschungsdokumentation.hasLink()) {
      return ref != null
          && ref.isSetTargets()
          && ref.getTargets().stream().anyMatch(forschungsdokumentation.link()::equals)
          && resourceText(bibl).equals(forschungsdokumentation.ressource());
    }

    return ref == null
        && resourceText(bibl).equals(forschungsdokumentation.ressource());
  }

  private static String resourceText(Bibl bibl) {
    Ref ref = bibl.getContent().stream()
        .filter(Ref.class::isInstance)
        .map(Ref.class::cast)
        .findFirst()
        .orElse(null);
    List<Object> content = ref == null ? bibl.getContent() : ref.getContent();

    return content.stream()
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .collect(Collectors.joining());
  }

  private static boolean containsLink(ListBibl bibliography, String link) {
    return refsOf(bibliography)
        .filter(Ref::isSetTargets)
        .flatMap(ref -> ref.getTargets().stream())
        .anyMatch(link::equals);
  }

  private static boolean containsRessourceWithoutLink(
      ListBibl bibliography,
      String ressource) {

    return bibliography.getMilestonesAndGbsAndPbs().stream()
        .filter(Bibl.class::isInstance)
        .map(Bibl.class::cast)
        .filter(bibl -> bibl.getContent().stream().noneMatch(Ref.class::isInstance))
        .flatMap(bibl -> bibl.getContent().stream())
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .map(StringUtils::normalizeSpace)
        .anyMatch(ressource::equals);
  }

  private static Stream<Ref> refsOf(ListBibl bibliography) {
    return bibliography.getMilestonesAndGbsAndPbs().stream()
        .filter(Bibl.class::isInstance)
        .map(Bibl.class::cast)
        .flatMap(bibl -> bibl.getContent().stream())
        .filter(Ref.class::isInstance)
        .map(Ref.class::cast);
  }

  private static Additional getAdditional(TEI tei) {
    Objects.requireNonNull(tei, "tei is required");

    try {
      return KODListBiblMapper.getRequiredAdditional(tei);
    } catch (Exception exception) {
      throw new TEICommandException("Missing additional element in TEI-XML", exception);
    }
  }
}
