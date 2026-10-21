package de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DatensicherungDownload;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.XMLDokument;
import java.io.IOException;
import java.util.stream.Stream;

public interface ZipDateisystemRepository {

  void saveToFile(final String dateiName,
      Stream<? extends XMLDokument> dokumente) throws IOException;

  boolean deleteFile(String dateiName) throws IOException;

  DatensicherungDownload getFileDownload(String dateiName) throws IOException;
}
