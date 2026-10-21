package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Holds all active import jobs taken over
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 08.06.21
 */
@ApplicationScoped
public class DatenuebernahmeStatusModel {

  private static Set<String> ACTIVE_JOBS = Collections.synchronizedSet(new LinkedHashSet<>());

  public void addActiveJob(String id) {
    if (id != null) {
      if (ACTIVE_JOBS.contains(id)) {
        throw new RuntimeException(
            "ImportJob '" + id + "' befindet sich bereits in der Übernahme.");
      }
      ACTIVE_JOBS.add(id);
    }
  }

  public void removeActiveJob(String id) {
    if (id != null) {
      ACTIVE_JOBS.remove(id);
    }
  }

  public ImportStatus getStatusForImportJob(ImportViewModel importViewModel) {
    String id = importViewModel.getId();
    if (id != null && ACTIVE_JOBS.contains(id)) {
      return ImportStatus.IN_UEBERNAHME;
    }
    return importViewModel.getImportStatus();
  }


  public Set<String> getActiveJobs() {
    return ACTIVE_JOBS;
  }
}
