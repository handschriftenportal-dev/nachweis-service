package de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.XMLDokument;
import java.util.stream.Stream;

public interface DatensicherungSelectAllTei {

  Stream<? extends XMLDokument> selectAllTeiXml();
}
