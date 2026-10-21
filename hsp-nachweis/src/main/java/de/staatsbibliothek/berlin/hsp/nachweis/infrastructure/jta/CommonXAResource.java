package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta;

import com.arjuna.ats.jta.transaction.Transaction;
import com.arjuna.ats.jta.xa.XidImple;
import org.slf4j.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import static javax.transaction.xa.XAException.XAER_DUPID;
import static javax.transaction.xa.XAException.XAER_NOTA;

/**
 * Common part mapping of the industry standard XA interface based on the X/Open CAE specification
 * (Distributed transaction processing: The XA specification).
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 04.03.21
 */
public abstract class CommonXAResource implements XAResource {

    protected Logger logger;

    protected Xid currentXid;

    protected String txId;

    protected int transactionTimeout;

    protected CommonXAResource(Logger logger) {
        this.logger = logger;
    }

    protected static String getTxUid(Xid xid) {
        XidImple xidImple = (XidImple) xid;
        return xidImple.getTransactionUid().stringForm();
    }

    public static String getTransactionId(jakarta.transaction.Transaction jtaTransaction) {
        if (!(jtaTransaction instanceof Transaction)) {
            throw new RuntimeException("Unknown Transaction Type");
        }
        Transaction transaction = (Transaction) jtaTransaction;
        XidImple txId = (XidImple) transaction.getTxId();
        String transactionId = txId.getTransactionUid().stringForm();
        return transactionId;
    }

    @Override
    public void start(Xid xid, int i) throws XAException {
        logger.info("---->>>>start {} {}", xid, i);
        if (currentXid == null) {
            currentXid = xid;
            String uid = getTxUid(xid);
            this.txId = uid;
        } else if (currentXid.equals(xid)) {
            String errorMsg = "The XAResource already initialized with this XID: " + xid;
            logger.error(errorMsg);
            throw new HSPXAException(errorMsg, XAER_DUPID);
        } else {
            String errorMsg =
                    "The XAResource already initialized with the XID: "
                            + currentXid
                            + " but a new xid is provided "
                            + xid;
            logger.error(errorMsg);
            throw new HSPXAException(errorMsg, XAER_NOTA);
        }
        logger.info("Try to begin transaction for {}", txId);
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        boolean isSameRM = this.equals(xaResource);
        logger.info("---->>>>isSameRM {}", isSameRM);
        return isSameRM;
    }

    @Override
    public void end(Xid xid, int i) throws XAException {
        logger.info("---->>>>end {} {}", xid, i);
    }

    @Override
    public void forget(Xid xid) throws XAException {
        logger.info("---->>>>forget {}", xid);
    }

    @Override
    public Xid[] recover(int i) throws XAException {
        logger.info("---->>>>recover {}", i);
        Xid[] result = {currentXid};
        return result;
    }

    protected String checkTransaction(Xid xid) {
        String newTxId = getTxUid(xid);
        if (!newTxId.equals(txId)) {
            throw new RuntimeException(
                    "Owned transactionID " + txId + " different then provided one " + newTxId);
        }
        return newTxId;
    }
}
