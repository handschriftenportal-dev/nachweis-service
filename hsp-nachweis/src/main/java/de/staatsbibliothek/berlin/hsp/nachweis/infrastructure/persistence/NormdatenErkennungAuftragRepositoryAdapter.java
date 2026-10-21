package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung.NormdatenErkennungAuftrag;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung.NormdatenErkennungAuftragRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung.NormdatenErkennungAuftragStatus;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import org.eclipse.microprofile.graphql.Query;

@ApplicationScoped
public class NormdatenErkennungAuftragRepositoryAdapter implements
    NormdatenErkennungAuftragRepository {

  @Override
  public Optional<NormdatenErkennungAuftrag> findFirstByStatus(NormdatenErkennungAuftragStatus status) {
    return find("status", status).firstResultOptional();
  }

  @Override
  public Optional<NormdatenErkennungAuftrag> findByIdWithErgebnisse(String id) {
    return find("SELECT r FROM NormdatenErkennungAuftrag r LEFT JOIN FETCH r.ergebnisse WHERE r.id = ?1", id)
        .firstResultOptional();
  }
}