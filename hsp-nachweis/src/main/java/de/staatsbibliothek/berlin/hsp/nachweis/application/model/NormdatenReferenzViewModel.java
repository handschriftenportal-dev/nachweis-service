package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.io.Serial;
import java.io.Serializable;

/**
 * Materialisierte View-Repräsentation einer {@link NormdatenReferenz} — kapselt nur die im UI
 * benötigten Strings, ohne Hibernate-Bytecode-Enhancement und Lazy-Felder. Sicher außerhalb aktiver
 * Hibernate-Session zu rendern.
 */
public record NormdatenReferenzViewModel(String id, String name, String gndID) implements
    Serializable {

  @Serial
  private static final long serialVersionUID = 42L;

  public static NormdatenReferenzViewModel from(NormdatenReferenz ref) {
    return ref == null
        ? null
        : new NormdatenReferenzViewModel(ref.getId(), ref.getName(), ref.getGndID());
  }
}
