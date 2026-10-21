package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.DatensicherungBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.DatensicherungException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.entities.Datensicherung;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DokumentTyp;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class DatensicherungControllerTest {

  private final String xml;
  @Inject DatensicherungBoundary service;
  @Inject Event<String> event;
  @Inject BearbeiterBoundary bearbeiterService;

  public DatensicherungControllerTest() throws Exception {
    URL url =
        this.getClass()
            .getClassLoader()
            .getResource("oai-pmh/tei-HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469.xml");
    xml = Files.readString(Paths.get(Objects.requireNonNull(url).toURI()));
  }

  @Test
  void testSiteAvailability() {
    Event<String> eventMock = mock(Event.class);
    DatensicherungBoundary serviceMock =
        mock(DatensicherungBoundary.class, Mockito.RETURNS_DEEP_STUBS);
    BearbeiterBoundary bearbeiterServiceMock =
        mock(BearbeiterBoundary.class, Mockito.RETURNS_DEEP_STUBS);
    DatensicherungController controller =
        new DatensicherungController(serviceMock, bearbeiterServiceMock, eventMock);
    assertNotNull(controller);

    given().when().get("/datensicherung.xhtml").then().statusCode(200);
  }

  @Test
  @DisplayName("Datensicherung starten erfolgreich")
  void testDatensicherungStarten() {
    Event<String> eventMock = mock(Event.class);
    DatensicherungBoundary serviceMock =
        mock(DatensicherungBoundary.class, Mockito.RETURNS_DEEP_STUBS);
    BearbeiterBoundary bearbeiterServiceMock =
        mock(BearbeiterBoundary.class, Mockito.RETURNS_DEEP_STUBS);
    DatensicherungController controller =
        new DatensicherungController(serviceMock, bearbeiterServiceMock, eventMock);
    Datensicherung ds = Datensicherung.initialisieren(Set.of(DokumentTyp.KOD), "Benutzer1");
    doReturn(ds.getId()).when(serviceMock).createInitialDatensicherung(any(), any());
    CompletionStage<String> completionStageMock = mock(CompletionStage.class);
    Mockito.when(eventMock.fireAsync(any())).thenReturn(completionStageMock);

    controller.starteDatensicherung();

    verify(serviceMock, times(1)).createInitialDatensicherung(any(), any());
    verify(eventMock, times(1)).fireAsync(any());
  }

  @Test
  @DisplayName("DatensicherungHistorie sortieren")
  void testeDatensicherungSortieren() {
    Event<String> eventMock = mock(Event.class);
    DatensicherungBoundary serviceMock = mock(DatensicherungBoundary.class);
    BearbeiterBoundary bearbeiterServiceMock = mock(BearbeiterBoundary.class);
    DatensicherungController controller =
        new DatensicherungController(serviceMock, bearbeiterServiceMock, eventMock);

    Datensicherung d1 =
        Datensicherung.builder().id("1").startZeit(LocalDateTime.of(2024, 1, 5, 10, 0, 30)).build();
    Datensicherung d2 =
        Datensicherung.builder().id("2").startZeit(LocalDateTime.of(2024, 1, 5, 10, 0, 0)).build();
    Datensicherung d3 =
        Datensicherung.builder().id("3").startZeit(LocalDateTime.of(2024, 1, 7, 9, 0, 0)).build();

    List<Datensicherung> datensicherungen = List.of(d1, d2, d3);

    assertEquals("1", datensicherungen.get(0).getId());
    assertEquals("2", datensicherungen.get(1).getId());
    assertEquals("3", datensicherungen.get(2).getId());

    List<Datensicherung> datensicherungenSorted = controller.sortDatensicherungen(datensicherungen);

    assertEquals("3", datensicherungenSorted.get(0).getId());
    assertEquals("1", datensicherungenSorted.get(1).getId());
    assertEquals("2", datensicherungenSorted.get(2).getId());
  }

  @Test
  @DisplayName("Datensicherung anlegen der Datensicherung schlägt fehl")
  void testDatensicherungStartenThrow2() {
    DatensicherungBoundary spyService = spy(service);
    DatensicherungController controller =
        new DatensicherungController(spyService, bearbeiterService, event);

    controller.setDokumentAuswahl(null);
    controller.setDokumentOptionen(null);

    assertDoesNotThrow(controller::starteDatensicherung);
    assertThrows(
        DatensicherungException.class, () -> spyService.createInitialDatensicherung(null, null));
  }
}
