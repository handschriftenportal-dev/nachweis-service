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
 * @since 01.08.2022
 */
public class OrteModelTest {

  NormdatenReferenzBoundary normdatenReferenzBoundary = Mockito.mock(NormdatenReferenzBoundary.class);

  OrteModel orteModel;

  NormdatenReferenz abenberg;
  NormdatenReferenz konstanz;

  @BeforeEach
  void onSetup() {
    abenberg = createAbenberg();
    konstanz = createKonstanz();

    when(normdatenReferenzBoundary.findAllByIdOrNameAndType(null, NormdatenReferenz.ORT_TYPE_NAME, true))
        .thenReturn(Set.of(abenberg, konstanz));

    orteModel = new OrteModel(normdatenReferenzBoundary);
  }

  @Test
  void testSetup() {
    orteModel.setup();
    assertEquals(2, orteModel.getAllOrte().size());
  }

  @Test
  void testFindGeonames() {
    assertNotNull(orteModel.findGeonamesIdentifikator(abenberg));
    assertEquals("2959855", orteModel.findGeonamesIdentifikator(abenberg).getText());
    assertNull(orteModel.findGeonamesIdentifikator(konstanz));
  }

  @Test
  void testFindGettyIdentifikator() {
    assertNull(orteModel.findGettyIdentifikator(abenberg));
    assertNotNull(orteModel.findGettyIdentifikator(konstanz));
    assertEquals("7005174", orteModel.findGettyIdentifikator(konstanz).getText());
    assertNull(orteModel.findGeonamesIdentifikator(konstanz));
  }

  private NormdatenReferenz createAbenberg() {
    return new NormdatenReferenzBuilder()
        .withId("5a64e61f-68a0-392a-92ea-1daec3d6a036")
        .withName("Abenberg")
        .addVarianterName(new VarianterName("Stadt Abenberg", "de"))
        .withGndID("4297920-1")
        .addIdentifikator(new Identifikator("2959855", "https://www.geonames.org/2959855",
            Identifikator.GEONAMES_TYPE))
        .build();
  }

  private NormdatenReferenz createKonstanz() {
    return new NormdatenReferenzBuilder()
        .withId("850a7158-ac07-36bb-8fda-b07432ad13ad")
        .withName("Konstanz")
        .withGndID("4032215-4")
        .addIdentifikator(new Identifikator("7005174", "http://vocab.getty.edu/page/tgn/7005174",
            Identifikator.GETTY_TYPE))
        .build();
  }


}
