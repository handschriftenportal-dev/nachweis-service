package de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreRepositoryException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;
import java.util.Collection;
import java.util.List;

/**
 * Provide access to the document locks persistence layer
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 17.08.21
 */
public interface SperreRepository extends CRUDRepository<Sperre, String> {

  Collection<Sperre> findByBearbeiter(String bearbeitername)
      throws DokumentSperreRepositoryException;

  Collection<Sperre> findByTransactionId(String txId) throws DokumentSperreRepositoryException;

  List<Sperre> findBySperreEintraege(SperreEintrag... sperreEintraege)
      throws DokumentSperreRepositoryException;

  List<Sperre> findConflictSperrenForTransaction(String transactionId,
      SperreEintrag... sperreEintraege)
      throws DokumentSperreRepositoryException;

  List<Sperre> findConflictSperrenForBearbeiter(String bearbeiterId,
      SperreEintrag... sperreEintraege)
      throws DokumentSperreRepositoryException;

  default boolean isLocked(SperreEintrag... sperreEintraege)
      throws DokumentSperreRepositoryException {
    return !findBySperreEintraege(sperreEintraege).isEmpty();
  }

  List<Sperre> findExpiredSperren(int expirationMinutes);
}
