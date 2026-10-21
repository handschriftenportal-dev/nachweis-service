package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mockito;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 14.02.2020.
 *
 * @see SuchDokumentService
 */
@QuarkusTest
@TestInstance(Lifecycle.PER_CLASS)
@TestTransaction
class SuchDokumentServiceTest {

  static final int DOCUMENTS_PER_UPDATE = 20;

  SuchPort suchPortMock;

  @Inject
  BeschreibungsRepository beschreibungsRepository;

  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;

  @Inject
  ObjectMapper objectMapper;

  @Inject
  BearbeiterBoundary bearbeiterBoundary;

  @Test
  void testKodUebernehmen() throws Exception {
    SuchDokumentService testling = createTestling();

    KulturObjektDokument kod = createKulturObjektDokument("HSP-323", "456");

    testling.kodUebernehmen(kod);

    Mockito.verify(suchPortMock, Mockito.times(1)).update(eq(true), any(SuchDokument.class));
  }

  @Test
  void testBeschreibungUebernehmen() throws Exception {
    SuchDokumentService testling = createTestling();

    Beschreibung beschreibung = createBeschreibung("HSP-1", "123");

    testling.beschreibungUebernehmen(beschreibung);

    Mockito.verify(suchPortMock, Mockito.times(1))
        .update(eq(true), any(SuchDokument.class));

    Mockito.doThrow(new SolrServiceException("Test"))
        .when(suchPortMock).update(anyBoolean(), any(SuchDokument.class));
    SolrUebernahmeException exception = assertThrows(SolrUebernahmeException.class,
        () -> testling.beschreibungUebernehmen(beschreibung));
    assertTrue(exception.getCause().getMessage().contains("Test"));
  }

  @Test
  void testAllBeschreibungUebernehmen() throws Exception {
    SuchDokumentService testling = createTestling();

    Beschreibung beschreibung1 = createBeschreibung("HSP-1", "123");
    Beschreibung beschreibung2 = createBeschreibung("HSP-2", "456");

    testling.allBeschreibungenUebernehmen(Arrays.asList(beschreibung1, beschreibung2));

    Mockito.verify(suchPortMock, Mockito.times(1))
        .update(eq(true), any(SuchDokument[].class));
  }

  @Test
  void testAllKODsUebernehmen() throws Exception {
    SuchDokumentService testling = createTestling();

    KulturObjektDokument kod1 = createKulturObjektDokument("HSP-323", "456");
    KulturObjektDokument kod2 = createKulturObjektDokument("HSP-123", "42356");

    testling.allKODsUebernehmen(Arrays.asList(kod1, kod2));

    Mockito.verify(suchPortMock, Mockito.times(1))
        .update(eq(true), any(SuchDokument[].class));
  }

  @Test
  void testSuchDokumentUebernehmen() throws SolrServiceException, SolrUebernahmeException {
    SuchDokumentService testling = createTestling();

    KulturObjektDokument kod = createKulturObjektDokument("HSP-123", "Sig. 123");
    SuchDokument suchDokument = SuchDokumentMapper.map(kod, "kodAsJson");

    testling.suchDokumentUebernehmen(suchDokument);

    Mockito.verify(suchPortMock, Mockito.times(1)).update(true, suchDokument);

    Mockito.doThrow(new SolrServiceException("Test"))
        .when(suchPortMock).update(anyBoolean(), any(SuchDokument.class));
    SolrUebernahmeException exception = assertThrows(SolrUebernahmeException.class,
        () -> testling.suchDokumentUebernehmen(suchDokument));
    assertTrue(exception.getCause().getMessage().contains("Test"));
  }

  @Test
  void testSuchDokumentUebernehmenOhneTransaktion()
      throws SolrUebernahmeException, SolrServiceException {
    SuchDokumentService testling = createTestling();

    KulturObjektDokument kod = createKulturObjektDokument("HSP-123", "Sig. 123");
    SuchDokument suchDokument = SuchDokumentMapper.map(kod, "kodAsJson");

    testling.suchDokumentUebernehmenOhneTransaktion(suchDokument);

    Mockito.verify(suchPortMock, Mockito.times(1))
        .updateWithoutTransaction(false, suchDokument);

    Mockito.doThrow(new SolrServiceException("Test"))
        .when(suchPortMock).updateWithoutTransaction(anyBoolean(), any(SuchDokument.class));
    SolrUebernahmeException exception = assertThrows(SolrUebernahmeException.class,
        () -> testling.suchDokumentUebernehmenOhneTransaktion(suchDokument));
    assertTrue(exception.getCause().getMessage().contains("Test"));
  }

