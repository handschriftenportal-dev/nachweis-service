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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterService.UNBEKANNTER_BEARBEITER_ID;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact.PERSON_TYPE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import java.util.Optional;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 12.11.2021
 */
public class BearbeiterControllerTest {

  MockedStatic<FacesContext> contextStatic = mockStatic(FacesContext.class);
  FacesContext facesContext = mock(FacesContext.class);
  ExternalContext context = mock(ExternalContext.class);
  Flash flash = mock(Flash.class);

  OAUTH2Config oauth2ConfigMock;

  BearbeiterBoundary bearbeiterBoundary;

  NormdatenReferenzBoundary normdatenReferenzBoundaryMock;

  NormdatenReferenz stabiNR = new NormdatenReferenz("1", "Staatsbibliothek zu Berlin", "5036103-X",
      NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);

  NormdatenReferenz lutherNR = new NormdatenReferenz("2", "Luther, Martin", "118575449",
      PERSON_TYPE_NAME);

  Bearbeiter bearbeiter;

  @BeforeEach
  void beforeEach() throws BearbeiterException {
    contextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
    when(facesContext.getExternalContext()).thenReturn(context);
    when(context.getFlash()).thenReturn(flash);

    bearbeiter = Bearbeiter.newBuilder()
        .withBearbeitername("b-ml123")
        .withVorname("Martin")
        .withNachname("Luther")
        .withEmail("martin@luther.de")
        .withRolle("Redakteur")
        .build();

    bearbeiterBoundary = mock(BearbeiterBoundary.class);
    when(bearbeiterBoundary.getLoggedBearbeiter())
        .thenReturn(bearbeiter);
    when(bearbeiterBoundary.getUnbekannterBearbeiter())
        .thenReturn(new Bearbeiter(UNBEKANNTER_BEARBEITER_ID, UNBEKANNTER_BEARBEITER_ID));
    when(bearbeiterBoundary.updateLoggedBearbeiter(any(NormdatenReferenz.class), any(NormdatenReferenz.class)))
        .thenAnswer(invocationOnMock -> {
          bearbeiter.setPerson((NormdatenReferenz) invocationOnMock.getArguments()[0]);
          bearbeiter.setInstitution((NormdatenReferenz) invocationOnMock.getArguments()[1]);
          return bearbeiter;
        });

    oauth2ConfigMock = mock(OAUTH2Config.class);
    when(oauth2ConfigMock.isLoginCheckEnabled()).thenReturn(true);

    normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(stabiNR.getId(), stabiNR.getTypeName()))
        .thenReturn(Optional.of(stabiNR));
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(lutherNR.getId(), lutherNR.getTypeName()))
        .thenReturn(Optional.of(lutherNR));
  }

  @AfterEach
  public void resetMocks() {
    contextStatic.close();
  }

  @Test
  void testSetup() {
    BearbeiterViewController controller = new BearbeiterViewController(bearbeiterBoundary, oauth2ConfigMock,
        normdatenReferenzBoundaryMock);

    controller.setup();

    assertEquals(bearbeiter, controller.getBearbeiter());
    assertNull(controller.getSelectedPerson());
    assertNull(controller.getSelectedInstitution());
    assertFalse(controller.isEdit());
    verify(bearbeiterBoundary, times(1)).getLoggedBearbeiter();
  }

  @Test
  void testBearbeiterAktualisieren() throws BearbeiterException {
    BearbeiterViewController controller = new BearbeiterViewController(bearbeiterBoundary, oauth2ConfigMock,
        normdatenReferenzBoundaryMock);

    controller.setup();
    controller.editieren();
    controller.setSelectedInstitution(stabiNR);
    controller.setSelectedPerson(lutherNR);

    controller.bearbeiterAktualisieren();

    assertFalse(controller.isEdit());
    assertEquals(bearbeiter, controller.getBearbeiter());
    assertEquals(lutherNR, controller.getBearbeiter().getPerson());
    assertEquals(stabiNR, controller.getBearbeiter().getInstitution());
    verify(bearbeiterBoundary, times(1)).getLoggedBearbeiter();
    verify(bearbeiterBoundary, times(1))
        .updateLoggedBearbeiter(any(NormdatenReferenz.class), any(NormdatenReferenz.class));
  }

  @Test
  void testEditierenAndAbbrechen() {
    BearbeiterViewController controller = new BearbeiterViewController(bearbeiterBoundary, oauth2ConfigMock,
        normdatenReferenzBoundaryMock);

    controller.setup();
    assertFalse(controller.isEdit());
    controller.editieren();
    assertTrue(controller.isEdit());
    controller.abbrechen();
    assertFalse(controller.isEdit());
  }

}
