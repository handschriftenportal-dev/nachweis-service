package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.Concept;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.ConceptScheme;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenGraphQLBoundary;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 04.12.20
 */
@Dependent
@Slf4j
public class NormdatenReferenzService implements NormdatenReferenzBoundary,
    Serializable {

  @Serial
  private static final long serialVersionUID = 7992690266107947322L;

  private final NormdatenGraphQLBoundary normdatenGraphQLBoundary;

  private final NormdatenReferenzRepository normdatenReferenzRepository;

  @Inject
  public NormdatenReferenzService(
      NormdatenGraphQLBoundary normdatenGraphQLBoundary,
      NormdatenReferenzRepository normdatenReferenzRepository) {
    this.normdatenGraphQLBoundary = normdatenGraphQLBoundary;
    this.normdatenReferenzRepository = normdatenReferenzRepository;
  }

  @Override
  @Transactional
  public Optional<NormdatenReferenz> findOneByIdOrNameAndType(String idOrName,
      String type) {
    log.debug("findOneByIdOrNameAndNodeLabel idOrName={}, type={}", idOrName,
        type);

    if (Objects.isNull(idOrName) || idOrName.isEmpty()) {
      return Optional.empty();
    }

    return normdatenGraphQLBoundary
        .findOneByIdOrNameAndNodeLabel(idOrName, type)
        .map(NormdatenReferenzMapper::map);
  }

  @Override
  @Transactional
  public Set<NormdatenReferenz> findAllByIdOrNameAndType(
      String idOrName, String type, boolean allFields) {
    log.debug(
        "findAllByIdOrNameAndType idOrName={}, type={}, allFields={}", idOrName,
        type, allFields);

    if ((Objects.isNull(type) || type.isEmpty())
        && (Objects.isNull(idOrName) || idOrName.isEmpty())) {
      return Collections.emptySet();
    }

    return normdatenGraphQLBoundary
        .findAllByIdOrNameAndNodeLabel(idOrName, type, allFields)
        .stream()
        .map(NormdatenReferenzMapper::map)
        .collect(Collectors.toSet());
  }

  @Override
  @Transactional
  public Optional<NormdatenReferenz> findSpracheById(String id) {
    log.debug("findSpracheById id={}", id);

    if (Objects.isNull(id) || id.isEmpty()) {
      return Optional.empty();
    }

    return normdatenReferenzRepository.findByIdOrIdentifikatorOptional(
            id)
        .or(() -> normdatenGraphQLBoundary.findLanguageById(id)
            .map(NormdatenReferenzMapper::map))
        .map(normdatenReferenzRepository::save);
  }

  @Override
  @Transactional
  public Set<NormdatenReferenz> findKoerperschaftenByOrtId(String ortId,
      boolean allFields) {
    log.debug("findKoerperschaftenByOrtId ortId={}, allFields={}", ortId,
        allFields);
    if (Objects.isNull(ortId)) {
      return Collections.emptySet();
    }

    return normdatenGraphQLBoundary.findCorporateBodiesByPlaceId(ortId,
            allFields).stream()
        .map(NormdatenReferenzMapper::map)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<NormdatenReferenz> findConceptSchemesWithType(String type) {

    if (type == null || type.isBlank()) {
      throw new IllegalArgumentException("Bad concept scheme type");
    }

    List<ConceptScheme> conceptScheme =
        normdatenGraphQLBoundary.findSkosConceptSchemesWithinSchemeConceptScheme(
            type);

    return conceptScheme.stream()
        .map(
            cs ->
                new NormdatenReferenzBuilder()
                    .withId(cs.getId())
                    .withName(cs.getNotation())
                    .withUri(cs.getUri())
                    .withTypeName(type)
                    .withVarianterName(
                        cs.getLabels() != null
                            ? cs.getLabels().stream()
                            .map(l -> new VarianterName(l.getText(),
                                l.getIsoCode()))
                            .collect(Collectors.toSet())
                            : new HashSet<>())
                    .build())
        .collect(Collectors.toSet());
  }

  @Override
  public Set<NormdatenReferenz> findConceptWithConceptSchemeReferenz(
      NormdatenReferenz conceptScheme) {

    if (conceptScheme == null || conceptScheme.getName().isEmpty()) {
      throw new IllegalArgumentException("Bad concept scheme");
    }

    List<Concept> concepts =
        normdatenGraphQLBoundary.findConceptsWithConceptSchemeType(
            conceptScheme.getName());

    return concepts.stream()
        .peek(c -> log.info("Map Concept {} ", c.toString()))
        .map(mapConceptToNormdatenReferenz(conceptScheme))
        .peek(n -> log.info("Mapped to Normdatenreference {}", n))
        .collect(Collectors.toSet());
  }

  Function<Concept, NormdatenReferenz> mapConceptToNormdatenReferenz(
      NormdatenReferenz conceptScheme) {
    return cs ->
        new NormdatenReferenzBuilder()
            .withId(cs.getId())
            .withName(cs.getNotation())
            .withUri(cs.getUri())
            .withTypeName(conceptScheme.getName())
            .withVarianterName(
                cs.getLabels() != null
                    ? cs.getLabels().stream()
                    .map(l -> new VarianterName(l.getText(), l.getIsoCode()))
                    .collect(Collectors.toSet())
                    : new HashSet<>())
            .build();
  }

  @Override
  public Optional<NormdatenReferenz> findConceptWithSchemeTypeAndURI(
      String schemeType, String uri) {

    List<Concept> concepts = normdatenGraphQLBoundary.findConceptsWithConceptSchemeType(
        schemeType);

    return concepts.stream()
        .filter(concept -> compareUrisWithoutProtocol(concept.getUri(), uri))
        .findAny()
        .map(
            concept ->
                new NormdatenReferenzBuilder()
                    .withId(concept.getId())
                    .withName(concept.getNotation())
                    .withUri(concept.getUri())
                    .withVarianterName(
                        concept.getLabels() != null
                            ? concept.getLabels().stream()
                            .map(l -> new VarianterName(l.getText(),
                                l.getIsoCode()))
                            .collect(Collectors.toSet())
                            : new HashSet<>())
                    .withTypeName(schemeType)
                    .build());
  }

  @Override
  @Transactional
  @TransactionConfiguration(timeout = 18600)
  public void updateAllNormdatenReferenzen() {

    log.info("Starting update all Normdatenreferenzen");

    normdatenReferenzRepository.listAll(false).stream()
        .filter(r -> r.getId().contains("NORM-"))
        .forEach(
            normdatenReferenz ->
                normdatenGraphQLBoundary
                    .findOneByIdOrNameAndNodeLabel(normdatenReferenz.getId(),
                        null)
                    .map(NormdatenReferenzMapper::map)
                    .ifPresent(normdatenReferenzRepository::save));

    log.info("Ending update all Normdatenreferenzen");
  }

  protected boolean compareUrisWithoutProtocol(String uri1, String uri2) {
    return uri1.replaceAll("https|http", "")
        .equals(uri2.replaceAll("https|http", ""));
  }

  @Override
  @Transactional
  public void updateNormdatenReferenzWithID(String id) {
    log.info("Update Normdatenreferenzen with ID {}", id);
    normdatenGraphQLBoundary
        .findOneByIdOrNameAndNodeLabel(id, null)
        .map(NormdatenReferenzMapper::map)
        .ifPresent(normdatenReferenzRepository::save);
  }
}
