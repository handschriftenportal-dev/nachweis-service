package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;
import java.util.List;
import java.util.Optional;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 03.12.20
 */
public interface NormdatenReferenzRepository extends CRUDRepository<NormdatenReferenz, String> {

  NormdatenReferenz findByName(String name);

  default Optional<NormdatenReferenz> findByNameOptional(String name) {
    return Optional.ofNullable(findByName(name));
  }

  NormdatenReferenz findByGndID(String gndID);

  default Optional<NormdatenReferenz> findByGndIDOptional(String gndID) {
    return Optional.ofNullable(findByGndID(gndID));
  }

  NormdatenReferenz findByIdOrIdentifikator(String id);

  default Optional<NormdatenReferenz> findByIdOrIdentifikatorOptional(String id) {
    return Optional.ofNullable(findByIdOrIdentifikator(id));
  }

  NormdatenReferenz findByIdOrName(String idOrName, String type) throws Exception;

  List<NormdatenReferenz> findReferencedCorporateBodies();

}
