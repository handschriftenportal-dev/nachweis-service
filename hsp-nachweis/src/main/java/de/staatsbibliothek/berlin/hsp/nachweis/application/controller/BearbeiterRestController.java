package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.exception.OAUTH2Exception;
import de.staatsbibliothek.berlin.javaee.authentication.repository.jwt.OAuth2JWTParser;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/bearbeiter")
public class BearbeiterRestController {

  private static final String BEARER_PREFIX = "Bearer ";
  private final OAuth2JWTParser jwtParser;
  private final BearbeiterBoundary bearbeiterService;
  private final Logger logger = LoggerFactory.getLogger(BearbeiterRestController.class);

  @Inject
  public BearbeiterRestController(BearbeiterBoundary bearbeiterService, OAuth2JWTParser jwtParser) {
    this.bearbeiterService = bearbeiterService;
    this.jwtParser = jwtParser;
  }

  @GET
  @Path("/config")
  @Produces(MediaType.APPLICATION_JSON)
  @Authenticated
  public Response getConfig(@HeaderParam(HttpHeaders.AUTHORIZATION) String jwt)
      throws OAUTH2Exception {

    var username = getAuthenticatedUser(jwt);

    logger.info("GET Bearbeiter Request for Config: Username: {}", username);

    var response = bearbeiterService.getConfig(username);

    return Response.ok(response).build();
  }

  @POST
  @Path("/config")
  @Consumes(MediaType.APPLICATION_JSON)
  @Authenticated
  public Response setConfig(@HeaderParam(HttpHeaders.AUTHORIZATION) String jwt, String payload)
      throws OAUTH2Exception {
    var username = getAuthenticatedUser(jwt);
    bearbeiterService.setConfig(payload, username);

    return Response.status(Status.CREATED).build();
  }

  public String getAuthenticatedUser(String token) throws OAUTH2Exception {
    var jwt = token.replace(BEARER_PREFIX, "");
    return jwtParser.createAuthenticationFromToken(jwt).getUsername();
  }
}
