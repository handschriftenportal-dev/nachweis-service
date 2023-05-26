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

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.TEIController.CALLBACK_PARAMS_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DatenDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.faces.context.FacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class TEIControllerTest {

  TEIController teiController = new TEIController();

  FacesContext context = Mockito.mock(FacesContext.class);


  BeschreibungsRepository beschreibungsRepository = Mockito.mock(BeschreibungsRepository.class);

  KulturObjektDokumentRepository kulturObjektDokumentRepository = Mockito.mock(KulturObjektDokumentRepository.class);

  MockedStatic<FacesContext> facesContextCurrentInstance;

  @BeforeEach
  public void setUp() {
    teiController.setBeschreibungsRepository(beschreibungsRepository);
    teiController.setKulturObjektDokumentRepository(kulturObjektDokumentRepository);
    facesContextCurrentInstance = Mockito.mockStatic(FacesContext.class);
    facesContextCurrentInstance.when(FacesContext::getCurrentInstance).thenReturn(context);
  }

  @AfterEach
  public void derigisterStaticMock() {
    facesContextCurrentInstance.close();
  }

  @Test
  void loadWithIdBeschreibung() {

    final String testIdBeschreibung = "123_Beschreibung";

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder().withId(testIdBeschreibung).build();

    Map<String, String> callback_params = new HashMap<>();
    callback_params.put(TEIController.DATEN_DOKUMENT_ID, testIdBeschreibung);
    callback_params.put(TEIController.DATEN_DOKUMENT_TYP, DatenDokumentTyp.BESCHREIBUNG.toString());
    Map<Object, Object> attributes = new HashMap<>();
    attributes.put(CALLBACK_PARAMS_KEY, callback_params);

    when(context.getAttributes()).thenReturn(attributes);
    when(beschreibungsRepository.findByIdOptional(testIdBeschreibung)).thenReturn(Optional.ofNullable(beschreibung));

    teiController.loadWithID();

    assertEquals(testIdBeschreibung, teiController.getId());
  }

  @Test
  void loadWithIdKOD() {

    final String testIdKOD = "123_KOD";

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument.KulturObjektDokumentBuilder().withId(testIdKOD).build();

    Map<String, String> callback_params = new HashMap<>();
    callback_params.put(TEIController.DATEN_DOKUMENT_ID, testIdKOD);
    callback_params.put(TEIController.DATEN_DOKUMENT_TYP, DatenDokumentTyp.KOD.toString());
    Map<Object, Object> attributes = new HashMap<>();
    attributes.put(CALLBACK_PARAMS_KEY, callback_params);

    when(context.getAttributes()).thenReturn(attributes);
    when(kulturObjektDokumentRepository.findByIdOptional(testIdKOD)).thenReturn(Optional.ofNullable(kulturObjektDokument));

    teiController.loadWithID();

    assertEquals(testIdKOD, teiController.getId());
  }
}
