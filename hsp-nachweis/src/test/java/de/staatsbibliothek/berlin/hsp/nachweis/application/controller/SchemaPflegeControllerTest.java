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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.SchemaPflegeDateienModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDateiBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.faces.event.ValueChangeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.primefaces.component.toggleswitch.ToggleSwitch;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 31.03.2022
 */

public class SchemaPflegeControllerTest {

  SchemaPflegeDateienModel schemaPflegeDateienModel;
  BearbeiterBoundary bearbeiterBoundary;
  SchemaPflegeDateiBoundary schemaResourceDateiBoundary;
  ImportBoundary importBoundary;

  Bearbeiter bearbeiter = new Bearbeiter(UNBEKANNTER_BEARBEITER_ID, UNBEKANNTER_BEARBEITER_ID);
  String filename = "MXML-to-TEI-P5.xsl";
  String dateiId = UUID.nameUUIDFromBytes(filename.getBytes(StandardCharsets.UTF_8)).toString();
  String version = "1.2.3";

  @BeforeEach
  void init() {
    schemaPflegeDateienModel = mock(SchemaPflegeDateienModel.class);
    bearbeiterBoundary = mock(BearbeiterBoundary.class);
    schemaResourceDateiBoundary = mock(SchemaPflegeDateiBoundary.class);
    importBoundary = mock(ImportBoundary.class);
    OAUTH2Config oauth2Config = Mockito.mock(OAUTH2Config.class);
    when(oauth2Config.isPermissionCheckEnabled()).thenReturn(Boolean.FALSE);
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
  }

  @Test
  void testIsAuswahlButtonDisabled() {
    SchemaPflegeController schemaPflegeController = new SchemaPflegeController(
        schemaPflegeDateienModel, bearbeiterBoundary, schemaResourceDateiBoundary, importBoundary);

    assertTrue(schemaPflegeController.isAuswahlButtonDisabled());

    schemaPflegeController.setDateiId(dateiId);
    assertTrue(schemaPflegeController.isAuswahlButtonDisabled());

    schemaPflegeController.setVersion(version);
    assertFalse(schemaPflegeController.isAuswahlButtonDisabled());
  }

  @Test
  void testHandleFileUpload() throws Exception {
    SchemaPflegeController schemaPflegeController = new SchemaPflegeController(
        schemaPflegeDateienModel, bearbeiterBoundary, schemaResourceDateiBoundary, importBoundary);

    FileUploadEvent event = mock(FileUploadEvent.class);
    UploadedFile uploadedFile = mock(UploadedFile.class);

    InputStream datei = new ByteArrayInputStream(filename.getBytes(StandardCharsets.UTF_8));

    when(event.getFile()).thenReturn(uploadedFile);
    when(uploadedFile.getFileName()).thenReturn(filename);
    when(uploadedFile.getInputStream()).thenReturn(datei);

    schemaPflegeController.setDateiId(dateiId);
    schemaPflegeController.setVersion(version);
    schemaPflegeController.handleFileUpload(event);

    verify(schemaResourceDateiBoundary, times(1))
        .upload(dateiId, version, UNBEKANNTER_BEARBEITER_ID, datei);

    assertNull(schemaPflegeController.getDateiId());
    assertNull(schemaPflegeController.getVersion());
  }

  @Test
  void testChangeAutomatischenUebernahme() {
    SchemaPflegeController schemaPflegeController = new SchemaPflegeController(
        schemaPflegeDateienModel, bearbeiterBoundary, schemaResourceDateiBoundary, importBoundary);

    when(importBoundary.isAutomatischenUebernahmeAktiv()).thenReturn(false);
    ValueChangeEvent event = new ValueChangeEvent(new ToggleSwitch(), Boolean.FALSE, Boolean.TRUE);

    schemaPflegeController.changeAutomatischenUebernahme(event);

    verify(importBoundary, times(1)).setAutomatischenUebernahmeAktiv(true);
  }

}
