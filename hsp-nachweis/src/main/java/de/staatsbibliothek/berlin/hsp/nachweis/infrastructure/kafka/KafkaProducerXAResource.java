package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.CommonXAResource;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.HSPXAException;
import org.slf4j.LoggerFactory;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import static javax.transaction.xa.XAException.XAER_INVAL;
import static javax.transaction.xa.XAException.XA_RBCOMMFAIL;

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
