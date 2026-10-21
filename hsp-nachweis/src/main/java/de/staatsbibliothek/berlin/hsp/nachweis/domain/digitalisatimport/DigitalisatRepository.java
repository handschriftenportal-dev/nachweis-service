package de.staatsbibliothek.berlin.hsp.nachweis.domain.digitalisatimport;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 17.05.24.
 * @version 1.0
 */
public interface DigitalisatRepository extends CRUDRepository<Digitalisat, String> {

}
