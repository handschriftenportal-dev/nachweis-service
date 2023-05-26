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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.common.SolrDocumentList;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 14.02.2020.
 */
@Data
@NoArgsConstructor
public class Ergebnis implements Serializable {

  private static final long serialVersionUID = -8736694651357595261L;

  private int nOfErgebnis = 0;

  private Page page = null;

  private List<ErgebnisEintrag> eintragListe = new ArrayList<>();

  private int facetCountTypeKOD;
  private int facetCountTypeBeschreibung;

  private int facetCountVerwaltungIntern;
  private int facetCountVerwaltungExtern;

  private int facetCountContainsDigitalisat;
  private int facetCountContainsBeschreibung;
  private int facetCountContainsBuchschmuck;
  private int facetCountPubliziert;

  private Map<String, Integer> institutionen = new TreeMap<>();

  private Map<String, Integer> autoren = new TreeMap<>();

  public void addErgebnisEintrag(final ErgebnisEintrag eintrag) {
    eintragListe.add(eintrag);
  }

  public void addErgebnisePageEnries(final SolrDocumentList solrDocuments,
      Page page) {
    if (Objects.nonNull(solrDocuments.getNumFound())) {
      this.setNOfErgebnis((int) solrDocuments.getNumFound());
    }

    if (Objects.nonNull(solrDocuments.getStart())) {
      this.setPage(new Page(page.getNumberOfRows(), (int) solrDocuments.getStart()));
    }
  }

}
