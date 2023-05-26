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

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.StreamReader.readStream;
import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.SperreRepository;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import de.staatsbibliothek.berlin.javaee.authentication.service.AuthenticationService;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 09.10.2020.
 * @version 1.0
 */
@QuarkusTest
class DigitalisatErfassenControllerIT {

  static Bearbeiter BEARBEITER = new Bearbeiter("1", "Unbekannter Bearbeiter");

  @TestHTTPResource("/kulturObjektDokument/digitalisat-anlegen.xhtml?kodid=test1")
  URL url;

  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;

  @Inject
  SperreRepository sperreRepository;

  @Inject
  BearbeiterRepository bearbeiterRepository;

  KulturObjektDokument kod;
  Sperre sperre;

  @BeforeAll
  public static void beforeAll() {
    BearbeiterService bearbeiterBoundary = mock(BearbeiterService.class);

    AuthenticationService authenticationService = mock(AuthenticationService.class);
    bearbeiterBoundary.setAuthenticationService(authenticationService);

    AuthenticationRepository authenticationRepository = mock(AuthenticationRepository.class);
    bearbeiterBoundary.setAuthenticationRepository(authenticationRepository);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(BEARBEITER);
    QuarkusMock.installMockForType(bearbeiterBoundary, BearbeiterBoundary.class);
  }

  @Test
  void testOpenPage() throws IOException, JAXBException {
    prepareData();
    assertNotNull(kod);
    assertNotNull(sperre);

    Optional<KulturObjektDokument> byIdOptional = kulturObjektDokumentRepository.findByIdOptional(kod.getId());
    assertTrue(byIdOptional.isPresent());

    try (InputStream in = url.openStream()) {
      String contents = readStream(in);
      System.out.println(contents);
      assertTrue(contents.contains("Digitalisat zu Mscr.Dresd.A.111"));
    } finally {
      removeData();
    }
  }

  @Transactional
  void prepareData() throws IOException, JAXBException {
    Path kodPath = Path.of("src", "integration-test", "resources", "tei-HSP-kod.xml");
    TEI tei;
    try (InputStream is = newInputStream(kodPath)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei = teiList.get(0);
    }

    NormdatenReferenz besitzer = new NormdatenReferenz("2",
        "Sächsische Landesbibliothek - Staats- und Universitätsbibliothek Dresden", "");

    Identifikation identifikation = new IdentifikationBuilder()
        .withId("ident1")
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .build();

    String kodID = "test1";
    kod = new KulturObjektDokumentBuilder(kodID)
        .withGueltigerIdentifikation(identifikation)
        .withTEIXml(TEIObjectFactory.marshal(tei))
        .build();

    if (kulturObjektDokumentRepository.findByIdOptional(kodID).isPresent()) {
      kulturObjektDokumentRepository.deleteById(kodID);
    }
    kulturObjektDokumentRepository.saveAndFlush(kod);

    if (bearbeiterRepository.findByIdOptional(BEARBEITER.getId()).isPresent()) {
      bearbeiterRepository.deleteByIdAndFlush(BEARBEITER.getId());
    }
    bearbeiterRepository.saveAndFlush(BEARBEITER);

    sperre = Sperre.newBuilder()
        .withId("sp_test1")
        .withSperreGrund("DigitalisatAnlegen")
        .withBearbeiter(BEARBEITER)
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("test1", SperreDokumentTyp.KOD))
        .build();

    if (sperreRepository.findByIdOptional(sperre.getId()).isPresent()) {
      sperreRepository.deleteByIdAndFlush(sperre.getId());
    }
    sperreRepository.saveAndFlush(sperre);
  }

  @Transactional
  void removeData() {
    if (Objects.nonNull(sperre) && sperreRepository.findByIdOptional(sperre.getId()).isPresent()) {
      sperreRepository.deleteByIdAndFlush(sperre.getId());
    }

    if (Objects.nonNull(kod) && kulturObjektDokumentRepository.findByIdOptional(kod.getId()).isPresent()) {
      kulturObjektDokumentRepository.deleteById(kod.getId());
    }

    if (bearbeiterRepository.findByIdOptional(BEARBEITER.getId()).isPresent()) {
      bearbeiterRepository.deleteByIdAndFlush(BEARBEITER.getId());
    }
  }

}
