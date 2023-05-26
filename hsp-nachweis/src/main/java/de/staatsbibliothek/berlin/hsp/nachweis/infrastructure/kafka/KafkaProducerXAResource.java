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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import static javax.transaction.xa.XAException.XAER_INVAL;
import static javax.transaction.xa.XAException.XA_RBCOMMFAIL;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.CommonXAResource;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.HSPXAException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import org.slf4j.LoggerFactory;

/**
 * Kafka Producer mapping of the industry standard XA interface based on the X/Open CAE Specification (Distributed
 * Transaction Processing: The XA Specification).
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 16.02.21
 */
public class KafkaProducerXAResource extends CommonXAResource {

  private KafkaTransactionalProducer transactionalProducer;

  public KafkaProducerXAResource(KafkaTransactionalProducer transactionalProducer) {
    super(LoggerFactory.getLogger(KafkaProducerXAResource.class));
    this.transactionalProducer = transactionalProducer;
  }

  //Begin
  @Override
  public boolean setTransactionTimeout(int transactionTimeout) throws XAException {
    logger.info("---->>>>setTransactionTimeout {}", transactionTimeout);
    transactionalProducer.setTransactionTimeout(transactionTimeout);
    return true;
  }

  @Override
  public void start(Xid xid, int i) throws XAException {
    super.start(xid, i);
    transactionalProducer.beginTransaction(txId);
  }

  @Override
  public void commit(Xid xid, boolean b) throws XAException {
    logger.info("---->>>>commit {} {}", xid, b);
    try {
      checkTransaction(xid);
      transactionalProducer.commitTransaction(txId);
    } catch (Exception e) {
      logger.error("Unable to commit transaction {}", e.getMessage(), e);
      throw new HSPXAException("Unable to commit transaction " + e.getMessage(), e, XAER_INVAL);
    }
  }

  @Override
  public int getTransactionTimeout() throws XAException {
    return transactionalProducer.getTransactionTimeout();
  }

  @Override
  public int prepare(Xid xid) throws XAException {
    logger.info("---->>>>prepare {}", xid);
    return XA_OK;
  }

  @Override
  public void rollback(Xid xid) throws XAException {
    logger.info("---->>>>rollback {}", xid);
    try {
      checkTransaction(xid);
      transactionalProducer.rollbackTransaction(txId);
    } catch (Exception e) {
      logger.error("Unable to rollback transaction {}", e.getMessage(), e);
      throw new HSPXAException("Unable to rollback transaction " + e.getMessage(), e, XA_RBCOMMFAIL);
    }
  }

}
