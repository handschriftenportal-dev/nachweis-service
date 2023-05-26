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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 21.08.20
 */
@EqualsAndHashCode(callSuper = true)
@SessionScoped
@Named
@Data
@Slf4j
@LoginCheck
public class KoerperschaftModel extends DataTableStoreController implements Serializable {

  private static final long serialVersionUID = 6627873585723268521L;

  private static final String DATATABLE_WIDGET_VAR = "koerperschaftTableWidget";
  private static final String ROOT_CLIENT_ID = "koerperschaftform:koerperschaftTable";

  private static final String ID_NAME = "name";
  private static final String ID_VARIANTE_NAMEN = "varianteNamen";
  private static final String ID_KOERPERSCHAFT_ID = "koerperschaftId";
  private static final String ID_GNDID = "gndID";
  private static final String ID_ISIL_ID = "isilID";

  private static final List<String> COLUMS_INDEX = new ArrayList<>(
      Arrays.asList(ID_NAME, ID_VARIANTE_NAMEN, ID_KOERPERSCHAFT_ID, ID_GNDID, ID_ISIL_ID));

  private static final List<Boolean> COLUMNS_VISABLE = new ArrayList<>(
      Arrays.asList(true, true, true, true, true));

  private static final List<String> COLUMNS_WIDTH = new ArrayList<>(
      Arrays
          .asList("300", "655", "150", "100", "100"));

  private static final List<SortMeta> INITIAL_SORTING = Collections.singletonList(
      SortMeta.builder().field(ID_NAME).order(SortOrder.ASCENDING).build());

  private transient NormdatenReferenzBoundary normdatenReferenzBoundary;

  private List<NormdatenReferenz> allKoerperschaften;

  private List<NormdatenReferenz> filteredKoerperschaften;

  KoerperschaftModel() {
    super(DATATABLE_WIDGET_VAR, ROOT_CLIENT_ID, COLUMS_INDEX,
        INITIAL_SORTING, COLUMNS_WIDTH, COLUMNS_VISABLE, INIT_HIT_PRO_PAGE, ALL_POSSIBLE_HIT_PRO_PAGE);
  }

  @Inject
  public KoerperschaftModel(NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this();
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @PostConstruct
  void setup() {
    allKoerperschaften = new ArrayList<>();
    filteredKoerperschaften = null;

    Set<NormdatenReferenz> newKoerperschaften = normdatenReferenzBoundary
        .findAllByIdOrNameAndType(null, NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME, true);

    if (Objects.isNull(newKoerperschaften) || newKoerperschaften.isEmpty()) {
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          "Fehler beim Laden der Körperschaften!", "");
      PrimeFaces.current().dialog().showMessageDynamic(msg);
      return;
    }

    allKoerperschaften.addAll(newKoerperschaften);
    allKoerperschaften.sort(Comparator.comparing(NormdatenReferenz::getName));
  }

  public Identifikator findIsil(NormdatenReferenz model) {
    return Optional.ofNullable(model)
        .stream()
        .map(sprache -> sprache.getIdentifikatorByTypeName(Identifikator.ISIL_IDENTIFIER_TYPE))
        .flatMap(Collection::stream)
        .findFirst()
        .orElse(null);
  }

  public List<NormdatenReferenz> getAllKoerperschaften() {
    return allKoerperschaften;
  }

  public void setAllKoerperschaften(List<NormdatenReferenz> allKoerperschaften) {
    this.allKoerperschaften = allKoerperschaften;
  }

  public List<NormdatenReferenz> getFilteredKoerperschaften() {
    return filteredKoerperschaften;
  }

  public void setFilteredKoerperschaften(List<NormdatenReferenz> filteredKoerperschaften) {
    this.filteredKoerperschaften = filteredKoerperschaften;
  }
}
