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

package de.staatsbibliothek.berlin.hsp.nachweis;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.netflix.discovery.EurekaClient;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaImportConsumer;
import de.staatsbibliothek.berlin.javaee.authentication.filter.OAUTH2RequestFilter;
import de.staatsbibliothek.berlin.javaee.authentication.service.AuthenticationService;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 11.06.2019.
 */
@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
public class ApplicationTest {

  private final KafkaImportConsumer consumer = Mockito.mock(KafkaImportConsumer.class);
  private final EurekaClient eurekaClient = Mockito.mock(EurekaClient.class);
  private final AuthenticationService authenticationService = Mockito.mock(AuthenticationService.class);
  private final OAUTH2RequestFilter oauth2RequestFilter = Mockito.mock(OAUTH2RequestFilter.class);

  @Inject
  Application application;


  @Test
  public void test_GivenNoneStartAtive_Steup() {

    Application application = new Application(consumer, eurekaClient, authenticationService,
        oauth2RequestFilter, false, "1.0", "0.1");

    application.onStart(null);

    Mockito.verify(consumer, Mockito.times(0)).createConsumer();
    Mockito.verify(consumer, Mockito.times(0)).startConsumer();
  }

  @Test
  public void test_GivenStartAtive_Steup() {

    Application application = new Application(consumer, eurekaClient, authenticationService,
        oauth2RequestFilter, true, "1.0", "0.1");

    application.onStart(null);

    Mockito.verify(consumer, Mockito.times(1)).createConsumer();
    Mockito.verify(consumer, Mockito.times(1)).startConsumer();
  }

  @Test
  public void Given_Application_Destroy() {

    Application application = new Application(consumer, eurekaClient, authenticationService,
        oauth2RequestFilter, true, "1.0", "0.1");

    application.onStop(null);

    Mockito.verify(consumer, Mockito.times(1)).stopConsumer();
  }

  @Test
  void getVersion() {
    String version = application.getVersion();
    assertNotNull(version);
    assertTrue(version.split("\\.").length > 2);
    assertFalse("@project.version@".equals(version));
  }

  @Test
  void getHspVersion() {
    String hspVersion = application.getHspVersion();
    assertNotNull(hspVersion);
    assertTrue(hspVersion.split("\\.").length > 2);
    assertFalse("@handschriftenportal.version@".equals(hspVersion));
  }
}
