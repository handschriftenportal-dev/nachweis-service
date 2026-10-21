package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DokumentTyp;

public interface XMLDokument {

  String getId();

  String getTeiXml();

  DokumentTyp getDokumentTyp();

  void setTeiXml(String teiXml);
}
