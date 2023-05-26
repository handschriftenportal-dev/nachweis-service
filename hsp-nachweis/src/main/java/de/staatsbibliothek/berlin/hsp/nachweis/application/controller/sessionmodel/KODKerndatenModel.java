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

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.ABMESSUNG;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.BESCHREIBSTOFF;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.BUCHSCHMUCK;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.ENTSTEHUNGSORT;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.ENTSTEHUNGSZEIT;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.FORMAT;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.GRUNDSPRACHE;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.HANDSCHRIFTENTYP;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.MUSIKNOTATION;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.STATUS;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.TITEL;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.UMFANG;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEI2AttributsReferenzMapper;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@ViewScoped
@Named("kodKerndatenModel")
@Slf4j
@Getter
public class KODKerndatenModel implements Serializable {

  private static final long serialVersionUID = 8216239612948138106L;
  private static final String BESCHREIBUNG_RESET_ID = "RESET";

  @Getter(AccessLevel.NONE)
  private final Map<AttributsTyp, TypenToWerteMapConsumer<? extends AttributsReferenz>> typenToWerteMap
      = new EnumMap<>(AttributsTyp.class);

  private final Map<String, Titel> titel = new LinkedHashMap<>();
  private final Map<String, Beschreibstoff> beschreibstoffe = new LinkedHashMap<>();
  private final Map<String, Umfang> umfange = new LinkedHashMap<>();
  private final Map<String, Abmessung> abmessungen = new LinkedHashMap<>();
  private final Map<String, Format> formate = new LinkedHashMap<>();
  private final Map<String, Entstehungsort> entstehungsorte = new LinkedHashMap<>();
  private final Map<String, Entstehungszeit> entstehungszeiten = new LinkedHashMap<>();
  private final Map<String, Grundsprache> grundsprachen = new LinkedHashMap<>();
  private final Map<String, Buchschmuck> buchschmuck = new LinkedHashMap<>();
  private final Map<String, Handschriftentyp> handschriftentypen = new LinkedHashMap<>();
  private final Map<String, Musiknotation> musiknotationen = new LinkedHashMap<>();
  private final Map<String, Status> status = new LinkedHashMap<>();

  private final Map<AttributsTyp, String> originalIds = new EnumMap<>(AttributsTyp.class);
  private final Map<AttributsTyp, String> selectedIds = new EnumMap<>(AttributsTyp.class);
  private final List<String> bestaetigungsMeldungen = new ArrayList<>();

  private Map<String, BeschreibungsViewModel> beschreibungen;

  private boolean editable;
  @Setter
  private String selectedBeschreibungId;

  public KODKerndatenModel() {
    typenToWerteMap.put(TITEL, new TypenToWerteMapConsumer<>(Titel.class, titel));
    typenToWerteMap.put(BESCHREIBSTOFF, new TypenToWerteMapConsumer<>(Beschreibstoff.class, beschreibstoffe));
    typenToWerteMap.put(UMFANG, new TypenToWerteMapConsumer<>(Umfang.class, umfange));
    typenToWerteMap.put(ABMESSUNG, new TypenToWerteMapConsumer<>(Abmessung.class, abmessungen));
    typenToWerteMap.put(FORMAT, new TypenToWerteMapConsumer<>(Format.class, formate));
    typenToWerteMap.put(ENTSTEHUNGSZEIT, new TypenToWerteMapConsumer<>(Entstehungszeit.class, entstehungszeiten));
    typenToWerteMap.put(ENTSTEHUNGSORT, new TypenToWerteMapConsumer<>(Entstehungsort.class, entstehungsorte));
    typenToWerteMap.put(GRUNDSPRACHE, new TypenToWerteMapConsumer<>(Grundsprache.class, grundsprachen));
    typenToWerteMap.put(BUCHSCHMUCK, new TypenToWerteMapConsumer<>(Buchschmuck.class, buchschmuck));
    typenToWerteMap.put(HANDSCHRIFTENTYP, new TypenToWerteMapConsumer<>(Handschriftentyp.class, handschriftentypen));
    typenToWerteMap.put(MUSIKNOTATION, new TypenToWerteMapConsumer<>(Musiknotation.class, musiknotationen));
    typenToWerteMap.put(STATUS, new TypenToWerteMapConsumer<>(Status.class, status));
  }

