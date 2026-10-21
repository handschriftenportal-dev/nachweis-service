package de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.entities.Datensicherung;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DatensicherungDownload;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DokumentTyp;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public interface DatensicherungBoundary {

  SortedSet<DokumentTyp> getDokumenteAuswahlOptionen();

  void starteDatensicherung(String datensicherungId);

  List<Datensicherung> getAlleDatensicherungen();

  boolean deleteDatensicherung(String id);

  DatensicherungDownload getFileByDatensicherungId(String id)
      throws DatensicherungException;

  String createInitialDatensicherung(Set<DokumentTyp> auswahl,
      String benutzer) throws DatensicherungException;
}
