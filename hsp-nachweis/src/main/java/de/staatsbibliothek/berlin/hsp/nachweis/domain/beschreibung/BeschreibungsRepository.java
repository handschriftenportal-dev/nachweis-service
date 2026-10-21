package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.DatensicherungSelectAllTei;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungInReviewDbView;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungListDbView;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungRejectedDbView;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.11.2019.
 */
public interface BeschreibungsRepository extends CRUDRepository<Beschreibung, String>,
    DatensicherungSelectAllTei {

  List<Beschreibung> findAllById(Collection<String> ids);

  Optional<Beschreibung> findByPublikation(Publikation publikation);

  Beschreibung findByAltIdentifier(String altIdentifier);

  default Optional<Beschreibung> findByAltIdentifierOptional(String altIdentifier) {
    return Optional.ofNullable(findByAltIdentifier(altIdentifier));
  }

  List<Beschreibung> findByKatalogId(String katalogId);

  List<BeschreibungListDbView> findFilteredBeschreibungListDTOs(int first, int pageSize,
      Map<String, String> filterBy, Map<String, Boolean> sortByAsc);

  int countFilteredBeschreibungListDTOs(Map<String, String> filterBy);

  BeschreibungListDbView findBeschreibungListDTOById(String id);

  default Optional<BeschreibungListDbView> findBeschreibungListDTOByIdOptional(String id) {
    return Optional.ofNullable(findBeschreibungListDTOById(id));
  }

  List<BeschreibungListDbView> findLatestModified();

  List<BeschreibungListDbView> findLatestModified(int maxResults);

  List<BeschreibungListDbView> findLatestModifiedByBearbeiter(int maxResults, String bearbeiter);

  List<Beschreibung> findAllBeschreibungWithActiveApprovalRequest();

  List<BeschreibungListDbView> findPublikationenByBearbeiter(String bearbeiter);

  List<BeschreibungListDbView> findLatestPublikationen(int maxResults);

  List<BeschreibungInReviewDbView> findInReviewByBearbeiter(String bearbeiter);

  List<BeschreibungRejectedDbView> findRejectedByBearbeiter(String bearbeiter);
}
