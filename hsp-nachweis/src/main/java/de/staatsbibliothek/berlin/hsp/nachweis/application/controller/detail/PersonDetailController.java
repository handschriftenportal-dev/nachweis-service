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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: Christoph Marten on 10.03.2021 at 14:50
 */
@ViewScoped
@Named
@Slf4j
public class PersonDetailController implements Serializable {

  private static final long serialVersionUID = 5348351518003799626L;
  private transient NormdatenReferenzBoundary normdatenReferenzBoundary;
  private transient NormdatenReferenz viewModel;

  @Inject
  public PersonDetailController(NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @PostConstruct
  void setup() {
    if (Objects.nonNull(FacesContext.getCurrentInstance()) && Objects.nonNull(
        FacesContext.getCurrentInstance().getExternalContext())) {

      FacesContext facesContext = FacesContext.getCurrentInstance();
      ExternalContext externalContext = facesContext.getExternalContext();

      if (Objects.nonNull(externalContext)) {
        final String personID = externalContext.getRequestParameterMap().get("id");
        if (Objects.nonNull(personID) && !personID.isEmpty()) {
          normdatenReferenzBoundary.findOneByIdOrNameAndType(personID, NormdatenReferenz.PERSON_TYPE_NAME)
              .ifPresent(normDatenPersonView -> viewModel = normDatenPersonView);
        }
      }
    }
  }

  public NormdatenReferenz getViewModel() {
    return viewModel;
  }

}
