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
