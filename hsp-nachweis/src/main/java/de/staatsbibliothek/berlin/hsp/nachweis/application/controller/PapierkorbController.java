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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GeloeschtesDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.PapierkorbModel;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 01.12.2022
 */

@ViewScoped
@Named
@LoginCheck
@Slf4j
public class PapierkorbController implements Serializable {

  private static final long serialVersionUID = 3448181074097561643L;

  private final PapierkorbModel papierkorbModel;

  @Inject
  public PapierkorbController(PapierkorbModel papierkorbModel) {
    this.papierkorbModel = papierkorbModel;
  }

  public PapierkorbModel getPapierkorbModel() {
    return papierkorbModel;
  }

  public StreamedContent loadTeiXML(GeloeschtesDokument geloeschtesDokument) {
    if (Objects.nonNull(geloeschtesDokument) && Objects.nonNull(geloeschtesDokument.getTeiXML())) {
      log.info("loadTeiXML: id={}", geloeschtesDokument);
      byte[] data = geloeschtesDokument.getTeiXML().getBytes(StandardCharsets.UTF_8);
      return DefaultStreamedContent.builder()
          .name(geloeschtesDokument.getDokumentId() + "_tei.xml")
          .contentType(MediaType.TEXT_XML)
          .stream(() -> new ByteArrayInputStream(data))
          .contentLength(data.length)
          .build();
    } else {
      return null;
    }
  }

}
