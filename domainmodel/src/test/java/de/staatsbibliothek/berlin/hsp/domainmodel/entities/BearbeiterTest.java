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
    NormdatenReferenz person = new NormdatenReferenz("Martin Luther", "118575449");
    NormdatenReferenz institution = new NormdatenReferenz("Lutherhaus Wittenberg", "10055695-4");

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
}
