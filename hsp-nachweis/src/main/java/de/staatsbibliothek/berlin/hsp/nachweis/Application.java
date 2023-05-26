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

import com.netflix.discovery.EurekaClient;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaImportConsumer;
import de.staatsbibliothek.berlin.javaee.authentication.OAUTH2;
import de.staatsbibliothek.berlin.javaee.authentication.filter.OAUTH2RequestFilter;
import de.staatsbibliothek.berlin.javaee.authentication.service.AuthenticationService;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 04.06.2019.
 * <p>
 * Class for Bootstrap and configure the application
 */

@Singleton
@Named("appInfo")
public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  private EurekaClient eurekaClient;

  //private OAUTH2RequestFilter oauth2RequestFilter;
  //private AuthenticationService authenticationService;

  private String hspVersion;
  private String version;
  private KafkaImportConsumer consumer;
  private boolean activeImportConsumer;

  Application() {
  }

  @Inject
  public Application(
      KafkaImportConsumer consumer, EurekaClient eurekaClient,
      @OAUTH2 AuthenticationService authenticationService,
      OAUTH2RequestFilter oauth2RequestFilter,
      @ConfigProperty(name = "kafka.import.active") boolean activeImportConsumer,
      @ConfigProperty(name = "hsp.version") String hspVersion,
      @ConfigProperty(name = "nachweis.version") String version) {

    this.consumer = consumer;
    this.eurekaClient = eurekaClient;
    this.activeImportConsumer = activeImportConsumer;
    this.hspVersion = hspVersion;
    this.version = version;
    //this.oauth2RequestFilter = oauth2RequestFilter;
    //this.authenticationService = authenticationService;
  }

  public void onStart(@Observes StartupEvent ev) {

    logger.info(
        "------------------ Application HSP Nachweis Service has been started sucessfully ------------------");

    if (activeImportConsumer) {

      consumer.createConsumer();
      consumer.startConsumer();
    }
  }

  public void onStop(@Observes ShutdownEvent ev) {

    if (activeImportConsumer) {
      consumer.stopConsumer();
    }
    eurekaClient.shutdown();
    logger.info(
        "------------------ Application HSP Nachweis Service shutdown sucessfully ------------------");
  }

  public String getVersion() {
    return version;
  }

  public String getHspVersion() {
    return hspVersion;
  }
}
