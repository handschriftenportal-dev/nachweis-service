package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 29.06.2022
 */
public class NormdatenReferenzConverterTest {

  @Test
  void testGetAsObject() {

    String normdatenReferenzString = "123"
        + NormdatenReferenzConverter.SEPARATOR + "Konrad"
        + NormdatenReferenzConverter.SEPARATOR + "GNDID"
        + NormdatenReferenzConverter.SEPARATOR + GNDEntityFact.PERSON_TYPE_NAME
        + NormdatenReferenzConverter.SEPARATOR + "URI"
        + NormdatenReferenzConverter.SEPARATOR + "LABEL";

    NormdatenReferenzConverter converter = new NormdatenReferenzConverter();

    NormdatenReferenz normdatenReferenz = converter.getAsObject(null, null,
        normdatenReferenzString);

    assertNotNull(normdatenReferenz);

    assertEquals("123", normdatenReferenz.getId());
    assertEquals("Konrad", normdatenReferenz.getName());
    assertEquals("GNDID", normdatenReferenz.getGndID());
    assertEquals(GNDEntityFact.PERSON_TYPE_NAME, normdatenReferenz.getTypeName());
    assertEquals("LABEL", normdatenReferenz.getVariantName("de"));

    String normdatenReferenzStringNoGnd = "123"
        + NormdatenReferenzConverter.SEPARATOR + "Konrad"
        + NormdatenReferenzConverter.SEPARATOR + "NOTSET"
        + NormdatenReferenzConverter.SEPARATOR + GNDEntityFact.PERSON_TYPE_NAME
        + NormdatenReferenzConverter.SEPARATOR + "URI"
        + NormdatenReferenzConverter.SEPARATOR + "LABEL";

    normdatenReferenz = converter.getAsObject(null, null, normdatenReferenzStringNoGnd);

    assertNotNull(normdatenReferenz);

    assertEquals("123", normdatenReferenz.getId());
    assertEquals("Konrad", normdatenReferenz.getName());
    assertNull(normdatenReferenz.getGndID());
    assertEquals(GNDEntityFact.PERSON_TYPE_NAME, normdatenReferenz.getTypeName());
  }

  @Test
  void testGetAsString() {

    NormdatenReferenzConverter converter = new NormdatenReferenzConverter();

    NormdatenReferenz normdatenReferenz = new NormdatenReferenz("123", "Konrad", "GNDID",
        GNDEntityFact.PERSON_TYPE_NAME);

    String normdatenReferenzString = "123"
        + NormdatenReferenzConverter.SEPARATOR + "Konrad"
        + NormdatenReferenzConverter.SEPARATOR + "GNDID"
        + NormdatenReferenzConverter.SEPARATOR + GNDEntityFact.PERSON_TYPE_NAME
        + NormdatenReferenzConverter.SEPARATOR + "NOTSET"
        + NormdatenReferenzConverter.SEPARATOR + "NOTSET";

    assertEquals(normdatenReferenzString, converter.getAsString(null, null, normdatenReferenz));

    normdatenReferenz = new NormdatenReferenz("123", "Konrad", null,
        GNDEntityFact.PERSON_TYPE_NAME);

    String normdatenReferenzStringNoGnd = "123"
        + NormdatenReferenzConverter.SEPARATOR + "Konrad"
        + NormdatenReferenzConverter.SEPARATOR + "NOTSET"
        + NormdatenReferenzConverter.SEPARATOR + GNDEntityFact.PERSON_TYPE_NAME
        + NormdatenReferenzConverter.SEPARATOR + "NOTSET"
        + NormdatenReferenzConverter.SEPARATOR + "NOTSET";

    assertEquals(normdatenReferenzStringNoGnd,
        converter.getAsString(null, null, normdatenReferenz));
  }
}
