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

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.SperreEintragViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.SperrenViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.SperrenViewModel.SperrenViewModelBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.SignatureValue;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 24.08.21
 */

@EqualsAndHashCode(callSuper = true)
@SessionScoped
@Named
@Data
@Slf4j
@LoginCheck
public class SperrenModel extends DataTableStoreController implements Serializable {

  private static final long serialVersionUID = 6966128472762849185L;

  private static final String DATATABLE_WIDGET_VAR = "sperrenTableWidget";
  private static final String ROOT_CLIENT_ID = "sperrenform:sperrenTable";

  private static final String ID_SPERRE_LOESCHEN = "sperreLoeschen";
  private static final String ID_BEARBEITERNAME = "bearbeitername";
  private static final String ID_START_DATUM = "startDatum";
  private static final String ID_SPERRE_TYP = "sperreTyp";
  private static final String ID_SPERRE_GRUND = "sperreGrund";
  private static final String ID_EINTRAGE_SIGNATUREN = "eintraege_signaturen";
  private static final String ID_EINTRAGE_HSPIDS = "eintraege_hspIds";
  private static final String ID_TX_ID = "txId";

  private static final List<String> COLUMNS_INDEX = Arrays.asList(ID_SPERRE_LOESCHEN, ID_BEARBEITERNAME,
      ID_START_DATUM, ID_SPERRE_TYP, ID_SPERRE_GRUND, ID_EINTRAGE_SIGNATUREN, ID_EINTRAGE_HSPIDS,
      ID_TX_ID);
  private static final List<Boolean> COLUMNS_VISIBLE = Arrays.asList(true, true, true, true, true, true,
      true, true);
  private static final List<String> COLUMNS_WIDTH = Arrays.asList("50", "150", "150", "150", "200", "150",
      "200", "200");
  private static final List<SortMeta> INITIAL_SORTING = Collections.singletonList(
      SortMeta.builder().field(ID_BEARBEITERNAME).order(SortOrder.ASCENDING).build());

  private List<SperrenViewModel> allSperren;
  private List<SperrenViewModel> filteredSperren;

  private transient DokumentSperreBoundary dokumentSperreBoundary;
  private transient KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  private transient BeschreibungsBoundary beschreibungsBoundary;

  SperrenModel() {
    super(DATATABLE_WIDGET_VAR, ROOT_CLIENT_ID, COLUMNS_INDEX,
        INITIAL_SORTING, COLUMNS_WIDTH, COLUMNS_VISIBLE, INIT_HIT_PRO_PAGE,
        ALL_POSSIBLE_HIT_PRO_PAGE);
  }

  @Inject
  public SperrenModel(DokumentSperreBoundary dokumentSperreBoundary,
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      BeschreibungsBoundary beschreibungsBoundary) {
    this();
    this.dokumentSperreBoundary = dokumentSperreBoundary;
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
    this.beschreibungsBoundary = beschreibungsBoundary;

    alleSperrenAnzeigen();
  }

  public void alleSperrenAnzeigen() {

    Map<String, SignatureValue> kodSignaturen = kulturObjektDokumentBoundary.getAllSignaturesWithKodIDs();
    Map<String, SignatureValue> beschreibungSignaturen = beschreibungsBoundary.getAllSignaturesWithBeschreibungIDs();

    try {
      this.filteredSperren = null;
      this.allSperren = dokumentSperreBoundary.findAll()
          .stream()
          .map(s -> mapSperre(s, kodSignaturen, beschreibungSignaturen))
          .collect(Collectors.toList());
    } catch (DokumentSperreException e) {
      log.info("Error loading Sperren!", e);
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          "Fehler beim Laden der Sperren!", e.getMessage());
      PrimeFaces.current().dialog().showMessageDynamic(msg);
    }
  }

  public void sperreLoeschen(String id) {
    log.info("Sperre löschen: id={}", id);
    try {
      Sperre sperre = Sperre.newBuilder().withId(id).build();
      boolean success = dokumentSperreBoundary.releaseSperre(sperre);
      log.info("Sperre gelöscht: id={}, success={}", id, success);

      clearAllFilters();
      alleSperrenAnzeigen();
    } catch (DokumentSperreException e) {
      log.info("Error releasing Sperre for id= " + id, e);
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          "Fehler beim Löschen der Sperren!", e.getMessage());
      PrimeFaces.current().dialog().showMessageDynamic(msg);
    }
  }

  protected SperrenViewModel mapSperre(Sperre sperre, Map<String, SignatureValue> kodSignaturen,
      Map<String, SignatureValue> beschreibungSignaturen) {

    List<SperreEintragViewModel> sperreEintragViewModels = mapSperreEintraege(
        sperre.getSperreEintraege(),
        kodSignaturen, beschreibungSignaturen);

    return new SperrenViewModelBuilder()
        .withId(sperre.getId())
        .withBearbeitername(
            Objects.isNull(sperre.getBearbeiter()) ? "unbekannt" : sperre.getBearbeiter().getName())
        .withStartDatum(sperre.getStartDatum())
        .withSperreTyp(sperre.getSperreTyp())
        .withSperreGrund(sperre.getSperreGrund())
        .withTxId(sperre.getTxId())
        .withSperreEintraege(sperreEintragViewModels)
        .build();
  }

  protected List<SperreEintragViewModel> mapSperreEintraege(Set<SperreEintrag> sperreEintraege,
      Map<String, SignatureValue> kodSignaturen,
      Map<String, SignatureValue> beschreibungSignaturen) {

    List<SperreEintragViewModel> sperreEintragViewModels = new ArrayList<>();
    if (Objects.nonNull(sperreEintraege)) {
      sperreEintraege.forEach(se -> {
        SignatureValue signatureValue = null;
        if (SperreDokumentTyp.KOD == se.getTargetType()) {
          signatureValue = kodSignaturen.get(se.getTargetId());
        } else if (SperreDokumentTyp.BESCHREIBUNG == se.getTargetType()) {
          signatureValue = beschreibungSignaturen.get(se.getTargetId());
        }
        String signature =
            Objects.nonNull(signatureValue) ? signatureValue.getSignature() : "Signatur unbekannt";
        sperreEintragViewModels.add(
            new SperreEintragViewModel(se.getTargetId(), se.getTargetType(), signature));
      });
    }
    return sperreEintragViewModels;
  }
}
