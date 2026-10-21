package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 10.11.2021
 */
public class BearbeiterTest {

  @Test
  void testGetName() {
    assertEquals("unbekannt", Bearbeiter.newBuilder().build().getName());
    assertEquals("Vorname Nachname",
        Bearbeiter.newBuilder().withVorname("Vorname").withNachname("Nachname").build().getName());
    assertEquals("Vorname", Bearbeiter.newBuilder().withVorname("Vorname").build().getName());
    assertEquals("Nachname", Bearbeiter.newBuilder().withNachname("Nachname").build().getName());
    assertEquals("bearbeitername",
        Bearbeiter.newBuilder().withBearbeitername("bearbeitername").build().getName());
  }

  @Test
  void testBuilder() {
    NormdatenReferenz person = new NormdatenReferenz.NormdatenReferenzBuilder().withName(
            "Martin Luther")
        .withGndID("118575449").build();
    NormdatenReferenz institution = new NormdatenReferenz.NormdatenReferenzBuilder().withName(
        "Lutherhaus Wittenberg").withGndID("10055695-4").build();

    Bearbeiter bearbeiter = Bearbeiter.newBuilder()
        .withBearbeitername("b-ml123")
        .withVorname("Martin")
        .withNachname("Luther")
        .withEmail("martin@luther.de")
        .withRolle("Redakteur")
        .withInstitution(institution)
        .withPerson(person)
        .build();

    assertEquals("b-ml123", bearbeiter.getId());
    assertEquals("b-ml123", bearbeiter.getBearbeitername());
    assertEquals("Martin", bearbeiter.getVorname());
    assertEquals("Luther", bearbeiter.getNachname());
    assertEquals("martin@luther.de", bearbeiter.getEmail());
    assertEquals("Redakteur", bearbeiter.getRolle());
    assertEquals(person, bearbeiter.getPerson());
    assertEquals(institution, bearbeiter.getInstitution());
  }

  @Test
  void testFullName() {
    assertEquals("unbekannt", Bearbeiter.newBuilder().build().getFullName());
    assertEquals("Nachname, Vorname",
        Bearbeiter.newBuilder().withVorname("Vorname").withNachname("Nachname").build()
            .getFullName());
  }
}
