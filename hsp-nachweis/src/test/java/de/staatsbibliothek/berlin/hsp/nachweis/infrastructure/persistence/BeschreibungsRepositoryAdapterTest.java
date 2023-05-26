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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 12.04.22
 */

@QuarkusTest
class BeschreibungsRepositoryAdapterTest {

  static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

  @Inject
  BeschreibungsRepository beschreibungsRepository;

  @Test
  @TestTransaction
  void testFindByAltIdentifier() {
    String altIdentifier = "alt_Id";
    Beschreibung beschreibung_1 = createBeschreibung("b-1", "kod-1", "Titel-1", "SIG. I", "SIG. I alt",
        altIdentifier, true);

    beschreibungsRepository.save(beschreibung_1);

    Beschreibung result = beschreibungsRepository.findByAltIdentifier(altIdentifier);
    assertNotNull(result);
    assertEquals(beschreibung_1, result);
  }

  @Test
  @TestTransaction
  void testFindByKatalogId() {
    Beschreibung beschreibung_1 = createBeschreibung("b-1", "kod-1", "Titel-1", "SIG. I", "SIG. I alt",
        "alt_1", true);

    beschreibungsRepository.save(beschreibung_1);
    List<Beschreibung> beschreibungen = beschreibungsRepository.findByKatalogId("kat-1");
    assertNotNull(beschreibungen);
    assertEquals(1, beschreibungen.size());
    Assertions.assertEquals(beschreibung_1, beschreibungen.get(0));
  }

  @Test
  @TestTransaction
  void testFindLatestModified() {
    Beschreibung beschreibung_1 = createBeschreibung("b-1", "kod-1", "Titel-1", "SIG. I", "SIG. I alt",
        "alt_1", true);
    beschreibung_1.setErstellungsDatum(LocalDateTime.of(2021, 3, 3, 12, 0));
    beschreibung_1.setAenderungsDatum(LocalDateTime.of(2022, 4, 4, 12, 0));

    Beschreibung beschreibung_2 = createBeschreibung("b-2", "kod-2", "Titel-2", "SIG. II", "SIG. II alt",
        "alt_2", false);
    beschreibung_2.setErstellungsDatum(LocalDateTime.of(2021, 1, 1, 12, 0));
    beschreibung_2.setAenderungsDatum(LocalDateTime.of(2022, 2, 2, 12, 0));

    beschreibungsRepository.saveAndFlush(beschreibung_1);
    beschreibungsRepository.saveAndFlush(beschreibung_2);

    List<BeschreibungListDTO> viewModels = beschreibungsRepository.findLatestModified();
    assertNotNull(viewModels);
    assertEquals(2, viewModels.size());

    assertBeschreibungListDTOEqualsBeschreibung(beschreibung_1, viewModels.get(0));
    assertBeschreibungListDTOEqualsBeschreibung(beschreibung_2, viewModels.get(1));
  }

  @Test
  @TestTransaction
  void testFindFilteredBeschreibungListDTOs() {
    Beschreibung beschreibung = createBeschreibung("b-1", "kod-1", "Titel-1", "SIG. I", "SIG. I alt",
        "alt_1", true);
    beschreibung.setErstellungsDatum(LocalDateTime.of(2021, 3, 3, 12, 0));
    beschreibung.setAenderungsDatum(LocalDateTime.of(2022, 4, 4, 12, 0));

    beschreibungsRepository.saveAndFlush(beschreibung);

    Map<String, Boolean> sortByAsc = new HashMap<>();
    sortByAsc.put("hspId", Boolean.TRUE);

    Map<String, String> filterBy_1 = new HashMap<>();
    filterBy_1.put("kodId", "HSP-3B");
    List<BeschreibungListDTO> result_1 =
        beschreibungsRepository.findFilteredBeschreibungListDTOs(0, 10, filterBy_1, sortByAsc);
    assertNotNull(result_1);
    assertEquals(0, result_1.size());

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    Identifikation identifikation = beschreibung.getGueltigeIdentifikation().orElseThrow();

    Map<String, String> filterBy_2 = new HashMap<>();
    filterBy_2.put("hspId", beschreibung.getId());
    filterBy_2.put("kodId", beschreibung.getKodID());
    filterBy_2.put("katalogId", beschreibung.getKatalogID());
    filterBy_2.put("gueltigeSignatur", identifikation.getIdent());
    filterBy_2.put("titel", "TITEL-1");
    filterBy_2.put("verwaltungsTyp", beschreibung.getVerwaltungsTyp().name());
    filterBy_2.put("dokumentObjektTyp", beschreibung.getDokumentObjektTyp().name());
    filterBy_2.put("aufbewahrungsort", identifikation.getAufbewahrungsOrt().getName());
    filterBy_2.put("besitzer", identifikation.getBesitzer().getName());
    filterBy_2.put("erstellungsdatumString", beschreibung.getErstellungsDatum().format(dtf));
    filterBy_2.put("aenderungsdatumString", beschreibung.getAenderungsDatum().format(dtf));

    List<BeschreibungListDTO> result_2 = beschreibungsRepository.findFilteredBeschreibungListDTOs(0,
        10, filterBy_2, sortByAsc);
    assertNotNull(result_2);
    assertEquals(1, result_2.size());

    assertBeschreibungListDTOEqualsBeschreibung(beschreibung, result_2.get(0));
  }

