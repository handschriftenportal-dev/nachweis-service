package de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BestandNachweisDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.BestandInstitutionElementDTO;
import java.util.List;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 24.04.23.
 * @version 1.0
 */
public interface BestandsRepository {

  BestandNachweisDTO findAllBestaende();

  List<BestandInstitutionElementDTO> findAllKulturObjekteForInstitution();

  List<BestandInstitutionElementDTO> findAllBeschreibungenForInstitution();

  List<BestandInstitutionElementDTO> findAllDigitalisateForKOD();
}
