package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;
import java.util.List;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 14.02.2020.
 */
public interface SuchDokumentBoundary {

  Ergebnis search(final Suche suche) throws SolrServiceException;

  void reindexAllKulturObjektDokumente() throws SolrUebernahmeException;

  void reindexAllBeschreibungen() throws SolrUebernahmeException;

  void kodUebernehmen(final KulturObjektDokument kod) throws SolrUebernahmeException;

  void allKODsUebernehmen(final List<KulturObjektDokument> kods) throws SolrUebernahmeException;

  void allBeschreibungenUebernehmen(final List<Beschreibung> beschreibungen) throws SolrUebernahmeException;
  void beschreibungUebernehmen(final Beschreibung beschreibung) throws SolrUebernahmeException;

  void dokumentLoeschen(String... iDs) throws SolrUebernahmeException;

}
