package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 12.05.21
 */
public interface NormdatenGraphQLBoundary {

  String NORM_ID_PREFIX = "NORM-";

  String DNB_URL_BASE = "https://d-nb.info/gnd/";

  GraphQlData findByQuery(GraphQlQuery query) throws InterruptedException;

  Optional<GNDEntityFact> findOneByIdOrNameAndNodeLabel(String idOrName, String nodeLabel);

  Set<GNDEntityFact> findAllByIdOrNameAndNodeLabel(String idOrName, String type, boolean allFields);

  Optional<GNDEntityFact> findLanguageById(String id);

  Set<GNDEntityFact> findCorporateBodiesByPlaceId(String id, boolean allFields);

  Optional<GNDEntityFact> createGNDEntityFactAsNode(GNDEntityFact gndEntityFact);

  List<Concept> findConceptsWithConceptSchemeType(String conceptSchemeType);

  List<ConceptScheme> findSkosConceptSchemesWithinSchemeConceptScheme(String conceptScheme);
}
