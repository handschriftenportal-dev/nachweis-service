package de.staatsbibliothek.berlin.hsp.nachweis.application.model.normdatenerkennung;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.Erklaerung;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.Treffer;

public record TrefferViewModel(
    int viewIndex,
    int originalTrefferIndex,
    String preferredName,
    String identifier,
    int konfidenzProzent,
    String erklaerungsTyp,
    boolean empfehlung,
    GNDEntityFact gndEntityFact,
    Erklaerung erklaerung,
    String label,
    String quelle,
    int grundwertProzent,
    int wertreduktionProzent
) {

  public static TrefferViewModel ofEmpfehlung(Treffer treffer, int viewIndex) {
    return of(treffer, viewIndex, -1, true);
  }

  public static TrefferViewModel ofTreffer(Treffer treffer, int viewIndex, int originalTrefferIndex) {
    return of(treffer, viewIndex, originalTrefferIndex, false);
  }

  private static TrefferViewModel of(Treffer treffer, int viewIndex, int originalTrefferIndex,
      boolean empfehlung) {
    var fact = treffer.getGndEntityFact();
    var erkl = treffer.getErklaerung();
    var preferredName = fact != null ? fact.getPreferredName() : "";
    var identifier = resolveIdentifier(fact);
    var konfidenzProzent = erkl != null ? Math.round(erkl.konfidenz() * 100) : 0;
    var grundwertProzent = erkl != null ? Math.round(erkl.grundwert() * 100) : 0;
    var wertreduktionProzent = erkl != null ? Math.round(erkl.wertreduktion() * 100) : 0;
    var label = (empfehlung ? "\u2605 " : "") + preferredName + " (" + identifier + ")"
        + (konfidenzProzent > 0 ? "  " + konfidenzProzent + "%" : "");
    var quelle = erkl != null ? erkl.quelle() : null;
    return new TrefferViewModel(
        viewIndex,
        originalTrefferIndex,
        preferredName,
        identifier,
        konfidenzProzent,
        erkl != null ? erkl.typ() : null,
        empfehlung,
        fact,
        erkl,
        label,
        quelle,
        grundwertProzent,
        wertreduktionProzent
    );
  }

  public int getViewIndex() { return viewIndex; }
  public String getPreferredName() { return preferredName; }
  public String getIdentifier() { return identifier; }
  public int getKonfidenzProzent() { return konfidenzProzent; }
  public String getErklaerungsTyp() { return erklaerungsTyp; }
  public boolean isEmpfehlung() { return empfehlung; }
  public GNDEntityFact getGndEntityFact() { return gndEntityFact; }

  private static String resolveIdentifier(GNDEntityFact fact) {
    if (fact == null) {
      return "";
    }
    if (fact.getGndIdentifier() != null && !fact.getGndIdentifier().isBlank()) {
      return fact.getGndIdentifier();
    }
    return fact.getId() != null ? fact.getId() : "";
  }
}
