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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.validation;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.TeiXmlValidationServicePort.SCHEMA_TEI_ALL;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.TeiXmlValidationServicePort.SCHEMA_TEI_ODD;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.ValidationResult;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.TeiXmlValidationServicePort;
import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.03.2022
 */

@ApplicationScoped
@Slf4j
public class TeiXmlValidationService implements TeiXmlValidationBoundary {

  @Setter
  private TeiXmlValidationServicePort validationServicePort;

  public TeiXmlValidationService() {

  }

  @Inject
  public TeiXmlValidationService(@RestClient TeiXmlValidationServicePort validationServicePort) {
    this.validationServicePort = validationServicePort;
  }

  @Override
  public ValidationResult validateTeiXml(String teiXml, String locale, boolean odd) throws TeiXmlValidationException {
    if (Objects.isNull(teiXml) || teiXml.isEmpty()) {
      throw new TeiXmlValidationException("Can't validate empty teiXml");
    }

    ValidationResult validationResult;
    try {
      validationResult = validationServicePort.validateTeiXml(teiXml, locale, odd ? SCHEMA_TEI_ODD : SCHEMA_TEI_ALL);
    } catch (Exception e) {
      throw new TeiXmlValidationException("Error calling validationService", e);
    }

    if (Objects.isNull(validationResult)) {
      throw new TeiXmlValidationException("ValidationServicePort returned no ValidationResult!");
    }
    return validationResult;
  }

  @Override
  public boolean validateTeiXml(KulturObjektDokument kod) throws TeiXmlValidationException {
    if (Objects.nonNull(kod)) {
      return validate(kod.getTeiXML(), kod.getId(), "validierung_fehler_kod", false);
    } else {
      log.warn("Can' t validate null.");
      return false;
    }
  }

  @Override
  public boolean validateTeiXml(Beschreibung beschreibung) throws TeiXmlValidationException {
    if (Objects.nonNull(beschreibung)) {
      return validate(beschreibung.getTeiXML(), beschreibung.getId(), "validierung_fehler_beschreibung", true);
    } else {
      log.warn("Can' t validate null.");
      return false;
    }
  }

  @Override
  public boolean validateTeiXml(Katalog katalog) throws TeiXmlValidationException {
    if (Objects.nonNull(katalog)) {
      return validate(katalog.getTeiXML(), katalog.getId(), "validierung_fehler_katalog", false);
    } else {
      log.warn("Can' t validate null.");
      return false;
    }
  }

  private boolean validate(String teiXML, String id, String messageKey, boolean withOdd)
      throws TeiXmlValidationException {
    ValidationResult validationResult = validateTeiXml(teiXML, I18NController.getLocale().getLanguage(), withOdd);
    if (!validationResult.isValid()) {
      throw new TeiXmlValidationException(I18NController.getMessage(messageKey, id, validationResult.getMessage()));
    } else {
      return validationResult.isValid();
    }
  }

}
