package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects.ManuscriptaMediaevaliaController.HSP_PRAESENTATION_BASE_URL;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects.ManuscriptaMediaevaliaController.HSP_PRAESENTATION_INFO_REDIRECT_URL;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects.MMRedirectStrategies.Beschreibungen;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogRepository;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 18.10.23.
 * @version 1.0
 */

@Slf4j
@RequestScoped
@Beschreibungen
public class MMBeschreibungURICreation implements MMDynamicURICreationStrategie {

  private final KatalogRepository katalogRepository;
  private final BeschreibungsRepository beschreibungsRepository;

  @Inject
  public MMBeschreibungURICreation(KatalogRepository katalogRepository,
      BeschreibungsRepository beschreibungsRepository) {
    this.katalogRepository = katalogRepository;
    this.beschreibungsRepository = beschreibungsRepository;
  }

  @Override
  public URI create(String path) {

    log.info("Creating URi for MM Beschreibung Redirect {}", path);

    final String number = extractMXMLNumberFromPath(path);

    log.info("Extract Number {} ", number);

    if (!number.isEmpty()) {
      return beschreibungsRepository.findByAltIdentifierOptional(number)
          .map(b -> {
            boolean hasKatalogReferenz = katalogRepository.findKatalogBeschreibungReferenzByBeschreibungsID(
                b.getId()).isPresent();
            String idParameter = hasKatalogReferenz || DokumentObjektTyp.HSP_DESCRIPTION
                .equals(b.getDokumentObjektTyp()) ?
                "?id=" + b.getId() : "?id=" + b.getKodID();

            return URI.create(HSP_PRAESENTATION_BASE_URL + idParameter);
          })
          .orElse(URI.create(HSP_PRAESENTATION_INFO_REDIRECT_URL));
    }

    return URI.create(HSP_PRAESENTATION_BASE_URL);
  }

  protected String extractMXMLNumberFromPath(String path) {

    Pattern pattern = Pattern.compile("obj[\\d]{8}($|,T$|$\\.html||,T\\.html)");
    Matcher matcher = pattern.matcher(path);
    if (matcher.find()) {
      return matcher.group().replaceAll("\\.html", "")
          .replaceAll("obj", "MXML-").replaceAll(",", "-");
    }
    return "";
  }
}
