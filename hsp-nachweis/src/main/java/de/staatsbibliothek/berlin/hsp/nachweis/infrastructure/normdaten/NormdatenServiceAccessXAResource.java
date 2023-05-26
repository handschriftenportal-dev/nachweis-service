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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.CommonXAResource;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.slf4j.Logger;

/**
 * Enable normdaten cache for the main transaction lifetime
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 18.05.21
 */
public class NormdatenServiceAccessXAResource extends CommonXAResource {

  public static final int UNKNOWN_TRANSACTION_STATUS = -777;
  private final Set<String> activeTransactions;
  private TransactionManager transactionManager;
  private String transactionId;

  protected NormdatenServiceAccessXAResource(Logger logger, TransactionManager transactionManager) {
    super(logger);
    this.transactionManager = transactionManager;
    this.transactionTimeout = 70;
    this.activeTransactions = new HashSet<>();
    this.transactionId = null;
  }

  @Override
  public void commit(Xid xid, boolean b) throws XAException {
    cleanUpCache();
  }

  @Override
  public int getTransactionTimeout() throws XAException {
    return transactionTimeout;
  }

  @Override
  public void start(Xid xid, int i) throws XAException {
    //Ignored for the cache clean up functionality
    logger.debug("---->>>>start {} {}", xid, i);
  }

  @Override
  public void end(Xid xid, int i) throws XAException {
    logger.debug("---->>>>end {} {}", xid, i);
  }

  @Override
  public void forget(Xid xid) throws XAException {
    logger.debug("---->>>>forget {}", xid);
  }

  @Override
  public int prepare(Xid xid) throws XAException {
    cleanUpCache();
    return XA_RDONLY;
  }

  @Override
  public void rollback(Xid xid) throws XAException {
    cleanUpCache();
  }

  @Override
  public boolean setTransactionTimeout(int transactionTimeout) throws XAException {
    this.transactionTimeout = transactionTimeout;
    return true;
  }

  void cleanUpCache() {
    try {
      Transaction jtaTransaction = transactionManager.getTransaction();
      String transactionId = getTransactionId(jtaTransaction);
      if (activeTransactions.contains(transactionId)) {
        activeTransactions.remove(transactionId);
      } else {
        logger.warn("Requested to clean up transactionId={}, but this transaction is not registered!", transactionId);
      }
    } catch (Exception ex) {
      logger.warn("Unable to unregister transaction");
    }
  }

  @Transactional
  String registerForTransaction() {
    try {
      Transaction jtaTransaction = transactionManager.getTransaction();
      String transactionId = getTransactionId(jtaTransaction);
      if (!activeTransactions.contains(transactionId) && jtaTransaction.getStatus() == Status.STATUS_ACTIVE) {
        activeTransactions.add(transactionId);
        jtaTransaction.enlistResource(this);
        logger.info("NormdatenServiceAccessXAResource for transactionID= {} with STATUS_ACTIVE registered.",
            transactionId);
        this.transactionId = transactionId;
      }
      return transactionId;
    } catch (Exception e) {
      throw new RuntimeException("Unable to enlistResource " + e.getMessage(), e);
    }
  }

  private int getTransactionStatus(Transaction jtaTransaction) {
    try {
      return jtaTransaction.getStatus();
    } catch (SystemException e) {
      return UNKNOWN_TRANSACTION_STATUS;
    }
  }

  @Override
  public boolean isSameRM(XAResource xaResource) throws XAException {
    return equals(xaResource);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NormdatenServiceAccessXAResource that = (NormdatenServiceAccessXAResource) o;
    return Objects.equals(transactionId, that.transactionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionId);
  }
}
