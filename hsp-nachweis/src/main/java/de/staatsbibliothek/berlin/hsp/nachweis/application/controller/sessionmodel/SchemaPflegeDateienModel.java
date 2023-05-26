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

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SchemaPflegeRechte.PERMISSION_SCHEMAPFLEGEDATEN_ALS_LISTE_ANZEIGEN;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDateiBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.03.2022
 */

@EqualsAndHashCode(callSuper = true)
@SessionScoped
@Named
@Slf4j
public class SchemaPflegeDateienModel extends DataTableStoreController implements Serializable {

  private static final long serialVersionUID = -4748414698856136333L;
  private static final String DATATABLE_WIDGET_VAR = "schemaPflegeDateienTableWidget";
  private static final String ROOT_CLIENT_ID = "schemapflegedateienform:schemaPflegeDateienTable";
  private static final String ID_DATEI_NAME = "dateiName";
  private static final String ID_DATEI_TYP = "dateiTyp";
  private static final String ID_XML_FORMAT = "xmlFormat";
  private static final String ID_DATEI_ID = "dateiId";
  private static final String ID_BEARBEITERNAME = "bearbeitername";
  private static final String ID_VERSION = "version";
  private static final String ID_AENDERUNGS_DATUM = "aenderungsDatum";
  private static final String ID_DOWNLOAD = "download";

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

  private static final List<String> COLUMS_INDEX = new ArrayList<>(
      Arrays.asList(ID_DATEI_NAME, ID_DATEI_TYP, ID_XML_FORMAT, ID_DATEI_ID, ID_BEARBEITERNAME, ID_VERSION,
          ID_AENDERUNGS_DATUM, ID_DOWNLOAD));
  private static final List<Boolean> COLUMNS_VISIBLE = new ArrayList<>(
      Arrays.asList(true, true, true, true, true, true, true, true));
  private static final List<String> COLUMNS_WIDTH = new ArrayList<>(
      Arrays.asList("200", "100", "100", "200", "150", "100", "100", "50"));

  private static final List<SortMeta> INITIAL_SORTING
      = Collections.singletonList(SortMeta.builder().field(ID_DATEI_NAME).order(SortOrder.ASCENDING).build());

  private SchemaPflegeDateiBoundary schemaResourceDateiBoundary;

  private List<SchemaPflegeDatei> schemaPflegeDateien;
  private List<SchemaPflegeDatei> filteredSchemaPflegeDateien;

  public SchemaPflegeDateienModel() {
    super(DATATABLE_WIDGET_VAR, ROOT_CLIENT_ID, COLUMS_INDEX,
        INITIAL_SORTING, COLUMNS_WIDTH, COLUMNS_VISIBLE, 15,
        ALL_POSSIBLE_HIT_PRO_PAGE);
  }

  @Inject
  public SchemaPflegeDateienModel(SchemaPflegeDateiBoundary schemaResourceDateiBoundary) {
    this();
    this.schemaResourceDateiBoundary = schemaResourceDateiBoundary;
  }

  @PostConstruct
  public void setup() {
    log.debug("Start setup");

    schemaPflegeDateien = new ArrayList(schemaResourceDateiBoundary.findAll());
    schemaPflegeDateien.sort(Comparator.comparing(SchemaPflegeDatei::getDateiName));
    filteredSchemaPflegeDateien = null;
  }

  @CheckPermission(permission = PERMISSION_SCHEMAPFLEGEDATEN_ALS_LISTE_ANZEIGEN)
  public List<SchemaPflegeDatei> getSchemaPflegeDateien() {
    return schemaPflegeDateien;
  }

  public List<SchemaPflegeDatei> getFilteredSchemaPflegeDateien() {
    return filteredSchemaPflegeDateien;
  }

  public void setFilteredSchemaPflegeDateien(List<SchemaPflegeDatei> filteredSchemaPflegeDateien) {
    this.filteredSchemaPflegeDateien = filteredSchemaPflegeDateien;
  }

  public String getAenderungsDatum(SchemaPflegeDatei schemaPflegeDatei) {
    try {
      if (Objects.isNull(schemaPflegeDatei) || Objects.isNull(schemaPflegeDatei.getAenderungsDatum())) {
        return "";
      }
      return schemaPflegeDatei.getAenderungsDatum().format(DATE_TIME_FORMATTER);

    } catch (Exception exception) {
      log.error("Error during getAenderungsDatum  {} {}",
          exception.getMessage(),
          schemaPflegeDatei.getAenderungsDatum());
      return "";
    }
  }

  public StreamedContent loadDatei(String id) {
    log.info("loadDatei: id={}", id);
    Optional<SchemaPflegeDatei> schemaPflegeDatei = schemaResourceDateiBoundary.findById(id);
    if (schemaPflegeDatei.isPresent() && Objects.nonNull(schemaPflegeDatei.get().getDatei())) {

      byte[] data = schemaPflegeDatei.get().getDatei().getBytes(StandardCharsets.UTF_8);
      return DefaultStreamedContent.builder()
          .name(schemaPflegeDatei.get().getDateiName())
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .stream(() -> new ByteArrayInputStream(data))
          .contentLength(data.length)
          .build();
    } else {
      return null;
    }
  }
}
