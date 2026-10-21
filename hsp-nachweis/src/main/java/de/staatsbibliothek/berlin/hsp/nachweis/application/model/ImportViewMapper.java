package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel.BeschreibungsViewBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewModel.ImportViewModelBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentNotFoundException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamActorDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamTargetDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */
@ApplicationScoped
public class ImportViewMapper {

  public static final String BR = "<br/>";
  private static final Logger log = LoggerFactory.getLogger(ImportViewMapper.class);

  private final ImportBoundary importBoundary;
  private final KulturObjektDokumentBoundary kulturObjektDokumentBoundary;

  @Inject
  public ImportViewMapper(
      ImportBoundary importBoundary,
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary
  ) {
    this.importBoundary = importBoundary;
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
  }

  private List<BeschreibungsViewModel> createBeschreibungsViewModel(
      BeschreibungImport beschreibungImportJob,
      ActivityStreamObjectDTO streamObject)
      throws Exception {

    List<Beschreibung> beschreibungen = importBoundary.convertTEI2Beschreibung(streamObject);
    List<BeschreibungsViewModel> beschreibungsViewModels = new ArrayList<>();

    for (Beschreibung beschreibung : beschreibungen) {
      BeschreibungsViewModel view = new BeschreibungsViewBuilder()
          .withID((beschreibung).getId())
          .withImportJobID(beschreibungImportJob.getId())
          .withTEIXML(beschreibung.getTeiXML())
          .build();

      if ((beschreibung).getBeschreibungsStruktur() != null) {
        beschreibung
            .getBeschreibungsStruktur().stream()
            .filter(BeschreibungsKomponenteKopf.class::isInstance)
            .map(ko -> (BeschreibungsKomponenteKopf) ko)
            .findAny()
            .ifPresent(kopf -> {
              view.setTitel(kopf.getTitel());
              beschreibung.getGueltigeIdentifikation().ifPresent(
                  identifikation -> view.setSignatur(identifikation.getIdent()));
            });
      }
      checkBeschreibungsImportPreCondition(beschreibung, view);
      beschreibungsViewModels.add(view);
    }

    return beschreibungsViewModels;
  }

  void checkBeschreibungsImportPreCondition(Beschreibung beschreibung,
      BeschreibungsViewModel view) {
    try {

      Optional<Beschreibung> existingBeschreibung = importBoundary.findExistingBeschreibung(
          beschreibung);

      if (existingBeschreibung.isPresent()) {
        Beschreibung b = existingBeschreibung.get();
        view.setPreconditionState(b.isPublished());
        view.setPreconditionResult("Beschreibung ("
            + b.getVerwaltungsTyp()
            + ") existiert bereits "
            + b.getId()
        );
      } else {
        kulturObjektDokumentBoundary.findKodByBeschreibung(beschreibung)
            .ifPresentOrElse(
                k -> {
                  view.setPreconditionResult(getKODView(k));
                  view.setPreconditionState(true);
                },
                () -> {
                  view.setPreconditionResult(
                      "Kulturobjekt konnte mit Minimaldaten von Beschreibung "
                          + beschreibung.formatSignatureLabel()
                          + " nicht gefunden werden!");
                  view.setPreconditionState(false);
                });
      }

    } catch (KulturObjektDokumentNotFoundException error) {
      view.setPreconditionState(false);
      view.setPreconditionResult(error.getMessage());
    }
  }

  String getKODView(KulturObjektDokument kod) {
    if (kod == null) {
      return "";
    }

    var identifikation = kod.getGueltigeIdentifikation();

    String ident = identifikation != null && identifikation.getIdent() != null
        ? identifikation.getIdent()
        : "";

    String besitzer = identifikation != null && identifikation.getBesitzer() != null
        ? identifikation.getBesitzer().getName()
        : "";

    return "Kulturobjektdokument " + kod.getId() + BR + ident + BR + besitzer;
  }

  public ImportViewModel map(BeschreibungImport beschreibungImportJob,
      boolean doExtractBeschreibungFromActivityStream) {
    if (Objects.nonNull(beschreibungImportJob) && Objects.nonNull(
        beschreibungImportJob.getMessageDTO())) {

      ActivityStreamMessageDTO message = beschreibungImportJob.getMessageDTO();
      ActivityStreamActorDTO actor = message.getActor();
      ActivityStreamTargetDTO target = message.getTarget();

      String benutzerName = "";
      String institution = "";
      String dateiname = "";
      String importUrl = "";

      if (actor != null) {
        benutzerName = actor.getName() != null ? actor.getName() : "";
      }

      if (target != null) {
        dateiname = target.getName() != null ? target.getName() : "";
      }

      List<BeschreibungsViewModel> beschreibungsViewModels = new ArrayList<>();

      if (doExtractBeschreibungFromActivityStream) {
        for (ActivityStreamObjectDTO streamObject : message.getObjects()) {
          log.info("Trying to find Beschreibungen in Activity Message {} ",
              beschreibungImportJob.getId());

          if (ActivityStreamsDokumentTyp.BESCHREIBUNG.equals(streamObject.getType())) {

            importUrl = streamObject.getUrl();

            try {
              log.info("Try to extract Beschreibung from AS");
              beschreibungsViewModels.addAll(
                  createBeschreibungsViewModel(beschreibungImportJob, streamObject));
            } catch (Exception ex) {
              beschreibungImportJob.setFehlerBeschreibung(ex.getMessage());
              log.error("Problem with ActivityStream {}", ex.getMessage(), ex);
            }
          }
        }
      }

      log.debug("Add number of beschreibungen {} ", beschreibungsViewModels.size());

      return new ImportViewModelBuilder()
          .withId(beschreibungImportJob.getId())
          .withBenutzer(benutzerName)
          .withInstitution(institution)
          .withDateiName(dateiname)
          .withImportUrl(importUrl)
          .withImportDatum(
              beschreibungImportJob.getCreationDate() != null
                  ? beschreibungImportJob.getCreationDate() : null)
          .withImportStatus(beschreibungImportJob.getStatus())
          .withFehler(beschreibungImportJob.getFehlerBeschreibung())
          .withSelection(beschreibungImportJob.isSelectedForImport())
          .withBeschreibung(beschreibungsViewModels).build();
    }

    return null;
  }

}
