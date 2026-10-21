package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 16.11.23.
 * @version 1.0
 */

@Getter
@Setter
@ToString
public class BestandInstitutionElementDTO {

  String besitzendeEinrichtung;

  private String besitzendeEinrichtungID;

  private String aufbewahrungsOrt;

  private String aufbewahrungsOrtID;

  private long bestandNachweis;

  public BestandInstitutionElementDTO() {
  }

  public BestandInstitutionElementDTO(String besitzendeEinrichtung, String besitzendeEinrichtungID,
      String aufbewahrungsOrt, String aufbewahrungsOrtID, long bestandNachweis) {
    this.besitzendeEinrichtung = besitzendeEinrichtung;
    this.besitzendeEinrichtungID = besitzendeEinrichtungID;
    this.aufbewahrungsOrt = aufbewahrungsOrt;
    this.aufbewahrungsOrtID = aufbewahrungsOrtID;
    this.bestandNachweis = bestandNachweis;
  }
}
