package de.staatsbibliothek.berlin.hsp.nachweis.domain.initium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.initium.InitiumGraphQLClient;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.initium.InitiumPageResult;
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
class InitiumServiceTest {

  @Mock
  InitiumGraphQLClient client;

  InitiumService service;

  static Stream<Arguments> singleFieldFilters() {
    return Stream.of(
        Arguments.of("id", InitiumFilter.builder().id("HSP-123").build()),
        Arguments.of("text", InitiumFilter.builder().text("some text").build()),
        Arguments.of("language", InitiumFilter.builder().language("Lateinisch").build())
    );
  }

  @BeforeEach
  void setUp() {
    service = new InitiumService(client);
  }

  // --- filter normalization ---

  @Test
  void findInitien_passesNullFilter_whenFilterIsNull() {
    when(client.findInitia(eq(10), eq(1), isNull())).thenReturn(emptyResult());

    service.findInitien(1, 10, null);

    verify(client).findInitia(10, 1, null);
  }

  @Test
  void findInitien_passesNullFilter_whenAllFieldsEmpty() {
    when(client.findInitia(eq(10), eq(1), isNull())).thenReturn(emptyResult());

    service.findInitien(1, 10, InitiumFilter.builder().build());

    verify(client).findInitia(10, 1, null);
  }

  @Test
  void findInitien_passesNullFilter_whenAllFieldsBlank() {
    InitiumFilter blank = InitiumFilter.builder()
        .id("   ")
        .text("\n\t")
        .language(" ")
        .build();

    when(client.findInitia(eq(10), eq(1), isNull())).thenReturn(emptyResult());

    service.findInitien(1, 10, blank);

    verify(client).findInitia(10, 1, null);
  }

  @ParameterizedTest(name = "passes filter when only {0} is set")
  @MethodSource("singleFieldFilters")
  void findInitien_passesFilter_whenAnySingleFieldPresent(String fieldName, InitiumFilter filter) {
    when(client.findInitia(eq(10), eq(1), eq(filter))).thenReturn(emptyResult());

    service.findInitien(1, 10, filter);

    verify(client).findInitia(10, 1, filter);
  }

  @Test
  void findInitien_passesFilter_whenMultipleFieldsSet() {
    InitiumFilter mixed = InitiumFilter.builder()
        .id("HSP-123")
        .text("some text")
        .build();

    when(client.findInitia(eq(10), eq(1), eq(mixed))).thenReturn(emptyResult());

    service.findInitien(1, 10, mixed);

    verify(client).findInitia(10, 1, mixed);
  }

  @Test
  void findInitien_swapsPageAndPageSizeForClient() {
    when(client.findInitia(eq(25), eq(3), isNull())).thenReturn(emptyResult());

    service.findInitien(3, 25, null);

    verify(client).findInitia(25, 3, null);
  }

  // --- defensive null handling ---

  @Test
  void findInitien_returnsEmptyResult_whenClientThrows() {
    when(client.findInitia(10, 1, null))
        .thenThrow(new RuntimeException("Fehler"));

    InitiumPageResult result = service.findInitien(1, 10, null);

    assertNotNull(result);
    assertNotNull(result.getItems());
    assertTrue(result.getItems().isEmpty());
    assertEquals(0, result.getTotalCount());
  }

  private InitiumPageResult emptyResult() {
    return new InitiumPageResult(List.of(), 0, 1, 0, false);
  }
}
