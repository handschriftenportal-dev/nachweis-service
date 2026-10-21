package de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.04.23.
 * @version 1.0
 */
public class BestandsView {

  public BestandsView() {
  }

  public BestandsView(String fehlerMeldung) {
    this.fehlerMeldung = fehlerMeldung;
  }

  public enum BestandElementTyps {KULTUROBJEKTE, BESCHREIBUNGEN, BESCHREIBUNGEN_INTERN, BESCHREIBUNGEN_EXTERN, BESCHREIBUNGEN_KATALOG, DIGITALISATE, KATALOGE}

  private List<BestandsElement> bestandsElemente = new ArrayList<>();

  private List<BestandInstitutionElement> bestandInstitutionElementeForKODs = new ArrayList<>();

  private List<BestandInstitutionElement> bestandInstitutionElementeForBeschreibungen = new ArrayList<>();

  private List<BestandInstitutionElement> bestandInstitutionElementeForDigitalisate = new ArrayList<>();
  private String fehlerMeldung;

  public List<BestandsElement> getBestandsElemente() {
    return bestandsElemente;
  }

  public Optional<BestandsElement> getKulturobjekteBestandsElement() {
    return bestandsElemente.stream()
        .filter(
            bestandsElement -> BestandElementTyps.KULTUROBJEKTE.equals(bestandsElement.getTyp()))
        .findFirst();
  }

  public List<BestandInstitutionElement> getBestandInstitutionElementeForKODs() {
    return bestandInstitutionElementeForKODs;
  }

  public List<BestandInstitutionElement> getBestandInstitutionElementeForBeschreibungen() {
    return bestandInstitutionElementeForBeschreibungen;
  }

  public List<BestandInstitutionElement> getBestandInstitutionElementeForDigitalisate() {
    return bestandInstitutionElementeForDigitalisate;
  }

  public String getFehlerMeldung() {
    return fehlerMeldung;
  }


}
