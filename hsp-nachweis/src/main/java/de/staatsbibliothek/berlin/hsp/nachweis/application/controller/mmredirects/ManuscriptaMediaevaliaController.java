package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects.MMRedirectStrategies.Beschreibungen;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects.MMRedirectStrategies.Signaturen;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects.MMRedirectStrategies.Sozietaeten;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 17.10.23.
 * @version 1.0
 * <p>
 * This Controller should handle all HTTP request to the former Domain manuscripta-mediaevalia and redirect to new platforms
 */

@Path("/")
public class ManuscriptaMediaevaliaController {

  @Getter
  public static final Map<Pattern, URI> staticRedirects = new ConcurrentHashMap<>();

  private static final Map<Pattern, MMDynamicURICreationStrategie> dynamicPattern = new ConcurrentHashMap<>();
  private static final Logger logger = LoggerFactory.getLogger(
      ManuscriptaMediaevaliaController.class);
  public static final String HTTP_BILDER_MANUSCRIPTA_MEDIAEVALIA_DE = "http://bilder.manuscripta-mediaevalia.de/";
  public static final String NO_CACHE_NO_STORE = "no-cache no-store";

  public static final String HSP_PRAESENTATION_BASE_URL = "https://handschriftenportal.de/";

  public static final String HSP_PRAESENTATION_INFO_REDIRECT_URL = "https://handschriftenportal.de/info/redirect";

  @Inject
  public ManuscriptaMediaevaliaController(
      @Beschreibungen MMDynamicURICreationStrategie mmBeschreibungURICreation,
      @MMRedirectStrategies.Kataloge MMDynamicURICreationStrategie mmKatalogURICreation,
      @Sozietaeten MMDynamicURICreationStrategie mmSozietaetenURICreation,
      @Signaturen MMSignaturendokumenteURICreation mmSignaturendokumenteURICreation) {
    dynamicPattern.put(Pattern.compile(".*obj[\\d]{8}($|,T$|$\\.html||,T\\.html)"),
        mmBeschreibungURICreation);
    dynamicPattern.put(Pattern.compile(".*(HSK|hsk)[\\d]{3,4}.*"), mmKatalogURICreation);
    dynamicPattern.put(Pattern.compile(".*soz[\\d]{8}.*"), mmSozietaetenURICreation);
    dynamicPattern.put(Pattern.compile(".*sigrefs.*"), mmSignaturendokumenteURICreation);
  }


  static {
    staticRedirects.put(Pattern.compile("(/)?info/projekte.html"),
        URI.create("https://handschriftenportal.de/projects"));
    staticRedirects.put(Pattern.compile("(/)?info/aktuelles.html"),
        URI.create("https://handschriftenportal.de/info/content"));
    staticRedirects.put(Pattern.compile("(/)?hs/kataloge/HSKRICH.htm"),
        URI.create(
            "https://www.ub.uni-leipzig.de/fileadmin/Resources/Public/images/Bilder_Forschungsbibliothek/Handschriftenzentrum/richtlinien_handschriftenkatalogisierung_1992.pdf"));
    staticRedirects.put(Pattern.compile("(/)?hs/Rechtshandschriften-Handreichung10-04.pdf"),
        URI.create(
            "http://www.handschriftenzentren.de/wp-content/uploads/2022/08/Rechtshandschriften-Handreichung10-04.pdf"));
    staticRedirects.put(
        Pattern.compile("(/)?hs/konzeptpapier_2011_bestandsliste_publikationsversion.pdf"),
        URI.create(
            "http://www.handschriftenzentren.de/wp-content/uploads/2016/10/konzeptpapier_2011_bestandsliste_publikationsversion.pdf"));
    staticRedirects.put(Pattern.compile("(/)?hs/KonzeptpapierBestandliste09-04.pdf"),
        URI.create(
            "http://www.handschriftenzentren.de/wp-content/uploads/2022/08/KonzeptpapierBestandliste09-04.pdf"));
    staticRedirects.put(Pattern.compile("(/)?hs/protokoll_Stuttgart_040403.pdf"),
        URI.create(
            "http://www.handschriftenzentren.de/wp-content/uploads/2022/08/protokoll_Stuttgart_040403.pdf"));
    staticRedirects.put(Pattern.compile("(/)?hs/probleme_katalog_handschriften_Leipzig_0410.pdf"),
        URI.create(
            "http://www.handschriftenzentren.de/wp-content/uploads/2022/08/Rechtshandschriften-Handreichung10-04.pdf"));
    staticRedirects.put(Pattern.compile("(/)?gaeste/grotefend/grotefend.htm"),
        URI.create(
            "https://www.handschriftenzentren.de/materialien/grotefend/"));
    staticRedirects.put(Pattern.compile("(/)?hs/Limbeck_Neuzeitl_Handschr.pdf"),
        URI.create(
            "https://www.hab.de/wp-content/uploads/2021/01/hab_vortrag_neuzeitliche_handschriften_limbeck.pdf"));
  }

  @APIResponse(responseCode = "301", description = "Redirect to old MM-Pages or Handschriftenzentrum")
  @GET
  @Path("/manuscripta/{mmresource:.*}")
  public Response handleRequestForStaticMMRedirect(@PathParam("mmresource") String mmresource) {

    logger.info("Getting ManuscriptaMediaevalia Request for {}", mmresource);
    final Optional<MMDynamicURICreationStrategie> properDynamicStrategie = findProperDynamicStrategie(
        mmresource);
    final Optional<URI> staticURI = findProperStaticStrategie(mmresource);

    if (staticURI.isPresent()) {
      logger.info("Found Static Redirect ... {}", mmresource);
      return Response.status(Status.MOVED_PERMANENTLY)
          .location(staticURI.get())
          .header(HttpHeaders.CACHE_CONTROL, NO_CACHE_NO_STORE)
          .build();
    } else if (properDynamicStrategie.isPresent()) {

      logger.info("Found Dynamic Redirect ... {}", mmresource);
      return Response.status(Status.MOVED_PERMANENTLY)
          .location(properDynamicStrategie.get().create(mmresource))
          .header(HttpHeaders.CACHE_CONTROL, NO_CACHE_NO_STORE)
          .build();
    } else {
      logger.info("Default MM Redirect ... {}", mmresource);
      return Response.status(Status.MOVED_PERMANENTLY)
          .location(URI.create(HSP_PRAESENTATION_BASE_URL))
          .header(HttpHeaders.CACHE_CONTROL, NO_CACHE_NO_STORE)
          .build();
    }
  }

  @APIResponse(responseCode = "301", description = "Redirect to old MM-Bilder Cataloge Webpage")
  @GET()
  @Path("/manuscripta-bilder/{mmresource:.*}")
  public Response handleRequestForMMBilderRedirect(@PathParam("mmresource") String mmresource) {

    logger.info("Getting ManuscriptaMediaevalia-Bilder Request for {}", mmresource);

    return Response.status(Status.MOVED_PERMANENTLY)
        .location(URI.create(HTTP_BILDER_MANUSCRIPTA_MEDIAEVALIA_DE + mmresource))
        .header(HttpHeaders.CACHE_CONTROL, "no-cache")
        .build();

  }

  protected Optional<MMDynamicURICreationStrategie> findProperDynamicStrategie(String path) {
    return dynamicPattern.entrySet().stream().filter(entry -> entry.getKey().matcher(path).find())
        .map(Entry::getValue).findFirst();
  }

  protected Optional<URI> findProperStaticStrategie(String path) {
    return staticRedirects.entrySet().stream().filter(entry -> entry.getKey().matcher(path).find())
        .map(Entry::getValue).findFirst();
  }
}
