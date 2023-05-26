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

package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import java.util.Objects;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 04.05.2021.
 * @version 1.0
 */

@Named
@FacesConverter(value = "gndEntityConverter", managed = true)
public class NormdatenGNDEntityConverter implements Converter<GNDEntityFact> {

  public static final String SEPARATOR = "#SEP#";

  @Override
  public GNDEntityFact getAsObject(FacesContext facesContext, UIComponent uiComponent, String s)
      throws ConverterException {

    String[] fact = s.split(SEPARATOR);
    if (fact.length == 4) {
      return new GNDEntityFact(fact[0], fact[1], fact[2], fact[3]);
    } else if (fact.length == 3) {
      return new GNDEntityFact(fact[0], fact[1], fact[2]);
    }
    return null;
  }

  @Override
  public String getAsString(FacesContext facesContext, UIComponent uiComponent,
      GNDEntityFact gndEntityFact) throws ConverterException {
    return gndEntityFact.getId() + SEPARATOR
        + gndEntityFact.getGndIdentifier() + SEPARATOR
        + gndEntityFact.getPreferredName()
        + (Objects.nonNull(gndEntityFact.getTypeName()) ? SEPARATOR + gndEntityFact.getTypeName() : "");
  }
}
