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

package de.staatsbibliothek.berlin.hsp.domainmodel.aggregates;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.01.2020.
 * @version 1.0
 * <p>
 * Eine KulturObjektKomponente ist ein Teil eines KulturObjektes. Allerdings ist dieser Teil alleinstehende so
 * bedeutend, dass dieser separat dokumentiert und beschrieben werden muss. Ein Beispiel ist ein bedeutendes Fragment
 * oder Einband eines anderen KulturObjektDokumentes. Eine Kulturobjektkomponente repräsentiert eine
 * adressierungsbedürftige physische Komponente des Kulturobjekts
 */
@Entity
@Table(name = "kulturobjektkomponente")
public class KulturObjektKomponente extends KulturObjektDokument implements Serializable {

  private static final long serialVersionUID = -2645284910367919105L;


  protected KulturObjektKomponente() {
  }

  protected KulturObjektKomponente(String id) {
    super();
    this.id = id;
  }

  @Override
  public void setTeiXML(String teiXML) {
    throw new IllegalCallerException("Diese Methode darf auf diesem Objekt nicht aufgerufen werden");
  }

  public void setKomponenten(
      Set<KulturObjektKomponente> komponenten) {
    throw new IllegalCallerException("Diese Methode darf auf diesem Objekt nicht aufgerufen werden");
  }

  public void setRegistrierungsDatum(LocalDateTime registrierungsDatumDatum) {
    throw new IllegalCallerException("Diese Methode darf auf diesem Objekt nicht aufgerufen werden");
  }

  public void setGndIdentifier(String gndIdentifier) {
    throw new IllegalCallerException("Diese Methode darf auf diesem Objekt nicht aufgerufen werden");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof KulturObjektKomponente)) {
      return false;
    }
    KulturObjektKomponente that = (KulturObjektKomponente) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("KulturObjektKomponente{");
    sb.append("id='").append(id).append('\'');
    sb.append(", gueltigeIdentifikation=").append(gueltigeIdentifikation);
    sb.append(", alternativeIdentifikationen=").append(alternativeIdentifikationen);
    sb.append(", digitalisate=").append(digitalisate);
    sb.append(", beschreibungenIDs=").append(beschreibungenIDs);
    sb.append(", externeReferenzen=").append(externeReferenzen);
    sb.append('}');
    return sb.toString();
  }
}
