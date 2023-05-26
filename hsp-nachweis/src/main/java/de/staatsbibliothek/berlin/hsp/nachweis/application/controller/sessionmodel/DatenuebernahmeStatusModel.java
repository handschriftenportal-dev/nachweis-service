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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;

/**
 * Holds all active import jobs taken over
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 08.06.21
 */
@ApplicationScoped
public class DatenuebernahmeStatusModel {

  private static volatile Set<String> ACTIVE_JOBS = Collections.synchronizedSet(new LinkedHashSet<>());

  public void addActiveJob(String id) {
    if (id != null) {
      if (ACTIVE_JOBS.contains(id)) {
        throw new RuntimeException("ImportJob '" + id + "' befindet sich bereits in der Übernahme.");
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
