package de.staatsbibliothek.berlin.hsp.nachweis.application.model.normdatenerkennung;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung.NormdatenErkennungEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.Treffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.Setter;

public class NormdatenErkennungAuftragErgebnisViewModel {

  public static final int MANUAL_SENTINEL = -1;

  @Getter private final String id;
  @Getter private final NormdatenTyp normdatenTyp;
  @Getter private final String displayText;
  @Getter private final List<TrefferViewModel> treffer;
  @Getter @Setter private boolean reviewed;

  private enum ErrorType { NOT_FOUND, TECHNICAL, TYPE_MISMATCH }

  @Getter @Setter private Integer selectedIndex;
  @Getter @Setter private String manualGndId;
  @Getter @Setter private GNDEntityFact resolvedFact;

  private ErrorType lookupError;
  @Getter private String lookupErrorDetail;

  public boolean isResolveError()       { return lookupError == ErrorType.NOT_FOUND; }
  public boolean isTechnicalError()     { return lookupError == ErrorType.TECHNICAL; }
  public boolean isTypeMismatchError()  { return lookupError == ErrorType.TYPE_MISMATCH; }
  public boolean isNoError()            { return lookupError == null; }

  public void setResolveError(String detail)      { lookupError = ErrorType.NOT_FOUND;     lookupErrorDetail = detail; }
  public void setTechnicalError(String detail)    { lookupError = ErrorType.TECHNICAL;     lookupErrorDetail = detail; }
  public void setTypeMismatchError(String detail) { lookupError = ErrorType.TYPE_MISMATCH; lookupErrorDetail = detail; }
  public void clearLookupError()                  { lookupError = null;                    lookupErrorDetail = null; }

  public NormdatenErkennungAuftragErgebnisViewModel(NormdatenErkennungEintrag r) {
    this.id = r.getId();
    this.normdatenTyp = r.getNormdatenTyp();
    this.displayText = r.getDisplayText();
    this.reviewed = r.getUpdatedAt() != null;
    this.treffer = buildTreffer(r);

    switch (r.getSelectionType()) {
      case RECOMMENDATION -> this.selectedIndex = 0;
      case MATCH -> {
        int origIdx = r.getSelectedMatchIndex() != null ? r.getSelectedMatchIndex() : 0;
        this.selectedIndex = this.treffer.stream()
            .filter(t -> t.originalTrefferIndex() == origIdx)
            .findFirst()
            .map(TrefferViewModel::viewIndex)
            .orElse(null);
      }
      case MANUAL -> {
        this.selectedIndex = MANUAL_SENTINEL;
        this.manualGndId = r.getManualIdentifier();
      }
      case NONE -> this.selectedIndex = null;
    }
  }

  public boolean isManualMode() {
    return selectedIndex != null && selectedIndex == MANUAL_SENTINEL;
  }

  public boolean isNoResults() {
    return treffer.isEmpty();
  }

  public TrefferViewModel getSelectedTreffer() {
    if (isManualMode() || selectedIndex == null || selectedIndex < 0) {
      return null;
    }
    if (selectedIndex < treffer.size()) {
      return treffer.get(selectedIndex);
    }
    return null;
  }

  private static List<TrefferViewModel> buildTreffer(NormdatenErkennungEintrag r) {
    var ergebnis = r.getErgebnis();
    if (ergebnis == null) {
      return List.of();
    }
    var list = new ArrayList<TrefferViewModel>();
    var skipId = addEmpfehlung(ergebnis.getEmpfehlung(), list);
    addSortedTreffer(ergebnis.getTreffer(), skipId, list);
    return List.copyOf(list);
  }

  private static String addEmpfehlung(Treffer empfehlung, List<TrefferViewModel> list) {
    if (empfehlung == null) {
      return null;
    }
    list.add(TrefferViewModel.ofEmpfehlung(empfehlung, 0));
    var fact = empfehlung.getGndEntityFact();
    return fact != null ? fact.getId() : null;
  }

  private static void addSortedTreffer(
      List<Treffer> trefferList, String skipId, List<TrefferViewModel> list) {
    if (trefferList == null) {
      return;
    }
    IntStream.range(0, trefferList.size())
        .filter(i -> {
          var fact = trefferList.get(i).getGndEntityFact();
          return skipId == null || fact == null || !skipId.equals(fact.getId());
        })
        .boxed()
        .sorted(Comparator.comparingDouble(i -> {
          var e = trefferList.get(i).getErklaerung();
          return e != null ? -(double) e.konfidenz() : 0.0;
        }))
        .forEach(origIdx ->
            list.add(TrefferViewModel.ofTreffer(trefferList.get(origIdx), list.size(), origIdx)));
  }
}
