/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel.BeschreibungsViewBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewModel.ImportViewModelBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.KulturObjektDokumentNotFoundException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamActorDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamTargetDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */
public abstract class ImportViewMapper {

  private static final Logger log = LoggerFactory.getLogger(ImportViewMapper.class);
  public static final String BR = "<br/>";


  public static ImportViewModel map(BeschreibungImport beschreibungImportJob, ImportBoundary importBoundary,
      boolean doExtractBeschreibungFromActivityStream) {
    if (Objects.nonNull(beschreibungImportJob) && Objects.nonNull(beschreibungImportJob.getMessageDTO())) {

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

          log.info("Trying to find Beschreibungen in Activity Message {} ", beschreibungImportJob.getId());

          if (ActivityStreamsDokumentTyp.BESCHREIBUNG.equals(streamObject.getType())) {

            importUrl = streamObject.getUrl();

            try {
              log.info("Try to extract Beschreibung from AS");
              createBeschreibungsViewModel(beschreibungImportJob, importBoundary, beschreibungsViewModels,
                  streamObject);
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
              beschreibungImportJob.getCreationDate() != null ? beschreibungImportJob.getCreationDate() : null)
          .withImportStatus(beschreibungImportJob.getStatus())
          .withFehler(beschreibungImportJob.getFehlerBeschreibung())
          .withSelection(beschreibungImportJob.isSelectedForImport())
          .withBeschreibung(beschreibungsViewModels).build();
    }

    return null;
  }

  private static void createBeschreibungsViewModel(BeschreibungImport beschreibungImportJob,
      ImportBoundary importBoundary,
      List<BeschreibungsViewModel> beschreibungsViewModels, ActivityStreamObjectDTO streamObject) throws Exception {

    List<Beschreibung> beschreibungen = importBoundary.convertTEI2Beschreibung(streamObject);

    for (Beschreibung beschreibung : beschreibungen) {

      BeschreibungsViewModel view = new BeschreibungsViewBuilder()
          .withID((beschreibung).getId())
          .withImportJobID(beschreibungImportJob.getId())
          .withTEIXML(beschreibung.getTeiXML())
          .build();

      if ((beschreibung).getBeschreibungsStruktur() != null) {
        beschreibung
            .getBeschreibungsStruktur().stream()
            .filter(k -> k instanceof BeschreibungsKomponenteKopf)
            .map(ko -> (BeschreibungsKomponenteKopf) ko)
            .findAny()
            .ifPresent(kopf -> {
              view.setTitel(kopf.getTitel());

              kopf.getIdentifikationen().stream()
                  .filter(i -> i.getIdentTyp().equals(IdentTyp.GUELTIGE_SIGNATUR)).findFirst().ifPresent(i -> {
                view.setSignatur(i.getIdent());
              });
            });
      }

      checkBeschreibungsImportPreCondition(importBoundary, beschreibung, view);

      beschreibungsViewModels.add(view);
    }
  }

  static void checkBeschreibungsImportPreCondition(ImportBoundary importBoundary, Beschreibung beschreibung,
      BeschreibungsViewModel view) {
    try {

      Optional<Beschreibung> beschreibungAlreadyExist = importBoundary.checkIfBeschreibungAlreadyExists(beschreibung);

      if(beschreibungAlreadyExist.isPresent()) {
        view.setPreconditionState(VerwaltungsTyp.EXTERN.equals(beschreibungAlreadyExist.get().getVerwaltungsTyp()));
        view.setPreconditionResult("Beschreibung ("+beschreibungAlreadyExist.get().getVerwaltungsTyp()+") existiert bereits "+beschreibungAlreadyExist.get().getId());
        return;
      }else {

        Optional<KulturObjektDokument> kod = importBoundary.ermittleKOD(beschreibung);

        if (kod.isPresent()) {
          view.setPreconditionResult(getKODView(kod));
          view.setPreconditionState(true);
          return;
        }
      }

    } catch (KulturObjektDokumentNotFoundException error) {
      view.setPreconditionState(false);
      view.setPreconditionResult(error.getMessage());
    }
  }

  static String getKODView(Optional<KulturObjektDokument> kod) {

    return kod.map(
        kulturObjektDokument -> "Kulturobjektdokument "+kulturObjektDokument.getId() + BR + (
            kulturObjektDokument.getGueltigeIdentifikation() != null
                && kulturObjektDokument.getGueltigeIdentifikation().getIdent() != null ?
                kulturObjektDokument.getGueltigeIdentifikation().getIdent() : "")
            + BR +
            (kulturObjektDokument.getGueltigeIdentifikation().getBesitzer() != null
                ? kulturObjektDokument.getGueltigeIdentifikation().getBesitzer().getName() : "")).orElse("");
  }

}
