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

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator.ISO_639_1_TYPE;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator.ISO_639_2_TYPE;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
 * @author Konstantin.Goerlitz@sbb.spk-berlin.de
 * @since 08.07.21
 */
@EqualsAndHashCode(callSuper = true)
@SessionScoped
@Named
@Data
@Slf4j
@LoginCheck
public class SpracheModel extends DataTableStoreController implements Serializable {

  private static final long serialVersionUID = 974997770394181009L;

  private static final String DATATABLE_WIDGET_VAR = "spracheTableWidget";
  private static final String ROOT_CLIENT_ID = "spracheform:spracheTable";

  private static final String ID_NAME = "name";
  private static final String ID_VARIANTE_NAMEN = "varianteNamen";
  private static final String ID_SPRACHE_ID = "spracheId";
  private static final String ID_GNDID = "gndID";
  private static final String ID_ISO_6391 = "iso6391";
  private static final String ID_ISO_6392 = "iso6392";

  private static final List<String> COLUMS_INDEX = new ArrayList<>(
      Arrays.asList(ID_NAME, ID_VARIANTE_NAMEN, ID_SPRACHE_ID, ID_GNDID,
          ID_ISO_6391, ID_ISO_6392));

  private static final List<Boolean> COLUMNS_VISABLE = new ArrayList<>(
      Arrays.asList(true, true, true, true, true));

  private static final List<String> COLUMNS_WIDTH = new ArrayList<>(
      Arrays.asList("150", "450", "200", "100", "100", "100"));

  private static final List<SortMeta> INITIAL_SORTING = Collections.singletonList(
      SortMeta.builder().field(ID_NAME).order(SortOrder.ASCENDING).build());

  private transient List<NormdatenReferenz> allSprachen = new ArrayList<>();

  private transient List<NormdatenReferenz> filteredSprachen;

  private transient NormdatenReferenzBoundary normdatenReferenzBoundary;

  SpracheModel() {
    super(DATATABLE_WIDGET_VAR, ROOT_CLIENT_ID, COLUMS_INDEX,
        INITIAL_SORTING, COLUMNS_WIDTH, COLUMNS_VISABLE, INIT_HIT_PRO_PAGE,
        ALL_POSSIBLE_HIT_PRO_PAGE);
  }

  @Inject
  public SpracheModel(NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this();
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @PostConstruct
  void setup() {
    allSprachen.clear();
    filteredSprachen = null;

    Set<NormdatenReferenz> newSprachen = normdatenReferenzBoundary
        .findAllByIdOrNameAndType(null, NormdatenReferenz.SPRACHE_TYPE_NAME, true);

    if (Objects.isNull(newSprachen) || newSprachen.isEmpty()) {
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          "Fehler beim laden der Sprachen!", "");
      PrimeFaces.current().dialog().showMessageDynamic(msg);
      return;
    }
    allSprachen.addAll(newSprachen);
    allSprachen.sort(Comparator.comparing(NormdatenReferenz::getName));
  }

  public Set<Identifikator> iso6391Identifikatoren(NormdatenReferenz normdatenSprache) {
    return Optional.ofNullable(normdatenSprache)
        .map(sprache -> sprache.getIdentifikatorByTypeName(ISO_639_1_TYPE))
        .orElse(Collections.emptySet());
  }

  public Set<Identifikator> iso6392Identifikatoren(NormdatenReferenz normdatenSprache) {
    return Optional.ofNullable(normdatenSprache)
        .map(sprache -> sprache.getIdentifikatorByTypeName(ISO_639_2_TYPE))
        .orElse(Collections.emptySet());
  }

  public String identifikatorenAsString(Set<Identifikator> identifikatoren) {
    return identifikatoren.stream()
        .map(Identifikator::getText)
        .collect(Collectors.joining(" "));
  }

  public List<NormdatenReferenz> getAllSprachen() {
    return allSprachen;
  }

  public void setAllSprachen(List<NormdatenReferenz> allSprachen) {
    this.allSprachen = allSprachen;
  }

  public List<NormdatenReferenz> getFilteredSprachen() {
    return filteredSprachen;
  }

  public void setFilteredSprachen(List<NormdatenReferenz> filteredSprachen) {
    this.filteredSprachen = filteredSprachen;
  }
}
