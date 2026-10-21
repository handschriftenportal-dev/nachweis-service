package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren.KulturObjektDokumentRegistrierenException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren.KulturObjektDokumentRegistrierenException.ERROR_TYPE;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.08.2020.
 * @version 1.0
 */
public class KulturObjektRegistrierenModelTest {

  KulturObjektDokumentBoundary kulturObjektDokumentBoundary = mock(
      KulturObjektDokumentBoundary.class);
  BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);

  NormdatenReferenzBoundary normdatenReferenzBoundary = mock(NormdatenReferenzBoundary.class);

  @Test
  void testConstruction() {

    KulturObjektRegistrierenModel model = new KulturObjektRegistrierenModel(
        kulturObjektDokumentBoundary, bearbeiterBoundary, normdatenReferenzBoundary);

    assertNotNull(model);

    assertNotNull(model.getKulturObjektDokumentBoundary());

    assertNotNull(model.getKoerperschaftViewModelMap());
  }

  @Test
  void testHandleFileUpload()
      throws IOException, KulturObjektDokumentRegistrierenException, SperreAlreadyExistException {
    FileUploadEvent event = mock(FileUploadEvent.class);
    UploadedFile file = mock(UploadedFile.class);
    byte[] content = new byte[1024];

    when(event.getFile()).thenReturn(file);
    when(file.getContent()).thenReturn(content);

    NormdatenReferenz koerperschaftViewModel = new NormdatenReferenz("1",
        "Staatsbibliothek zu Berlin",
        "234234-x", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);

    NormdatenReferenz orteViewModel = createNormDatenOrt();

    KulturObjektRegistrierenModel model = new KulturObjektRegistrierenModel(
        kulturObjektDokumentBoundary, bearbeiterBoundary, normdatenReferenzBoundary);

    model.setOrteViewModel(orteViewModel);
    model.setKoerperschaftViewModel(koerperschaftViewModel);

    when(
        normdatenReferenzBoundary.findOneByIdOrNameAndType(koerperschaftViewModel.getId(),
            GNDEntityFact.CORPORATE_BODY_TYPE_NAME))
        .thenReturn(Optional.of(koerperschaftViewModel));

    when(normdatenReferenzBoundary.findOneByIdOrNameAndType(orteViewModel.getId(),
        GNDEntityFact.PLACE_TYPE_NAME))
        .thenReturn(Optional.of(orteViewModel));

    model.handleFileUpload(event);

    verify(kulturObjektDokumentBoundary, times(1)).registrieren(
        bearbeiterBoundary.getUnbekannterBearbeiter(), orteViewModel, koerperschaftViewModel,
        content);

    model.setOrteViewModel(orteViewModel);
    model.setKoerperschaftViewModel(koerperschaftViewModel);

    when(kulturObjektDokumentBoundary.registrieren(bearbeiterBoundary.getUnbekannterBearbeiter(),
        orteViewModel, koerperschaftViewModel, content))
        .thenThrow(new KulturObjektDokumentRegistrierenException("Fehler",
            ERROR_TYPE.TECHNICAL, null));

    model.handleFileUpload(event);

    assertEquals("Fehler", model.getError().getMessage());

    assertEquals(ERROR_TYPE.TECHNICAL, model.getError().getErrorType());
  }

  @Test
  void testneuRegistrieren() throws IOException {
    NormdatenReferenz modelKoerperschaft = new NormdatenReferenz(
        TEIValues.UUID_PREFIX + UUID.randomUUID(),
        "Staatsbibliothek zu Berlin", "234234-x", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);

    NormdatenReferenz modelOrt = createNormDatenOrt();

    KulturObjektRegistrierenModel model = new KulturObjektRegistrierenModel(
        kulturObjektDokumentBoundary, bearbeiterBoundary, normdatenReferenzBoundary);
    model.setError(new KulturObjektDokumentRegistrierenException(
        "Kulturobjekt mit gültiger Signatur bereits im System: testSignatur"
            + modelKoerperschaft, Arrays.asList(),
        ERROR_TYPE.GUELTIGE_SIGNATUR_IN_SYSTEM));
    model.setRegistrierteKODs(Arrays.asList());
    model.setFailureKODs(Arrays.asList());
    model.setOrteViewModel(modelOrt);
    model.setKoerperschaftViewModel(modelKoerperschaft);

    model.neuRegistrieren();

    assertNull(model.getError());

    assertNull(model.getFailureKODs());

    assertNull(model.getRegistrierteKODs());

    assertNull(model.getOrteViewModel());

    assertNull(model.getKoerperschaftViewModel());
  }

  private NormdatenReferenz createNormDatenOrt() {
    return new NormdatenReferenzBuilder()
        .withId("20")
        .withGndID("4005728-8")
        .withName("Berlin")
        .build();
  }
}
