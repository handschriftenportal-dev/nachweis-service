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

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator.ISO_639_1_TYPE;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator.ISO_639_2_TYPE;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konstantin.goerlitz@sbb.spk-berlin.de on 16.07.2021.
 */

@ViewScoped
@Slf4j
@Named
public class SpracheDetailController implements Serializable {

  private static final long serialVersionUID = -3160714450958901126L;
  private NormdatenReferenz model;
  private transient NormdatenReferenzBoundary normdatenReferenzBoundary;

  @Inject
  public SpracheDetailController(NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @PostConstruct
  public void setup() {
    if (Objects.nonNull(FacesContext.getCurrentInstance())) {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      ExternalContext externalContext = facesContext.getExternalContext();

      if (Objects.nonNull(externalContext)) {
        final String id = externalContext.getRequestParameterMap().get("id");
        if (Objects.nonNull(id) && !id.isEmpty()) {
          normdatenReferenzBoundary.findSpracheById(id)
              .ifPresent(sprache -> model = sprache);
        }
      }
    }
  }

  public NormdatenReferenz getModel() {
    return model;
  }

  void setModel(NormdatenReferenz model) {
    this.model = model;
  }

  public Set<Identifikator> getIso6391Identifikatoren() {
    return Optional.ofNullable(model)
        .map(sprache -> sprache.getIdentifikatorByTypeName(ISO_639_1_TYPE))
        .orElse(Collections.emptySet());
  }

  public Set<Identifikator> getIso6392Identifikatoren() {
    return Optional.ofNullable(model)
        .map(sprache -> sprache.getIdentifikatorByTypeName(ISO_639_2_TYPE))
        .orElse(Collections.emptySet());
  }

}
