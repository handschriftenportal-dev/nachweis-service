/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

  Collection<Sperre> findByBearbeiter(String bearbeitername) throws DokumentSperreRepositoryException;

  Collection<Sperre> findByTransactionId(String txId) throws DokumentSperreRepositoryException;

  List<Sperre> findBySperreEintraege(SperreEintrag... sperreEintraege)
      throws DokumentSperreRepositoryException;

  List<Sperre> findConflictSperrenForTransaction(String transactionId, SperreEintrag... sperreEintraege)
      throws DokumentSperreRepositoryException;

  List<Sperre> findConflictSperrenForBearbeiter(String bearbeiterId, SperreEintrag... sperreEintraege)
      throws DokumentSperreRepositoryException;

  default boolean isLocked(SperreEintrag... sperreEintraege) throws DokumentSperreRepositoryException {
    return !findBySperreEintraege(sperreEintraege).isEmpty();
  }
}
