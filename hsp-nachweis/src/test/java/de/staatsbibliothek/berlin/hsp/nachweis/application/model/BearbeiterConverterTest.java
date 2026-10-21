package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import org.junit.jupiter.api.Test;

public class BearbeiterConverterTest {

    @Test
    void testGetAsObject() {

        String bearbeiterString = "112233"
                + BearbeiterConverter.SEPARATOR + "112233"
                + BearbeiterConverter.SEPARATOR + "vorname"
                + BearbeiterConverter.SEPARATOR + "nachname";

        BearbeiterConverter converter = new BearbeiterConverter();

        Bearbeiter bearbeiter = converter.getAsObject(null, null,
                bearbeiterString);

        assertNotNull(bearbeiter);

        assertEquals("112233", bearbeiter.getId());
        assertEquals("112233", bearbeiter.getBearbeitername());
        assertEquals("vorname", bearbeiter.getVorname());
        assertEquals("nachname", bearbeiter.getNachname());

    }

    @Test
    void testGetAsString() {

        BearbeiterConverter converter = new BearbeiterConverter();

        Bearbeiter bearbeiter = new Bearbeiter.BearbeiterBuilder()
                .withBearbeitername("112233")
                .withVorname("vorname").withNachname("nachname")
                .build();

        String bearbeiterString = "112233"
                + BearbeiterConverter.SEPARATOR + "112233"
                + BearbeiterConverter.SEPARATOR + "vorname"
                + BearbeiterConverter.SEPARATOR + "nachname";

        assertEquals(bearbeiterString, converter.getAsString(null, null, bearbeiter));

    }
    @Test
    void testGetAsStringWithVorname() {
        BearbeiterConverter converter = new BearbeiterConverter();
        Bearbeiter bearbeiter = new Bearbeiter.BearbeiterBuilder()
                .withVorname("vorname")
                .build();
        String bearbeiterString =  null
                + BearbeiterConverter.SEPARATOR + null
                + BearbeiterConverter.SEPARATOR + "vorname"
                + BearbeiterConverter.SEPARATOR + null;

        assertEquals(bearbeiterString, converter.getAsString(null, null, bearbeiter));

    }
}