  @Test
  void testSearch() throws Exception {
    SuchDokumentService testling = createTestling();

    final Suche suche = Suche.builder().build();
    testling.search(suche);

    Mockito.verify(suchPortMock, Mockito.times(1)).search(suche);
  }

  @Test
  void testDokumentLoeschen() throws Exception {
    SuchDokumentService testling = createTestling();

    testling.dokumentLoeschen("HSP-123");

    Mockito.verify(suchPortMock, Mockito.times(1)).delete("HSP-123");

    Mockito.doThrow(new SolrServiceException("Test")).when(suchPortMock).delete(anyString());

    SolrUebernahmeException exception = assertThrows(SolrUebernahmeException.class,
        () -> testling.dokumentLoeschen("HSP-456"));
    assertEquals("Error during delete Dokument with id [HSP-456]", exception.getMessage());
  }

  @Test
  void testReindexAllKulturObjektDokumente() throws SolrUebernahmeException, SolrServiceException {
    SuchDokumentService testling = createTestling();

    for (int id = 10; id < 32; id++) {
      kulturObjektDokumentRepository.saveAndFlush(
          createKulturObjektDokument("HSP-" + id, "Sig. " + id));
    }

    testling.reindexAllKulturObjektDokumente();

    Mockito.verify(suchPortMock, Mockito.times(1))
        .deleteBySuchDokumentTypWithoutTransaction(SuchDokumentTyp.KOD);

    Mockito.verify(suchPortMock, Mockito.times(2))
        .updateWithoutTransaction(eq(false), any(SuchDokument[].class));

    Mockito.verify(suchPortMock, Mockito.times(2)).commitWithoutTransaction();
  }

  @Test
  void testReindexAllBeschreibungen() throws SolrUebernahmeException, SolrServiceException {
    SuchDokumentService testling = createTestling();

    for (int id = 10; id < 32; id++) {
      beschreibungsRepository.saveAndFlush(createBeschreibung("HSP-" + id, "Sig. " + id));
    }

    testling.reindexAllBeschreibungen();

    Mockito.verify(suchPortMock, Mockito.times(1))
        .deleteBySuchDokumentTypWithoutTransaction(SuchDokumentTyp.BS);

    Mockito.verify(suchPortMock, Mockito.times(2))
        .updateWithoutTransaction(eq(false), any(SuchDokument[].class));

    Mockito.verify(suchPortMock, Mockito.times(2)).commitWithoutTransaction();
  }

  @Test
  void testErzeugeSuchDokumentBeschreibung() throws SolrUebernahmeException {
    SuchDokumentService testling = createTestling();

    SuchDokument suchDokument = testling.erzeugeSuchDokument(createBeschreibung("HSP-1", "123"));
    assertNotNull(suchDokument);

    SolrUebernahmeException exception = assertThrows(SolrUebernahmeException.class,
        () -> testling.erzeugeSuchDokument((Beschreibung) null));
    assertEquals("Beschreibung must not be null!", exception.getMessage());
  }

  @Test
  void testErzeugeSuchDokumentKulturObjektDokument() throws SolrUebernahmeException {
    SuchDokumentService testling = createTestling();

    SuchDokument suchDokument = testling.erzeugeSuchDokument(
        createKulturObjektDokument("HSP-1", "123"));
    assertNotNull(suchDokument);

    SolrUebernahmeException exception = assertThrows(SolrUebernahmeException.class,
        () -> testling.erzeugeSuchDokument((KulturObjektDokument) null));
    assertEquals("KulturObjektDokument must not be null!", exception.getMessage());
  }

