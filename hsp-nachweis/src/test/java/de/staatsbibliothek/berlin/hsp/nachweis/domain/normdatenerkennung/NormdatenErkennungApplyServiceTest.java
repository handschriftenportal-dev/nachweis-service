package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIQuery;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.Erklaerung;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenUpdateService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.Treffer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.normdatenerkennung.NormdatenErkennungErgebnis;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NormdatenErkennungApplyServiceTest {

  private final Bearbeiter bearbeiter = new Bearbeiter("1", "Test");
  private NormdatenErkennungAuftragService  auftragService;
  private BeschreibungsRepository beschreibungsRepository;
  private NormdatenUpdateService normdatenUpdateService;
  private NormdatenErkennungApplyService service;

  private static String firstPersNameXpath(String teiXml) throws Exception {
    var tei = TEIObjectFactory.unmarshalOne(
        new ByteArrayInputStream(teiXml.getBytes(StandardCharsets.UTF_8))).orElseThrow();
    return TEIQuery.findAllPersNamesWithPath(tei).keySet().iterator().next();
  }

  private static NormdatenErkennungEintrag recommendationResponse(
      String xpath, String normId, String gndId, String quelle) {
    var fact = new GNDEntityFact(normId, gndId, "Test Person", "Person");
    var erklaerung = new Erklaerung(null, null, null, null, 0.9f, 0.9f, 0f, quelle);
    var ergebnis = new NormdatenErkennungErgebnis(null, new Treffer(erklaerung, fact));
    var response = new NormdatenErkennungEintrag(xpath, NormdatenTyp.Person, "Test Person");
    response.setErgebnis(ergebnis);
    return response;
  }

  @BeforeEach
  void setUp() {
    auftragService = mock(NormdatenErkennungAuftragService.class);
    beschreibungsRepository = mock(BeschreibungsRepository.class);
    normdatenUpdateService = mock(NormdatenUpdateService.class);
    service = new NormdatenErkennungApplyService(
        auftragService, beschreibungsRepository, normdatenUpdateService);
  }

  @Test
  void applyAuftrag_doesNothing_whenAuftragNotFound() {
    when(auftragService.findById("unknown")).thenReturn(null);

    service.applyAuftrag("unknown");

    verify(auftragService, never()).save(any());
  }

  @Test
  void applyAuftrag_marksError_whenBeschreibungNotFound() {
    var auftrag = new NormdatenErkennungAuftrag("b-missing", bearbeiter);
    when(auftragService.findByIdWithErgebnisse(auftrag.getId())).thenReturn(Optional.of(auftrag));
    when(auftragService.findById(auftrag.getId())).thenReturn(Optional.of(auftrag));
    when(beschreibungsRepository.findById("b-missing")).thenReturn(null);

    service.applyAuftrag(auftrag.getId());

    assertEquals(NormdatenErkennungAuftragStatus.ERROR, auftrag.getStatus());
    verify(auftragService).save(auftrag);
  }

  @Test
  void applyAuftrag_marksError_whenTeiIsBlank() {
    var auftrag = new NormdatenErkennungAuftrag("b-blank", bearbeiter);
    var beschreibung = mock(Beschreibung.class);

    when(auftragService.findByIdWithErgebnisse(auftrag.getId())).thenReturn(Optional.of(auftrag));
    when(auftragService.findById(auftrag.getId())).thenReturn(Optional.of(auftrag));
    when(beschreibungsRepository.findById("b-blank")).thenReturn(beschreibung);
    when(beschreibung.getTeiXML()).thenReturn("");

    service.applyAuftrag(auftrag.getId());

    assertEquals(NormdatenErkennungAuftragStatus.ERROR, auftrag.getStatus());
    assertNotNull(auftrag.getErrorMessage());
  }

  @Test
  void applyAuftrag_completes_whenNoResponsesNeedUpdating() throws Exception {
    var auftrag = new NormdatenErkennungAuftrag("b-1", bearbeiter);
    var beschreibung = mock(Beschreibung.class);
    var teiXml = Files.readString(Path.of("src/test/resources/beschreibung_freiburg_1988.xml"));

    when(auftragService.findByIdWithErgebnisse(auftrag.getId())).thenReturn(Optional.of(auftrag));
    when(auftragService.findById(auftrag.getId())).thenReturn(Optional.of(auftrag));
    when(beschreibungsRepository.findById("b-1")).thenReturn(beschreibung);
    when(beschreibung.getTeiXML()).thenReturn(teiXml);

    service.applyAuftrag(auftrag.getId());

    assertEquals(NormdatenErkennungAuftragStatus.APPLIED, auftrag.getStatus());
    verify(beschreibungsRepository).save(beschreibung);
  }

  @Test
  void applyAuftrag_setsPersNameKey_forGraphRecommendation() throws Exception {
    var teiXml = Files.readString(Path.of("src/test/resources/beschreibung_freiburg_1988.xml"));
    var xpath = firstPersNameXpath(teiXml);

    var auftrag = new NormdatenErkennungAuftrag("b-1", bearbeiter);
    auftrag.addErgebnis(
        recommendationResponse(xpath, "NORM-123", "gnd456", Erklaerung.QUELLE_GRAPH));

    var beschreibung = mock(Beschreibung.class);
    when(auftragService.findByIdWithErgebnisse(auftrag.getId())).thenReturn(Optional.of(auftrag));
    when(auftragService.findById(auftrag.getId())).thenReturn(Optional.of(auftrag));
    when(beschreibungsRepository.findById("b-1")).thenReturn(beschreibung);
    when(beschreibung.getTeiXML()).thenReturn(teiXml);

    service.applyAuftrag(auftrag.getId());

    assertEquals(NormdatenErkennungAuftragStatus.APPLIED, auftrag.getStatus());
    verifyNoInteractions(normdatenUpdateService);
    var xmlCaptor = ArgumentCaptor.forClass(String.class);
    verify(beschreibung).setTeiXML(xmlCaptor.capture());
    assertTrue(xmlCaptor.getValue().contains("NORM-123"));
  }

  @Test
  void applyAuftrag_callsEnsureInGraph_forLobidRecommendation() throws Exception {
    var teiXml = Files.readString(Path.of("src/test/resources/beschreibung_freiburg_1988.xml"));
    var xpath = firstPersNameXpath(teiXml);
    var fact = new GNDEntityFact("NORM-456", "gnd789", "Lobid Person", "Person");

    var auftrag = new NormdatenErkennungAuftrag("b-1", bearbeiter);
    auftrag.addErgebnis(
        recommendationResponse(xpath, "NORM-456", "gnd789", Erklaerung.QUELLE_LOBID));

    var beschreibung = mock(Beschreibung.class);
    when(auftragService.findByIdWithErgebnisse(auftrag.getId())).thenReturn(Optional.of(auftrag));
    when(auftragService.findById(auftrag.getId())).thenReturn(Optional.of(auftrag));
    when(beschreibungsRepository.findById("b-1")).thenReturn(beschreibung);
    when(beschreibung.getTeiXML()).thenReturn(teiXml);
    when(normdatenUpdateService.ensureInGraph("gnd789", NormdatenTyp.Person))
        .thenReturn(Optional.of(fact));

    service.applyAuftrag(auftrag.getId());

    verify(normdatenUpdateService).ensureInGraph("gnd789", NormdatenTyp.Person);
    assertEquals(NormdatenErkennungAuftragStatus.APPLIED, auftrag.getStatus());
  }

  @Test
  void applyAuftrag_doesNotUpdatePersName_whenLobidEnsureInGraphFails() throws Exception {
    var teiXml = Files.readString(Path.of("src/test/resources/beschreibung_freiburg_1988.xml"));
    var xpath = firstPersNameXpath(teiXml);

    var auftrag = new NormdatenErkennungAuftrag("b-1", bearbeiter);
    auftrag.addErgebnis(
        recommendationResponse(xpath, "NORM-456", "gnd789", Erklaerung.QUELLE_LOBID));

    var beschreibung = mock(Beschreibung.class);
    when(auftragService.findByIdWithErgebnisse(auftrag.getId())).thenReturn(Optional.of(auftrag));
    when(auftragService.findById(auftrag.getId())).thenReturn(Optional.of(auftrag));
    when(beschreibungsRepository.findById("b-1")).thenReturn(beschreibung);
    when(beschreibung.getTeiXML()).thenReturn(teiXml);
    when(normdatenUpdateService.ensureInGraph(anyString(), any())).thenReturn(Optional.empty());

    service.applyAuftrag(auftrag.getId());

    assertEquals(NormdatenErkennungAuftragStatus.APPLIED, auftrag.getStatus());
    var xmlCaptor = ArgumentCaptor.forClass(String.class);
    verify(beschreibung).setTeiXML(xmlCaptor.capture());
    assertFalse(xmlCaptor.getValue().contains("NORM-456"),
        "persName key must not be updated when ensureInGraph fails");
  }

  @Test
  void applyAuftrag_callsEnsureInGraph_forManualSelection() throws Exception {
    var teiXml = Files.readString(Path.of("src/test/resources/beschreibung_freiburg_1988.xml"));
    var xpath = firstPersNameXpath(teiXml);
    var fact = new GNDEntityFact("NORM-999", "gnd-manual-999", "Manual Person", "Person");

    var response = new NormdatenErkennungEintrag(xpath, NormdatenTyp.Person, "Manual Person");
    response.setSelectionManualIdentifier("gnd-manual-999", bearbeiter);

    var auftrag = new NormdatenErkennungAuftrag("b-1", bearbeiter);
    auftrag.addErgebnis(response);

    var beschreibung = mock(Beschreibung.class);
    when(auftragService.findByIdWithErgebnisse(auftrag.getId())).thenReturn(Optional.of(auftrag));
    when(auftragService.findById(auftrag.getId())).thenReturn(Optional.of(auftrag));
    when(beschreibungsRepository.findById("b-1")).thenReturn(beschreibung);
    when(beschreibung.getTeiXML()).thenReturn(teiXml);
    when(normdatenUpdateService.ensureInGraph("gnd-manual-999", NormdatenTyp.Person))
        .thenReturn(Optional.of(fact));

    service.applyAuftrag(auftrag.getId());

    verify(normdatenUpdateService).ensureInGraph("gnd-manual-999", NormdatenTyp.Person);
    assertEquals(NormdatenErkennungAuftragStatus.APPLIED, auftrag.getStatus());
  }
}
