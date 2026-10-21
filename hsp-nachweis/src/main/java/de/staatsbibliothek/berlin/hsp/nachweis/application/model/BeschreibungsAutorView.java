package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import java.io.Serial;
import java.io.Serializable;
import lombok.NonNull;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 11.11.2021.
 * @version 1.0
 */
public record BeschreibungsAutorView(String preferredName, String gndid) implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Override
  public @NonNull String toString() {
    return "BeschreibungsAutorView{" + "preferredName='" + preferredName + '\''
        + ", gndid='" + gndid + '\''
        + '}';
  }
}
