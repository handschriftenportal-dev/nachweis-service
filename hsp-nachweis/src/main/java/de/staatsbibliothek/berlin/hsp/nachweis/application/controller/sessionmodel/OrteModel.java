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

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator.GEONAMES_TYPE;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator.GETTY_TYPE;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.NormdatenRechte.PERMISSION_NORMDATEN_ALS_LISTE_ANZEIGEN;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
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
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 12.08.20
 */
@EqualsAndHashCode(callSuper = true)
@SessionScoped
@Named
@Slf4j
@LoginCheck
public class OrteModel extends DataTableStoreController implements Serializable {

  private static final long serialVersionUID = -5448526512008817850L;
  private static final String DATATABLE_WIDGET_VAR = "orteTableWidget";
  private static final String ROOT_CLIENT_ID = "orteform:orteTable";
  private static final String ID_NAME = "name";
  private static final String ID_VARIANTE_NAMEN = "varianteNamen";
  private static final String ID_HSP_ID = "hspID";
  private static final String ID_GNDID = "gndID";
  private static final String ID_GEONAMES_ID = "geonamesID";
  private static final String ID_GETTY_ID = "gettyID";

  private static final List<String> COLUMS_INDEX = new ArrayList<>(
      Arrays.asList(ID_NAME, ID_VARIANTE_NAMEN, ID_HSP_ID, ID_GNDID, ID_GEONAMES_ID, ID_GETTY_ID));
  private static final List<Boolean> COLUMNS_VISABLE = new ArrayList<>(
      Arrays.asList(true, true, true, true, true, true));
  private static final List<String> COLUMNS_WIDTH = new ArrayList<>(
      Arrays.asList("100", "655", "330", "100", "100", "100"));

  private static final List<SortMeta> INITIAL_SORTING = Collections
      .singletonList(SortMeta.builder().field(ID_NAME).order(SortOrder.ASCENDING).build());

  private List<NormdatenReferenz> allOrte;

  private List<NormdatenReferenz> filteredOrte;

  private NormdatenReferenzBoundary normdatenReferenzBoundary;

  OrteModel() {
    super(DATATABLE_WIDGET_VAR, ROOT_CLIENT_ID, COLUMS_INDEX,
        INITIAL_SORTING, COLUMNS_WIDTH, COLUMNS_VISABLE, INIT_HIT_PRO_PAGE, ALL_POSSIBLE_HIT_PRO_PAGE);
  }

  @Inject
  public OrteModel(NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this();
    log.info("Entering constructor..");
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @PostConstruct
  public void setup() {
    allOrte = new ArrayList<>();
    filteredOrte = null;

    Set<NormdatenReferenz> newOrte = normdatenReferenzBoundary
        .findAllByIdOrNameAndType(null, NormdatenReferenz.ORT_TYPE_NAME, true);

    if (Objects.isNull(newOrte) || newOrte.isEmpty()) {
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          "Fehler beim Laden der Orte!", "");
      PrimeFaces.current().dialog().showMessageDynamic(msg);
      return;
    }
    allOrte.addAll(newOrte);
    allOrte.sort(Comparator.comparing(NormdatenReferenz::getName));
  }

  @CheckPermission(permission = PERMISSION_NORMDATEN_ALS_LISTE_ANZEIGEN)
  public List<NormdatenReferenz> getAllOrte() {
    return allOrte;
  }

  public void setAllOrte(List<NormdatenReferenz> allOrte) {
    this.allOrte = allOrte;
  }

  public List<NormdatenReferenz> getFilteredOrte() {
    return filteredOrte;
  }

  public void setFilteredOrte(List<NormdatenReferenz> filteredOrte) {
    this.filteredOrte = filteredOrte;
  }

  public Identifikator findGeonamesIdentifikator(NormdatenReferenz viewModel) {
    return Optional.ofNullable(viewModel)
        .stream()
        .map(ort -> ort.getIdentifikatorByTypeName(GEONAMES_TYPE))
        .flatMap(Collection::stream)
        .findFirst()
        .orElse(null);
  }

  public Identifikator findGettyIdentifikator(NormdatenReferenz viewModel) {
    return Optional.ofNullable(viewModel)
        .stream()
        .map(ort -> ort.getIdentifikatorByTypeName(GETTY_TYPE))
        .flatMap(Collection::stream)
        .findFirst()
        .orElse(null);
  }
}
