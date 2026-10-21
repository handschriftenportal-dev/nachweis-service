package de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.DatensicherungDTO;

public interface DatensicherungRepository extends CRUDRepository<DatensicherungDTO, String> {
  
}
