package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.EntstehungsortParameter;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIBeschreibungCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIQuery;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIKODAttributsReferenzCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIKulturObjektDokumentCommand;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenUpdateService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.tei_c.ns._1.Change;

/**
 * Re-marshalt die Beschreibungs-TEI und synchronisiert die abgeleiteten KOD-AttributsReferenzen.
 * Wird vom automatischen Batch-Import und (via {@code BeschreibungsService.publizieren}) vom
 * manuellen Approve-Pfad gleichermaßen verwendet, damit beide Pfade die gleichen TEI-Folgeupdates
 * auslösen.
 */
@Slf4j
@ApplicationScoped
public class BeschreibungsSynchronizer {

  private static final String CHANGE_DESCRIPTION =
      "Change Attributsreferenzen nach Publikation";

  private BeschreibungsRepository beschreibungsRepository;
  private KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  private BearbeiterBoundary bearbeiterBoundary;
  private NormdatenUpdateService normdatenUpdateService;

  protected BeschreibungsSynchronizer() {
  }

  @Inject
  public BeschreibungsSynchronizer(
      BeschreibungsRepository beschreibungsRepository,
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      BearbeiterBoundary bearbeiterBoundary,
      NormdatenUpdateService normdatenUpdateService) {
    this.beschreibungsRepository = beschreibungsRepository;
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.normdatenUpdateService = normdatenUpdateService;
  }

  public void updateTEIDocuments(
      KulturObjektDokument kod,
      Beschreibung beschreibung,
      List<GNDEntityFact> entstehungsOrte) throws Exception {

    List<EntstehungsortParameter> entstehungsortParameters = entstehungsOrte.stream()
        .map(gnd -> new EntstehungsortParameter(gnd.getId(),
            gnd.getPreferredName(),
            gnd.getGndIdentifier()))
        .toList();

    TEIBeschreibungCommand.updateBeschreibungsXML(beschreibung, entstehungsortParameters);
    Change change = createAutoChangeEntry();
    beschreibungsRepository.saveAndFlush(beschreibung);

    kulturObjektDokumentBoundary.attributsReferenzenAusBeschreibungenAktualisieren(
        kod, AttributsReferenzStatusTyp.AUTOMATISCH, change.getId());

    TEIKODAttributsReferenzCommand.updateAttributsReferenzen(kod, change);
    TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(kod);
  }

  private @NonNull Change createAutoChangeEntry() {
    return TEIKODAttributsReferenzCommand.createChangeElement(
        HSP_UUI_Factory.generate(), LocalDateTime.now(),
        AttributsReferenzStatusTyp.AUTOMATISCH,
        CHANGE_DESCRIPTION,
        bearbeiterBoundary.getUnbekannterBearbeiter());
  }

  public List<GNDEntityFact> findEntstehungsorteAsEntityFacts(Beschreibung beschreibung) {
    return TEIQuery.queryForTermOriginGNDID(beschreibung).stream()
        .filter(Objects::nonNull)
        .distinct()
        .map(gndid -> normdatenUpdateService.createPlaceIfNotExist(gndid).orElse(null))
        .filter(Objects::nonNull)
        .toList();
  }
}