  @Test
  void testDeleteAllByTyp() throws SolrUebernahmeException, SolrServiceException {
    SuchDokumentService testling = createTestling();

    testling.deleteAllByTyp(SuchDokumentTyp.BS);

    Mockito.verify(suchPortMock, Mockito.times(1))
        .deleteBySuchDokumentTypWithoutTransaction(SuchDokumentTyp.BS);

    Mockito.doThrow(new SolrServiceException("Test"))
        .when(suchPortMock)
        .deleteBySuchDokumentTypWithoutTransaction(SuchDokumentTyp.BS);

    SolrUebernahmeException exception = assertThrows(SolrUebernahmeException.class,
        () -> testling.deleteAllByTyp(SuchDokumentTyp.BS));
    assertEquals("Error deleting all documents of type BS from SOLR: Test", exception.getMessage());
  }

  @Test
  void testCommitSOLR() throws SolrServiceException, SolrUebernahmeException {
    SuchDokumentService testling = createTestling();

    testling.commitSOLR();

    Mockito.verify(suchPortMock, Mockito.times(1)).commitWithoutTransaction();

    Mockito.doThrow(new SolrServiceException("Test"))
        .when(suchPortMock)
        .commitWithoutTransaction();

    SolrUebernahmeException exception = assertThrows(SolrUebernahmeException.class,
        testling::commitSOLR);
    assertEquals("Error committing SOLR: Test", exception.getMessage());
  }

  @Test
  void mapDokumentToJson() throws SolrUebernahmeException {
    SuchDokumentService testling = createTestling();

    Beschreibung beschreibung = createBeschreibung("HSP-1", "Sig. 1");
    String beschreibungAsJson = testling.mapDokumentToJson(beschreibung, beschreibung.getId());

    assertNotNull(beschreibungAsJson);
    assertTrue(beschreibungAsJson.startsWith("{\"@type\":\"Beschreibung\",\"id\":\"HSP-1\""));

    KulturObjektDokument kod = createKulturObjektDokument("HSP-2", "Sig. 2");
    String kodAsJson = testling.mapDokumentToJson(kod, kod.getId());

    assertNotNull(kodAsJson);
    assertTrue(kodAsJson.startsWith("{\"@type\":\"KulturObjektDokument\",\"id\":\"HSP-2\""));
  }

  KulturObjektDokument createKulturObjektDokument(String id, String signatur) {
    Identifikation identifikation = new IdentifikationBuilder()
        .withId(UUID.randomUUID().toString())
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withIdent(signatur)
        .withBesitzer(new NormdatenReferenz(UUID.randomUUID().toString(), "SBB", "506306-X"))
        .withAufbewahrungsOrt(
            new NormdatenReferenz(UUID.randomUUID().toString(), "Berlin", "GND2323"))
        .build();

    Identifikation alternativeIdentifikation = new IdentifikationBuilder()
        .withId(UUID.randomUUID().toString())
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .withIdent(signatur)
        .withBesitzer(new NormdatenReferenz(UUID.randomUUID().toString(), "SBB", "506306-X"))
        .withAufbewahrungsOrt(
            new NormdatenReferenz(UUID.randomUUID().toString(), "Berlin", "GND2323"))
        .build();

    return new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId(id)
        .addBeschreibungsdokumentID("B-1")
        .withGueltigerIdentifikation(identifikation)
        .addAlternativeIdentifikation(alternativeIdentifikation)
        .build();
  }

  Beschreibung createBeschreibung(String id, String signatur) {
    Identifikation identifikation = new IdentifikationBuilder()
        .withId(HSP_UUI_Factory.generate(signatur.getBytes(StandardCharsets.UTF_8)))
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withIdent(signatur)
        .withBesitzer(new NormdatenReferenz("1", "SBB", "506306-X"))
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withId(HSP_UUI_Factory.generate())
        .build();

    return new Beschreibung.BeschreibungsBuilder(id, "kod1", identifikation)
        .addBeschreibungsKomponente(kopf)
        .build();
  }

  SuchDokumentService createTestling() {
    suchPortMock = mock(SuchPort.class);
    return new SuchDokumentService(suchPortMock,
        kulturObjektDokumentRepository,
        beschreibungsRepository,
        objectMapper,
        DOCUMENTS_PER_UPDATE, bearbeiterBoundary);
  }

}
