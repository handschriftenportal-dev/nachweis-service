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

import static de.staatsbibliothek.berlin.hsp.nachweis.application.model.DatenDokumentTyp.BESCHREIBUNG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.DatenHochladenModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DatenDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.XMLFormate;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportUploadDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgangBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgangException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 29.05.2020.
 * @version 1.0
 */
class DatenHochladenControllerTest {

  private ImportVorgangBoundary importVorgangBoundary;
  private ImportBoundary importBoundary;
  private BearbeiterBoundary bearbeiterBoundary;
  private DatenHochladenModel datenHochladenModel;

  private ImportRechte importRechte;

  @BeforeEach
  void init() {
    importVorgangBoundary = mock(ImportVorgangBoundary.class);
    bearbeiterBoundary = mock(BearbeiterBoundary.class);
    importBoundary = mock(ImportBoundary.class);
    datenHochladenModel = new DatenHochladenModel();

    OAUTH2Config oauth2Config = Mockito.mock(OAUTH2Config.class);
    when(oauth2Config.isPermissionCheckEnabled()).thenReturn(Boolean.FALSE);
    importRechte = new ImportRechte(null, oauth2Config);

    when(importBoundary.isAutomatischenUebernahmeAktiv()).thenReturn(false);
  }

  @Test
  void testConstruction() {

    DatenHochladenController hochladenController = new DatenHochladenController(
        importVorgangBoundary, datenHochladenModel, importRechte, bearbeiterBoundary, importBoundary);

    assertNotNull(hochladenController.getImportVorgangBoundary());
  }

  @ParameterizedTest
  @CsvSource(value = {"beschreibung,BESCHREIBUNG", "KATALOG,KATALOG",
      "ORT,ORT", "KOERPERSCHAFT,KOERPERSCHAFT", "BEZIEHUNG,BEZIEHUNG",
      "DIGITALISAT,DIGITALISAT", "PERSON,PERSON", "sPraCHE,SPRACHE", "KOD,NULL", ",NULL"},
      nullValues = {"NULL"})
  void testSetup(String dokumentTyp, DatenDokumentTyp selectedDokumentTyp) {
    DatenHochladenController hochladenController = new DatenHochladenController(
        importVorgangBoundary, datenHochladenModel, importRechte, bearbeiterBoundary, importBoundary);
    hochladenController.setDokumentTyp(dokumentTyp);
    hochladenController.setup();

    assertEquals(selectedDokumentTyp, hochladenController.getSelectedDokumentTyp());
  }

  @Test
  void testhandleFileUpload() throws ImportVorgangException {

    DatenHochladenController hochladenController = new DatenHochladenController(
        importVorgangBoundary, datenHochladenModel, importRechte, bearbeiterBoundary, importBoundary);

    FileUploadEvent event = mock(FileUploadEvent.class);
    UploadedFile uploadedFile = mock(UploadedFile.class);

    when(event.getFile()).thenReturn(uploadedFile);
    when(uploadedFile.getFileName()).thenReturn("test.xml");
    when(uploadedFile.getContent()).thenReturn("test.xml".getBytes(StandardCharsets.UTF_8));
    when(uploadedFile.getContentType()).thenReturn("text/xml");

    hochladenController.setSelectedDokumentTyp(DatenDokumentTyp.KOERPERSCHAFT);
    hochladenController.handleFileUpload(event);

    assertNotNull(hochladenController.getDatei());

    verify(importVorgangBoundary, times(1))
        .sendDateiUploadMessageToImportService(
            new ImportUploadDatei("test.xml", "test.xml".getBytes(StandardCharsets.UTF_8),
                "text/xml"),
            ActivityStreamsDokumentTyp.KOERPERSCHAFT,
            bearbeiterBoundary.getUnbekannterBearbeiter());

    assertTrue(hochladenController.isUploadSuccessful());

    assertNull(hochladenController.getSelectedDokumentTyp());

    assertNull(hochladenController.getSelectedXMLFormat());
  }

