package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KatalogDirector;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 02.03.2022.
 * @version 1.0
 */
public class KatalogModelTest {

  @Test
  void testCreation() {

    KatalogModel katalogModel = new KatalogModel();
    assertNotNull(katalogModel);

    assertEquals(8, katalogModel.getVisibleColumns().size());
    assertEquals(8, katalogModel.getPositionColumns().size());
    assertEquals(8, katalogModel.getPositionColumnIDs().size());
  }

  @Test
  void testLoadAllData() {

    KatalogModel katalogModel = new KatalogModel();
    katalogModel.loadAllData();
    assertNotNull(katalogModel.getAllKataloge());
  }

  @Test
  void testGetAutoren() {
    KatalogModel katalogModel = new KatalogModel();
    KatalogDirector katalogDirector = new KatalogDirector(new KatalogBuilder());
    Katalog katalog = katalogDirector.createSimpleHSKKatalog("Titel Katalog", "<TEI></TEI>", "1985",
        "440",
        null);
    assertEquals("", katalogModel.getAutoren(katalog));

    katalog.getAutoren().add(new NormdatenReferenz("1", "Irene Stahl", "12312313"));

    assertEquals("Irene Stahl", katalogModel.getAutoren(katalog));
  }

  @Test
  void testVerlagAsString() {
    KatalogModel katalogModel = new KatalogModel();
    KatalogDirector katalogDirector = new KatalogDirector(new KatalogBuilder());
    Katalog katalog = katalogDirector.createSimpleHSKKatalog("Titel Katalog", "<TEI></TEI>", "1985",
        "440",
        new NormdatenReferenz("1", "Harrassnowitz", "2323"));
    assertEquals("Harrassnowitz", katalogModel.getVerlag(katalog));
  }

  @Test
  void testDateAsString() {
    KatalogModel katalogModel = new KatalogModel();
    KatalogDirector katalogDirector = new KatalogDirector(new KatalogBuilder());
    Katalog katalog = katalogDirector.createSimpleHSKKatalog("Titel Katalog", "<TEI></TEI>", "1985",
        "440",
        new NormdatenReferenz("1", "Harrassnowitz", "2323"));
    Assertions.assertEquals(katalog.getErstellDatum()
            .format(DateTimeFormatter.ofPattern(KatalogModel.KATALOG_DATE_PATTERN)),
        katalogModel.getDatumAsString(katalog.getErstellDatum()));
  }
}
