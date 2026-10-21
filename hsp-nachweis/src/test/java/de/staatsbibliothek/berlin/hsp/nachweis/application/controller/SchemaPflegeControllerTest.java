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
import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDateiBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

  Bearbeiter bearbeiter = new Bearbeiter(UNBEKANNTER_BEARBEITER_ID, UNBEKANNTER_BEARBEITER_ID);
  String filename = "MXML-to-TEI-P5.xsl";
  String dateiId = UUID.nameUUIDFromBytes(filename.getBytes(StandardCharsets.UTF_8)).toString();
  String version = "1.2.3";

  @BeforeEach
  void init() {
    schemaPflegeDateienModel = mock(SchemaPflegeDateienModel.class);
    bearbeiterBoundary = mock(BearbeiterBoundary.class);
    schemaResourceDateiBoundary = mock(SchemaPflegeDateiBoundary.class);
    OAUTH2Config oauth2Config = Mockito.mock(OAUTH2Config.class);
    when(oauth2Config.isPermissionCheckEnabled()).thenReturn(Boolean.FALSE);
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
  }

  @Test
  void testIsAuswahlButtonDisabled() {
    SchemaPflegeController schemaPflegeController = new SchemaPflegeController(
        schemaPflegeDateienModel, bearbeiterBoundary, schemaResourceDateiBoundary);

    assertTrue(schemaPflegeController.isAuswahlButtonDisabled());

    schemaPflegeController.setDateiId(dateiId);
    assertTrue(schemaPflegeController.isAuswahlButtonDisabled());

    schemaPflegeController.setVersion(version);
    assertFalse(schemaPflegeController.isAuswahlButtonDisabled());
  }

  @Test
  void testHandleFileUpload() throws Exception {
    SchemaPflegeController schemaPflegeController = new SchemaPflegeController(
        schemaPflegeDateienModel, bearbeiterBoundary, schemaResourceDateiBoundary);

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

}
