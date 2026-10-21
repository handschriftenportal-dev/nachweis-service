package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects.ManuscriptaMediaevaliaController.HSP_PRAESENTATION_BASE_URL;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects.ManuscriptaMediaevaliaController.HTTP_BILDER_MANUSCRIPTA_MEDIAEVALIA_DE;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogRepository;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestScoped
@MMRedirectStrategies.Kataloge
public class MMKatalogURICreation implements MMDynamicURICreationStrategie {

  private final KatalogRepository katalogRepository;

  @Inject
  public MMKatalogURICreation(KatalogRepository katalogRepository) {
    this.katalogRepository = katalogRepository;
  }

  @Override
  public URI create(String path) {

    log.info("Creating URi for MM Katalog Redirect {}", path);

    final String number = extractObjectNumberFromPath(path);

    log.info("Extract Number {} ", number);

    if (!number.isEmpty()) {
      return katalogRepository.findByHskId(number)
          .map(b -> URI.create(
              HSP_PRAESENTATION_BASE_URL + "?id=" + b.getDigitalisat().getManifestURL().toString()))
          .orElse(URI.create(HTTP_BILDER_MANUSCRIPTA_MEDIAEVALIA_DE + path));
    }

    return URI.create(HSP_PRAESENTATION_BASE_URL);

  }

  protected String extractObjectNumberFromPath(String path) {

    Pattern pattern = Pattern.compile("(HSK|hsk)[\\d]{3,4}");
    Matcher matcher = pattern.matcher(path);
    if (matcher.find()) {

      String hskid = matcher.group().replaceAll("HSK|hsk", "");

      return hskid.substring(1);
    }
    return "";
  }
}
