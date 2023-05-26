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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.PURLsModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLViewModel;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.primefaces.PrimeFaces;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 10.08.2022
 */
class PURLsControllerTest {

  MockedStatic<FacesContext> facesContextStatic;
  FacesContext facesContext;

  MockedStatic<PrimeFaces> primeFacesStatic;
  PrimeFaces primeFaces;

  PURLRepository purlRepositoryMock;
  KulturObjektDokumentBoundary kulturObjektDokumentBoundaryMock;
  BeschreibungsBoundary beschreibungsBoundaryMock;
  PURLBoundary purlBoundaryMock;

  PURLsModel purlsModel;
  PURLViewModel kodViewModel;
  PURLViewModel beschreibungViewModel;
  PURLViewModel kodViewModelNoPURLs;
  PURLViewModel beschreibungViewModelNoPURLs;

  @BeforeEach
  void beforeEach() {
    facesContext = mock(FacesContext.class);
    facesContextStatic = mockStatic(FacesContext.class);
    facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

    primeFaces = mock(PrimeFaces.class);
    primeFacesStatic = mockStatic(PrimeFaces.class);
    primeFacesStatic.when(PrimeFaces::current).thenReturn(primeFaces);

    purlBoundaryMock = mock(PURLService.class);
    when(purlBoundaryMock.isTemplateValid(nullable(String.class))).thenCallRealMethod();

    kulturObjektDokumentBoundaryMock = mock(KulturObjektDokumentBoundary.class);
    beschreibungsBoundaryMock = mock(BeschreibungsBoundary.class);

    kodViewModel = new PURLViewModel("HSP-123", DokumentObjektTyp.HSP_OBJECT,
        Set.of(new PURL(URI.create("https://resolver.test/internal_123"),
            URI.create("https://hsp.de/HSP-123"), PURLTyp.INTERNAL)));

    beschreibungViewModel = new PURLViewModel("HSP-789", DokumentObjektTyp.HSP_DESCRIPTION,
        Set.of(new PURL(URI.create("https://resolver.test/internal_789"),
            URI.create("https://hsp.de/HSP-789"), PURLTyp.INTERNAL)));

    kodViewModelNoPURLs = new PURLViewModel("HSP-456", DokumentObjektTyp.HSP_OBJECT, Collections.emptySet());

    beschreibungViewModelNoPURLs = new PURLViewModel("HSP-321", DokumentObjektTyp.HSP_DESCRIPTION,
        Collections.emptySet());

    purlRepositoryMock = mock(PURLRepository.class);
    when(purlRepositoryMock.findAllAsViewModels())
        .thenReturn(Set.of(kodViewModel, kodViewModelNoPURLs));

    purlsModel = new PURLsModel(purlRepositoryMock);
  }

  @AfterEach
  public void resetMocks() {
    facesContextStatic.close();
    primeFacesStatic.close();
  }

  @Test
  void testPurlsGenerieren_error() throws Exception {

    doThrow(new KulturObjektDokumentException("Error KOD-PURL"))
        .when(kulturObjektDokumentBoundaryMock).addPURL(any(), any());

    doThrow(new KulturObjektDokumentException("Error Beschreibung-PURL"))
        .when(beschreibungsBoundaryMock).addPURL(any(), any());

    PURLsController controller = new PURLsController(purlsModel,
        purlBoundaryMock,
        kulturObjektDokumentBoundaryMock,
        beschreibungsBoundaryMock);

    purlsModel.loadAllData();

    purlsModel.setSelectedPURLViewModels(new ArrayList<>(Arrays.asList(kodViewModel, beschreibungViewModel)));

    controller.purlsGenerieren();

    verify(kulturObjektDokumentBoundaryMock, times(1)).addPURL(any(), any());
    verify(beschreibungsBoundaryMock, times(1)).addPURL(any(), any());

    assertNotNull(controller.getGeneriertePURLsErfolg());
    assertEquals(0, controller.getGeneriertePURLsErfolg().size());

    assertNotNull(controller.getGeneriertePURLsFehler());
    assertEquals(2, controller.getGeneriertePURLsFehler().size());

    assertEquals("Error KOD-PURL", controller.getGeneriertePURLsFehler()
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey().equals(kodViewModel))
        .findFirst()
        .map(Entry::getValue)
        .orElse("Matching entry not found"));

