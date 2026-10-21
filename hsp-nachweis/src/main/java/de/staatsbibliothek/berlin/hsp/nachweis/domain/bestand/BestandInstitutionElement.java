package de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.BestandInstitutionElementDTO;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 16.11.23.
 * @version 1.0
 */

@Setter
@Getter
public class BestandInstitutionElement {

  private String besitzendeEinrichtung;

  private String aufbewahrungsOrt;

  private long bestandPraesentation;
  private long bestandNachweis;

  private BestandElementTyps elementTyp;

  protected BestandInstitutionElement(BestandInstitutionElementDTO dto, BestandElementTyps typ) {
    this.aufbewahrungsOrt = dto.getAufbewahrungsOrt();
    this.besitzendeEinrichtung = dto.getBesitzendeEinrichtung();
    this.bestandNachweis = dto.getBestandNachweis();
    this.elementTyp = typ;
  }

  public BestandInstitutionElement(String besitzendeEinrichtung, String aufbewahrungsOrt,
      int bestandPraesentation, int bestandNachweis, BestandElementTyps elementTyp) {
    this.besitzendeEinrichtung = besitzendeEinrichtung;
    this.aufbewahrungsOrt = aufbewahrungsOrt;
    this.bestandPraesentation = bestandPraesentation;
    this.bestandNachweis = bestandNachweis;
    this.elementTyp = elementTyp;
  }

  public long getBestandDifferenz() {
    return bestandNachweis - bestandPraesentation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BestandInstitutionElement that = (BestandInstitutionElement) o;
    return Objects.equals(besitzendeEinrichtung, that.besitzendeEinrichtung)
        && elementTyp == that.elementTyp;
  }

  @Override
  public int hashCode() {
    return Objects.hash(besitzendeEinrichtung, elementTyp);
  }

  @Override
  public String toString() {
    return
        "BestandInstitutionElement{" + "besitzendeEinrichtung='" + besitzendeEinrichtung + '\''
            + ", aufbewahrungsOrt='" + aufbewahrungsOrt + '\''
            + ", bestandPraesentation=" + bestandPraesentation
            + ", bestandNachweis=" + bestandNachweis
            + ", elementTyp=" + elementTyp
            + '}';
  }
}
