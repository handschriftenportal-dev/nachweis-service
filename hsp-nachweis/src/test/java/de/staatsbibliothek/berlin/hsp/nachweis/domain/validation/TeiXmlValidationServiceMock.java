package de.staatsbibliothek.berlin.hsp.nachweis.domain.validation;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.TeiXmlValidationServicePort;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import java.util.Locale;
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
          Locale locale = Locale.forLanguageTag(i.getArguments()[1].toString());
          if ("invalid".equals(i.getArguments()[0].toString())) {
            return new ValidationResult(false, "1", "1",
                I18NController.getMessage(locale, "validation_result_failed"));
          } else {
            return new ValidationResult(true, "", "",
                I18NController.getMessage(locale, "validation_result_successfull"));
          }
        });
    setValidationServicePort(teiXmlValidationServicePort);
  }


}
