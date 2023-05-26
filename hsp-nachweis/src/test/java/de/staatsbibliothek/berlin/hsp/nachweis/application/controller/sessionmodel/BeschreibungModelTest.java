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

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.BeschreibungListDTO;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
class BeschreibungModelTest {

  private static final LocalDateTime ERSTELL_DATUM = LocalDateTime.now();

  BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);
  @Inject
  BeschreibungsService boundary;
  @Inject
  BeschreibungsRepository repository;
  @Inject
  BeschreibungsRechte beschreibungsRechte;

  @Test
  void testModelValues() {

    BeschreibungModel beschreibungModel = getBeschreibungModel();

    assertEquals(12, beschreibungModel.getPositionColumnIDs().size());
    assertEquals("20", beschreibungModel.getColumnWidth(0));
    assertEquals(12, beschreibungModel.getVisibleColumns().size());
  }

  @Test
  @TestTransaction
  void testSortWithNullErstellungsDatum() {
    Beschreibung beschreibung1 = createBeschreibung("beschreibung1", "kod1", "Titel 1",
        "Sig. 1", "Alt. 1", "gnd-1", ERSTELL_DATUM);
    createtestBeschreibung(beschreibung1);
    Beschreibung beschreibung2 = createBeschreibung("beschreibung2", "kod2", "Titel 2",
        "Sig. 2", "Alt. 2", "gnd-2", null);
    createtestBeschreibung(beschreibung1);
    Beschreibung beschreibung3 = createBeschreibung("beschreibung3", "kod3", "Titel 3",
        "Sig. 3", "Alt. 3", "gnd-3", LocalDateTime.now());
    createtestBeschreibung(beschreibung1);

    BeschreibungModel beschreibungModel = getBeschreibungModel();

    repository.save(beschreibung1);
    repository.save(beschreibung2);
    repository.save(beschreibung3);

    beschreibungModel.reloadTableRecords();

    assertNotNull(beschreibungModel.getBeschreibungenDataModel());

    Map<String, SortMeta> sortBy = new HashMap<>();
    sortBy.put("erstellungsdatum", SortMeta.builder().field("erstellungsdatum").order(SortOrder.ASCENDING).build());

    List<BeschreibungListDTO> values = beschreibungModel.getBeschreibungenDataModel()
        .load(0, 10, sortBy, new HashMap<>());

    assertNotNull(values);
    assertEquals(3, values.size());

    assertEquals(beschreibung2.getId(), values.get(0).getHspId());
    assertEquals(beschreibung1.getId(), values.get(1).getHspId());
    assertEquals(beschreibung3.getId(), values.get(2).getHspId());
    repository.deleteByIdAndFlush(beschreibung1.getId());
    repository.deleteByIdAndFlush(beschreibung2.getId());
    repository.deleteByIdAndFlush(beschreibung3.getId());
  }

  @Test
  @TestTransaction
  void testReloadTableRecords() {
    Beschreibung beschreibung = createBeschreibung("beschreibung1", "kod1", "Titel 1",
        "Sig. 1", "Alt. 1", "gnd-1", ERSTELL_DATUM);

    BeschreibungModel beschreibungModel = getBeschreibungModel();
    beschreibungModel.setup();

    assertNotNull(beschreibungModel.getBeschreibungenDataModel());
    beschreibungModel.getBeschreibungenDataModel().load(0, 10, new HashMap<>(), new HashMap<>());

    int initSize = beschreibungModel.getBeschreibungenDataModel().getRowCount();
    createtestBeschreibung(beschreibung);

    beschreibungModel.reloadTableRecords();
    beschreibungModel.getBeschreibungenDataModel().load(0, 10, new HashMap<>(), new HashMap<>());

    assertEquals(initSize + 1, beschreibungModel.getBeschreibungenDataModel().getRowCount());
  }

  private BeschreibungModel getBeschreibungModel() {
    boundary.setDokumentSperreBoundary(mock(DokumentSperreBoundary.class));
    BeschreibungModel beschreibungModel = new BeschreibungModel(boundary,
        beschreibungsRechte, bearbeiterBoundary, false);
    beschreibungModel.setup();
    return beschreibungModel;
  }

  @TestTransaction
  void createtestBeschreibung(Beschreibung beschreibung) {
    repository.save(beschreibung);
  }

  @Test
  @TestTransaction
  void testDeleteSelectedBeschreibungen() {
    Beschreibung beschreibung = createBeschreibung("beschreibung1", "kod1", "Titel 1",
        "Sig. 1", "Alt. 1", "gnd-1", LocalDateTime.now().plusDays(3L));

    BeschreibungModel beschreibungModel = getBeschreibungModel();
    beschreibungModel.setup();

    assertNotNull(beschreibungModel.getBeschreibungenDataModel());
    beschreibungModel.getBeschreibungenDataModel().load(0, 10, new HashMap<>(), new HashMap<>());

    int initialSize = beschreibungModel.getBeschreibungenDataModel().getRowCount();

    createtestBeschreibung(beschreibung);

    beschreibungModel.reloadTableRecords();
    List<BeschreibungListDTO> allBeschreibungen = beschreibungModel.getBeschreibungenDataModel()
        .load(0, 10, new HashMap<>(), new HashMap<>());

    assertEquals(initialSize + 1, beschreibungModel.getBeschreibungenDataModel().getRowCount());
    beschreibungModel.setSelectedBeschreibungen(allBeschreibungen);

    when(bearbeiterBoundary.getLoggedBearbeiter())
        .thenReturn(new Bearbeiter("1", "Unbekannter Tester"));

    deleteSelectedBeschreibungenInNewTransaction(beschreibungModel);

    beschreibungModel.getBeschreibungenDataModel().load(0, 10, new HashMap<>(), new HashMap<>());

    assertEquals(initialSize, beschreibungModel.getBeschreibungenDataModel().getRowCount());
  }

  @TestTransaction
  void deleteSelectedBeschreibungenInNewTransaction(BeschreibungModel beschreibungModel) {
    beschreibungModel.deleteSelectedBeschreibungen();
  }

  @Test
  @TestTransaction
  void testReindexAllDocuments() throws Exception {
    BeschreibungsBoundary boundaryMock = mock(BeschreibungsBoundary.class);

    when(bearbeiterBoundary.getLoggedBearbeiter())
        .thenReturn(new Bearbeiter("1", "Unbekannter Tester"));

    BeschreibungModel beschreibungModel = new BeschreibungModel(boundaryMock,
        beschreibungsRechte, bearbeiterBoundary, false);

    beschreibungModel.reindexAllBeschreibungen();

    verify(boundaryMock, times(1)).reindexAllBeschreibungen(any(Bearbeiter.class));
  }

  @Test
  @TestTransaction
  void testCreateMessageForSperreAlreadyExistException() {
    when(bearbeiterBoundary.getLoggedBearbeiter())
        .thenReturn(new Bearbeiter("1", "Unbekannter Tester"));

    Beschreibung beschreibung1 = createBeschreibung("beschreibung1", "kod1", "Titel 1",
        "Sig. 1", "Alt. 1", "gnd-1", LocalDateTime.now().plusDays(3L));
    createtestBeschreibung(beschreibung1);

    Beschreibung beschreibung2 = createBeschreibung("beschreibung2", "kod2", "Titel 2",
        "Sig. 2", "Alt. 2", "gnd-2", LocalDateTime.now().plusDays(3L));
    createtestBeschreibung(beschreibung2);

    BeschreibungModel beschreibungModel = getBeschreibungModel();
    beschreibungModel.setup();

    List<BeschreibungListDTO> values = beschreibungModel.getBeschreibungenDataModel()
        .load(0, 10, new HashMap<>(), new HashMap<>());

    assertNotNull(values);
    assertEquals(2, values.size());

    Sperre sperre_1 = Sperre.newBuilder()
        .withSperreTyp(SperreTyp.MANUAL)
        .withId("Sperre1")
        .withBearbeiter(new Bearbeiter("2", "Bekannter Tester"))
        .withStartDatum(LocalDateTime.of(2021, 1, 1, 12, 0, 0))
        .addSperreEintrag(new SperreEintrag("beschreibung1", SperreDokumentTyp.BESCHREIBUNG))
        .build();

    Sperre sperre_2 = Sperre.newBuilder()
        .withSperreTyp(SperreTyp.MANUAL)
        .withId("Sperre2")
        .withBearbeiter(new Bearbeiter("2", "Bekannter Tester"))
        .withStartDatum(LocalDateTime.of(2021, 1, 1, 12, 0, 0))
        .addSperreEintrag(new SperreEintrag("kod2", SperreDokumentTyp.KOD))
        .build();

    assertEquals(2, beschreibungModel.getBeschreibungenDataModel().getRowCount());
    beschreibungModel.setSelectedBeschreibungen(values.subList(0, 1));

    SperreAlreadyExistException spaex_1 = new SperreAlreadyExistException("Ex 1",
        List.of(sperre_1));

    FacesMessage fm1 = beschreibungModel.createMessageForSperreAlreadyExistException(spaex_1);
    assertNotNull(fm1);
    assertEquals(SEVERITY_ERROR, fm1.getSeverity());
    assertEquals("Diese Beschreibung kann im Moment nicht gelöscht werden.", fm1.getSummary());
    assertEquals(
        "Sie oder ihr Kulturobjekt ist gesperrt durch Bekannter Tester seit 01.01.2021.\n",
        fm1.getDetail());

    beschreibungModel.setSelectedBeschreibungen(values);
    SperreAlreadyExistException spaex_2 = new SperreAlreadyExistException("Ex 2",
        List.of(sperre_1));

    FacesMessage fm2 = beschreibungModel.createMessageForSperreAlreadyExistException(spaex_2);
    assertNotNull(fm2);
    assertEquals(SEVERITY_ERROR, fm2.getSeverity());
    assertEquals("Diese Beschreibungen können im Moment nicht gelöscht werden.", fm2.getSummary());
    assertEquals(
        "Die Beschreibung beschreibung1 oder ihr Kulturobjekt ist gesperrt durch Bekannter Tester seit 01.01.2021.\n",
        fm2.getDetail());

    beschreibungModel.setSelectedBeschreibungen(values);
    SperreAlreadyExistException spaex_3 = new SperreAlreadyExistException("Ex 3",
        List.of(sperre_1, sperre_2));

    FacesMessage fm3 = beschreibungModel.createMessageForSperreAlreadyExistException(spaex_3);
    assertNotNull(fm3);
    assertEquals(SEVERITY_ERROR, fm3.getSeverity());
    assertEquals("Diese Beschreibungen können im Moment nicht gelöscht werden.", fm3.getSummary());
    assertEquals(
        "Die Beschreibung beschreibung1 oder ihr Kulturobjekt ist gesperrt durch Bekannter Tester seit 01.01.2021.\n"
            + "Die Beschreibung beschreibung2 oder ihr Kulturobjekt ist gesperrt durch Bekannter Tester seit 01.01.2021.\n",
        fm3.getDetail());

    beschreibungModel.setSelectedBeschreibungen(values);
    SperreAlreadyExistException spaex_4 = new SperreAlreadyExistException("Ex 4", null);

    FacesMessage fm4 = beschreibungModel.createMessageForSperreAlreadyExistException(spaex_4);
    assertNotNull(fm4);
    assertEquals(SEVERITY_ERROR, fm4.getSeverity());
    assertEquals("Diese Beschreibungen können im Moment nicht gelöscht werden.", fm4.getSummary());
    assertEquals(
        "Mindestens eine Beschreibung oder ihr Kulturobjekt ist durch einen anderen Bearbeiter gesperrt.",
        fm4.getDetail());
  }


  private Beschreibung createBeschreibung(String id, String kodId, String titel, String signatur, String altSignatur,
      String altIdentifier, LocalDateTime erstellungsDatum) {
    NormdatenReferenz besitzer =
        new NormdatenReferenz("774909e2", "Staatsbibliothek zu Berlin", "5036103-X", "CorporateBody");

    NormdatenReferenz aufbewahrungsOrt
        = new NormdatenReferenz("ee1611b6", "Berlin", "4005728-8", "Place");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent(signatur)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .build();

    Identifikation identifikationAlt = new Identifikation.IdentifikationBuilder()
        .withIdent(altSignatur)
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .build();

    BeschreibungsKomponenteKopf bkKopf = new BeschreibungsKomponenteKopfBuilder()
        .withId(UUID.randomUUID().toString())
        .withKulturObjektTyp(KulturObjektTyp.CODEX)
        .withTitel(titel)
        .withIndentifikationen(Set.of(identifikation, identifikationAlt))
        .build();

    return new BeschreibungsBuilder()
        .withId(id)
        .withKodID(kodId)
        .withKatalog("kat-1")
        .addBeschreibungsKomponente(bkKopf)
        .addAltIdentifier(altIdentifier)
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION)
        .withBeschreibungsTyp(BeschreibungsTyp.MEDIEVAL)
        .withErstellungsDatum(erstellungsDatum)
        .build();
  }


}
