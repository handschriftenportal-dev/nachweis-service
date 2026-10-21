package de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KatalogBeschreibungReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.DatensicherungSelectAllTei;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;
import java.util.Optional;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.02.2022.
 * @version 1.0
 */
public interface KatalogRepository extends CRUDRepository<Katalog, String>,
    DatensicherungSelectAllTei {

  Optional<Katalog> findByHskId(String hskId);

  Optional<KatalogBeschreibungReferenz> findKatalogBeschreibungReferenzByBeschreibungsID(
      String beschreibungsID);

}
