package de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.CommonXAResource;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import lombok.extern.slf4j.Slf4j;

/**
 * Sperre mapping of the industry standard XA interface based on the X/Open CAE Specification
 * (Distributed Transaction Processing: The XA Specification).
 *
 * <p>In Sperre case on the transaction endphase (commit / rollback ) deregister process will be
 * triggered
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 19.08.21
 */
@Slf4j
public class SperreAccessXAResource extends CommonXAResource {

    private final DokumentSperreBoundary dokumentSperreBoundary;

    private final Sperre sperre;

    public SperreAccessXAResource(DokumentSperreBoundary dokumentSperreBoundary, Sperre sperre) {
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
