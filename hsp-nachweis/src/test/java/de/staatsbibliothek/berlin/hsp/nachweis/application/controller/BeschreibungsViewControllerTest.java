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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.faces.context.ExternalContext;
import javax.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.primefaces.model.StreamedContent;
import org.tei_c.ns._1.TEI;

/**
 * Created by Christoph Marten on 29.09.2020 at 13:03
 */
class BeschreibungsViewControllerTest {

  static TEI tei;
  BeschreibungsViewController beschreibungsViewController = new BeschreibungsViewController();
  ExternalContext externalContext = mock(ExternalContext.class);
  BeschreibungsBoundary beschreibungsBoundary = mock(BeschreibungsBoundary.class);
  BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);

  @Test
  public void testConstruction() throws JAXBException, IOException {
    beschreibungsViewController.setExternalContext(externalContext);
    beschreibungsViewController.setBeschreibungsBoundary(beschreibungsBoundary);
    beschreibungsViewController.setBearbeiterBoundary(bearbeiterBoundary);
    Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();
    requestParameterMap.put("id", "1");

    when(externalContext.getRequestParameterMap()).thenReturn(requestParameterMap);
    when(beschreibungsBoundary.buildBeschreibungsViewModel("1")).thenReturn(Optional.of(new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("1")
        .withTitel("Flavius Josephus, de bello Judaico")
        .withSignatur("Mscr.Dresd.A.111")
        .withBestandhaltendeInstitutionName("Sächsische Landesbibliothek - Staats- und Universitätsbibliothek Dresden")
        .withBestandhaltendeInstitutionOrt("Berlin")
        .build()));

    beschreibungsViewController.setup();

    String id = beschreibungsViewController.getBeschreibungsViewModel().getId();
    String bestandhaltendeInstitutionName = beschreibungsViewController.getBeschreibungsViewModel()
        .getBestandhaltendeInstitutionName();
    String bestandhaltendeInstitutionOrt = beschreibungsViewController.getBeschreibungsViewModel()
        .getBestandhaltendeInstitutionOrt();
    String signatur = beschreibungsViewController.getBeschreibungsViewModel().getSignatur();
    String titel = beschreibungsViewController.getBeschreibungsViewModel().getTitel();

    assertEquals("1", id);
    assertEquals("Sächsische Landesbibliothek - Staats- und Universitätsbibliothek Dresden",
        bestandhaltendeInstitutionName);
    assertEquals("Berlin", bestandhaltendeInstitutionOrt);
    assertEquals("Mscr.Dresd.A.111", signatur);
    assertEquals("Flavius Josephus, de bello Judaico", titel);
  }

  @Test
  void testGetTeiContent() {
    beschreibungsViewController.setExternalContext(externalContext);
    beschreibungsViewController.setBeschreibungsBoundary(beschreibungsBoundary);
    beschreibungsViewController.setBearbeiterBoundary(bearbeiterBoundary);
    Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();
    requestParameterMap.put("id", "1");

    when(externalContext.getRequestParameterMap()).thenReturn(requestParameterMap);
    when(beschreibungsBoundary.buildBeschreibungsViewModel("1")).thenReturn(
        Optional.of(new BeschreibungsViewModel.BeschreibungsViewBuilder()
            .withID("1")
            .withTEIXML("<TEI/>")
            .build()));

    beschreibungsViewController.setup();

    StreamedContent teiContent = beschreibungsViewController.getTeiContent();
    assertNotNull(teiContent);
    assertEquals("tei-1.xml", teiContent.getName());
    assertEquals("text/xml", teiContent.getContentType());
    assertEquals(6, teiContent.getContentLength());
  }

  @Test
  void testGetPublikationsDatum() {
    Publikation publikation_1 = new Publikation("1", LocalDateTime.of(2020, 1, 2, 0, 0),
        PublikationsTyp.ERSTPUBLIKATION);
    Publikation publikation_2 = new Publikation("1", LocalDateTime.of(2020, 3, 4, 0, 0),
        PublikationsTyp.PUBLIKATION_HSP);

    List<Publikation> publikationen = Arrays.asList(publikation_1, publikation_2);

    assertEquals(publikation_1.getDatumDerVeroeffentlichung(),
        beschreibungsViewController.getPublikationsDatum(publikationen, PublikationsTyp.ERSTPUBLIKATION));
    assertEquals(publikation_2.getDatumDerVeroeffentlichung(),
        beschreibungsViewController.getPublikationsDatum(publikationen, PublikationsTyp.PUBLIKATION_HSP));
  }
}
