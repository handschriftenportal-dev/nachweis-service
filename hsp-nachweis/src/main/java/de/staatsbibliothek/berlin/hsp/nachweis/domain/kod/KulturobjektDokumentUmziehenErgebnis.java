package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.02.24.
 * @version 1.0
 */

public record KulturobjektDokumentUmziehenErgebnis(String id, String signatur,
                                                   List<String> existingKODIDs,
                                                   NormdatenReferenz neuerOrt,
                                                   NormdatenReferenz neuerBesitzer,
                                                   LocalDateTime executionTime,
                                                   String errorMessage) {

  public boolean isSuccessful() {
    return (errorMessage == null || errorMessage.isEmpty()) && existingKODIDs.isEmpty();
  }

  public String existingKODIDAsSeperatedString() {
    return String.join(";", existingKODIDs);
  }

  public String besitzendeEinrichtung() {
    if (neuerBesitzer == null) {
      return "";
    }
    return neuerBesitzer.getName();
  }

  public String ort() {
    if (neuerOrt == null) {
      return "";
    }
    return neuerOrt.getName();
  }

}
