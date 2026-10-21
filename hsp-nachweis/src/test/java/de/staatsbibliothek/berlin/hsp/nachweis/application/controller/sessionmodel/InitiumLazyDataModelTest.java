package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.initium.Initium;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.initium.InitiumFilter;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.initium.InitiumGraphQLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.initium.InitiumPageResult;
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
class InitiumLazyDataModelTest {

  @Mock
  InitiumGraphQLBoundary boundary;

  private InitiumLazyDataModel model;

  @BeforeEach
  void setUp() {
    model = new InitiumLazyDataModel(boundary);
  }

  @Test
  void loadMapped_calculatesPageFromFirstEntryAndSize_passesFilterAndReturnsTotalCount() {
    List<Initium> items = List.of(
        Initium.builder().id("ID-1").build(),
        Initium.builder().id("ID-2").build());

    when(boundary.findInitien(anyInt(), anyInt(), any(InitiumFilter.class)))
        .thenReturn(new InitiumPageResult(items, 42, 3, 5, true));

    Map<String, String> filter = Map.of("id", "NORM-123", "text", "gloria", "language",
        "lateinisch");

    List<Initium> result = model.loadMapped(20, 10, Map.of(), filter);

    assertEquals(items, result);
    assertEquals(42, model.count(Collections.emptyMap()));

    ArgumentCaptor<InitiumFilter> captor = ArgumentCaptor.forClass(InitiumFilter.class);
    verify(boundary).findInitien(eq(3), eq(10), captor.capture());

    InitiumFilter sent = captor.getValue();
    assertEquals("NORM-123", sent.id());
    assertEquals("gloria", sent.text());
    assertEquals("lateinisch", sent.language());
  }

  @Test
  void loadMapped_defaultsToSize10_whenPageSizeIsZero() {
    when(boundary.findInitien(anyInt(), anyInt(), any(InitiumFilter.class)))
        .thenReturn(new InitiumPageResult(List.of(), 0, 1, 1, false));

    assertEquals(List.of(), model.loadMapped(0, 0, Map.of(), Map.of()));
    verify(boundary).findInitien(eq(1), eq(10), any(InitiumFilter.class));
  }

  @Test
  void loadMapped_defaultsToSize10_whenPageSizeIsNegative() {
    when(boundary.findInitien(anyInt(), anyInt(), any(InitiumFilter.class)))
        .thenReturn(new InitiumPageResult(List.of(), 0, 1, 1, false));

    assertEquals(List.of(), model.loadMapped(0, -5, Map.of(), Map.of()));
    verify(boundary).findInitien(eq(1), eq(10), any(InitiumFilter.class));
  }

  @Test
  void loadMapped_trimsBlanksToNull_andTrimsWhitespace() {
    when(boundary.findInitien(anyInt(), anyInt(), any(InitiumFilter.class)))
        .thenReturn(new InitiumPageResult(List.of(), 0, 1, 1, false));

    model.loadMapped(0, 10, Map.of(),
        Map.of("id", "   ", "text", "", "language", " Kirchenslawisch "));

    ArgumentCaptor<InitiumFilter> captor = ArgumentCaptor.forClass(InitiumFilter.class);
    verify(boundary).findInitien(eq(1), eq(10), captor.capture());

    InitiumFilter sent = captor.getValue();
    assertNull(sent.id());
    assertNull(sent.text());
    assertEquals("Kirchenslawisch", sent.language());
  }

  @Test
  void loadMapped_filtersAbsentFromMap_becomeNull() {
    when(boundary.findInitien(anyInt(), anyInt(), any(InitiumFilter.class)))
        .thenReturn(new InitiumPageResult(List.of(), 0, 1, 1, false));

    model.loadMapped(0, 10, Map.of(), Map.of());

    ArgumentCaptor<InitiumFilter> captor = ArgumentCaptor.forClass(InitiumFilter.class);
    verify(boundary).findInitien(anyInt(), anyInt(), captor.capture());

    InitiumFilter sent = captor.getValue();
    assertNull(sent.id());
    assertNull(sent.text());
    assertNull(sent.language());
  }

  @Test
  void loadMapped_returnsEmptyAndResetsCount_whenBoundaryThrows() {
    when(boundary.findInitien(anyInt(), anyInt(), any(InitiumFilter.class)))
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
    when(boundary.findInitien(anyInt(), anyInt(), any(InitiumFilter.class)))
        .thenReturn(new InitiumPageResult(
            List.of(Initium.builder().id("ID-1").build()), 50, 1, 5, true))
        .thenThrow(new RuntimeException("timeout"));

    model.loadMapped(0, 10, Map.of(), Map.of());
    assertEquals(50, model.count(Collections.emptyMap()));

    model.loadMapped(0, 10, Map.of(), Map.of());
    assertEquals(0, model.count(Collections.emptyMap()));
  }
}
