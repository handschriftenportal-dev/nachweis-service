package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.WerkzeugeRechte.PERMISSION_KANN_AKTUALISIEREN_ALLE_KOD_XML;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * This class represents a technical batch job to update all KOD TEI files only because an TEI-Schema invalidation of
 * bibl missing id or
 *
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 30.09.25.
 * @version 1.0
 */

@Slf4j
@ApplicationScoped
public class WerkzeugBatchUpdateAllKODTEI {

  private final KulturObjektDokumentBoundary kulturObjektDokumentBoundary;

  private final BearbeiterBoundary bearbeiterBoundary;

  @Getter
  private int counter = 0;

  @Getter
  private final WerkzeugBatchJob batch =
      new WerkzeugBatchJob("Aktualisierung aller XML Dateien der Kulturobjekte");

  @Inject
  public WerkzeugBatchUpdateAllKODTEI(KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      BearbeiterBoundary bearbeiterBoundary) {
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  @CheckPermission(permission = PERMISSION_KANN_AKTUALISIEREN_ALLE_KOD_XML)
  public void execute() {

    final List<KulturObjektDokument> kulturObjektDokuments = kulturObjektDokumentBoundary.findAll();

    try {

      batch.start(bearbeiterBoundary.getLoggedBearbeiter());

      for (KulturObjektDokument kod : kulturObjektDokuments) {
        counter++;
        if ((kod.getBeschreibungenIDs() != null && !kod.getBeschreibungenIDs().isEmpty())
            || (kod.getDigitalisate() != null && !kod.getDigitalisate().isEmpty())) {
          kulturObjektDokumentBoundary.updateKODXML(kod.getId());
        }
        log.info("KOD TEI updated! {} number {} of {}", kod.getId(), counter, kulturObjektDokuments.size());
      }

      batch.end("Successfully updated all Kulturobjekte");

    } catch (Exception error) {
      log.error("Error while updating KOD TEI", error);
      batch.fail(error.getMessage());
    }
  }

}
