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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.10.2020.
 * @version 1.0
 */

@SessionScoped
@Named
@Data
@Slf4j
@LoginCheck
public class DigitalisatErfassenModel implements Serializable {

  private static final long serialVersionUID = -4968518784266012126L;

  private KulturObjektDokumentViewModel kulturObjektDokumentViewModel;

  private DigitalisatViewModel digitalisatViewModel;

  private Map<String, NormdatenReferenz> koerperschaftViewModelMap = new HashMap<>();

  private Map<String, DigitalisatTyp> digitalisatTypen = new TreeMap<>();

  @PostConstruct
  public void construct() {

    if (digitalisatViewModel == null) {
      log.info("Creating new Digitalisat Erfassen Model");
      digitalisatViewModel = new DigitalisatViewModel();
    }

    digitalisatTypen.put("Komplett vom Original", DigitalisatTyp.KOMPLETT_VOM_ORIGINAL);
    digitalisatTypen.put("Komplett von Reproduktion", DigitalisatTyp.KOMPLETT_VON_REPRODUKTION);
    digitalisatTypen.put("Wasserzeichen", DigitalisatTyp.WASSERZEICHEN);
    digitalisatTypen.put("Teil von Original", DigitalisatTyp.TEIL_VOM_ORIGINAL);
    digitalisatTypen.put("Teil von Reproduktion", DigitalisatTyp.TEIL_VON_REPRODUKTION);
  }
}
