package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLViewModel;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 10.08.2022
 */

public class PURLsModelTest {

  PURLRepository purlRepository;

  PURLViewModel kodViewModel;
  PURLViewModel beschreibungViewModel;

  PURLsModel purlsModel;

  @BeforeEach
  void onSetup() {
    kodViewModel = new PURLViewModel("HSP-123", DokumentObjektTyp.HSP_OBJECT,
        Set.of(createPURL("https://resolver.test/internal_123", "https://hsp.de/HSP-123", PURLTyp.INTERNAL)));

    beschreibungViewModel = new PURLViewModel("HSP-456", DokumentObjektTyp.HSP_DESCRIPTION,
        new LinkedHashSet<>(
            Arrays.asList(createPURL("https://resolver.test/urn_456", "https://hsp.de/HSP-456", PURLTyp.EXTERNAL),
                createPURL("https://resolver.test/doi_456", "https://hsp.de/HSP-456", PURLTyp.EXTERNAL))));

    purlRepository = Mockito.mock(PURLRepository.class);
    when(purlRepository.findAllAsViewModels())
        .thenReturn(List.of(kodViewModel, beschreibungViewModel));

    when(purlRepository.count()).thenReturn(2L);

    purlsModel = new PURLsModel(purlRepository);
  }

  @Test
  void testSetup() {
    purlsModel.setup();
    assertNotNull(purlsModel.getPurlViewModels());
    assertEquals(2, purlsModel.getPurlViewModels().size());
    assertNull(purlsModel.getFilteredPURLViewModels());
    assertNull(purlsModel.getSelectedPURLViewModels());

    Mockito.verify(purlRepository, Mockito.times(1)).findAllAsViewModels();
  }

  @Test
  void testLoadAllData() {
    assertNull(purlsModel.getPurlViewModels());

    purlsModel.loadAllData();

    assertNotNull(purlsModel.getPurlViewModels());
    assertEquals(2, purlsModel.getPurlViewModels().size());
    assertNull(purlsModel.getFilteredPURLViewModels());
    assertNull(purlsModel.getSelectedPURLViewModels());

    Mockito.verify(purlRepository, Mockito.times(1)).findAllAsViewModels();
  }

  @Test
  void testPurlsAsString() {
    assertEquals("", purlsModel.purlsAsString(null));
    assertEquals("", purlsModel.purlsAsString(Collections.emptySet()));
    assertEquals("https://resolver.test/internal_123", purlsModel.purlsAsString(kodViewModel.getInternalPURLs()));
    assertEquals("https://resolver.test/urn_456 https://resolver.test/doi_456",
        purlsModel.purlsAsString(beschreibungViewModel.getExternalPURLs()));
  }

  private PURL createPURL(String purl, String target, PURLTyp typ) {
    return new PURL(URI.create(purl), URI.create(target), typ);
  }
}
