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

import static io.restassured.RestAssured.given;
import static java.nio.file.Files.newInputStream;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import io.quarkus.test.junit.QuarkusTest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.HttpHeaders;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 22.11.2019.
 * @version 1.0
 */

@QuarkusTest
@Dependent
class BeschreibungRestControllerIT {

  public static final String BESCHREIBUNGS_ID = "HSP-87332024-06c1-3e6a-b565-cc502dab0944";
  @Inject
  BeschreibungsRepository beschreibungsRepository;

  @BeforeEach
  @Transactional
  void build() {

    NormdatenReferenz besitzer = new NormdatenReferenz("ds", "Staatsbibliothek zu Berlin", "");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withId("1212")
        .withTitel("Flavius Josephus, de bello Judaico")
        .withIndentifikationen(new LinkedHashSet<>(Collections.singletonList(identifikation)))
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId(BESCHREIBUNGS_ID)
        .withKodID("KOD1")
        .addBeschreibungsKomponente(kopf)
        .build();

    if (beschreibungsRepository.findByIdOptional(BESCHREIBUNGS_ID).isEmpty()) {
      beschreibungsRepository.save(beschreibung);
    }
  }

  @Test
  void testGetByReferenzRESTEndpoint() {

    given()
        .when().get("/rest/beschreibungen/" + BESCHREIBUNGS_ID)
        .then()
        .statusCode(200);
  }

  @Test
  void testupdateBeschreibungWithTEI() throws IOException, SAXException {

    Path xmlPath = Path.of("src", "integration-test", "resources", "tei-code116.xml");

    try (InputStream is = newInputStream(xmlPath)) {

      String tei = new BufferedReader(
          new InputStreamReader(is, StandardCharsets.UTF_8))
          .lines()
          .collect(Collectors.joining("\n"));

      given()
          .when().body(tei).header(HttpHeaders.CONTENT_TYPE, "application/xml;charset=UTF-8")
          .post("/rest/beschreibungen/" + BESCHREIBUNGS_ID)
          .then()
          .statusCode(200).body("success", equalTo(true)).log();

      Beschreibung beschreibung = beschreibungsRepository.findById(BESCHREIBUNGS_ID);

      assertEquals(BESCHREIBUNGS_ID, beschreibung.getId());
      assertEquals(VerwaltungsTyp.INTERN, beschreibung.getVerwaltungsTyp());

      XMLUnit.setIgnoreWhitespace(true);

      Diff diff = XMLUnit.compareXML(tei, beschreibung.getTeiXML());
      diff.overrideDifferenceListener(new DateDifferenceListener());

      assertTrue(diff.identical(), "TEI Compared failed: " + diff.toString());
    }
  }

  @Test
  public void testValidateBeschreibungTEI() {

    String tei = "<TEI></TEI>";

    given()
        .when().body(tei).header(HttpHeaders.CONTENT_TYPE, "application/xml;charset=UTF-8")
        .post("/rest/beschreibungen/validate")
        .then()
        .statusCode(200)
        .log().body()
        .body("content.valid", equalTo(true));

  }

  static class DateDifferenceListener implements DifferenceListener {

    @Override
    public int differenceFound(Difference difference) {
      switch (difference.getControlNodeDetail().getXpathLocation()) {
        case "/TEI[1]/teiHeader[1]/revisionDesc[1]/change[1]/date[1]/@when":
        case "/TEI[1]/teiHeader[1]/revisionDesc[1]/change[1]/date[1]/text()[1]":
          return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
        default:
          return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
      }
    }

    @Override
    public void skippedComparison(Node control, Node test) {

    }
  }
}
