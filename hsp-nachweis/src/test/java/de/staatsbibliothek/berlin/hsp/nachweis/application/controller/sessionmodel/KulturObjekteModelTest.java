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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.IdentifikationRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.papierkorb.PapierkorbBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.transformation.TeiXmlTransformationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.KulturObjektDokumentListDTO;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 02.12.20
 */
@QuarkusTest
@TestTransaction
@Execution(ExecutionMode.SAME_THREAD)
class KulturObjekteModelTest {

  private static final LocalDateTime REGISTRIERUNGS_DATUM = LocalDateTime.now();

  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;

  @Inject
  IdentifikationRepository identifikationRepository;

  @Inject
  SuchDokumentBoundary suchDokumentService;

  @Inject
  BeschreibungsBoundary beschreibungsService;

  @Inject
  KafkaIndexingProducer kafkaIndexingProducer;

  @Inject
  BeschreibungsRepository beschreibungsRepository;

  @Inject
  KulturObjektDokumentService boundary;

  BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);

  @Inject
  KulturObjektDokumentRepository repository;

  @Inject
  KODRechte kodRechte;

  @Inject
  TeiXmlValidationBoundary teiXmlValidationBoundaryMock;

  @Inject
  TeiXmlTransformationBoundary teiXmlTransformationServiceMock;

  NormdatenReferenz koerperschaft = new NormdatenReferenz("koerpId", "Czarnecki LTD", "DE-007");
  NormdatenReferenz ort = new NormdatenReferenz("ortId", "Berlin", "DE-008");
  Identifikation identifikation = new IdentifikationBuilder()
      .withId("id1")
      .withAufbewahrungsOrt(ort)
      .withBesitzer(koerperschaft)
      .withIdent("Chaka")
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .build();
  KulturObjektDokument kulturObjektDokument = new KulturObjektDokumentBuilder()
      .withId("kod1")
      .withGueltigerIdentifikation(identifikation)
      .withGndIdentifier("eeee")
      .withRegistrierungsDatum(REGISTRIERUNGS_DATUM)
      .withTEIXml("<xml></xml>")
      .build();
  private KulturObjekteModel kulturObjekteModel;

  @Test
  @TestTransaction
  void testReloadTableRecords() {
    kulturObjekteModel = new KulturObjekteModel(boundary, kodRechte, bearbeiterBoundary, false);
    kulturObjekteModel.setup();

    assertNotNull(kulturObjekteModel.getKulturObjektDokumenteDataModel());

    kulturObjekteModel.getKulturObjektDokumenteDataModel().load(0, 10, new HashMap<>(), new HashMap<>());

    assertEquals(0, kulturObjekteModel.getKulturObjektDokumenteDataModel().getRowCount());

    repository.saveAndFlush(kulturObjektDokument);

    kulturObjekteModel.reloadTableRecords();

    assertNotNull(kulturObjekteModel.getKulturObjektDokumenteDataModel());

    kulturObjekteModel.getKulturObjektDokumenteDataModel().load(0, 10, new HashMap<>(), new HashMap<>());

    assertEquals(1, kulturObjekteModel.getKulturObjektDokumenteDataModel().getRowCount());
  }

  @Test
  @TestTransaction
  void testDeleteSelectedKods() {
    DokumentSperreBoundary dokumentSperreService = mock(DokumentSperreBoundary.class);
    PURLBoundary purlServiceMOCK = mock(PURLBoundary.class);
    PapierkorbBoundary papierkorbServiceMOCK = mock(PapierkorbBoundary.class);

    KulturObjektDokumentService kulturObjektDokumentService = new KulturObjektDokumentService(
        kulturObjektDokumentRepository, identifikationRepository, suchDokumentService,
        beschreibungsService, dokumentSperreService, kafkaIndexingProducer,
        beschreibungsRepository, purlServiceMOCK, papierkorbServiceMOCK, false,
        teiXmlTransformationServiceMock, teiXmlValidationBoundaryMock);
    kulturObjekteModel = new KulturObjekteModel(kulturObjektDokumentService, kodRechte,
        bearbeiterBoundary, false);
    when(bearbeiterBoundary.getLoggedBearbeiter())
        .thenReturn(new Bearbeiter("1", "Unbekannter Tester"));
    repository.saveAndFlush(kulturObjektDokument);
    kulturObjekteModel.setup();
    assertNotNull(kulturObjekteModel.getKulturObjektDokumenteDataModel());

    List<KulturObjektDokumentListDTO> models = kulturObjekteModel.getKulturObjektDokumenteDataModel()
        .load(0, 1, new HashMap<>(), new HashMap<>());
    assertEquals(1, kulturObjekteModel.getKulturObjektDokumenteDataModel().getRowCount());
    assertNotNull(models);
    assertEquals(1, models.size());
    kulturObjekteModel.setSelectedKulturObjektDokumente(models);

    kulturObjekteModel.deleteSelectedKods();

    assertNotNull(kulturObjekteModel.getKulturObjektDokumenteDataModel());
    kulturObjekteModel.getKulturObjektDokumenteDataModel().load(0, 10, new HashMap<>(), new HashMap<>());
    assertEquals(0, kulturObjekteModel.getKulturObjektDokumenteDataModel().getRowCount());
  }

  @Test
  void testReindexAllDocuments() throws Exception {
    KulturObjektDokumentBoundary boundaryMock = mock(KulturObjektDokumentBoundary.class);

    when(bearbeiterBoundary.getLoggedBearbeiter())
        .thenReturn(new Bearbeiter("1", "Unbekannter Tester"));

    kulturObjekteModel = new KulturObjekteModel(boundaryMock, kodRechte, bearbeiterBoundary, false);

    kulturObjekteModel.reindexAllKulturObjektDokumente();

    verify(boundaryMock, times(1)).reindexAllKulturObjektDokumente(any(Bearbeiter.class));
  }


}
