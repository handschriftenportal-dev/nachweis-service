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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

  public enum BestandElementTyps {KULTUROBJEKTE, BESCHREIBUNGEN,BESCHREIBUNGEN_INTERN,BESCHREIBUNGEN_EXTERN,BESCHREIBUNGEN_KATALOG, DIGITALISATE, KATALOGE}
  private List<BestandsElement> bestandsElemente = new ArrayList<>();
  private String fehlerMeldung;
  public List<BestandsElement> getBestandsElemente() {
    return bestandsElemente;
  }

  public Optional<BestandsElement> getKulturobjekteBestandsElement() {
    return bestandsElemente.stream()
        .filter(bestandsElement -> BestandElementTyps.KULTUROBJEKTE.equals(bestandsElement.getTyp())).findFirst();
  }

  public String getFehlerMeldung() {
    return fehlerMeldung;
  }

  public static class BestandsElement {
    private String label;
    private BestandElementTyps typ;
    private int bestandNachweis;
    private int bestandPraesentation;

    public BestandsElement(BestandElementTyps typ,String label, int bestandNachweis, int bestandPraesentation) {
      this.label = label;
      this.bestandNachweis = bestandNachweis;
      this.bestandPraesentation = bestandPraesentation;
      this.typ = typ;
    }

    public String getLabel() {
      return label;
    }

    public int getBestandNachweis() {
      return bestandNachweis;
    }

    public int getBestandPraesentation() {
      return bestandPraesentation;
    }

    public BestandElementTyps getTyp() {
      return typ;
    }

    public int getBestandDifferenz() {

      if(bestandNachweis == -1 || bestandPraesentation == -1) {
        return -1;
      }

      return bestandNachweis-bestandPraesentation;
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
}
