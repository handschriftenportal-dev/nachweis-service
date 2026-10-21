package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.07.2022
 */
class KoerperschaftModelTest {

  NormdatenReferenzBoundary normdatenReferenzBoundary = Mockito.mock(NormdatenReferenzBoundary.class);

  KoerperschaftModel koerperschaftModel;

  NormdatenReferenz sbb;
  NormdatenReferenz bsb;

  @BeforeEach
  void onSetup() {
    sbb = createSBB();
    bsb = createBSB();

    when(normdatenReferenzBoundary.findAllByIdOrNameAndType(null, NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME, true))
        .thenReturn(Set.of(sbb, bsb));

    koerperschaftModel = new KoerperschaftModel(normdatenReferenzBoundary);
  }

  @Test
  void testSetup() {
    koerperschaftModel.setup();
    assertEquals(2, koerperschaftModel.getAllKoerperschaften().size());
  }

  @Test
  void testFindIsil() {
    assertNotNull(koerperschaftModel.findIsil(sbb));
    assertEquals("DE-1", koerperschaftModel.findIsil(sbb).getText());
    assertNull(koerperschaftModel.findIsil(null));
  }

  private NormdatenReferenz createSBB() {
    return new NormdatenReferenzBuilder()
        .withId("1")
        .withName("Stabi")
        .withGndID("12122-X")
        .withTypeName(NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME)
        .addIdentifikator(new Identifikator("DE-1", "", "Isil"))
        .addVarianterName(new VarianterName("Staatsbibliothek zu Berlin", "de"))
        .build();
  }

  private NormdatenReferenz createBSB() {
    return new NormdatenReferenzBuilder()
        .withId("2")
        .withName("BSB")
        .withGndID("12122-Z")
        .withTypeName(NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME)
        .addIdentifikator(new Identifikator("DE-2", "", "Isil"))
        .addVarianterName(new VarianterName("Bayerische Staatsbibliothek", "de"))
        .build();
  }
}
