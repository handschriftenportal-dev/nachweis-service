package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects.ManuscriptaMediaevaliaController.HSP_PRAESENTATION_BASE_URL;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects.MMRedirectStrategies.Sozietaeten;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.enterprise.context.RequestScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestScoped
@Sozietaeten
public class MMSozietaetenURICreation implements MMDynamicURICreationStrategie {

  @Override
  public URI create(String path) {

    final String number = extractSozietaetenNumberFromPath(path);

    if (!number.isEmpty()) {
      return URI.create(
          HSP_PRAESENTATION_BASE_URL + "search?q=%22" + number.replace("soz", "soz_") + "%22");
    }

    return URI.create(HSP_PRAESENTATION_BASE_URL);
  }

  protected String extractSozietaetenNumberFromPath(String path) {

    Pattern pattern = Pattern.compile("soz[\\d]{8}");
    Matcher matcher = pattern.matcher(path);
    if (matcher.find()) {
      return matcher.group();
    }
    return "";
  }
}
