package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GeloeschtesDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.PapierkorbModel;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
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
          .contentLength((long) data.length)
          .build();
    } else {
      return null;
    }
  }

}
