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

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.PapierkorbRechte.PERMISSION_PAPIERKORB_ALS_LISTE_ANZEIGEN;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GeloeschtesDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.papierkorb.PapierkorbBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 30.11.22
 */

@EqualsAndHashCode(callSuper = true)
@SessionScoped
@Named
@Slf4j
@LoginCheck
public class PapierkorbModel extends DataTableStoreController implements Serializable {

  private static final long serialVersionUID = 692731738581317028L;

  private static final String DATATABLE_WIDGET_VAR = "papierkorbTableWidget";
  private static final String ROOT_CLIENT_ID = "papierkorbForm:papierkorbTable";
  private static final String ID_ID = "id";
  private static final String ID_DOKUMENT_ID = "dokumentId";
  private static final String ID_DOKUMENT_OBJEKT_TYP = "dokumentObjektTyp";
  private static final String ID_GUELTIGE_SIGNATUR = "gueltigeSignatur";
  private static final String ID_ALTERNATIVE_SIGNATUREN = "alternativeSignaturen";
  private static final String ID_INTERNE_PURLS = "internePurls";
  private static final String ID_BESITZER = "besitzer";
  private static final String ID_AUFBEWAHRUNGSORT = "aufbewahrungsort";
  private static final String ID_BEARBEITER = "bearbeiter";
  private static final String ID_LOESCHDATUM = "loeschdatum";
  private static final String ID_TEIXML = "teiXML";

  private static final List<String> COLUMS_INDEX =
      List.of(ID_ID, ID_DOKUMENT_ID, ID_DOKUMENT_OBJEKT_TYP, ID_GUELTIGE_SIGNATUR, ID_ALTERNATIVE_SIGNATUREN,
          ID_INTERNE_PURLS, ID_BESITZER, ID_AUFBEWAHRUNGSORT, ID_BEARBEITER, ID_LOESCHDATUM, ID_TEIXML);
  private static final List<Boolean> COLUMNS_VISIBLE =
      List.of(false, true, true, true, false, false, true, true, true, true, true);
  private static final List<String> COLUMNS_WIDTH =
      List.of("200", "200", "200", "200", "200", "200", "200", "200", "200", "200", "50");
  private static final List<SortMeta> INITIAL_SORTING
      = Collections.singletonList(SortMeta.builder().field(ID_LOESCHDATUM).order(SortOrder.DESCENDING).build());

  private List<GeloeschtesDokument> allDokumente;

  private List<GeloeschtesDokument> allFilteredDokumente;

  private PapierkorbBoundary papierkorbBoundary;

  PapierkorbModel() {
    super(DATATABLE_WIDGET_VAR, ROOT_CLIENT_ID, COLUMS_INDEX,
        INITIAL_SORTING, COLUMNS_WIDTH, COLUMNS_VISIBLE, INIT_HIT_PRO_PAGE,
        ALL_POSSIBLE_HIT_PRO_PAGE);
  }

  @Inject
  public PapierkorbModel(PapierkorbBoundary papierkorbBoundary) {
    this();
    this.papierkorbBoundary = papierkorbBoundary;
  }

  @PostConstruct
  public void setup() {
    loadAllData();
  }

  public void loadAllData() {
    log.info("Loading Papierkorb data started...");

    if (allDokumente == null) {
      allDokumente = new LinkedList<>();
      allFilteredDokumente = null;
    }

    allDokumente.clear();

    if (papierkorbBoundary != null) {
      allDokumente.addAll(papierkorbBoundary.findAllGeloeschteDokumente());
    }

    log.info("Loading Papierkorb data finished: {} geloeschteDokumente", allDokumente.size());
  }

  @CheckPermission(permission = PERMISSION_PAPIERKORB_ALS_LISTE_ANZEIGEN)
  public List<GeloeschtesDokument> getAllDokumente() {
    return allDokumente;
  }

  public List<GeloeschtesDokument> getAllFilteredDokumente() {
    return allFilteredDokumente;
  }

  public void setAllFilteredDokumente(List<GeloeschtesDokument> allFilteredDokumente) {
    this.allFilteredDokumente = allFilteredDokumente;
  }

}
