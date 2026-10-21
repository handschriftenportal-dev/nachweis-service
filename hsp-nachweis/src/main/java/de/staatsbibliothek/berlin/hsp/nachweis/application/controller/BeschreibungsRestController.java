package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.NachweisErfassungResponse;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.tei.TeiSnapshotZipper;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import de.staatsbibliothek.berlin.javaee.authentication.exception.OAUTH2Exception;
import de.staatsbibliothek.berlin.javaee.authentication.repository.jwt.OAuth2JWTParser;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.11.2019.
 * @version 1.0
 * <p>
 * Controller to implement a REST API for Beschreibungsdokumente
 */
@Path("/beschreibungen")
public class BeschreibungsRestController {

  private static final Logger logger = LoggerFactory.getLogger(BeschreibungsRestController.class);
  private static final String BEARER_PREFIX = "Bearer ";
  private final OAuth2JWTParser jwtParser;
  private final BeschreibungsBoundary beschreibungsBoundary;
  private final KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  private final DokumentSperreBoundary dokumentSperreBoundary;

  @Inject
  public BeschreibungsRestController(BeschreibungsBoundary beschreibungsBoundary,
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      DokumentSperreBoundary dokumentSperreBoundary,
      OAuth2JWTParser jwtParser) {
    this.beschreibungsBoundary = beschreibungsBoundary;
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
    this.dokumentSperreBoundary = dokumentSperreBoundary;
    this.jwtParser = jwtParser;
  }

  @Path("/{id}/sperren")
  @GET()
  @Produces(MediaType.APPLICATION_JSON)
  @Authenticated
  public Response getSperrenForBeschreibungen(@PathParam("id") String id) {
    List<Sperre> sperren = new ArrayList<>();

    try {
      sperren.addAll(dokumentSperreBoundary.findBySperreEintraege(
          new SperreEintrag(id, SperreDokumentTyp.BESCHREIBUNG)));
    } catch (DokumentSperreException e) {
      logger.error("Error during find sperren", e);
    }

    return Response
        .status(Status.OK)
        .entity(sperren)
        .build();
  }


  @Path("/{id}/signaturen")
  @GET()
  @Produces(MediaType.APPLICATION_JSON)
  @Authenticated
  public Response getKODSignaturenForBeschreibungen(@PathParam("id") String id) {
    Optional<Beschreibung> beschreibung = beschreibungsBoundary.findById(id);
    List<String> kodSignaturen = new ArrayList<>();

    if (beschreibung.isPresent()) {
      kulturObjektDokumentBoundary.findById(beschreibung.get().getKodID()).ifPresent(kod -> {
        if (kod.getAlternativeIdentifikationen() != null) {
          kodSignaturen.addAll(kod.getAlternativeIdentifikationen().stream().map(i -> i.getIdent())
              .filter(Objects::nonNull)
              .collect(Collectors.toList()));
        }

      });
    }
    return Response
        .status(Status.OK)
        .entity(kodSignaturen)
        .build();
  }

  @Path("{id}")
  @GET()
  @PermitAll
  @Produces(MediaType.APPLICATION_JSON)
  public Response getBeschreibungsdokument(@PathParam("id") String id) {
    logger.info("REST API GET Request for Beschreibung with id {} ", id);

    Optional<Beschreibung> beschreibung = beschreibungsBoundary.findById(id);

    if (beschreibung.isPresent()) {
      logger.debug("Writing Beschreibung as {} ", beschreibung);
      return Response
          .status(Response.Status.OK)
          .entity(beschreibung.get().getTeiXML())
          .build();
    }

    return Response
        .status(Status.NOT_FOUND)
        .build();
  }

  @Path("/{id}/tei")
  @GET()
  @PermitAll
  @Produces(MediaType.APPLICATION_XML)
  public Response downloadBeschreibungTei(@PathParam("id") String id) {
    logger.info("REST API GET TEI download for Beschreibung with id {} ", id);

    return beschreibungsBoundary.findById(id)
        .map(Beschreibung::getTeiXML)
        .filter(StringUtils::isNotBlank)
        .map(tei -> Response.ok(tei)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + TeiSnapshotZipper.teiXmlFilename(id) + "\"")
            .build())
        .orElseGet(() -> Response.status(Status.NOT_FOUND).build());
  }

  @Path("/{id}/tei-snapshot")
  @GET()
  @PermitAll
  @Produces("application/zip")
  public Response downloadBeschreibungTeiSnapshot(@PathParam("id") String id) {
    logger.info("REST API GET TEI snapshot download for Beschreibung with id {} ", id);

    return beschreibungsBoundary.findTeiSnapshotZip(id)
        .map(zip -> Response.ok(zip)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + TeiSnapshotZipper.teiSnapshotZipFilename(id) + "\"")
            .build())
        .orElseGet(() -> Response.status(Status.NOT_FOUND).build());
  }

  @Path("{id}")
  @Consumes(MediaType.APPLICATION_XML)
  @Produces(MediaType.APPLICATION_JSON)
  @POST
  @Authenticated
  public Response updateBeschreibungWithTEI(@PathParam("id") String id, String xml,
      @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) String language,
      @HeaderParam(HttpHeaders.AUTHORIZATION) String jwt) {

    NachweisErfassungResponse ergebnis = new NachweisErfassungResponse();

    if (language == null || language.isBlank()) {
      language = "de";
    }
    Locale locale = Locale.forLanguageTag(language);
    ResourceBundle resourceBundle = ResourceBundle.getBundle(I18NController.APPLICATION_MESSAGES,
        locale);

    try {
      var authenticatedUser = getAuthenticatedUser(jwt);
      beschreibungsBoundary.updateBeschreibungMitXML(id, xml, authenticatedUser.getUsername(),
          false,
          locale);
      ergebnis.withSuccess(resourceBundle.getString("beschreibung_savesuccessfull"));

      return Response
          .status(Response.Status.OK)
          .entity(ergebnis)
          .build();
    } catch (Exception error) {

      logger.error("Error during Update Beschreibung", error);
      String errorMessage = getMessage("beschreibung_savefailed", error.getMessage());
      ergebnis.withError(errorMessage);

      return Response
          .status(Status.OK)
          .entity(ergebnis)
          .build();
    }
  }

  @Path("/validate")
  @POST()
  @Consumes(MediaType.APPLICATION_XML)
  @Produces(MediaType.APPLICATION_JSON)
  @Authenticated
  public Response validateBeschreibungTEI(String xmlTEI,
      @HeaderParam("de.staatsbibliothek.berlin.hsp.schema") String schema,
      @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) String locale) {

    NachweisErfassungResponse ergebnis = new NachweisErfassungResponse();

    if (locale == null || locale.isBlank()) {
      locale = "de";
    }

    ValidationResult validationResult = beschreibungsBoundary.validateXML(xmlTEI, schema, locale);

    if (validationResult.isValid()) {
      ergebnis.successWithContent(validationResult.getMessage(), validationResult);
    } else {
      ergebnis.errorWithContent(
          validationResult.getMessageWithDetails(Locale.forLanguageTag(locale)),
          validationResult
      );
    }

    return Response
        .status(Response.Status.OK)
        .entity(ergebnis)
        .build();
  }

  public Authentication getAuthenticatedUser(String token) throws OAUTH2Exception {
    return jwtParser.createAuthenticationFromToken(token.replace(BEARER_PREFIX, ""));
  }

}
