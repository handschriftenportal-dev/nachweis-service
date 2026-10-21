package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for TEI XML validation.
 * Acts as a reverse proxy to the import service for validation.
 *
 * @since 2026-08-20
 */
@Path("/validate")
@Slf4j
public class TeiXmlValidationRestController {

  private final TeiXmlValidationService validationService;

  @Inject
  public TeiXmlValidationRestController(TeiXmlValidationService validationService) {
    this.validationService = validationService;
  }

  /**
   * Validates TEI XML against the TEI schema (without ODD).
   *
   * @param teiXml the TEI XML to validate
   * @param locale the locale for error messages (optional, defaults to "de")
   * @return Response containing the ValidationResult
   */
  @Path("/tei")
  @POST
  @Consumes(MediaType.APPLICATION_XML)
  @Produces(MediaType.APPLICATION_JSON)
  public Response validateTei(String teiXml,
      @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @DefaultValue("de") String locale) {
    return Response
        .status(Response.Status.OK)
        .entity(validationService.validateTei(teiXml, locale))
        .build();
  }

  /**
   * Validates TEI XML against the ODD schema.
   *
   * @param teiXml the TEI XML to validate
   * @param locale the locale for error messages (optional, defaults to "de")
   * @return Response containing the ValidationResult
   */
  @Path("/odd")
  @POST
  @Consumes(MediaType.APPLICATION_XML)
  @Produces(MediaType.APPLICATION_JSON)
  public Response validateOdd(String teiXml,
      @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @DefaultValue("de") String locale) {
    return Response
        .status(Response.Status.OK)
        .entity(validationService.validateOdd(teiXml, locale))
        .build();
  }
}
