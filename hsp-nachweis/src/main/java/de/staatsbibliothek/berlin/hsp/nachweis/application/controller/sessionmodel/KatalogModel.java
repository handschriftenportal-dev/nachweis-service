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

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KatalogRechte.PERMISSION_KATALOGE_ALS_LISTE_ANZEIGEN;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 01.03.2022.
 * @version 1.0
 */

@EqualsAndHashCode(callSuper = true)
@SessionScoped
@Named
@Slf4j
@LoginCheck
public class KatalogModel extends DataTableStoreController implements Serializable {

  public static final String KATALOG_ID = "id";
  public static final String TITLE = "title";
  public static final String AUTOREN = "autoren";
  public static final String PUBLIKATIONS_JAHR = "publikationsJahr";
  public static final String VERLAG = "verlag";
  public static final String LIZENZ_URI = "lizenzUri";
  public static final String ERSTELL_DATUM = "erstellDatum";
  public static final String AENDERUNGS_DATUM = "aenderungsDatum";
  public static final String KATALOG_DATE_PATTERN = "dd.MM.yyyy HH:mm:ss";

  private static final long serialVersionUID = -5088369578028381277L;
  private static final String DATATABLE_WIDGET_VAR = "katalogTableWidget";
  private static final String ROOT_CLIENT_ID = "katalogdatatypeform:katalogTable";
  private static final List<String> COLUMS_INDEX = new ArrayList<>(
      Arrays.asList(KATALOG_ID, TITLE, AUTOREN, PUBLIKATIONS_JAHR, VERLAG, LIZENZ_URI, ERSTELL_DATUM,
          AENDERUNGS_DATUM));
  private static final List<Boolean> COLUMNS_VISABLE = new ArrayList<>(
      Arrays.asList(false, true, true, true, true, true, false, true));
  private static final List<String> COLUMNS_WIDTH = new ArrayList<>(
      Arrays.asList("100", "300", "100", "100", "100", "100", "100", "100"));
  private static final List<SortMeta> INITIAL_SORTING = Collections
      .singletonList(SortMeta.builder().field(AENDERUNGS_DATUM).order(SortOrder.ASCENDING).build());
  private List<Katalog> allKataloge;

  private List<Katalog> allfilteredKataloge;

  private KatalogBoundary katalogBoundary;

  KatalogModel() {
    super(DATATABLE_WIDGET_VAR, ROOT_CLIENT_ID, COLUMS_INDEX,
        INITIAL_SORTING, COLUMNS_WIDTH, COLUMNS_VISABLE, INIT_HIT_PRO_PAGE,
        ALL_POSSIBLE_HIT_PRO_PAGE);
  }

  @Inject
  public KatalogModel(KatalogBoundary katalogBoundary) {
    this();
    this.katalogBoundary = katalogBoundary;
  }

  @PostConstruct
  public void setup() {
    loadAllData();
  }

  public void loadAllData() {
    log.info("Loading Katalog Data ...");

    if (allKataloge == null) {
      allKataloge = new LinkedList<>();
    }

    allKataloge.clear();

    if (katalogBoundary != null) {
      allKataloge.addAll(katalogBoundary.findAll());
    }

    log.info("Loading Katalog Data finished {} ...", allKataloge.size());
  }

  @CheckPermission(permission = PERMISSION_KATALOGE_ALS_LISTE_ANZEIGEN)
  public List<Katalog> getAllKataloge() {
    return allKataloge;
  }

  public List<Katalog> getAllfilteredKataloge() {
    return allfilteredKataloge;
  }

  public void setAllfilteredKataloge(
      List<Katalog> allfilteredKataloge) {
    this.allfilteredKataloge = allfilteredKataloge;
  }

  public String getAutoren(Katalog katalog) {
    if (katalog != null && katalog.getAutoren() != null) {
      return katalog.getAutoren().stream().map(a -> a.getName()).collect(Collectors.joining(","));
    }
    return "";
  }

  public String getVerlag(Katalog katalog) {
    if (katalog != null && katalog.getVerlag() != null) {
      return katalog.getVerlag().getName();
    }

    return "";
  }

  public String getDatumAsString(LocalDateTime localDateTime) {
    if (localDateTime != null) {
      return localDateTime.format(DateTimeFormatter.ofPattern(KATALOG_DATE_PATTERN));
    }
    return "";
  }
}
