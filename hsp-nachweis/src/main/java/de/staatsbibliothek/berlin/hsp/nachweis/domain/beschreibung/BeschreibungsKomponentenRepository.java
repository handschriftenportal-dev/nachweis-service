package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GlobaleBeschreibungsKomponente;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.06.22.
 * @version 1.0
 */
public interface BeschreibungsKomponentenRepository extends CRUDRepository<GlobaleBeschreibungsKomponente, String> {

}
