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

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.KulturObjektRegistrierenModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.util.Objects;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.experimental.Delegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.08.2020.
 * @version 1.0
 */

@Named
@ViewScoped
@LoginCheck
public class KulturObjektRegistrierenController implements Serializable {

  private static final long serialVersionUID = 8965795879732550037L;

  private static final Logger logger = LoggerFactory.getLogger(KulturObjektRegistrierenController.class);

  private transient NormdatenReferenzBoundary normdatenReferenzBoundary;

  @Delegate
  private KulturObjektRegistrierenModel dataModel;

  @Inject
  public KulturObjektRegistrierenController(
      NormdatenReferenzBoundary normdatenReferenzBoundary,
      KulturObjektRegistrierenModel dataModel) {
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
    this.dataModel = dataModel;
  }

  public void placeChangedListener() {
    logger.info("Change Ort and active Koerperschaft");

    dataModel.getKoerperschaftViewModelMap().clear();

    if (Objects.isNull(dataModel.getOrteViewModel())) {
      return;
    }

    normdatenReferenzBoundary.findKoerperschaftenByOrtId(dataModel.getOrteViewModel().getId(), false)
        .stream()
        .forEach(normdatenKoerperschaftViewModel -> dataModel.getKoerperschaftViewModelMap()
            .put(normdatenKoerperschaftViewModel.getName(), normdatenKoerperschaftViewModel));
  }
}
