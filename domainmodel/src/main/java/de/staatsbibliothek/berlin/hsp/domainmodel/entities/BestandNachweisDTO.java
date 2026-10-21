package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import java.util.UUID;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 24.04.23.
 * @version 1.0
 */

public class BestandNachweisDTO {

  public BestandNachweisDTO(long anzahlKOD, long anzahlBeschreibungen,long anzahlBeschreibungenIntern,long anzahlBeschreibungenExtern,long anzahlBeschreibungenKatalog, long anzahlDigitalisate, long anzahlKataloge) {
    this.id = UUID.randomUUID().toString();
    this.anzahlKOD = anzahlKOD;
    this.anzahlBeschreibungen = anzahlBeschreibungen;
    this.anzahlBeschreibungenIntern = anzahlBeschreibungenIntern;
    this.anzahlBeschreibungenExtern = anzahlBeschreibungenExtern;
    this.anzahlBeschreibungenKatalog = anzahlBeschreibungenKatalog;
    this.anzahlDigitalisate = anzahlDigitalisate;
    this.anzahlKataloge = anzahlKataloge;
  }

  private String id;
  private long anzahlKOD;
  private long anzahlBeschreibungen;
  private long anzahlBeschreibungenIntern;
  private long anzahlBeschreibungenExtern;
  private long anzahlBeschreibungenKatalog;
  private long anzahlDigitalisate;
  private long anzahlKataloge;
  public BestandNachweisDTO() {
    this.id = UUID.randomUUID().toString();
  }

  public long getAnzahlKOD() {
    return anzahlKOD;
  }

  public long getAnzahlBeschreibungen() {
    return anzahlBeschreibungen;
  }

  public long getAnzahlDigitalisate() {
    return anzahlDigitalisate;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public long getAnzahlKataloge() {
    return anzahlKataloge;
  }

  public BestandNachweisDTO(long anzahlBeschreibungenIntern) {
    this.anzahlBeschreibungenIntern = anzahlBeschreibungenIntern;
  }

  public long getAnzahlBeschreibungenIntern() {
    return anzahlBeschreibungenIntern;
  }

  public long getAnzahlBeschreibungenExtern() {
    return anzahlBeschreibungenExtern;
  }

  public long getAnzahlBeschreibungenKatalog() {
    return anzahlBeschreibungenKatalog;
  }
}
