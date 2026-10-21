package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 12.02.2020.
 */
public interface SuchPort {

  void update(final SuchDokument... suchDokumente) throws SolrServiceException;

  void update(boolean deleteBeforeUpdate, final SuchDokument... suchDokumente) throws SolrServiceException;

  void updateWithoutTransaction(boolean deleteBeforeUpdate, final SuchDokument... suchDokumente)
      throws SolrServiceException;

  void commitWithoutTransaction() throws SolrServiceException;

  void delete(final String... iDs) throws SolrServiceException;

  void deleteAll() throws SolrServiceException;

  void deleteBySuchDokumentTypWithoutTransaction(SuchDokumentTyp suchDokumentTyp) throws SolrServiceException;

  Ergebnis search(final Suche suche) throws SolrServiceException;

}
