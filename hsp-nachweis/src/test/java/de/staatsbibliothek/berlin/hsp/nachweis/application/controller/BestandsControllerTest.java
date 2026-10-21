package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.TabChangeEvent;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.04.23.
 * @version 1.0
 */
@QuarkusTest
class BestandsControllerTest {

  @Inject
  private BestandsController controller;

  private BestandsBoundary bestandsBoundary = mock(BestandsBoundary.class);

  @Test
  void testCreation() {

    assertNotNull(controller);

    given()
        .when().get("/bestand.xhtml")
        .then()
        .statusCode(200);
  }

  @Test
  void testsetup() {
    Authentication authentication = mock(Authentication.class);
    BestandsBoundary bestandsBoundaryMock = mock(BestandsBoundary.class);
    BestandsController bestandsController = new BestandsController(bestandsBoundaryMock,
        authentication);
    BestandsView bestandsView = new BestandsView();

    when(bestandsBoundaryMock.createBestandsView()).thenReturn(bestandsView);

    bestandsController.setup();

    verify(bestandsBoundaryMock, times(1)).createBestandsView();
  }

  @Test
  void testOnTabChanged() {

    BestandsBoundary bestandsBoundaryMock = mock(BestandsBoundary.class);

    BestandsController bestandsController = new BestandsController(bestandsBoundaryMock,
        null);

    TabChangeEvent<?> tabChangeEvent = mock(TabChangeEvent.class);
    Tab tab = mock(Tab.class);

    when(tabChangeEvent.getTab()).thenReturn(
        tab);

    when(tab.getClientId()).thenReturn(
        BestandsController.CLIENTID_BESTAENDE_TAB_KOD_INSTITUTIONS);

    when(tab.isRendered()).thenReturn(true);

    when(bestandsBoundaryMock.createBestandsView()).thenReturn(new BestandsView());

    bestandsController.setup();

    bestandsController.onTabChanged(tabChangeEvent);

    verify(bestandsBoundaryMock, times(1)).loadBestandsInstitutionDataForKulturobjektdokument();

    when(tab.getClientId()).thenReturn(
        BestandsController.CLIENT_ID_BESTAENDE_TAB_BESCHREIBUNGEN_INSTITUTIONS);

    bestandsController.onTabChanged(tabChangeEvent);

    verify(bestandsBoundaryMock, times(1)).loadBestandsInstitutionDataForBeschreibungen();

    when(tab.getClientId()).thenReturn(
        BestandsController.CLIENT_ID_BESTAENDE_TAB_DIGITALISATE_INSTITUTIONS);

    bestandsController.onTabChanged(tabChangeEvent);

    verify(bestandsBoundaryMock, times(1)).loadBestandsInstitutionDataForDigitalisate();

    when(tab.getClientId()).thenReturn(
        BestandsController.CLIENTID_BESTAENDE_TAB);

    bestandsController.onTabChanged(tabChangeEvent);

    verify(bestandsBoundaryMock, times(1)).loadBestaendeOfAllEntities(any());
  }
}
