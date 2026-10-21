package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.DatensicherungSelectAllTei;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.KulturObjektDokumentListDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 29.11.2019.
 */
public interface KulturObjektDokumentRepository
    extends CRUDRepository<KulturObjektDokument, String>, DatensicherungSelectAllTei {

  KulturObjektDokument findByBeschreibung(String beschreibungId);

  default Optional<KulturObjektDokument> findByBeschreibungOptional(String beschreibungId) {
    return Optional.ofNullable(findByBeschreibung(beschreibungId));
  }

  List<KulturObjektDokument> findByIdentifikationIdent(String ident);

  Optional<KulturObjektDokument> findByPurl(String purl);

  List<KulturObjektDokument> findByAlternativeIdentifikationIdent(String ident);

  Optional<KulturObjektDokument> findByIdentifikationBeteiligeKoerperschaftName(String name);

  List<KulturObjektDokument> findByIdentifikationIdentAndBesitzerIDAndAufbewahrungsortID(
      String ident, String besitzerID, String aufbewahrungsOrtID);

  Optional<KulturObjektDokument> findByIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName(
      String ident, String besitzerName, String aufbewahrungsOrtName);

  Optional<KulturObjektDokument>
      findByAlternativeIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName(
          String ident, String besitzerName, String aufbewahrungsOrtName);

  Map<String, List<String>> getAllBeschreibungsIdsWithKodIDs();

  List<KulturObjektDokumentListDTO> findFilteredKulturObjektDokumentListDTOs(
      int first, int pageSize, Map<String, String> filterBy, Map<String, Boolean> sortByAsc);

  int countFilteredKulturObjektDokumentListDTOs(Map<String, String> filterBy);

  KulturObjektDokumentListDTO findKulturObjektDokumentListDTOById(String id);
}
