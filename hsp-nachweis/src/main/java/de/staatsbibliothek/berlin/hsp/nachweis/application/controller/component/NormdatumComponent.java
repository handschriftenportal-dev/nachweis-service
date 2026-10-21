package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.component;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UINamingContainer;
import org.apache.commons.lang3.StringUtils;

@FacesComponent(value = "normdatumComponent")
public class NormdatumComponent extends UINamingContainer {

  public String formatText(String textKey, String text) {
    if (Stream.of(textKey, text).anyMatch(StringUtils::isEmpty)) {
      return text;
    }

    return Arrays.stream(text.split(";"))
        .map(subText -> I18NController.getMessage(textKey + subText))
        .collect(Collectors.joining("; "));
  }
}
