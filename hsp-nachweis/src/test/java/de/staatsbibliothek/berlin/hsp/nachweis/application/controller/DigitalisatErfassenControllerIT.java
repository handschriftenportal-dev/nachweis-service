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
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 09.10.2020.
 * @version 1.0
 */
@QuarkusTest
class DigitalisatErfassenControllerIT {

  static final Bearbeiter BEARBEITER = new Bearbeiter("1", "Unbekannter Bearbeiter");

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
  static void beforeAll() {
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

    // Verify data is committed and visible in a new transaction
    QuarkusTransaction.requiringNew().run(() -> {
      assertTrue(kulturObjektDokumentRepository.findByIdOptional("test1").isPresent(),
          "KOD test1 should be committed and visible");
    });

    try (InputStream in = url.openStream()) {
      String contents = readStream(in);
      System.out.println(contents);
      assertTrue(contents.contains("Digitalisat"));
    } finally {
      removeData();
    }
  }

  void prepareData() throws IOException, JAXBException {
    Path kodPath = Path.of("src", "test", "resources", "tei-HSP-kod.xml");
    TEI tei;
    try (InputStream is = newInputStream(kodPath)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei = teiList.getFirst();
    }

    NormdatenReferenz besitzer =
        new NormdatenReferenz(
            "2", "Sächsische Landesbibliothek - Staats- und Universitätsbibliothek Dresden", "");

    Identifikation identifikation =
        new IdentifikationBuilder()
            .withId("ident1")
            .withIdent("Mscr.Dresd.A.111")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(besitzer)
            .build();

    String kodID = "test1";
    kod =
        new KulturObjektDokumentBuilder(kodID)
            .withGueltigerIdentifikation(identifikation)
            .withTEIXml(TEIObjectFactory.marshal(tei))
            .build();

    sperre =
        Sperre.newBuilder()
            .withId("sp_test1")
            .withSperreGrund("DigitalisatAnlegen")
            .withBearbeiter(BEARBEITER)
            .withSperreTyp(SperreTyp.MANUAL)
            .addSperreEintrag(new SperreEintrag("test1", SperreDokumentTyp.KOD))
            .build();

    QuarkusTransaction.requiringNew().run(() -> {
      if (kulturObjektDokumentRepository.findByIdOptional(kodID).isPresent()) {
        kulturObjektDokumentRepository.deleteById(kodID);
      }
      kulturObjektDokumentRepository.saveAndFlush(kod);

      if (bearbeiterRepository.findByIdOptional(BEARBEITER.getId()).isPresent()) {
        bearbeiterRepository.deleteByIdAndFlush(BEARBEITER.getId());
      }
      bearbeiterRepository.saveAndFlush(BEARBEITER);

      if (sperreRepository.findByIdOptional(sperre.getId()).isPresent()) {
        sperreRepository.deleteByIdAndFlush(sperre.getId());
      }
      sperreRepository.saveAndFlush(sperre);
    });
  }

  void removeData() {
    QuarkusTransaction.requiringNew().run(() -> {
      if (Objects.nonNull(sperre) && sperreRepository.findByIdOptional(sperre.getId())
          .isPresent()) {
        sperreRepository.deleteByIdAndFlush(sperre.getId());
      }

      if (Objects.nonNull(kod)
          && kulturObjektDokumentRepository.findByIdOptional(kod.getId()).isPresent()) {
        kulturObjektDokumentRepository.deleteById(kod.getId());
      }

      if (bearbeiterRepository.findByIdOptional(BEARBEITER.getId()).isPresent()) {
        bearbeiterRepository.deleteByIdAndFlush(BEARBEITER.getId());
      }
    });
  }
}
