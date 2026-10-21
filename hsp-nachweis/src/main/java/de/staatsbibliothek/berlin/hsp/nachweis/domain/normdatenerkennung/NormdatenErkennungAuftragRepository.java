package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;
import java.util.Optional;

public interface NormdatenErkennungAuftragRepository extends
    CRUDRepository<NormdatenErkennungAuftrag, String> {

  Optional<NormdatenErkennungAuftrag> findByIdWithErgebnisse(String id);

  Optional<NormdatenErkennungAuftrag> findFirstByStatus(NormdatenErkennungAuftragStatus status);
}
