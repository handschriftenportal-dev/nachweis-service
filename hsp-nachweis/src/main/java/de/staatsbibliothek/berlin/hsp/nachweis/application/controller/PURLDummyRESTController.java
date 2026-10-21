package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl.SBBPURLGenResponse;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.08.22.
 * @version 1.0
 */

@Path("/purl")
public class PURLDummyRESTController {

  @Path("/example")
  @GET()
  @Produces(MediaType.APPLICATION_JSON)
  public Response getDummyPurl() throws NoSuchAlgorithmException {
    SBBPURLGenResponse response = new SBBPURLGenResponse("ok", "", "HSP",
        String.valueOf(generateRandomDigits(8)), String.valueOf(generateRandomDigits(4)), String.valueOf(generateRandomDigits(4)));

    return Response
        .status(Status.OK)
        .entity(response)
        .build();
  }

  private static int generateRandomDigits(int n) throws NoSuchAlgorithmException {
    double m = Math.pow(10.0, n - 1.0);
    Random rand = SecureRandom.getInstanceStrong();
    return (int)Math.round(m) + rand.nextInt(9 * (int)Math.round(m));
  }

}
