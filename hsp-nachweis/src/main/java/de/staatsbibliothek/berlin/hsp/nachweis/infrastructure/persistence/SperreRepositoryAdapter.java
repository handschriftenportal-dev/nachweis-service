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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.SperreRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreRepositoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.LockModeType;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 17.08.21
 */
@Slf4j
@ApplicationScoped
public class SperreRepositoryAdapter implements SperreRepository {

  public static final String FIELD_BEARBEITER = "bearbeitername";
  public static final String FIELD_TX_ID = "txId";

  public static final String QUERY_LOCKED_LOCK_ENTRIES =
      "select distinct (s) from Sperre s"
          + " join fetch s.sperreEintraege se"
          + " where (se.targetId, se.targetType) in ";

  public static final String QUERY_LOCKED_LOCK_ENTRIES_OMIT_ALREADY_REGISTERED_IN_CURRENT_TRANSACTION =
      "select distinct (s) from Sperre s"
          + " join fetch s.sperreEintraege se"
          + " where (s." + FIELD_TX_ID + "!=?1 or s." + FIELD_TX_ID + "=null)"
          + " and (se.targetId, se.targetType) in ";

  public static final String QUERY_LOCKED_LOCK_ENTRIES_OMIT_ALREADY_REGISTERED_FOR_BEARBEITER =
      "select distinct (s) from Sperre s"
          + " join fetch s.sperreEintraege se"
          + " where (s.bearbeiter." + FIELD_BEARBEITER + "!=?1 or s." + FIELD_TX_ID + "!=null)"
          + " and (se.targetId, se.targetType) in ";

  public static final String QUERY_FIND_BY_BEARBEITER =
      "select distinct (s) from Sperre s"
          + " join fetch s.sperreEintraege se"
          + " where s." + FIELD_TX_ID + "=null"
          + " and s.bearbeiter." + FIELD_BEARBEITER + "=?1";

  public static final String QUERY_FIND_BY_TRANSACTION_ID = "from Sperre se where " + FIELD_TX_ID + "=?1 ";

  @Override
  public Collection<Sperre> findByBearbeiter(String bearbeitername) throws DokumentSperreRepositoryException {
    return getSperres(QUERY_FIND_BY_BEARBEITER, FIELD_BEARBEITER, bearbeitername);
  }

  @Override
  public Collection<Sperre> findByTransactionId(String txId) throws DokumentSperreRepositoryException {
    return getSperres(QUERY_FIND_BY_TRANSACTION_ID, FIELD_TX_ID, txId);
  }

  @Override
  public List<Sperre> findBySperreEintraege(SperreEintrag... sperreEintraege)
      throws DokumentSperreRepositoryException {
    try {
      return find(
          prepareLockedDocumentEntriesQuery(
              QUERY_LOCKED_LOCK_ENTRIES,
              sperreEintraege))
          .list();
    } catch (Exception ex) {
      throw new DokumentSperreRepositoryException("Error finding Sperren for sperreEintraege", ex);
    }
  }

  @Override
  public List<Sperre> findConflictSperrenForTransaction(String transactionId, SperreEintrag... sperreEintraege)
      throws DokumentSperreRepositoryException {

    List<Sperre> sperrenInConflict = new ArrayList<>();
    final int chunkSize = 100;
    try {
      for (int offset = 0; offset < sperreEintraege.length; offset += chunkSize) {
        log.info("findConflictSperrenForTransaction: offset={}", offset);

        SperreEintrag[] sperrenEintraegeChunk =
            Arrays.copyOfRange(sperreEintraege, offset, Math.min(sperreEintraege.length, offset + chunkSize));
        String query = prepareLockedDocumentEntriesQuery(
            QUERY_LOCKED_LOCK_ENTRIES_OMIT_ALREADY_REGISTERED_IN_CURRENT_TRANSACTION,
            sperrenEintraegeChunk);

        List<Sperre> sperrenInConflictChunk = find(query, transactionId)
            .withLock(LockModeType.OPTIMISTIC)
            .list();

        sperrenInConflict.addAll(sperrenInConflictChunk);
      }
      return sperrenInConflict;
    } catch (Exception ex) {
      throw new DokumentSperreRepositoryException("Error finding Sperren in conflict with transactionId="
          + transactionId, ex);
    }
  }

  @Override
  public List<Sperre> findConflictSperrenForBearbeiter(String bearbeitername, SperreEintrag... sperreEintraege)
      throws DokumentSperreRepositoryException {
    try {
      return find(
          prepareLockedDocumentEntriesQuery(
              QUERY_LOCKED_LOCK_ENTRIES_OMIT_ALREADY_REGISTERED_FOR_BEARBEITER,
              sperreEintraege
          ), bearbeitername)
          .withLock(LockModeType.OPTIMISTIC)
          .list();
    } catch (Exception ex) {
      throw new DokumentSperreRepositoryException("Error findig Sperren in conflict with bearbeitername="
          + bearbeitername, ex);
    }
  }

  private String prepareLockedDocumentEntriesQuery(String query, SperreEintrag... sperreEintraege) {
    StringBuilder sb = new StringBuilder(sperreEintraege.length * 64);
    sb.append(query);
    boolean notFirst = false;
    for (SperreEintrag sperreEintrag : sperreEintraege) {
      if (notFirst) {
        sb.append(',');
      } else {
        sb.append('(');
        notFirst = true;
      }
      sb.append("('").append(sperreEintrag.getTargetId()).append("','");
      sb.append(sperreEintrag.getTargetType()).append("')");
    }
    sb.append(")");
    String result = sb.toString();
    log.debug("Created LockEntry query: {}", result);
    return result;
  }

  private Collection<Sperre> getSperres(String query, String fieldname, String value)
      throws DokumentSperreRepositoryException {
    List<Sperre> sperres;
    try {
      sperres = find(query, value).list();
    } catch (Exception ex) {
      throw new DokumentSperreRepositoryException(
          "Unable to obtain sperres for " + fieldname + " = '" + value + "' due to " + ex.getMessage(), ex);
    }
    return sperres;
  }

}
