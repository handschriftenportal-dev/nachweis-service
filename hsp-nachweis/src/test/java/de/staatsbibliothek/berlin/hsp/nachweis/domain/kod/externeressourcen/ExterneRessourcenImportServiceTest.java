package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.DUPLICATE;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.KOD_NOT_FOUND;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.LINK_INVALID;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.RESOURCE_INVALID;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.ROW_INVALID;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.UUID_NOT_FOUND;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResult.Status.ERFOLGREICH;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResult.Status.FEHLER;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessource;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessourceTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExterneRessourcenImportServiceTest {

  private static final byte[] CSV_CONTENT = "csv".getBytes(StandardCharsets.UTF_8);
  private static final String KOD_ID = "HSP-123";
  private static final String URL = "https://handschriftencensus.de/123";

  @Mock
  ExterneRessourcenImportCSVParser csvParser;

  @Mock
  KulturObjektDokumentBoundary kulturObjektDokumentBoundary;

  ExterneRessourcenImportService service;

  @BeforeEach
  void beforeEach() {
    service = new ExterneRessourcenImportService(
        csvParser,
        new ExterneRessourcenImportEntryService(kulturObjektDokumentBoundary));
  }

  @Test
  void shouldImportByUuidWithoutFallback() throws Exception {
    ExterneRessourcenImportEntry entry = entry(KOD_ID, " HSC ", " " + URL + " ", 2);
    mockParsedRows(entry);
    when(kulturObjektDokumentBoundary.findById(KOD_ID)).thenReturn(Optional.of(kod()));

    List<ExterneRessourcenImportResult> results = service.importieren(CSV_CONTENT);

    assertEquals(1, results.size());
    ExterneRessourcenImportResult result = results.getFirst();
    assertEquals(KOD_ID, result.kodUuid());
    assertEquals("hsc", result.ressource());
    assertEquals(URL, result.link());
    assertEquals(ERFOLGREICH, result.status());
    assertTrue(result.errorMessageKey() == null);
    verify(kulturObjektDokumentBoundary, never()).findByPurl(any());
    verify(kulturObjektDokumentBoundary, never())
        .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
            any(), any(), any());
    verify(kulturObjektDokumentBoundary).externeRessourceImportieren(
        KOD_ID, new ExterneRessource(ExterneRessourceTyp.HSC, URL));
  }

  @Test
  void shouldResolveKodByPurlWithoutUuid() throws Exception {
    ExterneRessourcenImportEntry entry = entry("", "hsc", URL, 3);
    KulturObjektDokument kod = kod();
    mockParsedRows(entry);
    when(kulturObjektDokumentBoundary.findByPurl(entry.resolverAdresse()))
        .thenReturn(Optional.of(kod));

    ExterneRessourcenImportResult result =
        service.importieren(CSV_CONTENT).getFirst();

    assertEquals(KOD_ID, result.kodUuid());
    assertEquals(ERFOLGREICH, result.status());
    verify(kulturObjektDokumentBoundary, never()).findById(any());
    verify(kulturObjektDokumentBoundary, never())
        .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
            any(), any(), any());
  }

  @Test
  void shouldResolveKodBySignaturWithoutUuidAndPurl() throws Exception {
    ExterneRessourcenImportEntry entry = entry("", "", "hsc", URL, 3);
    KulturObjektDokument kod = kod();
    mockParsedRows(entry);
    when(kulturObjektDokumentBoundary
        .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
            entry.fuehrendeSignatur(),
            entry.besitzendeEinrichtung(),
            entry.aufbewahrungsort()))
        .thenReturn(kod);

    ExterneRessourcenImportResult result =
        service.importieren(CSV_CONTENT).getFirst();

    assertEquals(KOD_ID, result.kodUuid());
    assertEquals(ERFOLGREICH, result.status());
    verify(kulturObjektDokumentBoundary, never()).findById(any());
    verify(kulturObjektDokumentBoundary, never()).findByPurl(any());
  }

  @Test
  void shouldReportUnknownPurlWithoutSignaturFallback() throws Exception {
    ExterneRessourcenImportEntry entry = entry("", "hsc", URL, 6);
    mockParsedRows(entry);
    when(kulturObjektDokumentBoundary.findByPurl(entry.resolverAdresse()))
        .thenReturn(Optional.empty());

    ExterneRessourcenImportResult result =
        service.importieren(CSV_CONTENT).getFirst();

    assertEquals(FEHLER, result.status());
    assertEquals(KOD_NOT_FOUND, result.errorMessageKey());
    verify(kulturObjektDokumentBoundary, never())
        .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
            any(), any(), any());
    verify(kulturObjektDokumentBoundary, never())
        .externeRessourceImportieren(any(), any());
  }

  @Test
  void shouldReportUnknownUuidWithoutFallback() throws Exception {
    ExterneRessourcenImportEntry entry = entry("UNKNOWN", "hsc", URL, 7);
    mockParsedRows(entry);
    when(kulturObjektDokumentBoundary.findById("UNKNOWN")).thenReturn(Optional.empty());

    ExterneRessourcenImportResult result =
        service.importieren(CSV_CONTENT).getFirst();

    assertEquals("UNKNOWN", result.kodUuid());
    assertEquals(FEHLER, result.status());
    assertEquals(UUID_NOT_FOUND, result.errorMessageKey());
    assertArrayEquals(new Object[]{"UNKNOWN"}, result.errorMessageArgs());
    verify(kulturObjektDokumentBoundary, never()).findByPurl(any());
    verify(kulturObjektDokumentBoundary, never())
        .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
            any(), any(), any());
    verify(kulturObjektDokumentBoundary, never())
        .externeRessourceImportieren(any(), any());
  }

  @Test
  void shouldKeepEntryErrorAsMessageKeyForLaterLocalization() throws Exception {
    ExterneRessourcenImportEntry entry = entry("UNKNOWN", "hsc", URL, 1);
    mockParsedRows(entry);
    when(kulturObjektDokumentBoundary.findById("UNKNOWN")).thenReturn(Optional.empty());

    ExterneRessourcenImportResult result =
        service.importieren(CSV_CONTENT).getFirst();

    assertEquals(FEHLER, result.status());
    assertEquals(UUID_NOT_FOUND, result.errorMessageKey());
    assertArrayEquals(new Object[]{"UNKNOWN"}, result.errorMessageArgs());
  }

  @Test
  void shouldReportInvalidResourceAndLink() throws Exception {
    ExterneRessourcenImportEntry invalidResource = entry(KOD_ID, "unknown", URL, 4);
    ExterneRessourcenImportEntry invalidLink = entry(KOD_ID, "hsc", "ftp://example.org/1", 5);
    mockParsedRows(invalidResource, invalidLink);
    when(kulturObjektDokumentBoundary.findById(KOD_ID)).thenReturn(Optional.of(kod()));

    List<ExterneRessourcenImportResult> results = service.importieren(CSV_CONTENT);

    assertEquals(FEHLER, results.get(0).status());
    assertEquals(
        RESOURCE_INVALID,
        results.get(0).errorMessageKey());
    assertEquals(FEHLER, results.get(1).status());
    assertEquals(LINK_INVALID, results.get(1).errorMessageKey());
    verify(kulturObjektDokumentBoundary, never())
        .externeRessourceImportieren(any(), any());
  }

  @Test
  void shouldReportDuplicateWithLineNumber() throws Exception {
    ExterneRessourcenImportEntry entry = entry(KOD_ID, "hsc", URL, 9);
    ExterneRessource externeRessource = new ExterneRessource(ExterneRessourceTyp.HSC, URL);
    mockParsedRows(entry);
    when(kulturObjektDokumentBoundary.findById(KOD_ID)).thenReturn(Optional.of(kod()));
    doThrow(new ExterneRessourcenImportException(DUPLICATE, URL))
        .when(kulturObjektDokumentBoundary)
        .externeRessourceImportieren(KOD_ID, externeRessource);

    ExterneRessourcenImportResult result =
        service.importieren(CSV_CONTENT).getFirst();

    assertEquals(FEHLER, result.status());
    assertEquals(DUPLICATE, result.errorMessageKey());
    assertArrayEquals(new Object[]{URL}, result.errorMessageArgs());
  }

  @Test
  void shouldContinueAfterEntryErrorAndPreserveOrder() throws Exception {
    ExterneRessourcenImportEntry failed = entry("UNKNOWN", "hsc", URL, 2);
    ExterneRessourcenImportEntry successful = entry(KOD_ID, "hsc", URL, 3);
    mockParsedRows(failed, successful);
    when(kulturObjektDokumentBoundary.findById("UNKNOWN")).thenReturn(Optional.empty());
    when(kulturObjektDokumentBoundary.findById(KOD_ID)).thenReturn(Optional.of(kod()));

    List<ExterneRessourcenImportResult> results = service.importieren(CSV_CONTENT);

    assertEquals(List.of(FEHLER, ERFOLGREICH),
        results.stream().map(ExterneRessourcenImportResult::status).toList());
    assertEquals(List.of(2, 3),
        results.stream().map(ExterneRessourcenImportResult::lineNumber).toList());
  }

  @Test
  void shouldReportInvalidCsvRowAndContinueInInputOrder() throws Exception {
    ExterneRessourcenImportEntry first = entry(KOD_ID, "hsc", URL + "/1", 2);
    ExterneRessourcenImportEntry third = entry(KOD_ID, "hsc", URL + "/2", 4);
    mockParsedRows(first, 3, third);
    when(kulturObjektDokumentBoundary.findById(KOD_ID)).thenReturn(Optional.of(kod()));

    List<ExterneRessourcenImportResult> results = service.importieren(CSV_CONTENT);

    assertEquals(List.of(ERFOLGREICH, FEHLER, ERFOLGREICH),
        results.stream().map(ExterneRessourcenImportResult::status).toList());
    assertEquals(List.of(2, 3, 4),
        results.stream().map(ExterneRessourcenImportResult::lineNumber).toList());
    ExterneRessourcenImportResult invalidRow = results.get(1);
    assertEquals("", invalidRow.kodUuid());
    assertEquals("", invalidRow.ressource());
    assertEquals("", invalidRow.link());
    assertEquals(FEHLER, invalidRow.status());
    assertEquals(
        ROW_INVALID,
        invalidRow.errorMessageKey());
  }

  private void mockParsedRows(Object... rows) throws Exception {
    doAnswer(invocation -> {
      Consumer<ExterneRessourcenImportEntry> validEntryConsumer = invocation.getArgument(1);
      IntConsumer invalidRowConsumer = invocation.getArgument(2);
      for (Object row : rows) {
        if (row instanceof ExterneRessourcenImportEntry entry) {
          validEntryConsumer.accept(entry);
        } else {
          invalidRowConsumer.accept((Integer) row);
        }
      }
      return null;
    }).when(csvParser).parse(eq(CSV_CONTENT), any(), any());
  }

  private static ExterneRessourcenImportEntry entry(
      String uuid, String ressource, String link, int lineNumber) {
    return entry(uuid, "https://resolver.example.org/1", ressource, link, lineNumber);
  }

  private static ExterneRessourcenImportEntry entry(
      String uuid, String resolverAdresse, String ressource, String link, int lineNumber) {
    return ExterneRessourcenImportEntry.builder()
        .aufbewahrungsort("Berlin")
        .besitzendeEinrichtung("Staatsbibliothek zu Berlin")
        .fuehrendeSignatur("Ms. 1")
        .kodUuid(uuid)
        .resolverAdresse(resolverAdresse)
        .ressource(ressource)
        .link(link)
        .lineNumber(lineNumber)
        .build();
  }

  private static KulturObjektDokument kod() {
    return new KulturObjektDokumentBuilder().withId(KOD_ID).build();
  }
}
