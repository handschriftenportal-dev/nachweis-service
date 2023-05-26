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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.NachweisErfassungResponse;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.ValidationResult;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.11.2019.
 * @version 1.0
 */

public class BeschreibungsRestControllerTest {

  private static final Logger logger = LoggerFactory
      .getLogger(BeschreibungsRestControllerTest.class);
  private BeschreibungsBoundary beschreibungsBoundary = mock(BeschreibungsBoundary.class);
  private KulturObjektDokumentBoundary kulturObjektDokumentBoundary = mock(
      KulturObjektDokumentBoundary.class);
  private DokumentSperreBoundary dokumentSperreBoundary = mock(DokumentSperreBoundary.class);

  @Test
  public void getBeschreibungsdokumentTestWith404() throws JsonProcessingException {

    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController();
    beschreibungsRestController.setBeschreibungsBoundary(beschreibungsBoundary);

    Response response = beschreibungsRestController.getBeschreibungsdokument("123");

    verify(beschreibungsBoundary, times(1)).findById("123");

    assertEquals(404, response.getStatus());

  }

  @Test
  public void getBeschreibungsdokumentSignaturenTestWith200() {

    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController();
    beschreibungsRestController.setBeschreibungsBoundary(beschreibungsBoundary);
    beschreibungsRestController.setKulturObjektDokumentBoundary(kulturObjektDokumentBoundary);

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Ms1")
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .build();

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("1")
        .addAlternativeIdentifikation(identifikation)
        .build();

    Beschreibung beschreibungsdokument = new Beschreibung.BeschreibungsBuilder()
        .withId("2324")
        .withErstellungsDatum(LocalDateTime.of(2019, 11, 29, 0, 0, 0))
        .withKodID("1")
        .build();

    when(beschreibungsBoundary.findById("2324")).thenReturn(Optional.of(beschreibungsdokument));
    when(kulturObjektDokumentBoundary.findById("1")).thenReturn(Optional.of(kod));

    Response response = beschreibungsRestController.getKODSignaturenForBeschreibungen("2324");

    verify(beschreibungsBoundary, times(1)).findById("2324");

    assertEquals(200, response.getStatus());

    assertEquals(Arrays.asList("Ms1"), response.getEntity());
  }

  @Test
  public void getBeschreibungsdokumentTestWith200() throws JsonProcessingException {

    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController();
    beschreibungsRestController.setBeschreibungsBoundary(beschreibungsBoundary);

    Beschreibung beschreibungsdokument = new Beschreibung.BeschreibungsBuilder()
        .withId("2324")
        .withErstellungsDatum(LocalDateTime.of(2019, 11, 29, 0, 0, 0))
        .build();

    when(beschreibungsBoundary.findById("2324")).thenReturn(Optional.of(beschreibungsdokument));

    Response response = beschreibungsRestController.getBeschreibungsdokument("2324");

    verify(beschreibungsBoundary, times(1)).findById("2324");

    assertEquals(200, response.getStatus());
  }

  @Test
  public void test_GivenBeschreibung_WithContent() {

    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController();
    beschreibungsRestController.setBeschreibungsBoundary(beschreibungsBoundary);

    NormdatenReferenz besitzer = new NormdatenReferenz("", "Staatsbibliothek zu Berlin", "");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withId("1212")
        .withTitel("Flavius Josephus, de bello Judaico")
        .withIndentifikationen(new LinkedHashSet<>(Arrays.asList(identifikation)))
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("40170459")
        .withKodID("1234")
        .addBeschreibungsKomponente(kopf)
        .build();

    when(beschreibungsBoundary.findById("40170459")).thenReturn(Optional.of(beschreibung));

    Response response = beschreibungsRestController.getBeschreibungsdokument("40170459");

    verify(beschreibungsBoundary, times(1)).findById("40170459");

    assertEquals(200, response.getStatus());

    logger.info("Getting response from REST Interface: {} ", response.getEntity());

  }

  @ParameterizedTest
  @ValueSource(strings = {"de", "en"})
  void testupdateBeschreibungWithTEI(String language) {

    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController();
    beschreibungsRestController.setBeschreibungsBoundary(beschreibungsBoundary);

    Response response = beschreibungsRestController.updateBeschreibungWithTEI("1", "xml", language);

    assertEquals(200, response.getStatus());
    assertTrue(((NachweisErfassungResponse) response.getEntity()).isSuccess());
    assertEquals("info", ((NachweisErfassungResponse) response.getEntity()).getLevel());
  }

  @Test
  void testValidateXML() {
    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController();
    beschreibungsRestController.setBeschreibungsBoundary(beschreibungsBoundary);
    ValidationResult validationResult = new ValidationResult(true, "", "", "Is valid");

    when(beschreibungsBoundary.validateXML("", "TEI", "de")).thenReturn(validationResult);

    Response response = beschreibungsRestController.validateBeschreibungTEI("", "TEI", "de");
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertEquals("ValidationResult{isValid=true, line='', column='', message='Is valid'}",
        ((NachweisErfassungResponse) response.getEntity()).getContent().toString());
  }

  @Test
  void testGetSperrenForBeschreibung() throws DokumentSperreException {

    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController();
    beschreibungsRestController.setDokumentSperreBoundary(dokumentSperreBoundary);

    assertEquals(0,
        ((List<Sperre>) beschreibungsRestController.getSperrenForBeschreibungen("1")
            .getEntity()).size());

    Sperre sperre = new Sperre();
    when(dokumentSperreBoundary.findBySperreEintraege(
        new SperreEintrag("1", SperreDokumentTyp.BESCHREIBUNG))).thenReturn(Arrays.asList(sperre));

    assertEquals(Response.Status.OK.getStatusCode(),
        beschreibungsRestController.getSperrenForBeschreibungen("1").getStatus());

    assertEquals(1,
        ((List<Sperre>) beschreibungsRestController.getSperrenForBeschreibungen("1")
            .getEntity()).size());
  }
}
