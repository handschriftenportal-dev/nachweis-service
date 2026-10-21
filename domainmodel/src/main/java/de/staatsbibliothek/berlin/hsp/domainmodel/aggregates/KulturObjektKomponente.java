package de.staatsbibliothek.berlin.hsp.domainmodel.aggregates;

import jakarta.persistence.Entity;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.01.2020.
 * @version 1.0
 * <p>Eine KulturObjektKomponente ist ein Teil eines KulturObjektes. Allerdings ist dieser Teil
 * alleinstehende so bedeutend, dass dieser separat dokumentiert und beschrieben werden muss. Ein
 * Beispiel ist ein bedeutendes Fragment oder Einband eines anderen KulturObjektDokumentes. Eine
 * Kulturobjektkomponente repräsentiert eine adressierungsbedürftige physische Komponente des
 * Kulturobjekts
 */
@Entity
public class KulturObjektKomponente extends KulturObjektDokument implements Serializable {

  @Serial
  private static final long serialVersionUID = -2645284910367919105L;

  protected KulturObjektKomponente() {
  }

  protected KulturObjektKomponente(String id) {
    super();
    setId(id);
  }

  @Override
  public void setTeiXML(String teiXML) {
    throw new IllegalCallerException(
        "Diese Methode darf auf diesem Objekt nicht aufgerufen werden");
  }

  @Override
  public void setKomponenten(Set<KulturObjektKomponente> komponenten) {
    throw new IllegalCallerException(
        "Diese Methode darf auf diesem Objekt nicht aufgerufen werden");
  }

  @Override
  public void setRegistrierungsDatum(LocalDateTime registrierungsDatumDatum) {
    throw new IllegalCallerException(
        "Diese Methode darf auf diesem Objekt nicht aufgerufen werden");
  }

  @Override
  public void setGndIdentifier(String gndIdentifier) {
    throw new IllegalCallerException(
        "Diese Methode darf auf diesem Objekt nicht aufgerufen werden");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof KulturObjektKomponente that)) {
      return false;
    }
    return getId().equals(that.getId());
  }

  @Override
  public String toString() {
    return new StringBuffer("KulturObjektKomponente{")
        .append("id='").append(getId()).append('\'')
        .append(", gueltigeIdentifikation=").append(getGueltigeIdentifikation())
        .append(", alternativeIdentifikationen=").append(getAlternativeIdentifikationen())
        .append(", digitalisate=").append(getDigitalisate())
        .append(", beschreibungenIDs=").append(getBeschreibungenIDs())
        .append(", externeReferenzen=").append(getExterneReferenzen())
        .append('}')
        .toString();
  }
}
