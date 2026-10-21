package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;

public interface NormdatenErkennungEintragRepository extends
    CRUDRepository<NormdatenErkennungEintrag, String> {

}
