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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DatenDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.XMLFormate;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.primefaces.model.file.UploadedFile;

/**
 * Store State in session for the viewscoped bean
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 07.07.20
 */
@Named
@SessionScoped
@LoginCheck
public class DatenHochladenModel implements Serializable {

  private static final long serialVersionUID = -291667390716803829L;

  private UploadedFile datei;

  private DatenDokumentTyp selectedDokumentTyp;

  private XMLFormate selectedXMLFormat;

  public UploadedFile getDatei() {
    return datei;
  }

  public void setDatei(UploadedFile datei) {
    this.datei = datei;
  }

  public DatenDokumentTyp getSelectedDokumentTyp() {
    return selectedDokumentTyp;
  }

  public void setSelectedDokumentTyp(
      DatenDokumentTyp selectedDokumentTyp) {
    this.selectedDokumentTyp = selectedDokumentTyp;
  }

  public XMLFormate getSelectedXMLFormat() {
    return selectedXMLFormat;
  }

  public void setSelectedXMLFormat(XMLFormate selectedXMLFormat) {
    this.selectedXMLFormat = selectedXMLFormat;
  }
}
