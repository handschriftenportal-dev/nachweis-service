package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta;

import static org.mockito.Mockito.when;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.jta.transaction.Transaction;
import com.arjuna.ats.jta.xa.XidImple;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import org.mockito.Mockito;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 18.05.21
 */
public class MockedTransactionManager {

  public TransactionManager transactionManagerMOCK = Mockito.mock(TransactionManager.class);
  public Transaction transactionMOCK = Mockito.mock(Transaction.class);
  public XidImple xidImpleMOCK = Mockito.mock((XidImple.class));
  public Uid uid = new Uid("19:05:1971:0:0");
  public String txId = uid.stringForm();

  public MockedTransactionManager() {
    try {
      when(xidImpleMOCK.getTransactionUid()).thenReturn(uid);
      when(transactionManagerMOCK.getTransaction()).thenReturn(transactionMOCK);
      when(transactionMOCK.getTxId()).thenReturn(xidImpleMOCK);
    } catch (SystemException ex) {
      throw new RuntimeException(ex);
    }
  }
}
