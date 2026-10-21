package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.person.Person;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.person.PersonFilter;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.person.PersonGraphQLClient;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.person.PersonPageResult;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.10.2020.
 * @version 1.0
 */
class AutocompleteNormdatenControllerTest {

  PersonGraphQLClient personGraphQLClient = Mockito.mock(PersonGraphQLClient.class);
  NormdatenReferenzBoundary normdatenReferenzBoundary = Mockito.mock(
      NormdatenReferenzBoundary.class);

  @Test
  void testAutocompleteKoerperschaft() {
    NormdatenReferenz stabi = new NormdatenReferenz("1", "Staatsbibliothek zu Berlin", "5036103-X",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);

    when(normdatenReferenzBoundary.findAllByIdOrNameAndType("Berlin",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME,
        false)).thenReturn(Set.of(stabi));

    AutocompleteNormdatenController controller = new AutocompleteNormdatenController(
        normdatenReferenzBoundary, personGraphQLClient);
    assertEquals(1, controller.autocompleteKoerperschaft("Berlin").size());
    assertEquals(0, controller.autocompleteKoerperschaft("Stadtbibliothek").size());
    verify(normdatenReferenzBoundary, times(2)).findAllByIdOrNameAndType(any(), any(),
        anyBoolean());
  }

  @Test
  void testAutocompletePerson() {
    PersonPageResult luther = new PersonPageResult(List.of(
        Person.builder()
            .id("1")
            .preferredName("Luther, Martin")
            .gndIdentifier("118575449")
            .build()),
        1,
        1,
        1,
        false);

    when(personGraphQLClient.findPersons(20, 1,
        PersonFilter.builder().name("Martin").build())).
        thenReturn(luther);
    when(personGraphQLClient.findPersons(20, 1,
        PersonFilter.builder().name("Achim").build())).
        thenReturn(new PersonPageResult(List.of(), 0, 0, 0, false));

    AutocompleteNormdatenController controller = new AutocompleteNormdatenController(
        normdatenReferenzBoundary, personGraphQLClient);
    assertEquals(1, controller.autocompletePerson("Martin").size());
    assertEquals(0, controller.autocompletePerson("Achim").size());
    verify(personGraphQLClient, times(2)).findPersons(eq(20), eq(1), any(PersonFilter.class));
  }

  @Test
  void testAutocompleteOrt() {
    NormdatenReferenz berlin = new NormdatenReferenz("1", "Berlin", "4005728-8",
        GNDEntityFact.PLACE_TYPE_NAME);

    when(normdatenReferenzBoundary.findAllByIdOrNameAndType("Berlin",
        NormdatenReferenz.ORT_TYPE_NAME,
        false)).thenReturn(Set.of(berlin));

    AutocompleteNormdatenController controller = new AutocompleteNormdatenController(
        normdatenReferenzBoundary, personGraphQLClient);
    assertEquals(1, controller.autocompleteOrt("Berlin").size());
    assertEquals(0, controller.autocompleteOrt("Bonn").size());
    verify(normdatenReferenzBoundary, times(2)).findAllByIdOrNameAndType(any(), any(),
        anyBoolean());
  }

}
