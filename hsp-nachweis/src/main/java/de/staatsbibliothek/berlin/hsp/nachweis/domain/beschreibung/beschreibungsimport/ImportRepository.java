package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;
import java.util.List;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */
public interface ImportRepository extends CRUDRepository<BeschreibungImport, String> {
  List<BeschreibungImport> findByStatus(ImportStatus status);
}
