package de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps;
import java.util.Objects;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 16.11.23.
 * @version 1.0
 */
public class BestandsElement {

  private String label;
  private BestandElementTyps typ;
  private long bestandNachweis;
  private long bestandPraesentation;

  public BestandsElement(BestandElementTyps typ, String label, long bestandNachweis,
      long bestandPraesentation) {
    this.label = label;
    this.bestandNachweis = bestandNachweis;
    this.bestandPraesentation = bestandPraesentation;
    this.typ = typ;
  }

  public String getLabel() {
    return label;
  }

  public long getBestandNachweis() {
    return bestandNachweis;
  }

  public long getBestandPraesentation() {
    return bestandPraesentation;
  }

  public BestandElementTyps getTyp() {
    return typ;
  }

  public long getBestandDifferenz() {

    if (bestandNachweis == -1 || bestandPraesentation == -1) {
      return -1;
    }

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
    BestandsElement that = (BestandsElement) o;
    return label.equals(that.label);
  }

  @Override
  public int hashCode() {
    return Objects.hash(label);
  }
}
