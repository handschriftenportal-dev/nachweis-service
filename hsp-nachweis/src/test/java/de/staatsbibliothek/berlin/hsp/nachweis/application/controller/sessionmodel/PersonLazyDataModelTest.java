package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.person.Person;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.person.PersonFilter;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.person.PersonGraphQLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.person.PersonPageResult;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonLazyDataModelTest {

  @Mock
  PersonGraphQLBoundary boundary;

  private PersonLazyDataModel model;

  @BeforeEach
  void setUp() {
    model = new PersonLazyDataModel(boundary);
  }

  @Test
  void loadMapped_calculatesPageFromFirstEntryAndSize_passesFilterAndReturnsTotalCount() {
    Person p1 = Person.builder().id("ID-1").build();
    Person p2 = Person.builder().id("ID-2").build();
    List<Person> items = List.of(p1, p2);

    when(boundary.findPersons(anyInt(), anyInt(), any(PersonFilter.class)))
        .thenReturn(new PersonPageResult(items, 999, 3, 100, true));

    Map<String, String> filter = Map.of(
        "id", "P-123", "preferredName", "Augustinus",
        "variantName", "Beatus", "gndIdentifier", "GND-1");

    List<Person> result = model.loadMapped(20, 10, Map.of(), filter);

    assertEquals(items, result);
    assertEquals(999, model.count(Collections.emptyMap()));

    ArgumentCaptor<PersonFilter> captor = ArgumentCaptor.forClass(PersonFilter.class);
    verify(boundary).findPersons(eq(3), eq(10), captor.capture());

    PersonFilter sent = captor.getValue();
    assertEquals("P-123", sent.id());
    assertEquals("Augustinus", sent.preferredName());
    assertEquals("Beatus", sent.variantName());
    assertEquals("GND-1", sent.gndIdentifier());
  }

  @Test
  void loadMapped_defaultsToSize10_whenPageSizeIsZero() {
    when(boundary.findPersons(anyInt(), anyInt(), any(PersonFilter.class)))
        .thenReturn(new PersonPageResult(List.of(), 0, 1, 1, false));

    assertEquals(List.of(), model.loadMapped(0, 0, Map.of(), Map.of()));
    verify(boundary).findPersons(eq(1), eq(10), any(PersonFilter.class));
  }

  @Test
  void loadMapped_defaultsToSize10_whenPageSizeIsNegative() {
    when(boundary.findPersons(anyInt(), anyInt(), any(PersonFilter.class)))
        .thenReturn(new PersonPageResult(List.of(), 0, 1, 1, false));

    assertEquals(List.of(), model.loadMapped(0, -5, Map.of(), Map.of()));
    verify(boundary).findPersons(eq(1), eq(10), any(PersonFilter.class));
  }

  @Test
  void loadMapped_trimsBlanksToNull_andTrimsWhitespace() {
    when(boundary.findPersons(anyInt(), anyInt(), any(PersonFilter.class)))
        .thenReturn(new PersonPageResult(List.of(), 0, 1, 1, false));

    model.loadMapped(0, 10, Map.of(), Map.of(
        "id", "   ", "preferredName", "", "variantName", " Beatus ", "gndIdentifier", "   "));

    ArgumentCaptor<PersonFilter> captor = ArgumentCaptor.forClass(PersonFilter.class);
    verify(boundary).findPersons(eq(1), eq(10), captor.capture());

    PersonFilter sent = captor.getValue();
    assertNull(sent.id());
    assertNull(sent.preferredName());
    assertEquals("Beatus", sent.variantName());
    assertNull(sent.gndIdentifier());
  }

  @Test
  void loadMapped_filtersAbsentFromMap_becomeNull() {
    when(boundary.findPersons(anyInt(), anyInt(), any(PersonFilter.class)))
        .thenReturn(new PersonPageResult(List.of(), 0, 1, 1, false));

    model.loadMapped(0, 10, Map.of(), Map.of());

    ArgumentCaptor<PersonFilter> captor = ArgumentCaptor.forClass(PersonFilter.class);
    verify(boundary).findPersons(anyInt(), anyInt(), captor.capture());

    PersonFilter sent = captor.getValue();
    assertNull(sent.id());
    assertNull(sent.preferredName());
    assertNull(sent.variantName());
    assertNull(sent.gndIdentifier());
  }

  @Test
  void loadMapped_returnsEmptyAndResetsCount_whenBoundaryThrows() {
    when(boundary.findPersons(anyInt(), anyInt(), any(PersonFilter.class)))
        .thenThrow(new RuntimeException("connection refused"));

    assertEquals(List.of(), model.loadMapped(0, 10, Map.of(), Map.of()));
    assertEquals(0, model.count(Collections.emptyMap()));
  }

  @Test
  void count_returnsZero_beforeAnyLoad() {
    assertEquals(0, model.count(Collections.emptyMap()));
  }

  @Test
  void loadMapped_errorResetsCountFromPreviousSuccess() {
    when(boundary.findPersons(anyInt(), anyInt(), any(PersonFilter.class)))
        .thenReturn(
            new PersonPageResult(List.of(Person.builder().id("P-1").build()), 50, 1, 5, true))
        .thenThrow(new RuntimeException("timeout"));

    model.loadMapped(0, 10, Map.of(), Map.of());
    assertEquals(50, model.count(Collections.emptyMap()));

    model.loadMapped(0, 10, Map.of(), Map.of());
    assertEquals(0, model.count(Collections.emptyMap()));
  }
}
