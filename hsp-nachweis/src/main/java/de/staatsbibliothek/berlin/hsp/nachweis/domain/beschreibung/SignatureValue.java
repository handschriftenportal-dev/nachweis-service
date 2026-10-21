package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 11.08.21
 */
public class SignatureValue implements Serializable {

  private static final long serialVersionUID = -428394530604773136L;

  public final String dokumentId;
  public final String signature;
  public final String aufbewaehrungsOrt;
  public final String besitzer;

  public SignatureValue(String dokumentId, String signature, String aufbewaehrungsOrt,
      String besitzer) {
    this.dokumentId = dokumentId;
    this.signature = signature;
    this.aufbewaehrungsOrt = aufbewaehrungsOrt;
    this.besitzer = besitzer;
  }

  public String getDokumentId() {
    return dokumentId;
  }

  public String getSignature() {
    return signature;
  }

  public String getAufbewaehrungsOrt() {
    return aufbewaehrungsOrt;
  }

  public String getBesitzer() {
    return besitzer;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SignatureValue that = (SignatureValue) o;
    return Objects.equals(signature, that.signature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(signature);
  }


  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("SignatureValue{");
    sb.append("dokumentId='").append(dokumentId).append('\'');
    sb.append(", signature='").append(signature).append('\'');
    sb.append(", aufbewahrungsOrt='").append(aufbewaehrungsOrt).append('\'');
    sb.append(", besitzer='").append(besitzer).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
