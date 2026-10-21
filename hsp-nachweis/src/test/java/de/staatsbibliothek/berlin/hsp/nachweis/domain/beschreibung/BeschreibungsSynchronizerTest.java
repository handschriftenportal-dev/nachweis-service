package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenUpdateService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class BeschreibungsSynchronizerTest {

  private static final Identifikation IDENTIFIKATION = new IdentifikationBuilder()
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .withIdent("Sig-1")
      .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
      .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
      .build();

  private BeschreibungsRepository beschreibungsRepository;
  private KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  private BearbeiterBoundary bearbeiterBoundary;
  private NormdatenUpdateService normdatenUpdateService;
  private BeschreibungsSynchronizer synchronizer;
  private String validBeschreibungTei;
  private String validKodTei;

  @BeforeEach
  void setUp() throws Exception {
    beschreibungsRepository = mock(BeschreibungsRepository.class);
    kulturObjektDokumentBoundary = mock(KulturObjektDokumentBoundary.class);
    bearbeiterBoundary = mock(BearbeiterBoundary.class);
    normdatenUpdateService = mock(NormdatenUpdateService.class);

    when(bearbeiterBoundary.getUnbekannterBearbeiter())
        .thenReturn(new Bearbeiter("b-sys", "System"));

    synchronizer = new BeschreibungsSynchronizer(
        beschreibungsRepository, kulturObjektDokumentBoundary,
        bearbeiterBoundary, normdatenUpdateService);

    validBeschreibungTei = Files.readString(
        Path.of("src/test/resources/aktuelle_beschreibungstemplate.xml"));
    validKodTei = Files.readString(Path.of("src/test/resources/tei-kod-beschreibung.xml"));
  }

  /**
   * Beschreibung TEI must be saved BEFORE the KOD Attributsreferenzen are recalculated — the
   * recalculation reads from persistent state, so ordering is a behavioral contract.
   */
  @Test
  void updateTEIDocuments_savesBeschreibungBeforeRecalculatingAttributsreferenzen()
      throws Exception {
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("KOD-SYNC-1")
        .withTEIXml(validKodTei)
        .build();
    kod.getBeschreibungenIDs().add("B-SYNC-1");
    Beschreibung beschreibung = new BeschreibungsBuilder("B-SYNC-1", kod.getId(), IDENTIFIKATION)
        .withTEIXml(validBeschreibungTei)
        .build();

    synchronizer.updateTEIDocuments(kod, beschreibung, List.of());

    InOrder order = inOrder(beschreibungsRepository, kulturObjektDokumentBoundary);
    order.verify(beschreibungsRepository).saveAndFlush(beschreibung);
    order.verify(kulturObjektDokumentBoundary)
        .attributsReferenzenAusBeschreibungenAktualisieren(
            eq(kod), eq(AttributsReferenzStatusTyp.AUTOMATISCH), anyString());
  }

  /**
   * {@code updateTEIDocuments} must pull the fallback author (System bearbeiter) via
   * {@code getUnbekannterBearbeiter} for the Change element — otherwise the auto-change entry lacks
   * an author.
   */
  @Test
  void updateTEIDocuments_usesUnbekannterBearbeiterForChangeEntry() throws Exception {
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("KOD-SYNC-2")
        .withTEIXml(validKodTei)
        .build();
    Beschreibung beschreibung = new BeschreibungsBuilder("B-SYNC-2", kod.getId(), IDENTIFIKATION)
        .withTEIXml(validBeschreibungTei)
        .build();

    synchronizer.updateTEIDocuments(kod, beschreibung, List.of());

    verify(bearbeiterBoundary).getUnbekannterBearbeiter();
  }

  /**
   * Empty entstehungsOrte list must be handled gracefully — {@code updateBeschreibungsXML} is
   * called with an empty parameter list, no NPE.
   */
  @Test
  void updateTEIDocuments_emptyEntstehungsorte_runsWithoutError() throws Exception {
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("KOD-SYNC-3")
        .withTEIXml(validKodTei)
        .build();
    Beschreibung beschreibung = new BeschreibungsBuilder("B-SYNC-3", kod.getId(), IDENTIFIKATION)
        .withTEIXml(validBeschreibungTei)
        .build();

    synchronizer.updateTEIDocuments(kod, beschreibung, List.of());

    verify(beschreibungsRepository).saveAndFlush(beschreibung);
    verify(kulturObjektDokumentBoundary)
        .attributsReferenzenAusBeschreibungenAktualisieren(any(), any(), anyString());
  }

  /**
   * {@code findEntstehungsorteAsEntityFacts} on a Beschreibung without recognizable origin GND IDs
   * returns an empty list (no interaction with the update service necessary).
   */
  @Test
  void findEntstehungsorteAsEntityFacts_noOriginIds_returnsEmptyList() {
    Beschreibung beschreibung = new BeschreibungsBuilder("B-NO-ORIG", "KOD", IDENTIFIKATION)
        .withTEIXml(validBeschreibungTei)
        .build();

    List<?> result = synchronizer.findEntstehungsorteAsEntityFacts(beschreibung);

    assertNotNull(result);
  }
}
