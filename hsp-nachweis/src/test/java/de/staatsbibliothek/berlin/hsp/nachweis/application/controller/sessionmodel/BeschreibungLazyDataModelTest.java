package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.TestUtils.BeschreibungTestUtils;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.primefaces.model.FilterMeta;

@QuarkusTest
class BeschreibungLazyDataModelTest {

  @Inject
  BeschreibungsBoundary beschreibungsBoundary;
  @Inject
  BeschreibungsRepository beschreibungsRepository;
  @Inject
  BearbeiterRepository bearbeiterRepository;

  BeschreibungLazyDataModel beschreibungLazyDataModel;

  @BeforeEach
  public void setUp() {
    beschreibungLazyDataModel = new BeschreibungLazyDataModel(beschreibungsBoundary);
  }


  @TestTransaction
  void setupTestData() {
    Bearbeiter bearbeiter = new Bearbeiter.BearbeiterBuilder().withBearbeitername("bearbeiter1")
        .withNachname("Nachname").withVorname("Vorname").build();
    Bearbeiter bearbeiter2 = new Bearbeiter.BearbeiterBuilder().withBearbeitername("bearbeiter2")
        .withNachname("Mustermann").withVorname("Max").build();
    bearbeiterRepository.save(bearbeiter);
    bearbeiterRepository.save(bearbeiter2);
    Beschreibung beschreibung_1 = BeschreibungTestUtils.createBeschreibung("b-1", "kod-1",
        "Titel-1", "SIG. I",
        "SIG. I alt",
        "alt_1",
        LocalDateTime.of(2021, 3, 3, 12, 0),
        Set.of(bearbeiter, bearbeiter2),
        true);
    beschreibung_1.setAenderungsDatum(LocalDateTime.of(2022, 4, 4, 12, 0));

    Beschreibung beschreibung_2 = BeschreibungTestUtils.createBeschreibung("b-2", "kod-2",
        "Titel-2", "SIG. II",
        "SIG. II alt",
        "alt_2",
        LocalDateTime.of(2021, 1, 1, 12, 0),
        Set.of(bearbeiter),
        false);
    beschreibung_2.setAenderungsDatum(LocalDateTime.of(2022, 2, 2, 12, 0));

    beschreibungsRepository.saveAndFlush(beschreibung_1);
    beschreibungsRepository.saveAndFlush(beschreibung_2);
  }

  @Test
  @TestTransaction
  void initialisation() {
    assertNotNull(beschreibungLazyDataModel);
    assertNotNull(beschreibungLazyDataModel.beschreibungsBoundary);
  }

  @Test
  @TestTransaction
  void testLoadMapped() {
    setupTestData();
    assertEquals(2, beschreibungLazyDataModel.loadMapped(0, 10, Map.of(), Map.of()).size());
  }

  @Test
  @TestTransaction
  void testLoadMappedWithFilter() {
    setupTestData();

    var result1 = beschreibungLazyDataModel.loadMapped(0, 10, Map.of(), Map.of("bearbeiter", "Nachname, Vorname"));
    var result2 = beschreibungLazyDataModel.loadMapped(0, 10, Map.of(), Map.of("bearbeiter", "Mustermann, Max"));

    assertEquals(2, result1.size());
    assertEquals(1, result2.size());
  }

  @Test
  @TestTransaction
  void testCount_withoutFilterMeta() {
    setupTestData();

    int result = beschreibungLazyDataModel.count(Map.of());

    assertEquals(2, result);
  }

  @Test
  @TestTransaction
  void testCount_withFilterMeta() {
    setupTestData();

    Map<String, FilterMeta> filterMetaMap = Map.of(
        "bearbeiter", FilterMeta.builder()
            .field("bearbeiter")
            .filterValue("Mustermann")
            .build()
    );

    int result = beschreibungLazyDataModel.count(filterMetaMap);
    assertEquals(1, result);
  }
}
