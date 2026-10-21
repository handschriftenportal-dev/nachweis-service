package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkeyupdate;

import static de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction.UPDATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIKulturObjektDokumentCommand;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.KulturObjektSortkeyUpdateException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortKeyBatchResult;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortKeyEntry;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyBatchService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.network.DistributedSaveCommand;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SortkeyBatchServiceTest {

  @Mock
  private KulturObjektDokumentRepository kulturObjektDokumentRepository;

  @Mock
  private DistributedSaveCommand<KulturObjektDokument> distributedSaveCommand;

  private SortkeyBatchService sortkeyBatchService;

  @BeforeEach
  void setUp() {
    sortkeyBatchService =
        new SortkeyBatchService(
            kulturObjektDokumentRepository,
            distributedSaveCommand);
  }

  @Test
  void shouldUpdateSortkeySuccessfully() throws Exception {

    String uuid = "550e8400-e29b-41d4-a716-446655440000";

    KulturObjektDokument kod = mock(KulturObjektDokument.class);
    Identifikation ident = mock(Identifikation.class);

    when(kod.getGueltigeIdentifikation()).thenReturn(ident);
    when(kod.getSortKey()).thenReturn("old");

    when(kod.getId()).thenReturn(uuid);

    when(kulturObjektDokumentRepository.list(anyString(), any(Object[].class)))
        .thenReturn(List.of(kod));

    SortKeyEntry sortKeyEntry =
        new SortKeyEntry(uuid, "new", 1);

    try (MockedStatic<TEIKulturObjektDokumentCommand> mocked =
        mockStatic(TEIKulturObjektDokumentCommand.class)) {

      mocked.when(() ->
              TEIKulturObjektDokumentCommand.updateMsIdentifierAndAllAltidentifier(any()))
          .thenAnswer(invocation -> null);

      SortKeyBatchResult result =
          sortkeyBatchService.processChunk(List.of(sortKeyEntry));

      verify(ident).setSortKey("new");
      verify(distributedSaveCommand).execute(eq(kod), eq(UPDATE));

      assertEquals(1, result.updatedCount());
      assertTrue(result.unknownUuids().isEmpty());
    }
  }

  @Test
  void shouldCollectUnknownUuid() throws Exception {

    String uuid = "550e8400-e29b-41d4-a716-446655440000";

    when(kulturObjektDokumentRepository.list(anyString(), any(Object[].class)))
        .thenReturn(List.of());

    SortKeyEntry sortKeyEntry =
        new SortKeyEntry(uuid, "Clm_1330", 1);

    SortKeyBatchResult result =
        sortkeyBatchService.processChunk(List.of(sortKeyEntry));

    assertEquals(0, result.updatedCount());
    assertEquals(List.of(uuid), result.unknownUuids());
  }

  @Test
  void shouldSkipIfSameSortkey() throws Exception {

    String uuid = "550e8400-e29b-41d4-a716-446655440001";

    KulturObjektDokument kod = mock(KulturObjektDokument.class);
    Identifikation ident = mock(Identifikation.class);

    when(kod.getGueltigeIdentifikation()).thenReturn(ident);
    when(kod.getSortKey()).thenReturn("Clm_1330");

    when(kod.getId()).thenReturn(uuid);

    when(kulturObjektDokumentRepository.list(anyString(), any(Object[].class)))
        .thenReturn(List.of(kod));

    SortKeyEntry sortKeyEntry =
        new SortKeyEntry(uuid, "Clm_1330", 1);

    SortKeyBatchResult result =
        sortkeyBatchService.processChunk(List.of(sortKeyEntry));

    verify(ident, never()).setSortKey(any());
    verify(distributedSaveCommand, never()).execute(any(), any());

    assertEquals(0, result.updatedCount());
    assertTrue(result.unknownUuids().isEmpty());
  }

  @Test
  void shouldSkipIfNoIdentifikation() throws Exception {

    String uuid = "550e8400-e29b-41d4-a716-446655440002";

    KulturObjektDokument kod = mock(KulturObjektDokument.class);

    when(kod.getGueltigeIdentifikation()).thenReturn(null);

    when(kod.getId()).thenReturn(uuid);

    when(kulturObjektDokumentRepository.list(anyString(), any(Object[].class)))
        .thenReturn(List.of(kod));

    SortKeyEntry sortKeyEntry =
        new SortKeyEntry(uuid, "Ms_Lat_Oct_118", 1);

    SortKeyBatchResult result =
        sortkeyBatchService.processChunk(List.of(sortKeyEntry));

    verify(distributedSaveCommand, never()).execute(any(), any());

    assertEquals(0, result.updatedCount());
    assertTrue(result.unknownUuids().isEmpty());
  }

  @Test
  void shouldThrowExceptionWhenSaveFails() throws Exception {

    String uuid = "550e8400-e29b-41d4-a716-446655440003";

    KulturObjektDokument kod = mock(KulturObjektDokument.class);
    Identifikation ident = mock(Identifikation.class);

    when(kod.getGueltigeIdentifikation()).thenReturn(ident);
    when(kod.getSortKey()).thenReturn("old_key");

    when(kod.getId()).thenReturn(uuid);

    when(kulturObjektDokumentRepository.list(anyString(), any(Object[].class)))
        .thenReturn(List.of(kod));

    doThrow(new RuntimeException("DB error"))
        .when(distributedSaveCommand)
        .execute(any(), any());

    SortKeyEntry sortKeyEntry =
        new SortKeyEntry(uuid, "new_key", 1);

    try (MockedStatic<TEIKulturObjektDokumentCommand> mocked =
        mockStatic(TEIKulturObjektDokumentCommand.class)) {

      mocked.when(() ->
              TEIKulturObjektDokumentCommand.updateMsIdentifierAndAllAltidentifier(any()))
          .thenAnswer(invocation -> null);

      assertThrows(
          KulturObjektSortkeyUpdateException.class,
          () -> sortkeyBatchService.processChunk(List.of(sortKeyEntry)));
    }
  }
}
