package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.normdatenerkennung.NormdatenErkennungErgebnis;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.normdatenerkennung.NormdatenErkennungGraphQLClient;
import io.smallrye.graphql.client.typesafe.api.ErrorOr;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NormdatenErkennungAuftragServiceTest {

  private final Bearbeiter bearbeiter = new Bearbeiter("1", "Test");
  private NormdatenErkennungAuftragRepository auftragRepository;
  private NormdatenErkennungEintragRepository eintragRepository;
  private NormdatenErkennungGraphQLClient erkennungClient;
  private NormdatenErkennungRequestParamExtractor paramExtractor;
  private BeschreibungsBoundary beschreibungsBoundary;
  private NormdatenErkennungAuftragService service;

  @BeforeEach
  void setUp() {
    auftragRepository = mock(NormdatenErkennungAuftragRepository.class);
    eintragRepository = mock(NormdatenErkennungEintragRepository.class);
    erkennungClient = mock(NormdatenErkennungGraphQLClient.class);
    paramExtractor = mock(NormdatenErkennungRequestParamExtractor.class);
    beschreibungsBoundary = mock(BeschreibungsBoundary.class);
    service = new NormdatenErkennungAuftragService(
        auftragRepository, eintragRepository, erkennungClient, paramExtractor, beschreibungsBoundary, 10);
  }

  @Test
  void enqueueBeschreibungAuftraege_savesOneAuftragPerBeschreibung() {
    service.enqueueBeschreibungAuftraege(List.of("b-1", "b-2", "b-3"), bearbeiter);

    verify(auftragRepository, times(3)).save(any(NormdatenErkennungAuftrag.class));
  }

  @Test
  void processAuftrag_savesAwaitingReview_whenNoParamsFound() throws Exception {
    var auftrag = new NormdatenErkennungAuftrag("b-1", bearbeiter);
    var beschreibung = mock(Beschreibung.class);
    when(auftragRepository.findById(auftrag.getId())).thenReturn(auftrag);
    when(beschreibungsBoundary.findById("b-1")).thenReturn(Optional.of(beschreibung));
    when(beschreibung.getTeiXML()).thenReturn("<tei/>");
    when(paramExtractor.extract(any())).thenReturn(List.of());

    service.processAuftrag(auftrag.getId());

    assertEquals(NormdatenErkennungAuftragStatus.AWAITING_REVIEW, auftrag.getStatus());
    verify(auftragRepository).save(auftrag);
    verifyNoInteractions(erkennungClient);
  }

  @Test
  void processAuftrag_savesError_whenBeschreibungNotFound() {
    var auftrag = new NormdatenErkennungAuftrag("b-missing", bearbeiter);
    when(auftragRepository.findById(auftrag.getId())).thenReturn(auftrag);
    when(beschreibungsBoundary.findById("b-missing")).thenReturn(Optional.empty());

    service.processAuftrag(auftrag.getId());

    assertEquals(NormdatenErkennungAuftragStatus.ERROR, auftrag.getStatus());
    verify(auftragRepository).save(auftrag);
    verifyNoInteractions(erkennungClient);
  }

  @Test
  void processAuftrag_savesError_whenExtractorFails() throws Exception {
    var auftrag = new NormdatenErkennungAuftrag("b-1", bearbeiter);
    var beschreibung = mock(Beschreibung.class);
    when(auftragRepository.findById(auftrag.getId())).thenReturn(auftrag);
    when(beschreibungsBoundary.findById("b-1")).thenReturn(Optional.of(beschreibung));
    when(beschreibung.getTeiXML()).thenReturn("<tei/>");
    when(paramExtractor.extract(any())).thenThrow(new RuntimeException("parse error"));

    service.processAuftrag(auftrag.getId());

    assertEquals(NormdatenErkennungAuftragStatus.ERROR, auftrag.getStatus());
    verify(auftragRepository).save(auftrag);
    verify(auftragRepository, never()).save(argThatHasStatus(NormdatenErkennungAuftragStatus.AWAITING_REVIEW));
  }

  @Test
  @SuppressWarnings("unchecked")
  void processAuftrag_queriesNormdaten_forEachRequestParam() throws Exception {
    var auftrag = new NormdatenErkennungAuftrag("b-1", bearbeiter);
    var beschreibung = mock(Beschreibung.class);
    var params = List.of(
        new NormdatenErkennungRequestParam("/persName[1]", "Author A", "", NormdatenTyp.Person),
        new NormdatenErkennungRequestParam("/persName[2]", "Author B", "gnd123", NormdatenTyp.Person)
    );
    var errorOrResult = (ErrorOr<NormdatenErkennungErgebnis>) mock(ErrorOr.class);
    when(auftragRepository.findById(auftrag.getId())).thenReturn(auftrag);
    when(beschreibungsBoundary.findById("b-1")).thenReturn(Optional.of(beschreibung));
    when(beschreibung.getTeiXML()).thenReturn("<tei/>");
    when(paramExtractor.extract(any())).thenReturn(params);
    when(erkennungClient.normdatenerkennung(any())).thenReturn(errorOrResult);

    service.processAuftrag(auftrag.getId());

    verify(erkennungClient, times(2)).normdatenerkennung(any());
    assertEquals(NormdatenErkennungAuftragStatus.AWAITING_REVIEW, auftrag.getStatus());
    assertEquals(2, auftrag.getErgebnisse().size());
  }

  @Test
  void setSelectionManualIdentifier_updatesEintrag() {
    var eintrag = new NormdatenErkennungEintrag("/persName[1]", NormdatenTyp.Person, "Test");
    when(eintragRepository.findById(eintrag.getId())).thenReturn(eintrag);

    service.setSelectionManualIdentifier(eintrag.getId(), "gnd123", bearbeiter);

    assertEquals(NormdatenErkennungAuswahlTyp.MANUAL, eintrag.getSelectionType());
    assertEquals("gnd123", eintrag.getManualIdentifier());
    verify(eintragRepository).save(eintrag);
  }

  @Test
  void setSelectionNone_updatesEintrag() {
    var eintrag = new NormdatenErkennungEintrag("/persName[1]", NormdatenTyp.Person, "Test");
    eintrag.setSelectionManualIdentifier("gnd123", bearbeiter);
    when(eintragRepository.findById(eintrag.getId())).thenReturn(eintrag);

    service.setSelectionNone(eintrag.getId(), bearbeiter);

    assertEquals(NormdatenErkennungAuswahlTyp.NONE, eintrag.getSelectionType());
    verify(eintragRepository).save(eintrag);
  }

  @Test
  void findByIdWithErgebnisse_returnsOptional() {
    var auftrag = new NormdatenErkennungAuftrag("b-1", bearbeiter);
    when(auftragRepository.findByIdWithErgebnisse(auftrag.getId())).thenReturn(Optional.of(auftrag));

    var result = service.findByIdWithErgebnisse(auftrag.getId());

    assertTrue(result.isPresent());
    assertEquals(auftrag, result.get());
  }

  private NormdatenErkennungAuftrag argThatHasStatus(NormdatenErkennungAuftragStatus status) {
    return org.mockito.ArgumentMatchers.argThat(a -> a.getStatus() == status);
  }
}
