package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.detail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 01.08.2022
 */
public class OrteDetailControllerTest {

  OrteDetailController orteDetailController;

  FacesContext context = Mockito.mock(FacesContext.class);
  ExternalContext externalContext = Mockito.mock(ExternalContext.class);

  MockedStatic<FacesContext> facesContextCurrentInstance;

  NormdatenReferenz ort;

  @BeforeEach
  void onSetup() {
    facesContextCurrentInstance = Mockito.mockStatic(FacesContext.class);
    facesContextCurrentInstance.when(FacesContext::getCurrentInstance).thenReturn(context);

    NormdatenReferenzBoundary normdatenReferenzBoundary = Mockito.mock(NormdatenReferenzBoundary.class);

    ort = createAbenberg();

    when(normdatenReferenzBoundary.findOneByIdOrNameAndType(ort.getId(), NormdatenReferenz.ORT_TYPE_NAME))
        .thenReturn(Optional.of(ort));

    orteDetailController = new OrteDetailController(normdatenReferenzBoundary);

    when(context.getExternalContext()).thenReturn(externalContext);
    when(externalContext.getRequestParameterMap()).thenReturn(Map.of("id", ort.getId()));
  }

  @AfterEach
  public void deregisterStaticMock() {
    facesContextCurrentInstance.close();
  }

  @Test
  void setup() {
    orteDetailController.setup();
    assertNotNull(orteDetailController.getViewModel());
    assertEquals(ort, orteDetailController.getViewModel());
  }

  @Test
  void testFindGettyIdentifikator() {
    orteDetailController.setup();
    assertNotNull(orteDetailController.findGettyIdentifikator());
    assertEquals("1038105", orteDetailController.findGettyIdentifikator().getText());
  }

  @Test
  void testFindGeonamesIdentifikator() {
    orteDetailController.setup();
    assertNotNull(orteDetailController.findGeonamesIdentifikator());
    assertEquals("2959855", orteDetailController.findGeonamesIdentifikator().getText());
  }

  private NormdatenReferenz createAbenberg() {
    return new NormdatenReferenzBuilder()
        .withId("5a64e61f-68a0-392a-92ea-1daec3d6a036")
        .withName("Abenberg")
        .addVarianterName(new VarianterName("Stadt Abenberg", "de"))
        .withGndID("4297920-1")
        .addIdentifikator(new Identifikator("2959855", "https://www.geonames.org/2959855",
            Identifikator.GEONAMES_TYPE))
        .addIdentifikator(new Identifikator("1038105", "http://vocab.getty.edu/page/tgn/1038105",
            Identifikator.GETTY_TYPE))
        .build();
  }
}