  public void init(KulturObjektDokumentViewModel kodViewModel) {
    if (Objects.nonNull(kodViewModel)) {
      log.info("init: kodId={}", kodViewModel.getId());

      this.originalIds.clear();
      this.selectedIds.clear();
      this.titel.clear();
      this.beschreibstoffe.clear();
      this.umfange.clear();
      this.abmessungen.clear();
      this.formate.clear();
      this.entstehungsorte.clear();
      this.entstehungszeiten.clear();
      this.grundsprachen.clear();
      this.buchschmuck.clear();
      this.handschriftentypen.clear();
      this.musiknotationen.clear();
      this.status.clear();

      this.beschreibungen = initBeschreibungen(kodViewModel.getBeschreibungen());
      this.editable = this.beschreibungen.size() > 0;

      if(this.editable) {
        BeschreibungsViewModel resetModel = new BeschreibungsViewModel.BeschreibungsViewBuilder()
            .withID(BESCHREIBUNG_RESET_ID)
            .withTitel(getMessage("kod_attributes_resetvalues")).build();
        this.beschreibungen.put(BESCHREIBUNG_RESET_ID,resetModel);
        this.beschreibungen = this.beschreibungen.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(
                Comparator.comparing(BeschreibungsViewModel::getTitel)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));
      }

      initIds(TITEL, getAttributsReferenzId(kodViewModel.getTitel()));
      initIds(BESCHREIBSTOFF, getAttributsReferenzId(kodViewModel.getBeschreibstoff()));
      initIds(UMFANG, getAttributsReferenzId(kodViewModel.getUmfang()));
      initIds(ABMESSUNG, getAttributsReferenzId(kodViewModel.getAbmessung()));
      initIds(FORMAT, getAttributsReferenzId(kodViewModel.getFormat()));
      initIds(ENTSTEHUNGSORT, getAttributsReferenzId(kodViewModel.getEntstehungsort()));
      initIds(ENTSTEHUNGSZEIT, getAttributsReferenzId(kodViewModel.getEntstehungszeit()));
      initIds(GRUNDSPRACHE, getAttributsReferenzId(kodViewModel.getGrundsprache()));
      initIds(HANDSCHRIFTENTYP, getAttributsReferenzId(kodViewModel.getHandschriftentyp()));
      initIds(STATUS, getAttributsReferenzId(kodViewModel.getStatus()));
      initIds(BUCHSCHMUCK, getAttributsReferenzId(kodViewModel.getBuchschmuck()));
      initIds(MUSIKNOTATION, getAttributsReferenzId(kodViewModel.getMusiknotation()));
    }
  }

  public void bearbeiten() {
    log.debug("bearbeiten called");

    this.beschreibungen.values().stream()
        .filter(b -> !BESCHREIBUNG_RESET_ID.equals(b.getId()))
        .map(this::mapAttributsReferenzen)
        .forEach(this::addAttributsReferenzenToWerte);
  }

  public void resetSelectedBeschreibungsID() {
    this.selectedBeschreibungId = null;
  }

  public void beschreibungswerteUebernehmen(AjaxBehaviorEvent event) {
    log.info("beschreibungswerteUebernehmen called: {} {}" , event,Objects.nonNull(this.selectedBeschreibungId));
    if (Objects.nonNull(this.selectedBeschreibungId) && !this.selectedBeschreibungId.isEmpty()) {
      if(BESCHREIBUNG_RESET_ID.equals(this.selectedBeschreibungId)) {
        selectedIds.clear();
      }else {
        selectedIds.put(TITEL, findReferenzIdForBeschreibungId(selectedBeschreibungId, titel));
        selectedIds.put(BESCHREIBSTOFF,
            findReferenzIdForBeschreibungId(selectedBeschreibungId, beschreibstoffe));
        selectedIds.put(UMFANG, findReferenzIdForBeschreibungId(selectedBeschreibungId, umfange));
        selectedIds.put(ABMESSUNG,
            findReferenzIdForBeschreibungId(selectedBeschreibungId, abmessungen));
        selectedIds.put(FORMAT, findReferenzIdForBeschreibungId(selectedBeschreibungId, formate));
        selectedIds.put(ENTSTEHUNGSORT,
            findReferenzIdForBeschreibungId(selectedBeschreibungId, entstehungsorte));
        selectedIds.put(ENTSTEHUNGSZEIT,
            findReferenzIdForBeschreibungId(selectedBeschreibungId, entstehungszeiten));
        selectedIds.put(GRUNDSPRACHE,
            findReferenzIdForBeschreibungId(selectedBeschreibungId, grundsprachen));
        selectedIds.put(HANDSCHRIFTENTYP,
            findReferenzIdForBeschreibungId(selectedBeschreibungId, handschriftentypen));
        selectedIds.put(STATUS, findReferenzIdForBeschreibungId(selectedBeschreibungId, status));
        selectedIds.put(BUCHSCHMUCK,
            findReferenzIdForBeschreibungId(selectedBeschreibungId, buchschmuck));
        selectedIds.put(MUSIKNOTATION,
            findReferenzIdForBeschreibungId(selectedBeschreibungId, musiknotationen));
      }
    }
  }

  public boolean isSelectionChanged() {
    return !Arrays.stream(AttributsTyp.values())
        .allMatch(typ -> Objects.equals(selectedIds.get(typ), originalIds.get(typ)));
  }
  public void kerndatenBestaetigen() {
    bestaetigungsMeldungen.clear();

    addBestaetigungsMeldung("kod_detail_status", "kod_kerndaten_status_", STATUS, status);
    addBestaetigungsMeldung("kod_detail_titel", null, TITEL, titel);
    addBestaetigungsMeldung("kod_detail_beschreibstoff", null, BESCHREIBSTOFF, beschreibstoffe);
    addBestaetigungsMeldung("kod_detail_umfang", null, UMFANG, umfange);
    addBestaetigungsMeldung("kod_detail_abmessungen", null, ABMESSUNG, abmessungen);
    addBestaetigungsMeldung("kod_detail_format", "kod_kerndaten_format_", FORMAT, formate);
    addBestaetigungsMeldung("kod_detail_entstehungsort", null, ENTSTEHUNGSORT, entstehungsorte);
    addBestaetigungsMeldung("kod_detail_entstehungszeit", null, ENTSTEHUNGSZEIT, entstehungszeiten);
    addBestaetigungsMeldung("kod_detail_grundsprache", null, GRUNDSPRACHE, grundsprachen);
    addBestaetigungsMeldung("kod_detail_handschriftentyp", "kod_kerndaten_handschriftentyp_",
        HANDSCHRIFTENTYP, handschriftentypen);
    addBestaetigungsMeldung("kod_detail_buchschmuck", "kod_kerndaten_buchschmuck_",
        BUCHSCHMUCK, buchschmuck);
    addBestaetigungsMeldung("kod_detail_musiknotation", "kod_kerndaten_musiknotation_",
        MUSIKNOTATION, musiknotationen);
  }

  public void kerndatenUebernehmen(KulturObjektDokumentViewModel kodViewModel) {
    if (Objects.nonNull(kodViewModel)) {
      log.debug("kerndatenUebernehmen: kodId={}", kodViewModel.getId());

      kodViewModel.setTitel(titel.get(selectedIds.get(TITEL)));
      kodViewModel.setBeschreibstoff(beschreibstoffe.get(selectedIds.get(BESCHREIBSTOFF)));
      kodViewModel.setUmfang(umfange.get(selectedIds.get(UMFANG)));
      kodViewModel.setAbmessung(abmessungen.get(selectedIds.get(ABMESSUNG)));
      kodViewModel.setFormat(formate.get(selectedIds.get(FORMAT)));
      kodViewModel.setEntstehungsort(entstehungsorte.get(selectedIds.get(ENTSTEHUNGSORT)));
      kodViewModel.setEntstehungszeit(entstehungszeiten.get(selectedIds.get(ENTSTEHUNGSZEIT)));
      kodViewModel.setGrundsprache(grundsprachen.get(selectedIds.get(GRUNDSPRACHE)));
      kodViewModel.setBuchschmuck(buchschmuck.get(selectedIds.get(BUCHSCHMUCK)));
      kodViewModel.setHandschriftentyp(handschriftentypen.get(selectedIds.get(HANDSCHRIFTENTYP)));
      kodViewModel.setMusiknotation(musiknotationen.get(selectedIds.get(MUSIKNOTATION)));
      kodViewModel.setStatus(status.get(selectedIds.get(STATUS)));
    }
  }

  void initIds(AttributsTyp attributsTyp, String id) {
    this.originalIds.put(attributsTyp, id);
    this.selectedIds.put(attributsTyp, id);
  }

  <T extends AttributsReferenz> void addBestaetigungsMeldung(String typKey, String valueKey, AttributsTyp attributsTyp,
      Map<String, T> werte) {

    String typText = I18NController.getMessage(typKey);
    T originalReferenz = werte.get(originalIds.get(attributsTyp));
    T selectedReferenz = werte.get(selectedIds.get(attributsTyp));

    if (Objects.isNull(originalReferenz) && !Objects.isNull(selectedReferenz)) {
      BeschreibungsViewModel selectedBeschreibung = beschreibungen.get(selectedReferenz.getReferenzId());
      String selectedValue = formatValue(valueKey, selectedReferenz.getText());
      String selectedPubDatum = I18NController.formatDate(selectedBeschreibung.getDatumErstpublikation());
      String selectedAutoren = selectedBeschreibung.getFormattedAutoren();

      bestaetigungsMeldungen.add(I18NController.getMessage("kod_kerndaten_referenz_uebernehmen",
          typText, selectedValue, selectedPubDatum, selectedAutoren));

    } else if (!Objects.isNull(originalReferenz) && Objects.isNull(selectedReferenz)) {
      BeschreibungsViewModel originalBeschreibung = beschreibungen.get(originalReferenz.getReferenzId());
      String originalValue = formatValue(valueKey, originalReferenz.getText());
      String originalPubDatum = I18NController.formatDate(originalBeschreibung.getDatumErstpublikation());
      String originalAutoren = originalBeschreibung.getFormattedAutoren();

      bestaetigungsMeldungen.add(I18NController.getMessage("kod_kerndaten_referenz_entfernen",
          typText, originalValue, originalPubDatum, originalAutoren));

    } else if (!Objects.isNull(originalReferenz) && !Objects.equals(originalReferenz, selectedReferenz)) {
      BeschreibungsViewModel selectedBeschreibung = beschreibungen.get(selectedReferenz.getReferenzId());
      String selectedValue = formatValue(valueKey, selectedReferenz.getText());
      String selectedPubDatum = I18NController.formatDate(selectedBeschreibung.getDatumErstpublikation());
      String selectedAutoren = selectedBeschreibung.getFormattedAutoren();

      BeschreibungsViewModel originalBeschreibung = beschreibungen.get(originalReferenz.getReferenzId());
      String originalValue = formatValue(valueKey, originalReferenz.getText());
      String originalPubDatum = I18NController.formatDate(originalBeschreibung.getDatumErstpublikation());
      String originalAutoren = originalBeschreibung.getFormattedAutoren();

      bestaetigungsMeldungen.add(I18NController.getMessage("kod_kerndaten_referenz_ersetzen",
          typText, originalValue, originalPubDatum, originalAutoren, selectedValue, selectedPubDatum, selectedAutoren));
    }
  }

  Map<String, BeschreibungsViewModel> initBeschreibungen(List<BeschreibungsViewModel> beschreibungsViewModels) {
    return Stream.ofNullable(beschreibungsViewModels)
        .flatMap(List::stream)
        .filter(beschreibung -> VerwaltungsTyp.EXTERN == beschreibung.getVerwaltungsTyp())
        .collect(Collectors.toMap(BeschreibungsViewModel::getId, Function.identity()));
  }

  Map<AttributsTyp, AttributsReferenz> mapAttributsReferenzen(BeschreibungsViewModel beschreibung) {
    try {
      return TEI2AttributsReferenzMapper.map(beschreibung.getTeiXML());
    } catch (Exception e) {
      log.error("Error in findAttributsReferenzen for beschreibung " + beschreibung.getId(), e);
      if (Objects.nonNull(FacesContext.getCurrentInstance())) {
        FacesContext.getCurrentInstance().addMessage("globalmessages",
            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                getMessage("kod_detail_attributsreferenzen_fehler", beschreibung.getId()),
                e.getMessage()));
      }
      return Collections.emptyMap();
    }
  }

  <A extends AttributsReferenz> String findReferenzIdForBeschreibungId(String beschreibungId,
      Map<String, A> referenzen) {
    return referenzen.values().stream()
        .filter(referenz -> beschreibungId.equals(referenz.getReferenzId()))
        .findFirst()
        .map(A::getId)
        .orElse("");
  }

  String getAttributsReferenzId(AttributsReferenz attributsReferenz) {
    return Optional.ofNullable(attributsReferenz)
        .map(AttributsReferenz::getId)
        .orElse("");
  }

  void addAttributsReferenzenToWerte(Map<AttributsTyp, AttributsReferenz> attributsReferenzen) {
    attributsReferenzen.forEach((key, value) ->
        Optional.ofNullable(this.typenToWerteMap.get(key))
            .ifPresent(consumer -> consumer.accept(value)));
  }

  private String formatValue(String textKey, String text) {
    if (Stream.of(textKey, text).anyMatch(StringUtils::isEmpty)) {
      return text;
    }

    return Arrays.stream(text.split(";"))
        .map(subText -> I18NController.getMessage(textKey + subText))
        .collect(Collectors.joining("; "));
  }

  private static class TypenToWerteMapConsumer<T extends AttributsReferenz> implements
      Consumer<AttributsReferenz> {

    private final Class<T> clazz;
    private final Map<String, T> werteMap;

    public TypenToWerteMapConsumer(Class<T> clazz, Map<String, T> werteMap) {
      this.clazz = clazz;
      this.werteMap = werteMap;
    }

    @Override
    public void accept(AttributsReferenz attributsReferenz) {
      werteMap.put(attributsReferenz.getId(), clazz.cast(attributsReferenz));
    }

  }

}
