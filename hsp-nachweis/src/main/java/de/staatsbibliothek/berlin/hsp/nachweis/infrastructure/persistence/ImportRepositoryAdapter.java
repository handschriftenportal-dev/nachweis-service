package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 10.11.20
 */
@ApplicationScoped
public class ImportRepositoryAdapter implements ImportRepository {
  @Override
  public List<BeschreibungImport> findByStatus(ImportStatus status) {
    return list("status", status);
  }
}
