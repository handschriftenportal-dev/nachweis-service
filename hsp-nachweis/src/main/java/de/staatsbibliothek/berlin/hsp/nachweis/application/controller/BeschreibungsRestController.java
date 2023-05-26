/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.NachweisErfassungResponse;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.ValidationResult;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
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

  @Inject
  BeschreibungsBoundary beschreibungsBoundary;

  @Inject
  KulturObjektDokumentBoundary kulturObjektDokumentBoundary;

  @Inject
  DokumentSperreBoundary dokumentSperreBoundary;

  @Path("/{id}/sperren")
  @GET()
  @Produces(MediaType.APPLICATION_JSON)
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

  @Path("{id}")
  @Consumes(MediaType.APPLICATION_XML)
  @Produces(MediaType.APPLICATION_JSON)
  @POST
  public Response updateBeschreibungWithTEI(@PathParam("id") String id, String xml,
      @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) String language) {

    NachweisErfassungResponse ergebnis = new NachweisErfassungResponse();

    if (language == null || language.isBlank()) {
      language = "de";
    }
    Locale locale = Locale.forLanguageTag(language);

    ResourceBundle resourceBundle = ResourceBundle.getBundle(I18NController.APPLICATION_MESSAGES, locale);

    try {

      beschreibungsBoundary.updateBeschreibungMitXML(id, xml, locale);

      ergebnis.withSuccess(resourceBundle.getString("beschreibung_savesuccessfull"));

      return Response
          .status(Response.Status.OK)
          .entity(ergebnis)
          .build();

    } catch (Exception error) {

      logger.error("Error during Update Beschreibung", error);

      ergebnis.withError(resourceBundle.getString("beschreibung_savefailed") + error.getMessage());

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
      ergebnis.errorWithContent(validationResult.getMessage(), validationResult);
    }

    return Response
        .status(Response.Status.OK)
        .entity(ergebnis)
        .build();
  }

  void setBeschreibungsBoundary(
      BeschreibungsBoundary beschreibungsBoundary) {
    this.beschreibungsBoundary = beschreibungsBoundary;
  }

  void setKulturObjektDokumentBoundary(
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary) {
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
  }

  void setDokumentSperreBoundary(
      DokumentSperreBoundary dokumentSperreBoundary) {
    this.dokumentSperreBoundary = dokumentSperreBoundary;
  }
}