    assertEquals("Error Beschreibung-PURL", controller.getGeneriertePURLsFehler()
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey().equals(beschreibungViewModel))
        .findFirst()
        .map(Entry::getValue)
        .orElse("Matching entry not found"));
  }

  @Test
  void testPurlsGenerieren_success() throws Exception {
    when(purlBoundaryMock.createNewPURL(any(), any())).thenReturn(new PURL());

    PURLsController controller = new PURLsController(purlsModel,
        purlBoundaryMock,
        kulturObjektDokumentBoundaryMock,
        beschreibungsBoundaryMock);

    purlsModel.loadAllData();

    purlsModel.setSelectedPURLViewModels(
        new ArrayList<>(Arrays.asList(kodViewModelNoPURLs, beschreibungViewModelNoPURLs)));

    controller.purlsGenerieren();

    verify(kulturObjektDokumentBoundaryMock, times(1)).addPURL(any(), any());
    verify(beschreibungsBoundaryMock, times(1)).addPURL(any(), any());

    assertNotNull(controller.getGeneriertePURLsErfolg());
    assertEquals(2, controller.getGeneriertePURLsErfolg().size());

    assertNotNull(controller.getGeneriertePURLsFehler());
    assertEquals(0, controller.getGeneriertePURLsFehler().size());

    assertTrue(controller.getGeneriertePURLsErfolg()
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey().equals(kodViewModelNoPURLs))
        .findFirst()
        .map(Entry::getValue)
        .isPresent());

    assertTrue(controller.getGeneriertePURLsErfolg()
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey().equals(beschreibungViewModelNoPURLs))
        .findFirst()
        .map(Entry::getValue)
        .isPresent());
  }

  @Test
  void testPurlGenerieren_KOD() throws BeschreibungsException, PURLException {
    PURLsController controller = new PURLsController(purlsModel,
        purlBoundaryMock,
        kulturObjektDokumentBoundaryMock,
        beschreibungsBoundaryMock);

    controller.purlGenerieren("123", DokumentObjektTyp.HSP_OBJECT);
    verify(purlBoundaryMock, times(1)).createNewPURL(any(), any());
    verify(kulturObjektDokumentBoundaryMock, times(1)).addPURL(any(), any());
    verify(beschreibungsBoundaryMock, times(0)).addPURL(any(), any());
  }

  @Test
  void testPurlGenerieren_Beschreibung() throws BeschreibungsException, PURLException {
    PURLsController controller = new PURLsController(purlsModel,
        purlBoundaryMock,
        kulturObjektDokumentBoundaryMock,
        beschreibungsBoundaryMock);

    controller.purlGenerieren("123", DokumentObjektTyp.HSP_DESCRIPTION);
    verify(purlBoundaryMock, times(1)).createNewPURL("123", DokumentObjektTyp.HSP_DESCRIPTION);
    verify(beschreibungsBoundaryMock, times(1)).addPURL(any(), any());
    verify(kulturObjektDokumentBoundaryMock, times(0)).addPURL(any(), any());
  }

  @Test
  void testPurlGenerieren_BeschreibungRetro() throws BeschreibungsException, PURLException {
    PURLsController controller = new PURLsController(purlsModel,
        purlBoundaryMock,
        kulturObjektDokumentBoundaryMock,
        beschreibungsBoundaryMock);

    controller.purlGenerieren("123", DokumentObjektTyp.HSP_DESCRIPTION_RETRO);
    verify(purlBoundaryMock, times(1)).createNewPURL("123", DokumentObjektTyp.HSP_DESCRIPTION_RETRO);
    verify(beschreibungsBoundaryMock, times(1)).addPURL(any(), any());
    verify(kulturObjektDokumentBoundaryMock, times(0)).addPURL(any(), any());
  }

  @Test
  void testPurlsAktualisieren() throws PURLException {
    PURLsController controller = new PURLsController(purlsModel,
        purlBoundaryMock,
        kulturObjektDokumentBoundaryMock,
        beschreibungsBoundaryMock);

    purlsModel.loadAllData();

    controller.purlsAktualisieren();

    verify(purlBoundaryMock, times(1)).updateInternalPURLs(any());

    ArgumentCaptor<FacesMessage> argument = ArgumentCaptor.forClass(FacesMessage.class);
    verify(facesContext, times(1)).addMessage(nullable(String.class), argument.capture());
    assertEquals("0 PURLs wurden erfolgreich aktualisiert.",
        argument.getValue().getSummary());
  }

  @Test
  void testResolverDateiGenerieren() throws PURLException {
    PURLsController controller = new PURLsController(purlsModel,
        purlBoundaryMock,
        kulturObjektDokumentBoundaryMock,
        beschreibungsBoundaryMock);

    purlsModel.loadAllData();

    controller.resolverDateiGenerieren();

    verify(purlBoundaryMock, times(1)).createDBMFile(any());

    ArgumentCaptor<FacesMessage> argument = ArgumentCaptor.forClass(FacesMessage.class);
    verify(facesContext, times(1)).addMessage(nullable(String.class), argument.capture());
    assertEquals("Die Resolver-Datei wurde erfolgreich generiert.",
        argument.getValue().getSummary());
  }

  @Test
  void testTemplatesAktualisieren() throws PURLException {
    PURLsController controller = new PURLsController(purlsModel,
        purlBoundaryMock,
        kulturObjektDokumentBoundaryMock,
        beschreibungsBoundaryMock);

    purlsModel.loadAllData();

    String kodTemplateNeu = "https://kod.neu/{0}";
    String beschreibungTemplateNeu = "https://beschreibung.neu/{0}";
    String beschreibungRetroTemplateNeu = "https://beschreibung_retro.neu/{0}";

    controller.editieren();
    controller.setKodTargetUrlTemplate(kodTemplateNeu);
    controller.setBeschreibungTargetUrlTemplate(beschreibungTemplateNeu);
    controller.setBeschreibungRetroTargetUrlTemplate(beschreibungRetroTemplateNeu);

    controller.templatesAktualisieren();

    verify(purlBoundaryMock, times(1))
        .updateTargetTemplate(DokumentObjektTyp.HSP_OBJECT, kodTemplateNeu);
    verify(purlBoundaryMock, times(1))
        .updateTargetTemplate(DokumentObjektTyp.HSP_DESCRIPTION, beschreibungTemplateNeu);
    verify(purlBoundaryMock, times(1))
        .updateTargetTemplate(DokumentObjektTyp.HSP_DESCRIPTION_RETRO, beschreibungRetroTemplateNeu);

    ArgumentCaptor<FacesMessage> argument = ArgumentCaptor.forClass(FacesMessage.class);
    verify(facesContext, times(1)).addMessage(nullable(String.class), argument.capture());
    assertEquals("Die Vorlagen wurden erfolgreich aktualisiert.", argument.getValue().getSummary());

    assertFalse(controller.isEdit());
  }

  @Test
  void testEditierenAbbrechen() {
    PURLsController controller = new PURLsController(purlsModel,
        purlBoundaryMock,
        kulturObjektDokumentBoundaryMock,
        beschreibungsBoundaryMock);

    purlsModel.loadAllData();
    assertFalse(controller.isEdit());

    controller.editieren();
    assertTrue(controller.isEdit());

    controller.abbrechen();
    assertFalse(controller.isEdit());
  }

  @Test
  void initTargetTemplates() {
    String kodTemplate = "https://kod.template/{0}";
    String beschreibungTemplate = "https://beschreibung.template/{0}";
    String beschreibungRetroTemplate = "https://beschreibung_retro.template/{0}";

    when(purlBoundaryMock.getTargetTemplate(DokumentObjektTyp.HSP_OBJECT)).thenReturn(kodTemplate);
    when(purlBoundaryMock.getTargetTemplate(DokumentObjektTyp.HSP_DESCRIPTION)).thenReturn(beschreibungTemplate);
    when(purlBoundaryMock.getTargetTemplate(DokumentObjektTyp.HSP_DESCRIPTION_RETRO)).thenReturn(
        beschreibungRetroTemplate);

    PURLsController controller = new PURLsController(purlsModel,
        purlBoundaryMock,
        kulturObjektDokumentBoundaryMock,
        beschreibungsBoundaryMock);

    controller.initTargetTemplates();

    assertEquals(kodTemplate, controller.getKodTargetUrlTemplate());
    assertEquals(beschreibungTemplate, controller.getBeschreibungTargetUrlTemplate());
    assertEquals(beschreibungRetroTemplate, controller.getBeschreibungRetroTargetUrlTemplate());
  }

  @Test
  void testValidateTargetTemplate() {
    PURLsController controller = new PURLsController(purlsModel,
        purlBoundaryMock,
        kulturObjektDokumentBoundaryMock,
        beschreibungsBoundaryMock);

    UIComponent uiComponentMock = mock(UIComponent.class);
    controller.validateTargetTemplate(facesContext, uiComponentMock, "https://kod.template/{0}");

    ValidatorException validatorException = assertThrows(ValidatorException.class,
        () -> controller.validateTargetTemplate(facesContext, uiComponentMock, "https://kod.template/"));

    assertNotNull(validatorException.getFacesMessage());
    assertEquals("Das Template https://kod.template/ ist ungültig.",
        validatorException.getFacesMessage().getSummary());
    assertEquals("Erforderlich ist eine URL mit {0} als Platzhalter für die Dokument-Id.",
        validatorException.getFacesMessage().getDetail());
  }

}
