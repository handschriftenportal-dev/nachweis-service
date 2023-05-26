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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.SchemaPflegeDateiMultipartBody;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.SchemaPflegeDateiServicePort;
import java.io.InputStream;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.03.2022
 */

@ApplicationScoped
@Slf4j
public class SchemaPflegeDateiService implements SchemaPflegeDateiBoundary, Serializable {

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
    SchemaPflegeDateiMultipartBody multipartBody = new SchemaPflegeDateiMultipartBody(version, bearbeiterName,
        datei);

    Response response = servicePort.upload(id, multipartBody);
    return response != null && Status.OK.getStatusCode() == response.getStatus();
  }

}
