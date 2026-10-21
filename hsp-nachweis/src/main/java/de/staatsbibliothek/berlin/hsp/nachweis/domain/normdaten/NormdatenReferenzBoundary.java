package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.util.Optional;
import java.util.Set;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 04.12.20
 */
public interface NormdatenReferenzBoundary {

  Optional<NormdatenReferenz> findOneByIdOrNameAndType(String idOrName, String type);

  Set<NormdatenReferenz> findAllByIdOrNameAndType(String idOrName, String type, boolean allFields);

  Optional<NormdatenReferenz> findSpracheById(String id);

  Set<NormdatenReferenz> findKoerperschaftenByOrtId(String ortId, boolean allFields);

  Set<NormdatenReferenz> findConceptSchemesWithType(String type);

  Set<NormdatenReferenz> findConceptWithConceptSchemeReferenz(NormdatenReferenz conceptScheme);

  Optional<NormdatenReferenz> findConceptWithSchemeTypeAndURI(String schemeType,
      String uri);

  void updateAllNormdatenReferenzen();

  void updateNormdatenReferenzWithID(String id);
}
