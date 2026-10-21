package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects.ManuscriptaMediaevaliaController.HSP_PRAESENTATION_BASE_URL;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects.MMRedirectStrategies.Signaturen;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestScoped
@Signaturen
public class MMSignaturendokumenteURICreation implements MMDynamicURICreationStrategie {

  //dokumente/html/sigrefsAdmont|||Stiftsarchiv|||Ms. 106

  private KulturObjektDokumentRepository kulturObjektDokumentRepository;

  @Inject
  public MMSignaturendokumenteURICreation(
      KulturObjektDokumentRepository kulturObjektDokumentRepository) {
    this.kulturObjektDokumentRepository = kulturObjektDokumentRepository;
  }

  @Override
  public URI create(String path) {

    final String[] minimalData = extractMinimalDatenFromPath(path);
    log.info("Found following minimal data {} in path {}", String.join(";", minimalData), path);

    if (minimalData.length == 3) {
      return findKODWithMinimaldaten(minimalData)
          .map(kod -> URI.create(HSP_PRAESENTATION_BASE_URL + "?id=" + kod.getId()))
          .orElse(URI.create(
              HSP_PRAESENTATION_BASE_URL + "/search?q=" + URLEncoder.encode(
                  minimalData[2] + "+" + minimalData[1] + "+"
                      + minimalData[0], StandardCharsets.UTF_8)));
    }

    return URI.create(HSP_PRAESENTATION_BASE_URL);
  }

  protected String[] extractMinimalDatenFromPath(String path) {
    Pattern pattern = Pattern.compile("sigrefs.*");
    Matcher matcher = pattern.matcher(path);
    if (matcher.find()) {
      String minimalDatenSection = matcher.group().replaceAll("sigrefs", "");
      return minimalDatenSection.split("\\|{3}");
    }
    return new String[]{};
  }

  Optional<KulturObjektDokument> findKODWithMinimaldaten(String[] minimalDaten) {

    List<KulturObjektDokument> allkods = kulturObjektDokumentRepository.findByIdentifikationIdent(
        minimalDaten[2]);
    allkods.addAll(
        kulturObjektDokumentRepository.findByAlternativeIdentifikationIdent(minimalDaten[2]));

    return allkods.stream()
        .peek(k -> log.info("Found KOD by signature {} ", k.getGueltigeSignatur()))
        .filter(k -> k.getGueltigeIdentifikation().getBesitzer().getName().equals(minimalDaten[1])
            || k.getGueltigeIdentifikation().getBesitzer().getVarianterName().stream().map(
            VarianterName::getName).toList().contains(minimalDaten[1]))
        .peek(k -> log.info("Found KOD by Besitzer {} ",
            k.getGueltigeIdentifikation().getBesitzer().getName()))
        .filter(k -> k.getGueltigeIdentifikation().getAufbewahrungsOrt().getName()
            .equals(minimalDaten[0]) || k.getGueltigeIdentifikation().getAufbewahrungsOrt()
            .getVarianterName().stream().map(
                VarianterName::getName).toList().contains(minimalDaten[0]))
        .peek(k -> log.info("Found KOD by Place {} ",
            k.getGueltigeIdentifikation().getAufbewahrungsOrt().getName()))
        .findFirst();

  }
}
