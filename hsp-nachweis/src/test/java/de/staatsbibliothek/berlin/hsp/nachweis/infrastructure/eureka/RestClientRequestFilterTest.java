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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.eureka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.netflix.appinfo.InstanceInfo;
import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 12.01.2022.
 * @version 1.0
 */
public class RestClientRequestFilterTest {

  @Test
  void testExtractVipServiceName() {
    RestClientRequestFilter restClientRequestFilter = new RestClientRequestFilter();
    assertEquals("IMPORTSERVICE",
        restClientRequestFilter.extractVipServiceName(URI.create("http://IMPORTSERVICE/rest")));

    assertEquals("HSP-FO-DISCOVERY",
        restClientRequestFilter.extractVipServiceName(URI.create("http://HSP-FO-DISCOVERY/rest")));
  }

  @Test
  void testISSecure() {
    RestClientRequestFilter restClientRequestFilter = new RestClientRequestFilter();
    assertFalse(restClientRequestFilter.isSecure(URI.create("")));

    assertTrue(restClientRequestFilter.isSecure(URI.create("https://IMPORTSERVICE/rest")));
  }

  @Test
  void testcreateNewURI() {
    RestClientRequestFilter restClientRequestFilter = new RestClientRequestFilter();

    InstanceInfo instanceInfo = Mockito.mock(InstanceInfo.class);

    Mockito.when(instanceInfo.getHostName()).thenReturn("b-dev1047.pk.de");
    Mockito.when(instanceInfo.getPort()).thenReturn(9298);

    Assertions.assertEquals("http://b-dev1047.pk.de:9298/rest",
        restClientRequestFilter.createNewURI(URI.create("http://IMPORTSERVICE/rest"),
            "IMPORTSERVICE"
            , false, instanceInfo).toASCIIString());
  }
}
