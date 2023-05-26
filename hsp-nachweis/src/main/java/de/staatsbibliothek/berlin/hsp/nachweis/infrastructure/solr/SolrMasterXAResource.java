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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import static javax.transaction.xa.XAException.XAER_RMFAIL;
import static javax.transaction.xa.XAException.XA_RBCOMMFAIL;
import static javax.transaction.xa.XAException.XA_RBOTHER;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.CommonXAResource;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.HSPXAException;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import org.slf4j.LoggerFactory;

/**
 * SOLR mapping of the industry standard XA interface based on the X/Open CAE Specification (Distributed Transaction
 * Processing: The XA Specification).
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 25.02.21
 */
public class SolrMasterXAResource extends CommonXAResource {

  public static final int MAX_AGGREGATED_SUCH_DOCUMENTS_FOR_UPDATE = 20;
  public static final int MAX_AGGREGATED_IDS_FOR_DELETE = 500;
  List<SolrMasterAction> storedActions = new ArrayList<>();
  private SolrSuchAdapter solrSuchAdapter;


  public SolrMasterXAResource(SolrSuchAdapter solrSuchAdapter) {
    super(LoggerFactory.getLogger(SolrMasterXAResource.class));
    transactionTimeout = 60;
    this.solrSuchAdapter = solrSuchAdapter;
  }

  //Begin
  @Override
  public boolean setTransactionTimeout(int transactionTimeout) throws XAException {
    logger.info("[SOLR]---->>>>setTransactionTimeout {}", transactionTimeout);
    this.transactionTimeout = transactionTimeout;
    return true;
  }

  @Override
  public void start(Xid xid, int i) throws XAException {
    super.start(xid, i);
    solrSuchAdapter.beginTransaction(txId, this);
  }

  @Override
  public void commit(Xid xid, boolean b) throws XAException {
    logger.info("[SOLR]---->>>>commit {} {}", xid, b);
    prepareOrCommitTransaction(xid, XAER_RMFAIL);
  }

  @Override
  public int getTransactionTimeout() throws XAException {
    return transactionTimeout;
  }

  @Override
  public int prepare(Xid xid) throws XAException {
    logger.info("[SOLR]---->>>>prepare {}", xid);
    prepareOrCommitTransaction(xid, XA_RBOTHER);
    return XA_RDONLY;
  }

  private void prepareOrCommitTransaction(Xid xid, int errorCode) throws HSPXAException {
    checkTransaction(xid);
    try {
      solrSuchAdapter.prepareTransaction(txId);
    } catch (SolrServiceException e) {
      logger.error("Unable to commit transaction {}", e.getMessage(), e);
      throw new HSPXAException("Unable to commit transaction " + e.getMessage(), e, errorCode);
    }
  }


  @Override
  public void rollback(Xid xid) throws XAException {
    logger.info("[SOLR]---->>>>rollback {}", xid);
    checkTransaction(xid);
    try {
      solrSuchAdapter.rollbackTransaction(txId);
    } catch (SolrServiceException e) {
      logger.error("Unable to rollback transaction {}", e.getMessage(), e);
      throw new HSPXAException("Unable to rollback transaction " + e.getMessage(), e, XA_RBCOMMFAIL);
    }
  }

  List<SolrMasterAction> getStoredActions() {
    return storedActions;
  }

  void storeActionInTransaction(SolrMasterAction action) {
    if (storedActions.isEmpty()) {
      storedActions.add(action);
    } else {
      mergeOrExtendStoredActions(action);
    }
  }

  private void mergeOrExtendStoredActions(SolrMasterAction action) {
    SolrMasterAction lastStoredAction = storedActions.get(storedActions.size() - 1);
    if (lastStoredAction.getType() == action.getType()) {
      switch (lastStoredAction.getType()) {
        case DELETE:
          List<String> idSToDelete = lastStoredAction.getIdSToDelete();
          if (idSToDelete.size() < MAX_AGGREGATED_IDS_FOR_DELETE) {
            idSToDelete.addAll(action.getIdSToDelete());
          } else {
            storedActions.add(action);
          }
          break;
        case UPDATE:
        case DELETE_BEFORE_UPDATE:
          List<SuchDokument> suchDokumentsToUpdate = lastStoredAction.getSuchDokumentsToUpdate();
          if (suchDokumentsToUpdate.size() < MAX_AGGREGATED_SUCH_DOCUMENTS_FOR_UPDATE) {
            suchDokumentsToUpdate.addAll(action.getSuchDokumentsToUpdate());
          } else {
            storedActions.add(action);
          }
          break;
        case DELETE_ALL:
          logger.debug("Ignored second call to deleteAll");
          break;
      }
    } else {
      storedActions.add(action);
    }
  }
}
