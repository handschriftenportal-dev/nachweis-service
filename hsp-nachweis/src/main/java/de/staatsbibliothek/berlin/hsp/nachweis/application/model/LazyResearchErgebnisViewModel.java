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

import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Ergebnis;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.ErgebnisEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Page;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Suche;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LazyResearchErgebnisViewModel extends LazyDataModel<ErgebnisEintrag> implements Serializable {

  private static final long serialVersionUID = 5486063921449285270L;

  private static final Logger log = LoggerFactory.getLogger(LazyResearchErgebnisViewModel.class);

  @Getter
  transient Ergebnis ergebnis = null;

  transient Suche suche;

  private transient SuchDokumentBoundary suchDokumentBoundary;

  public LazyResearchErgebnisViewModel(final SuchDokumentBoundary suchDokumentBoundary) {
    this.suchDokumentBoundary = suchDokumentBoundary;
  }

  public List<ErgebnisEintrag> search(final Suche suche) throws SolrServiceException {
    List<ErgebnisEintrag> result = null;

    this.suche = suche;

    ergebnis = suchDokumentBoundary.search(suche);

    if (Objects.nonNull(ergebnis)) {

      result = ergebnis.getEintragListe();

      this.setRowCount(ergebnis.getNOfErgebnis());
    }

    return result;
  }

  @Override
  public ErgebnisEintrag getRowData(String id) {
    ErgebnisEintrag result = null;

    if (Objects.nonNull(ergebnis)) {
      final Optional<ErgebnisEintrag> eintragOptional = ergebnis.getEintragListe().stream()
          .filter(eintrag -> eintrag.getId().equals(id)).findFirst();
      if (eintragOptional.isPresent()) {
        result = eintragOptional.get();
      }
    }

    return result;
  }

  @Override
  public String getRowKey(ErgebnisEintrag eintrag) {
    return eintrag.getId();
  }


  public List<ErgebnisEintrag> load(int first, int pageSize, String sortField, SortOrder sortOrder,
      Map<String, FilterMeta> filterBy) {
    return load(first, pageSize);
  }

  @Override
  public List<ErgebnisEintrag> load(int first, int pageSize, Map<String, SortMeta> sortBy,
      Map<String, FilterMeta> filterBy) {
    return load(first, pageSize);
  }

  private List<ErgebnisEintrag> load(int first, int pageSize) {
    List<ErgebnisEintrag> result = null;

    if (Objects.nonNull(suche)) {
      suche.setPage(new Page(pageSize, first));

      try {
        result = search(suche);
      } catch (SolrServiceException e) {
        log.warn("Unable to obtain search results {}", e.getMessage(), e);
      }
    }

    return result;
  }

}
