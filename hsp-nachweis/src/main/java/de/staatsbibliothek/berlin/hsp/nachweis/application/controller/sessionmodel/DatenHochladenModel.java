package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DatenDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.XMLFormate;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.file.UploadedFile;

/**
 * Store State in session for the viewscoped bean
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 07.07.20
 */
@Setter
@Getter
@Named
@SessionScoped
@LoginCheck
public class DatenHochladenModel implements Serializable {

  @Serial
  private static final long serialVersionUID = -291667390716803829L;

  private UploadedFile datei;

  private DatenDokumentTyp selectedDokumentTyp;

  private XMLFormate selectedXMLFormat;

}
