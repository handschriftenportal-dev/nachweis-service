package de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.SchemaPflegeDateiMultipartBody;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.SchemaPflegeDateiServicePort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.03.2022
 */

@ApplicationScoped
@Slf4j
public class SchemaPflegeDateiService implements SchemaPflegeDateiBoundary, Serializable {

  @Serial
  private static final long serialVersionUID = 4551047635597078744L;

  private final SchemaPflegeDateiServicePort servicePort;

  @Inject
  public SchemaPflegeDateiService(@RestClient SchemaPflegeDateiServicePort servicePort) {
    this.servicePort = servicePort;
  }

  @Override
  public Optional<SchemaPflegeDatei> findById(String id) {
    log.info("findById: id={}", id);
    return Optional.ofNullable(servicePort.findById(id));
  }

  @Override
  public Set<SchemaPflegeDatei> findAll() {
    log.info("findAll");
    return new LinkedHashSet<>(servicePort.findAll());
  }

  @Override
  public boolean upload(String id, String version, String bearbeiterName, InputStream datei) {
    log.info("upload: id={}", id);

    try {
      byte[] bytes = datei.readAllBytes();
      SchemaPflegeDateiMultipartBody multipartBody =
          new SchemaPflegeDateiMultipartBody(version, bearbeiterName, bytes);

      try (Response response = servicePort.upload(id, multipartBody)) {
        return response != null
            && response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL;
      }
    } catch (Exception exception) {
      log.error("upload failed: id={}", id, exception);
      return false;
    }
  }
}
