package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.detail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

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
 * Author: Christoph Marten on 10.03.2021 at 16:02
 */
class PersonDetailControllerTest {

  PersonDetailController personDetailController;

  FacesContext context = Mockito.mock(FacesContext.class);
  ExternalContext externalContext = Mockito.mock(ExternalContext.class);

  MockedStatic<FacesContext> facesContextCurrentInstance;

  @BeforeEach
  void onSetup() {
    facesContextCurrentInstance = Mockito.mockStatic(FacesContext.class);
    facesContextCurrentInstance.when(FacesContext::getCurrentInstance).thenReturn(context);

    NormdatenReferenzBoundary normdatenReferenzBoundary = Mockito.mock(NormdatenReferenzBoundary.class);

    Optional<NormdatenReferenz> person = Optional.of(new NormdatenReferenzBuilder()
        .withTypeName(NormdatenReferenz.PERSON_TYPE_NAME)
        .withId("12")
        .withGndID("22-swe2223")
        .withName("Stefan")
        .addVarianterName(new VarianterName("Steff", "de"))
        .addVarianterName(new VarianterName("Steffa", "de"))
        .build());

    when(normdatenReferenzBoundary.findOneByIdOrNameAndType("12", NormdatenReferenz.PERSON_TYPE_NAME))
        .thenReturn(person);

    personDetailController = new PersonDetailController(normdatenReferenzBoundary);

    when(context.getExternalContext()).thenReturn(externalContext);
    when(externalContext.getRequestParameterMap()).thenReturn(Map.of("id", "12"));
  }

  @AfterEach
  public void deregisterStaticMock() {
    facesContextCurrentInstance.close();
  }

  @Test
  void setup() {
    personDetailController.setup();
    assertNotNull(personDetailController.getViewModel());
    assertEquals("Stefan", personDetailController.getViewModel().getName());
    assertEquals("Steffa; Steff",
        personDetailController.getViewModel().getVarianteNamenAlsString());
    assertEquals("22-swe2223", personDetailController.getViewModel().getGndID());
    assertEquals("12", personDetailController.getViewModel().getId());
  }
}
