package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static org.hamcrest.core.Is.is;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 13.06.2019.
 */
public class BeschreibungImportViewModelTest {


  @Test
  public void testGivenValuesCreate() {

    ImportViewModel importViewModel = new ImportViewModel.ImportViewModelBuilder().withId("1")
        .withBenutzer("Konrad").withInstitution("SBB").withDateiName("Test.xml")
        .withImportUrl("http://localhost.de").withImportDatum(LocalDateTime.of(2019,7,20,0,0,0))
        .withImportStatus(ImportStatus.OFFEN).withBeschreibung(Arrays.asList())
        .withFehler("Fehler im System")
        .build();

    MatcherAssert.assertThat(importViewModel.getId(), is("1"));

    MatcherAssert.assertThat(importViewModel.getBenutzer(), is("Konrad"));

    MatcherAssert.assertThat(importViewModel.getInstitution(), is("SBB"));

    MatcherAssert.assertThat(importViewModel.getDateiName(), is("Test.xml"));

    MatcherAssert.assertThat(importViewModel.getImportUrl(), is("http://localhost.de"));

    MatcherAssert.assertThat(importViewModel.getImportDatum(), is(LocalDateTime.of(2019,7,20,0,0,0)));

    Assertions.assertEquals("Fehler im System", importViewModel.getFehler());
  }

  @Test
  public void test_GivenTwoViews_Equals() {

    ImportViewModel importViewModel1 = new ImportViewModel.ImportViewModelBuilder().withId("1")
        .withBenutzer("Konrad").withInstitution("SBB").withDateiName("Test.xml")
        .withImportUrl("http://localhost.de").withImportDatum(LocalDateTime.of(2019,7,20,0,0,0))
        .withImportStatus(ImportStatus.OFFEN).withBeschreibung(Arrays.asList())
        .build();

    ImportViewModel importViewModel2 = new ImportViewModel.ImportViewModelBuilder().withId("2")
        .withBenutzer("Konrad").withInstitution("SBB").withDateiName("Test.xml")
        .withImportUrl("http://localhost.de").withImportDatum(LocalDateTime.of(2019,7,20,0,0,0))
        .withImportStatus(ImportStatus.OFFEN).withBeschreibung(Arrays.asList())
        .build();

    Assertions.assertNotEquals(importViewModel1, importViewModel2);
  }
}
