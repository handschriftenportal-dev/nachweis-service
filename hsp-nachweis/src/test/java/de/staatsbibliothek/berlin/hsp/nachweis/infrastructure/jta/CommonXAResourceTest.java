package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.when;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.jta.xa.XidImple;
import javax.transaction.xa.XAException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 09.03.21
 */
class CommonXAResourceTest {

  @Test
  void testStart() throws XAException {
    CommonXAResource commonXAResource = Mockito.mock(CommonXAResource.class, CALLS_REAL_METHODS);
    commonXAResource.logger = LoggerFactory.getLogger(CommonXAResourceTest.class);
    commonXAResource.currentXid = null;
    XidImple xidImpleMOCK = Mockito.mock(XidImple.class, CALLS_REAL_METHODS);
    Uid uid = new Uid("19:05:1971:0:0");
    when(xidImpleMOCK.getTransactionUid()).thenReturn(uid);

    commonXAResource.start(xidImpleMOCK, 0);
    assertEquals(xidImpleMOCK, commonXAResource.currentXid);

    assertThrows(HSPXAException.class, () -> {
      commonXAResource.start(xidImpleMOCK, 0);
    });
  }
}
