package de.staatsbibliothek.berlin.hsp.nachweis.domain.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.person.PersonGraphQLClient;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.person.PersonPageResult;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

  @Mock
  PersonGraphQLClient client;

  PersonService service;

  static Stream<Arguments> singleFieldFilters() {
    return Stream.of(
        Arguments.of("id", PersonFilter.builder().id("NORM-123").build()),
        Arguments.of("name", PersonFilter.builder().name("Augustinus").build()),
        Arguments.of("preferredName", PersonFilter.builder().preferredName("Augustinus").build()),
        Arguments.of("variantName", PersonFilter.builder().variantName("Aurelius").build()),
        Arguments.of("gndIdentifier", PersonFilter.builder().gndIdentifier("118505114").build())
    );
  }

  // --- filter normalization ---

  @BeforeEach
  void setUp() {
    service = new PersonService(client);
  }

  @Test
  void findPersons_passesNullFilter_whenFilterIsNull() {
    when(client.findPersons(eq(10), eq(1), isNull()))
        .thenReturn(new PersonPageResult(List.of(), 0, 1, 0, false));

    service.findPersons(1, 10, null);

    verify(client).findPersons(10, 1, null);
  }

  @Test
  void findPersons_passesNullFilter_whenAllFieldsEmpty() {
    when(client.findPersons(eq(10), eq(1), isNull()))
        .thenReturn(new PersonPageResult(List.of(), 0, 1, 0, false));

    service.findPersons(1, 10, PersonFilter.builder().build());

    verify(client).findPersons(10, 1, null);
  }

  @Test
  void findPersons_passesNullFilter_whenAllFieldsBlank() {
    PersonFilter blank = PersonFilter.builder()
        .id("   ")
        .name("\t")
        .preferredName("\n\t")
        .variantName(" ")
        .gndIdentifier(" ")
        .build();

    when(client.findPersons(eq(10), eq(1), isNull()))
        .thenReturn(new PersonPageResult(List.of(), 0, 1, 0, false));

    service.findPersons(1, 10, blank);

    verify(client).findPersons(10, 1, null);
  }

  @ParameterizedTest(name = "passes filter when only {0} is set")
  @MethodSource("singleFieldFilters")
  void findPersons_passesFilter_whenAnySingleFieldPresent(String fieldName, PersonFilter filter) {
    when(client.findPersons(eq(10), eq(1), eq(filter)))
        .thenReturn(new PersonPageResult(List.of(), 5, 1, 1, false));

    service.findPersons(1, 10, filter);

    verify(client).findPersons(10, 1, filter);
  }

  @Test
  void findPersons_passesFilter_whenMultipleFieldsSet() {
    PersonFilter mixed = PersonFilter.builder()
        .name("Augustinus")
        .gndIdentifier("118505114")
        .build();

    when(client.findPersons(eq(10), eq(1), eq(mixed)))
        .thenReturn(new PersonPageResult(List.of(), 5, 1, 1, false));

    service.findPersons(1, 10, mixed);

    verify(client).findPersons(10, 1, mixed);
  }

  @Test
  void findPersons_swapsPageAndPageSizeForClient() {
    when(client.findPersons(eq(25), eq(3), isNull()))
        .thenReturn(new PersonPageResult(List.of(), 0, 3, 0, false));

    service.findPersons(3, 25, null);

    verify(client).findPersons(25, 3, null);
  }

  // --- defensive null handling ---

  @Test
  void findPersons_returnsEmptyResult_whenClientThrows() {
    when(client.findPersons(10, 1, null)).thenThrow(new RuntimeException("Fehler"));

    PersonPageResult result = service.findPersons(1, 10, null);

    assertNotNull(result);
    assertTrue(result.getItems().isEmpty());
    assertEquals(0, result.getTotalCount());
  }
}
