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

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator.ISIL_IDENTIFIER_TYPE;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 13.08.2020.
 * @version 1.0
 */

@ViewScoped
@Named
public class KoerperschaftsDetailController implements Serializable {

  private static final long serialVersionUID = -2472185105889279485L;

  private final transient NormdatenReferenzBoundary normdatenReferenzBoundary;
  private transient NormdatenReferenz model;

  @Inject
  public KoerperschaftsDetailController(
      NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @PostConstruct
  public void setup() {
    if (Objects.nonNull(FacesContext.getCurrentInstance())) {
      ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

      if (Objects.nonNull(externalContext)) {
        final String id = externalContext.getRequestParameterMap().get("id");
        if (Objects.nonNull(id) && !id.isEmpty()) {

          final Optional<NormdatenReferenz> koerperschaftView = normdatenReferenzBoundary
              .findOneByIdOrNameAndType(id, NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);

          koerperschaftView.ifPresent(normdatenKoerperschaftViewModel -> this.model = normdatenKoerperschaftViewModel);
        }
      }
    }
  }

  public Identifikator findIsil() {
    return Optional.ofNullable(this.model)
        .stream()
        .map(koerperschaft -> koerperschaft.getIdentifikatorByTypeName(ISIL_IDENTIFIER_TYPE))
        .flatMap(Collection::stream)
        .findFirst()
        .orElse(null);
  }

  public NormdatenReferenz getModel() {
    return model;
  }

  void setModel(NormdatenReferenz model) {
    this.model = model;
  }
}
