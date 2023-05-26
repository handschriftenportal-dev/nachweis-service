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

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgang;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgangBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 01.09.20
 */
@EqualsAndHashCode(callSuper = true)
@Named
@ViewScoped
@Slf4j
@Data
@LoginCheck
public class ImportVorgangModel extends DataTableStoreController implements Serializable {

  private static final long serialVersionUID = -9198417990704556695L;

  private static final String DATATABLE_WIDGET_VAR = "vorgaengeTableWidget";
  private static final String ROOT_CLIENT_ID = "importvorgaengeForm:importvorgaengeTable";
  private static final String ID_STATUS_INDICATOR = "statusIndicator";
  private static final String ID_STATUS = "status";
  private static final String ID_DATUM = "datum";
  private static final String ID_DATEN_TYP = "datentyp";
  private static final String ID_DATEINAME = "dateiname";
  private static final String ID_DATENFORMAT = "datenformat";
  private static final String ID_BENUTZER_NAME = "benutzerName";
  private static final String ID_FEHLER = "fehler";

  private static final List<String> COLUMS_INDEX = new ArrayList<>(
      Arrays.asList(ID_STATUS_INDICATOR, ID_STATUS, ID_DATUM, ID_DATEN_TYP, ID_DATEINAME,
          ID_DATENFORMAT,
          ID_BENUTZER_NAME,
          ID_FEHLER));
  private static final List<Boolean> COLUMNS_VISABLE = new ArrayList<>(
      Arrays.asList(true, true, true, true, true, true, true, false));
  private static final List<String> COLUMNS_WIDTH = new ArrayList<>(
      Arrays.asList("20", "100", "100", "100", "300", "100", "100", "100"));

  private static final List<SortMeta> INITIAL_SORTING = Collections
      .singletonList(SortMeta.builder().field(ID_DATUM).order(SortOrder.DESCENDING).build());

  private ImportVorgangBoundary importVorgangBoundary;

  private List<ImportVorgang> vorgaenge;

  private List<ImportVorgang> filteredVorgaenge;

  private BearbeiterBoundary bearbeiterBoundary;

  ImportVorgangModel() {
    super(DATATABLE_WIDGET_VAR, ROOT_CLIENT_ID, COLUMS_INDEX,
        INITIAL_SORTING, COLUMNS_WIDTH, COLUMNS_VISABLE, INIT_HIT_PRO_PAGE,
        ALL_POSSIBLE_HIT_PRO_PAGE);
  }

  @Inject
  public ImportVorgangModel(ImportVorgangBoundary importVorgangBoundary,
      BearbeiterBoundary bearbeiterBoundary) {
    this();
    this.importVorgangBoundary = importVorgangBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  @PostConstruct
  public void loadAllVorgaenge() {
    List<ImportVorgang> all = new ArrayList<>(importVorgangBoundary.findAll());
    if (vorgaenge == null) {
      vorgaenge = new ArrayList<>(all);
    } else {
      vorgaenge.clear();
      vorgaenge.addAll(all);
    }

    vorgaenge.sort(Comparator.comparing(ImportVorgang::getDatum));
    filteredVorgaenge = null;
  }

  public List<ImportVorgang> getFilteredVorgaenge() {
    return filteredVorgaenge;
  }

  public void setFilteredVorgaenge(List<ImportVorgang> filteredVorgaenge) {
    this.filteredVorgaenge = filteredVorgaenge;
  }

  public String mapBenutzernameToBearbeitername(String benutzerName) {
    Bearbeiter bearbeiter = bearbeiterBoundary.findBearbeiterWithName(benutzerName);

    if (bearbeiter == null) {
      bearbeiter = bearbeiterBoundary.getUnbekannterBearbeiter();
    }

    if (bearbeiter.getVorname() != null && bearbeiter.getNachname() != null) {
      return bearbeiter.getVorname() + " " + bearbeiter.getNachname();
    } else {
      return bearbeiter.getBearbeitername();
    }
  }
}
