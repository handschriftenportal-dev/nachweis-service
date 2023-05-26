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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl.SBBPURLGenPort.PARAM_MVID;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl.SBBPURLGenPort.PARAM_PID;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import java.io.Serializable;
import java.net.URI;
import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 02.08.22
 */

@ApplicationScoped
@Slf4j
public class SBBPURLGenerator implements PURLGenerator, Serializable {

  private static final long serialVersionUID = 4172119821106170800L;

  private static final String STATUS_ERROR = "ERROR";

  private final String configuredUrlPrefix;

  private final SBBPURLGenPort sbbPURLGenPort;

  @Inject
  public SBBPURLGenerator(
      @ConfigProperty(name = "purlgen.resolver.url") String configuredUrlPrefix,
      @RestClient SBBPURLGenPort sbbPURLGenPort) {
    this.configuredUrlPrefix = configuredUrlPrefix;
    this.sbbPURLGenPort = sbbPURLGenPort;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public PURL createNewPURL(URI target) throws PURLGeneratorException {
    log.debug("createNewPURL: target={}", target);

    Objects.requireNonNull(target, "Target is required!");

    String url = configuredUrlPrefix + createPID();

    URI source;
    try {
      source = URI.create(url);
    } catch (IllegalArgumentException iae) {
      throw new PURLGeneratorException("Created an invalid purl: " + url);
    }

    PURL purl = new PURL(source, target, PURLTyp.INTERNAL);
    log.info("Created purl {}", purl);
    return purl;
  }

  String createPID() throws PURLGeneratorException {
    SBBPURLGenResponse response;

    try {
      response = sbbPURLGenPort.createPURL(PARAM_PID, PARAM_MVID);
    } catch (Exception e) {
      throw new PURLGeneratorException("Error calling sbbPURLGenPort!", e);
    }

    Objects.requireNonNull(response, "Calling sbbPURLGenPort returned no response!");

    if (STATUS_ERROR.equals(response.getStatus())) {
      throw new PURLGeneratorException("Calling sbbPURLGenPort returned status error with message: "
          + response.getMessage());
    }

    String fullPid = response.getFullPid();
    if (Objects.isNull(fullPid) || fullPid.length() != 19) {
      throw new PURLGeneratorException("Calling sbbPURLGenPort returned invalid pid: " + fullPid);
    }

    return fullPid;
  }
}
