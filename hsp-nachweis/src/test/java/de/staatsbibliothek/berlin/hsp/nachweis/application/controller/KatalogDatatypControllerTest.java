package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.KatalogModel;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.02.2022.
 * @version 1.0
 */
public class KatalogDatatypControllerTest {

  private KatalogModel katalogModel = Mockito.mock(KatalogModel.class);

  private KatalogDatatypController katalogDatatypController = new KatalogDatatypController(
      katalogModel);

  @Test
  void testGetAllKataloge() {
    assertNotNull(katalogDatatypController);
  }
}
