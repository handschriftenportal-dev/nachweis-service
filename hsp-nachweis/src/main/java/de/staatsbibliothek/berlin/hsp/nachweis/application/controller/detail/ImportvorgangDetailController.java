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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.detail;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgang;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgangBoundary;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 18.09.20
 */
@ViewScoped
@Named
public class ImportvorgangDetailController implements Serializable {

  private static final long serialVersionUID = -3490539787210253062L;
  
  private ImportVorgangBoundary importVorgangBoundary;

  private ImportVorgang selectedVorgang;


  ImportvorgangDetailController() {
  }

  @Inject
  public ImportvorgangDetailController(ImportVorgangBoundary importVorgangBoundary) {
    this.importVorgangBoundary = importVorgangBoundary;
  }

  @PostConstruct
  public void setup() {
    FacesContext currentInstance = FacesContext.getCurrentInstance();
    if (Objects.nonNull(currentInstance)) {
      ExternalContext externalContext = currentInstance.getExternalContext();
      if (Objects.nonNull(externalContext)) {
        String id = externalContext.getRequestParameterMap().get("id");
        fillImportVorgang(id);
      }
    }
  }

  void fillImportVorgang(String id) {
    Optional<ImportVorgang> byId = importVorgangBoundary.findById(id);
    byId.ifPresent(importVorgang -> selectedVorgang = importVorgang);
  }

  public ImportVorgang getSelectedVorgang() {
    return selectedVorgang;
  }

  public void setSelectedVorgang(ImportVorgang selectedVorgang) {
    this.selectedVorgang = selectedVorgang;
  }

  public boolean isError() {
    if (selectedVorgang == null) {
      return false;
    }
    return selectedVorgang.isError();
  }
}