  @Test
  void testhandleFileUploadWithBeschreibungAndTags() throws ImportVorgangException {

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(
        new Bearbeiter("1", "Unbekannter Tester"));

    DatenHochladenController hochladenController = new DatenHochladenController(
        importVorgangBoundary, datenHochladenModel, importRechte, bearbeiterBoundary, importBoundary);

    FileUploadEvent event = mock(FileUploadEvent.class);
    UploadedFile uploadedFile = mock(UploadedFile.class);

    when(event.getFile()).thenReturn(uploadedFile);

    hochladenController.setSelectedDokumentTyp(DatenDokumentTyp.BESCHREIBUNG);
    hochladenController.handleFileUpload(event);

    assertNotNull(hochladenController.getDatei());

    verify(importVorgangBoundary, times(1))
        .sendDateiUploadMessageToImportService(
            any(ImportUploadDatei.class),
            any(ActivityStreamsDokumentTyp.class), any(Bearbeiter.class), any(List.class));

    assertTrue(hochladenController.isUploadSuccessful());

    assertNull(hochladenController.getSelectedDokumentTyp());

    assertNull(hochladenController.getSelectedXMLFormat());
  }

  @Test
  void testgetDokumentTypen() {

    DatenHochladenController hochladenController = new DatenHochladenController(
        importVorgangBoundary, datenHochladenModel, importRechte, bearbeiterBoundary, importBoundary);

    hochladenController.initDokumentTypen();

    assertEquals(8, hochladenController.getDokumentTypen().size());
  }

  @Test
  void testhandleViewRequest() {

    DatenHochladenController hochladenController = new DatenHochladenController(
        importVorgangBoundary, datenHochladenModel, importRechte, bearbeiterBoundary, importBoundary);
    hochladenController.setUploadSuccessful(true);
    hochladenController.handleViewRequest();

    assertFalse(hochladenController.isUploadSuccessful());
  }

  @Test
  void testhandleValueChanged() {
    DatenHochladenController hochladenController = new DatenHochladenController(
        importVorgangBoundary, datenHochladenModel, importRechte, bearbeiterBoundary, importBoundary);
    hochladenController.setUploadSuccessful(true);
    hochladenController.handleValueChanged(null);
    assertFalse(hochladenController.isUploadSuccessful());
  }

  @ParameterizedTest
  @EnumSource(value = DatenDokumentTyp.class, names = {"ORT", "KOERPERSCHAFT", "BESCHREIBUNG",
      "BEZIEHUNG", "DIGITALISAT", "PERSON", "SPRACHE", "KATALOG"})
  void testonChangeDokumententype(DatenDokumentTyp typ) {
    DatenHochladenController hochladenController = new DatenHochladenController(
        importVorgangBoundary, datenHochladenModel, importRechte, bearbeiterBoundary, importBoundary);
    hochladenController.setUploadSuccessful(true);
    hochladenController.onChangeDokumententype();
    assertFalse(hochladenController.isUploadSuccessful());

    hochladenController.setSelectedDokumentTyp(typ);
    assertEquals(XMLFormate.TEI_ALL, hochladenController.getSelectedXMLFormat());
  }

  @Test
  void testInitDokumentTypen() {
    DatenHochladenController hochladenController = new DatenHochladenController(
        importVorgangBoundary, datenHochladenModel, importRechte, bearbeiterBoundary, importBoundary);

    hochladenController.initDokumentTypen();

    assertNotNull(hochladenController.getDokumentTypen());
    assertEquals(8, hochladenController.getDokumentTypen().size());
  }

  @Test
  void testIsDateiauswahlDisabled() {
    ImportRechte importRechteMock = mock(ImportRechte.class);
    when(importRechteMock.kannDateiupload()).thenReturn(true);
    when(importRechteMock.kannBeschreibungAutomatischImportieren()).thenReturn(true);

    DatenHochladenController hochladenController = new DatenHochladenController(
        importVorgangBoundary, datenHochladenModel, importRechteMock, bearbeiterBoundary, importBoundary);

    assertTrue(hochladenController.isDateiauswahlDisabled());

    hochladenController.setSelectedDokumentTyp(BESCHREIBUNG);
    hochladenController.setSelectedXMLFormat(XMLFormate.TEI_ALL);

    assertFalse(hochladenController.isDateiauswahlDisabled());

    when(importBoundary.isAutomatischenUebernahmeAktiv()).thenReturn(true);
    when(importRechteMock.kannBeschreibungAutomatischImportieren()).thenReturn(false);

    assertTrue(hochladenController.isDateiauswahlDisabled());
  }

}