  @Test
  @TestTransaction
  void testCountFilteredBeschreibungListDTOs() {
    Beschreibung beschreibung = createBeschreibung("b-1", "kod-1", "Titel-1", "SIG. I", "SIG. I alt",
        "alt_1", true);
    beschreibung.setErstellungsDatum(LocalDateTime.of(2021, 3, 3, 12, 0));
    beschreibung.setAenderungsDatum(LocalDateTime.of(2022, 4, 4, 12, 0));
    beschreibungsRepository.saveAndFlush(beschreibung);

    Map<String, String> filterBy_1 = new HashMap<>();
    filterBy_1.put("kodId", "HSP-3B");

    assertEquals(0, beschreibungsRepository.countFilteredBeschreibungListDTOs(filterBy_1));

    Identifikation identifikation = beschreibung.getGueltigeIdentifikation().orElseThrow();

    Map<String, String> filterBy_2 = new HashMap<>();
    filterBy_2.put("hspId", beschreibung.getId());
    filterBy_2.put("kodId", beschreibung.getKodID());
    filterBy_2.put("katalogId", beschreibung.getKatalogID());
    filterBy_2.put("gueltigeSignatur", identifikation.getIdent());
    filterBy_2.put("titel", "TITEL-1");
    filterBy_2.put("verwaltungsTyp", beschreibung.getVerwaltungsTyp().name());
    filterBy_2.put("dokumentObjektTyp", beschreibung.getDokumentObjektTyp().name());
    filterBy_2.put("aufbewahrungsort", identifikation.getAufbewahrungsOrt().getName());
    filterBy_2.put("besitzer", identifikation.getBesitzer().getName());
    filterBy_2.put("erstellungsdatumString", beschreibung.getErstellungsDatum().format(DATE_TIME_FORMATTER));
    filterBy_2.put("aenderungsdatumString", beschreibung.getAenderungsDatum().format(DATE_TIME_FORMATTER));

    assertEquals(1, beschreibungsRepository.countFilteredBeschreibungListDTOs(filterBy_2));
  }

  @Test
  @TestTransaction
  void testFindBeschreibungListDTOById() {
    Beschreibung beschreibung = createBeschreibung("b-1", "kod-1", "Titel-1", "SIG. I", "SIG. I alt",
        "alt_1", true);
    beschreibung.setErstellungsDatum(LocalDateTime.of(2021, 3, 3, 12, 0));
    beschreibung.setAenderungsDatum(LocalDateTime.of(2022, 4, 4, 12, 0));
    beschreibungsRepository.saveAndFlush(beschreibung);

    BeschreibungListDTO viewModel = beschreibungsRepository.findBeschreibungListDTOById(
        beschreibung.getId());

    assertNotNull(viewModel);

    assertBeschreibungListDTOEqualsBeschreibung(beschreibung, viewModel);

    assertNull(beschreibungsRepository.findBeschreibungListDTOById("does.not.exist"));
  }

  private Beschreibung createBeschreibung(String id, String kodId, String titel, String signatur, String altSignatur,
      String altIdentifier, boolean intern) {
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
        .withVerwaltungsTyp(intern ? VerwaltungsTyp.INTERN : VerwaltungsTyp.EXTERN)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION)
        .withBeschreibungsTyp(BeschreibungsTyp.MEDIEVAL)
        .build();
  }

  void assertBeschreibungListDTOEqualsBeschreibung(Beschreibung beschreibung,
      BeschreibungListDTO viewModel) {
    assertNotNull(beschreibung);
    assertNotNull(viewModel);

    Identifikation identifikation = beschreibung.getGueltigeIdentifikation().orElse(null);
    assertNotNull(identifikation);

    BeschreibungsKomponenteKopf bkKopf = beschreibung.getBeschreibungsStruktur().stream()
        .filter(BeschreibungsKomponenteKopf.class::isInstance)
        .map(BeschreibungsKomponenteKopf.class::cast)
        .findFirst()
        .orElse(null);
    assertNotNull(bkKopf);

    assertEquals(beschreibung.getId(), viewModel.getHspId());
    assertEquals(beschreibung.getKodID(), viewModel.getKodId());
    assertEquals(beschreibung.getKatalogID(), viewModel.getKatalogId());
    assertEquals(beschreibung.getVerwaltungsTyp().name(), viewModel.getVerwaltungsTyp());
    assertEquals(identifikation.getIdent(), viewModel.getGueltigeSignatur());
    assertEquals(bkKopf.getTitel(), viewModel.getTitel());
    assertEquals(identifikation.getAufbewahrungsOrt().getName(), viewModel.getAufbewahrungsort());
    assertEquals(identifikation.getBesitzer().getName(), viewModel.getBesitzer());
    assertEquals(beschreibung.getErstellungsDatum(), viewModel.getErstellungsdatum());
    assertEquals(beschreibung.getErstellungsDatum().format(DATE_TIME_FORMATTER), viewModel.getErstellungsdatumString());
    assertEquals(beschreibung.getAenderungsDatum(), viewModel.getAenderungsdatum());
    assertEquals(beschreibung.getAenderungsDatum().format(DATE_TIME_FORMATTER), viewModel.getAenderungsdatumString());
  }

}
