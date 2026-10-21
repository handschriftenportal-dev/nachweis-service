package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 05.06.23.
 * @version 1.0
 */
public class NavigationModelTest {

  @Test
  void testCreation() {

    NavigationModel navigationModel = new NavigationModel();
    assertNotNull(navigationModel);
    assertFalse(navigationModel.isDataTypeSubmenuOpen());
    assertFalse(navigationModel.isOtherSubmenuOpen());
    assertFalse(navigationModel.isImportSubmenuOpen());
    assertFalse(navigationModel.isRedactionSubmenuOpen());

  }

  @Test
  void testOnClickHandler() {
    NavigationModel navigationModel = new NavigationModel();
    navigationModel.onClickHandler("/kataloge.xhtml");

    assertTrue(navigationModel.isDataTypeSubmenuOpen());
    assertFalse(navigationModel.isOtherSubmenuOpen());
    assertFalse(navigationModel.isImportSubmenuOpen());
    assertFalse(navigationModel.isRedactionSubmenuOpen());

    navigationModel.onClickHandler("/dateiupload.xhtml");
    assertFalse(navigationModel.isDataTypeSubmenuOpen());
    assertFalse(navigationModel.isOtherSubmenuOpen());
    assertTrue(navigationModel.isImportSubmenuOpen());
    assertFalse(navigationModel.isRedactionSubmenuOpen());

    navigationModel.onClickHandler("/bestand.xhtml");
    assertFalse(navigationModel.isDataTypeSubmenuOpen());
    assertTrue(navigationModel.isOtherSubmenuOpen());
    assertFalse(navigationModel.isImportSubmenuOpen());
    assertFalse(navigationModel.isRedactionSubmenuOpen());

    navigationModel.onClickHandler("/datenuebernahme.xhtml");
    assertFalse(navigationModel.isDataTypeSubmenuOpen());
    assertFalse(navigationModel.isOtherSubmenuOpen());
    assertFalse(navigationModel.isImportSubmenuOpen());
    assertTrue(navigationModel.isRedactionSubmenuOpen());

  }
}
