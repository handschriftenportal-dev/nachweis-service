package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

  @Serial
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

  private Map<String, Integer> bearbeiter = new TreeMap<>();

  public void addErgebnisEintrag(final ErgebnisEintrag eintrag) {
    eintragListe.add(eintrag);
  }

  public void addErgebnisePageEnries(final SolrDocumentList solrDocuments,
      Page page) {
    this.setNOfErgebnis((int) solrDocuments.getNumFound());
    this.setPage(new Page(page.getNumberOfRows(), (int) solrDocuments.getStart()));
  }

}
