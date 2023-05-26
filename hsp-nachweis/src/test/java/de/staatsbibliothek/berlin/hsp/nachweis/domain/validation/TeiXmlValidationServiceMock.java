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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.ValidationResult;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.TeiXmlValidationServicePort;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import lombok.extern.slf4j.Slf4j;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 30.03.2022
 */

@Priority(1)
@Alternative
@ApplicationScoped
@Slf4j
public class TeiXmlValidationServiceMock extends TeiXmlValidationService {

  public TeiXmlValidationServiceMock() {
    TeiXmlValidationServicePort teiXmlValidationServicePort = mock(TeiXmlValidationServicePort.class);

    when(teiXmlValidationServicePort.validateTeiXml(anyString(), anyString(), anyString()))
        .thenAnswer(i -> {
          ResourceBundle resourceBundle = ResourceBundle.getBundle(I18NController.APPLICATION_MESSAGES,
              Locale.forLanguageTag(i.getArguments()[1].toString()));

          if ("invalid".equals(i.getArguments()[0].toString())) {
            return new ValidationResult(false, "1", "1",
                resourceBundle.getString("validation_result_failed"));
          } else {
            return new ValidationResult(true, "", "",
                resourceBundle.getString("validation_result_successfull"));
          }
        });
    setValidationServicePort(teiXmlValidationServicePort);
  }


}
