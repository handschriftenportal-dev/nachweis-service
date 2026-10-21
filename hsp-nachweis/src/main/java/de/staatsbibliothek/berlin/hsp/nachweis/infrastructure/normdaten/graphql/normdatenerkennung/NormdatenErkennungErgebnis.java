package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.normdatenerkennung;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.Treffer;
import java.util.List;

public record NormdatenErkennungErgebnis(List<Treffer> treffer, Treffer empfehlung) {

  public List<Treffer> getTreffer() {
    return treffer;
  }

  public Treffer getEmpfehlung() {
    return empfehlung;
  }
}
