package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl.SBBPURLGenPort.PARAM_MVID;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl.SBBPURLGenPort.PARAM_PID;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLConfiguration;
import io.smallrye.config.ConfigMapping;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 02.08.22
 */
@ApplicationScoped
@Slf4j
public class SBBPURLGenerator implements PURLGenerator, Serializable {

  @Serial
  private static final long serialVersionUID = 4172119821106170800L;

  private static final String STATUS_ERROR = "ERROR";

  private final String configuredUrlPrefix;

  private final SBBPURLGenPort sbbPURLGenPort;

  @Inject
  public SBBPURLGenerator(
      @ConfigMapping(prefix = "purl") PURLConfiguration configuration,
      @RestClient SBBPURLGenPort sbbPURLGenPort) {
    this.configuredUrlPrefix = configuration.resolver().url();
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
      throw new PURLGeneratorException(
          "Calling sbbPURLGenPort returned status error with message: " + response.getMessage());
    }

    String fullPid = response.getFullPid();
    if (Objects.isNull(fullPid) || fullPid.length() != 19) {
      throw new PURLGeneratorException("Calling sbbPURLGenPort returned invalid pid: " + fullPid);
    }

    return fullPid;
  }
}
