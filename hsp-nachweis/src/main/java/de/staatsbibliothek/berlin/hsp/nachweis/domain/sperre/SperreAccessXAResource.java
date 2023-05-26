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
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.CommonXAResource;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import lombok.extern.slf4j.Slf4j;

/**
 * Sperre mapping of the industry standard XA interface based on the X/Open CAE Specification (Distributed Transaction
 * Processing: The XA Specification).
 *
 * In Sperre case on the transaction endphase (commit / rollback ) deregister process will be triggered
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 19.08.21
 */
@Slf4j
public class SperreAccessXAResource extends CommonXAResource {

  private final DokumentSperreBoundary dokumentSperreBoundary;

  private final Sperre sperre;


  public SperreAccessXAResource(DokumentSperreBoundary dokumentSperreBoundary,
      Sperre sperre) {
    super(log);
    this.dokumentSperreBoundary = dokumentSperreBoundary;
    this.sperre = sperre;
  }

  @Override
  public int getTransactionTimeout() throws XAException {
    return transactionTimeout;
  }

  @Override
  public boolean setTransactionTimeout(int i) throws XAException {
    transactionTimeout = i;
    return true;
  }

  public Sperre getSperre() {
    return sperre;
  }

  @Override
  public int prepare(Xid xid) throws XAException {
    return XA_OK;
  }

  @Override
  public void commit(Xid xid, boolean b) throws XAException {
    commitOrRollback(xid);
  }

  @Override
  public void rollback(Xid xid) throws XAException {
    commitOrRollback(xid);
  }

  @Transactional(TxType.REQUIRES_NEW)
  boolean cleanUpSperre() {
    if (sperre == null) {
      logger.warn("Sperre ist null!");
      return false;
    }
    try {
      logger.info("Release Sperre after transaction via XAResource {}", sperre);
      dokumentSperreBoundary.releaseSperre(sperre);
      return true;
    } catch (Exception e) {
      logger.error("Unable to release Sperre due to {}", e.getMessage(), e);
      return false;
    }
  }

  private void commitOrRollback(Xid xid) {
    logger.info("commitOrRollback... ");
    checkTransaction(xid);
    cleanUpSperre();
  }


}
