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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 * Author: Christoph Marten on 10.03.2021 at 13:03
 */
@EqualsAndHashCode(callSuper = true)
@SessionScoped
@Named
@Slf4j
public class PersonModel extends DataTableStoreController implements Serializable {

  private static final long serialVersionUID = 9087770245166202224L;
  private static final String DATATABLE_WIDGET_VAR = "personTableWidget";
  private static final String ROOT_CLIENT_ID = "personenform:personenTable";
  private static final String ID_NAME = "name";
  private static final String ID_VARIANTE_NAMEN = "varianteNamen";
  private static final String ID_PERSON_ID = "personId";
  private static final String ID_GNDID = "gndID";
  private static final List<String> COLUMS_INDEX = new ArrayList<>(
      Arrays
          .asList(ID_NAME, ID_VARIANTE_NAMEN, ID_PERSON_ID, ID_GNDID));
  private static final List<Boolean> COLUMNS_VISIBLE = new ArrayList<>(
      Arrays.asList(true, true, true, true));
  private static final List<String> COLUMNS_WIDTH = new ArrayList<>(
      Arrays.asList("300", "655", "250", "100"));
  private static final List<SortMeta> INITIAL_SORTING
      = Collections.singletonList(SortMeta.builder().field(ID_NAME).order(SortOrder.ASCENDING).build());

  private transient NormdatenReferenzBoundary normdatenReferenzBoundary;

  private transient List<NormdatenReferenz> normdatenPersonViews;
  private transient List<NormdatenReferenz> filteredPersons;

  PersonModel() {
    super(DATATABLE_WIDGET_VAR, ROOT_CLIENT_ID, COLUMS_INDEX,
        INITIAL_SORTING, COLUMNS_WIDTH, COLUMNS_VISIBLE, INIT_HIT_PRO_PAGE,
        ALL_POSSIBLE_HIT_PRO_PAGE);
  }

  @Inject
  public PersonModel(NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this();
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @PostConstruct
  public void setup() {
    log.debug("Start setup");

    Set<NormdatenReferenz> allPersons = normdatenReferenzBoundary
        .findAllByIdOrNameAndType(null, NormdatenReferenz.PERSON_TYPE_NAME, true);

    normdatenPersonViews = new ArrayList<>(allPersons);

    normdatenPersonViews.sort(Comparator.comparing(NormdatenReferenz::getName,
        Comparator.nullsFirst(Comparator.naturalOrder())));
    filteredPersons = null;
  }

  public List<NormdatenReferenz> getNormdatenPersonViews() {
    return normdatenPersonViews;
  }

  public void setNormdatenPersonViews(List<NormdatenReferenz> normdatenPersonViews) {
    this.normdatenPersonViews = normdatenPersonViews;
  }

  public List<NormdatenReferenz> getFilteredPersons() {
    return filteredPersons;
  }

  public void setFilteredPersons(List<NormdatenReferenz> filteredPersons) {
    this.filteredPersons = filteredPersons;
  }

}
